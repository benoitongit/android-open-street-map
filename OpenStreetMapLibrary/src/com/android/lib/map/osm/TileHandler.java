package com.android.lib.map.osm;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class TileHandler extends Handler {

	public static final int TILE_NOT_LOADED = 10;
	public static final int TILE_LOADED = 11;
	
	public View mView;
	
	public TileHandler() {
		super();
	}
	
	public TileHandler(View view) {
		super();
		mView = view;
	}
	
	@Override
	public void handleMessage(final Message msg) {

		switch (msg.what) {

			case TILE_LOADED:
				if (mView != null)
					mView.invalidate();
				break;
		}
	}
	
}
