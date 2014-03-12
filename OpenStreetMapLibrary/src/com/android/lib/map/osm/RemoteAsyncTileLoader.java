package com.android.lib.map.osm;


import com.android.lib.map.osm.models.MapTile;

import android.os.Handler;
import android.os.Message;

public class RemoteAsyncTileLoader {
	
	private static final int NB_THREAD_FOR_TILE_REQUEST = 3;
	
	private TileRequestTask[] mDownloadTileTasks;
	private int mIterator;

	
	public RemoteAsyncTileLoader(Handler handler) {
		
		mIterator = 0;
		mDownloadTileTasks = new TileRequestTask[NB_THREAD_FOR_TILE_REQUEST];
		
		for (int i = 0; i < mDownloadTileTasks.length; i++) {
			mDownloadTileTasks[i] = new TileRequestTask(handler);
		}
	}

	public void queueTileRequest(Tile tile) {
		
		// Make sure the zoom is not > to the actual max Tile OSM zoom 
		if (tile.zoom > OsmMapViewBase.MIN_ZOOM_LEVEL_FOR_TILES)
			return;
		//---
			
		// Check if tile is not already in a thread
		for (int i = 0; i < mDownloadTileTasks.length; i++) {
			if (mDownloadTileTasks[i].mRequestsQueue.contains(tile))
				return;
		}
		//---

		// if not add it
		mDownloadTileTasks[mIterator].queue(tile);
		//--
		
		mIterator++;
		if (mIterator == mDownloadTileTasks.length)
			mIterator = 0;
	}

	public void interruptThreads() {
		for (int i = 0; i < mDownloadTileTasks.length; i++) {
			mDownloadTileTasks[i].interrupt();
		}
	}
	
	private class TileRequestTask extends Thread {
		
		private static final int MAX_QUEUE_REQUEST = 6;
		
		public RequestsQueue mRequestsQueue;
		
		private RequestTile mRequestTile;
		private Handler mHandler;
		
		public TileRequestTask(Handler handler) {
			mHandler = handler;
			mRequestTile = new RequestTile();
			mRequestsQueue = new RequestsQueue(1, MAX_QUEUE_REQUEST);
			start();
		}

		public void queue(Tile tile) {
			mRequestsQueue.queue(tile);
			
			synchronized (this) {
				this.notify();	
			}
		}
		
		@Override
		public void run() {

			Tile tile;
			
			while (true) {
				tile = null;
				if (mRequestsQueue.hasRequest()) {
					tile = mRequestsQueue.dequeue();
				}
				if (tile != null) {
					loadTileAndSendMessage(tile);
				}
				try {
					synchronized (this) {
						if (mRequestsQueue.size() == 0) {
							this.wait();
						}
					}
					Thread.sleep(150);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		public void loadTileAndSendMessage(Tile remoteTile) {

			if (remoteTile != null && remoteTile.key != null) {
				try {
					remoteTile.bitmap = mRequestTile.loadBitmap(remoteTile);

					Message message = mHandler.obtainMessage();
					
					if (remoteTile != null && remoteTile.bitmap != null && remoteTile.bitmap.length > 0) {
						MapTile.insertTile(remoteTile, remoteTile.bitmap);
						message.what = TileHandler.TILE_LOADED;
					} else {
						message.what = TileHandler.TILE_NOT_LOADED;
					}

					mHandler.sendMessage(message);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	
	}
}
