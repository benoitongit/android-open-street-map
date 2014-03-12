package com.android.lib.map.osm;

import java.util.List;
import java.util.Map;

import com.android.lib.map.osm.InDbTileLoader.IDbTileLoaderListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class TilesProvider implements IDbTileLoaderListener {
		
	private InMemoryTilesCache mInMemoryTilesCache = null;;
	private ResizedTilesCache mResizedTilesCache = null;
	private RemoteAsyncTileLoader mRemoteTileLoader = null;
	private InDbTileLoader mInDbTileLoader;
	private Handler mHandler;
	private boolean mAllowRequestTilesViaInternet;
	private Bitmap mMapTileUnavailableBitmap=null;
	
	
	public TilesProvider(Context context, Handler handler, boolean allowRequestTilesViaInternet) {
		mInMemoryTilesCache = new InMemoryTilesCache(context, handler);
		mRemoteTileLoader = new RemoteAsyncTileLoader(handler);
		mResizedTilesCache = new ResizedTilesCache(handler);
		mInDbTileLoader = new InDbTileLoader(this);
		mHandler = handler;
		mAllowRequestTilesViaInternet = allowRequestTilesViaInternet;
	}
	
	public void setResizeBitmapCacheSize(int size){
		mResizedTilesCache.setBitmapCacheSize(size);
	}
	
	public void setBitmapMemoryCacheSize(int size){
		mInMemoryTilesCache.setBitmapCacheSize(size);
	}
	
	public void setMapTileUnavailableBitmap(Bitmap bitmap){
		mMapTileUnavailableBitmap = bitmap;
		mResizedTilesCache.setMapTileUnavailableBitmap(mMapTileUnavailableBitmap);
	}
	
	public Bitmap getTileBitmap(Tile tile) {
		
		if (mInMemoryTilesCache.hasTile(tile.key)) {
			return mInMemoryTilesCache.getTileBitmap(tile.key);
		}
		
		if (mAllowRequestTilesViaInternet) {
			
			mInDbTileLoader.queue(new Tile(tile));
			if (mResizedTilesCache.hasTile(tile)) {
				return mResizedTilesCache.getTileBitmap(tile);
			}
			
		} else {
			
			if (mResizedTilesCache.hasTile(tile)) {
				return mResizedTilesCache.getTileBitmap(tile);
			}
			mInDbTileLoader.queue(new Tile(tile));	
		
		}
		
		return null;
	}

	@Override
	public void onTilesLoadedFromDb(Map<Tile, Bitmap> tileBitmapMap) {
		for (Map.Entry<Tile, Bitmap> entry : tileBitmapMap.entrySet()) {
			Tile tile = entry.getKey();
			Bitmap bitmap = entry.getValue();
			mInMemoryTilesCache.add(tile.key, bitmap);
		}
		Message message = mHandler.obtainMessage();
		message.what = TileHandler.TILE_LOADED;
		mHandler.sendMessage(message);
	}
	
	@Override
	public void onTilesNotLoadedFromDb(List<Tile> tiles) {
		for (Tile tile : tiles) {
			
			Tile tileMinusOneZoomLevel = mResizedTilesCache.findClosestMinusTile(tile);
			if (tileMinusOneZoomLevel != null) {
				mResizedTilesCache.queueResize(new Tile(tile));				
			}
			
			if (mAllowRequestTilesViaInternet)
				mRemoteTileLoader.queueTileRequest(new Tile(tile));
			
		}
	}
	
	public void stopThreads() {
		mRemoteTileLoader.interruptThreads();
		mResizedTilesCache.interrupt();
		mInDbTileLoader.interrupt();
	}
	
	public void clearCache() {
		mInMemoryTilesCache.clean();
	}

	public void clearResizeCache() {
		mResizedTilesCache.clear();
	}
}
