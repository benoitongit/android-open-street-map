package com.android.lib.map.osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.android.lib.map.osm.models.MapTile;

import android.graphics.Bitmap;

public class InDbTileLoader extends Thread {
	
	private static final int STACK_DB_QUERY_LIMIT = 16;
	private static final int MAX_TILES_REQUEST_PER_SQL_QUERY = 16;
	
	private Stack<Tile> mTiles;
	private IDbTileLoaderListener mDbTileLoaderListener;

	
	public InDbTileLoader(IDbTileLoaderListener dbTileLoaderListener) {
		mTiles = new Stack<Tile>();
		mDbTileLoaderListener = dbTileLoaderListener;
		start();
	}

	public void queue(Tile tile) {

		if (!stackHasTile(tile)) {

			synchronized (this) {
				if (mTiles.size() >= STACK_DB_QUERY_LIMIT)
					mTiles.remove(mTiles.lastElement());

				mTiles.push(tile);
				//Log.i("InDbTileLoader", "InDbTileLoader ADD size=" + mTiles.size());

				this.notify();
			}
		}
	}
	
	@Override
	public void run() {
		
		List<Tile> tiles = new ArrayList<Tile>();

		while(true) {
			
			int i = 0;
			tiles.clear();
			
			if (stackSize() > 0) {
				while (stackSize() > 0 && i < MAX_TILES_REQUEST_PER_SQL_QUERY) {
					tiles.add(stackPop());
					i++;
				}
			}
			
			if (tiles.size() > 0) {

				List<Tile> tilesNotLoaded = new ArrayList<Tile>();
				Map<Tile, Bitmap> tilesLoaded = new HashMap<Tile, Bitmap>();
				Map<Tile, Bitmap> map = MapTile.getTiles(tiles);
				if (map != null) {
					for (Map.Entry<Tile, Bitmap> entry : map.entrySet()) {
						Tile tile = entry.getKey();
						Bitmap bitmap = entry.getValue();
						if (bitmap == null)
							tilesNotLoaded.add(tile);
						else
							tilesLoaded.put(tile, bitmap);
					}
					
					if (tilesLoaded.size() > 0)
						mDbTileLoaderListener.onTilesLoadedFromDb(tilesLoaded);
					if (tilesNotLoaded.size() > 0)
						mDbTileLoaderListener.onTilesNotLoadedFromDb(tilesNotLoaded);
				}
			}
			
			try {
				synchronized (this) {
					if (mTiles.size() == 0) {
						this.wait();
					}
				}
				//Thread.sleep(50);
			} 
			catch (InterruptedException e) {
				break;
			}
		}
	}
	
//	@Override
//	public void run() {
//		Tile tile;
//		while(true) 
//		{
//			tile = null;
//			if (stackSize() > 0)
//				tile = stackPop();
//			if (tile != null) {
//				Bitmap tileBitmap = loadFromDb(tile);
//				if (tileBitmap != null) {
//					mDbTileLoaderListener.onTileInDb(tile, tileBitmap);
//				} else {
//					mDbTileLoaderListener.onTileNotInDb(tile);
//				}
//			}
//			try 
//			{
//				synchronized (this) 
//				{
//					if(mTiles.size() == 0)
//					{
//						this.wait();
//					}
//				}
//				Thread.sleep(50);
//			} 
//			catch (InterruptedException e) 
//			{
//				break;
//			}
//		}
//	}
	
	private boolean stackHasTile(Tile aTile) {
		synchronized (this) {
			for (Tile tile : mTiles) {
				if (tile.key != null && tile.key.equals(aTile.key))
					return true;
			}
			return false;
		}
	}
	
	private Tile stackPop() {
		synchronized (this) {
			return mTiles.pop();
		}
	}

	private int stackSize() {
		synchronized (this) {
			return mTiles.size();
		}
	}
	
	public interface IDbTileLoaderListener {
		public void onTilesLoadedFromDb(Map<Tile, Bitmap> tileBitmapMap);
		public void onTilesNotLoadedFromDb(List<Tile> tiles);
	}
}
