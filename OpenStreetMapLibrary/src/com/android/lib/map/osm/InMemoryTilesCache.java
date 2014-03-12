package com.android.lib.map.osm;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

public class InMemoryTilesCache {
	private LRUMap<String, Bitmap> mBitmapCache = new LRUMap<String, Bitmap>(8, 8);
	private Object mLock = new Object();

	public InMemoryTilesCache(Context context, Handler handler) {

	}

	public void add(String tileKey, Bitmap bitmap) {
		synchronized (mLock) {
			mBitmapCache.put(tileKey, bitmap);
		}
	}

	public boolean hasTile(String tileKey) {
		synchronized (mLock) {
			return mBitmapCache.containsKey(tileKey);
		}
	}

	public void clean() {
		synchronized (mLock) {
			mBitmapCache.clear();
		}
	}

	public Bitmap getTileBitmap(String tileKey) {
		synchronized (mLock) {
			return mBitmapCache.get(tileKey);
		}
	}
	public void setBitmapCacheSize(int size){
		mBitmapCache = new LRUMap<String, Bitmap>(size, size+2);
	}
}
