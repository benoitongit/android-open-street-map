package com.android.lib.map.osm;

import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import com.android.lib.map.osm.models.MapTile;

public class ResizedTilesCache extends Thread {


	private static int MAX_SCALE_FACTOR = 8;
	private LRUMap<String, Bitmap> mExtrapolatedBitmapCache = new LRUMap<String, Bitmap>(8,8);
	private Queue<Tile> mRequests = new LinkedList<Tile>();
	private Tile mResizeTile;
	private TileScaler mTileScaler = new TileScaler();
	private Object mLock = new Object();
	private Handler mHandler;
	private Bitmap mMapTileUnavailableBitmap=null;
	
	public ResizedTilesCache(Handler handler) {
		this.mHandler = handler;
		start();
	}
	public void setBitmapCacheSize(int size){
		mExtrapolatedBitmapCache =  new LRUMap<String, Bitmap>(size,size+2);
	}
	public void setMapTileUnavailableBitmap(Bitmap bitmap){
		mMapTileUnavailableBitmap = bitmap;
	}
	public Tile findClosestMinusTile(Tile tile) {
		Tile minusZoomTile = generateMinusZoomTile(tile);

		//Log.i("findClosestCachedMinusTile", "LOOKING FOR zoom=" + tile.zoom + " mapX=" + tile.mapX + " mapY=" + tile.mapY);
		//Log.i("findClosestCachedMinusTile", "MinusTile LOOKING FOR zoom=" + minusZoomTile.zoom + " mapX=" + minusZoomTile.mapX + " mapY=" + minusZoomTile.mapY);
		
		if (minusZoomTile != null) {
			return minusZoomTile;
		}
		return null;
	}

	private Tile generateMinusZoomTile(Tile tile) {
		if (tile.zoom == 0) {
			return null;
		}
		Tile minusZoomTile = new Tile();
		minusZoomTile.zoom = tile.zoom - 1;
		minusZoomTile.mapX = tile.mapX / 2;
		minusZoomTile.mapY = tile.mapY / 2;
		minusZoomTile.mapTypeId = tile.mapTypeId;
		minusZoomTile.key = minusZoomTile.zoom + "/" + minusZoomTile.mapX + "/"
				+ minusZoomTile.mapY + ".png";

		return minusZoomTile;
	}
	
	public void queueResize(Tile resizeRequest) 
	{
		synchronized (mLock) 
		{
			if(!hasRequest(resizeRequest) && !mExtrapolatedBitmapCache.containsKey(resizeRequest.key))
			{
				mRequests.add(resizeRequest);
			}
		}
		synchronized (this) 
		{
			this.notify();
		}
	}
	
	private boolean hasRequest(Tile resizeRequest) 
	{
		synchronized (mLock) 
		{
    		for(Tile request : mRequests)
    		{
    			if(request.key == resizeRequest.key)
    			{
    				return true;
    			}
    		}
		}
		return false;
	}

	@Override
	public void run() {
		
		while(true) {
			synchronized (mLock) {
				mResizeTile = mRequests.poll();
			}
			
			if (mResizeTile != null) {
				
				Tile minusZoomTile = findClosestMinusTile(mResizeTile);
				if(minusZoomTile != null) {
					
					Bitmap minusZoomBitmap = null; 
					float scaleFactor=0;
					while (minusZoomBitmap==null && scaleFactor < MAX_SCALE_FACTOR) {
						minusZoomBitmap = MapTile.getTile(minusZoomTile);
						if(minusZoomBitmap == null){
							minusZoomTile = findClosestMinusTile(minusZoomTile);
						}
						
						if (minusZoomTile != null) {
							scaleFactor = (float)Math.pow(2,mResizeTile.zoom - minusZoomTile.zoom-1);
						} else {
							scaleFactor = MAX_SCALE_FACTOR;
						}
					}
					
				//System.out.println("FOUND TILE " + scaleFactor + " minusZoomBitmap = "+ minusZoomTile.zoom + " "+mResizeTile.zoom );
	    			Bitmap closestBitmap = mTileScaler.scale(minusZoomBitmap, minusZoomTile, mResizeTile.mapX, mResizeTile.mapY,scaleFactor);
    				if (closestBitmap != null) {
	    				synchronized (mLock) {
	    					mExtrapolatedBitmapCache.put(mResizeTile.key, closestBitmap);
	    				}
    				}else{
    					synchronized (mLock) {
	    					mExtrapolatedBitmapCache.put(mResizeTile.key, mMapTileUnavailableBitmap);// to handle out of memory issues
	    				}
    				}
	    				Message message = mHandler.obtainMessage();
	    				message.arg1 = mRequests.size();
	    				message.arg2 = 2;
	    				message.what = TileHandler.TILE_LOADED;
	    				mHandler.sendMessage(message);
	    			
				}else{
						synchronized (mLock) {
							mExtrapolatedBitmapCache.put(mResizeTile.key, mMapTileUnavailableBitmap); //to handle resize limit exceeded
						}
    					Message message = mHandler.obtainMessage();
    					message.arg1 = mRequests.size();
	    				message.arg2 = 2;
	    				message.what = TileHandler.TILE_LOADED;
	    				mHandler.sendMessage(message);
				}
			}
			
			try {
				synchronized (this) {
    				if(mRequests.size() == 0)
    				{
    					this.wait();
    				}
				}
				//Thread.sleep(50);
			
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public boolean hasTile(Tile tile) 
	{
		return mExtrapolatedBitmapCache.containsKey(tile.key);
	}

	public Bitmap getTileBitmap(Tile tile) 
	{
		synchronized (mLock) 
		{
			return mExtrapolatedBitmapCache.get(tile.key);
		}
	}

	public void clear() 
	{
		synchronized (mLock) 
		{
			mExtrapolatedBitmapCache.clear();
		}		
	}
	
	
	class TileScaler {
		
		public Bitmap scale(Bitmap minusZoomBitmap, Tile minusZoomTile, int mapX, int mapY, float scaleFactor) {
			if (minusZoomBitmap == null)
				return null;
			
			Bitmap closestBitmap = scaleUpAndChop(minusZoomBitmap, minusZoomTile, mapX, mapY, scaleFactor);
			return closestBitmap;
		}
		
		private Bitmap scaleUpAndChop(Bitmap minusZoomBitmap, Tile minusZoomTile, int mapX, int mapY, float scaleFactor) {
			
			Bitmap scaledBitmap = null;
			int xIncrement = 1;
			if(minusZoomTile.mapX!= 1) {
				xIncrement = mapX % (int)(2*scaleFactor);
			}
			int yIncrement = 1;
			if(minusZoomTile.mapY != 1) {
				yIncrement = mapY % (int)(2*scaleFactor);
			}
	
			//scaledBitmap = BitmapScaler.scaleTo(minusZoomBitmap, mapX, mapY ,scaleFactor,(int)(256*2*scaleFactor), (int)(256*2*scaleFactor));
		
			scaledBitmap = BitmapScaler.scaleTo(minusZoomBitmap, scaleFactor, xIncrement, yIncrement);
			return scaledBitmap;
		}

	}
	
}
