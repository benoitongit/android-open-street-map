package com.android.lib.map.osm;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.android.lib.map.osm.overlay.OsmOverlay;

public abstract class OsmMapViewBase extends SurfaceView implements android.view.GestureDetector.OnGestureListener, OnDoubleTapListener  {

	public final static int MAX_ZOOM_LEVEL = 19;
	public final static int MIN_ZOOM_LEVEL_FOR_TILES = 18;
	
	private final static int TILE_SIZE = Tile.TILE_SIZE;

	protected static int mMinZoomLevel = 0;
	
	protected int mZoomLevel;
	protected int mPendingZoomLevel;
	
	private int mOffsetX = 0;
	private int mOffsetY = 0;
	private int mTouchDownX = 0;
	private int mTouchDownY = 0;
	private int mTouchOffsetX;
	private int mTouchOffsetY;
	private Tile[] mTiles;

	private int[] mIncrementsX;
	private int[] mIncrementsY;
	private Animation mZoomInAnimation;
	private Animation mZoomOutAnimation;
	private Animation mZoomInDoubleTapAnimation;
	private TilesProvider mTilesProvider;
	private List<OsmOverlay> mOverlays;
	private int mMaptTypeId;
	private TileHandler mHandler;
	private GeoPoint setMapCenterWhenViewSizeChange;
	protected boolean mIsDoubleTap = false;
	private Bitmap mMapTileUnavailableBitmap = null;
	
	
	public OsmMapViewBase(Context context, int mapTypeId) {
		super(context);
				
		SurfaceHolder surfaceHolder = getHolder();
		surfaceHolder.setFormat(PixelFormat.RGB_565);
		
		mZoomLevel = 2;
		mPendingZoomLevel = 2;
		
		mZoomInAnimation = new ScaleAnimation(1.0f, 2f, 1.0f, 2f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mZoomInAnimation.setDuration(400L);

		mZoomOutAnimation = new ScaleAnimation(1, 0.5f, 1, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mZoomOutAnimation.setDuration(400L);
		
		mOverlays = new ArrayList<OsmOverlay>();
		mMaptTypeId = mapTypeId;
		
		mHandler = new TileHandler(this);
	}

	public List<OsmOverlay> getOverlays() {
		return mOverlays;
	}
	
	public boolean animateZoomIn() {
		if (mPendingZoomLevel >= MAX_ZOOM_LEVEL) {
			return false;
		}
		
		if (mIsDoubleTap) {
			double pivotX = this.mTouchDownX * 1.0 / getWidth();
			double pivotY = this.mTouchDownY * 1.0 / getHeight();
			mZoomInDoubleTapAnimation = new ScaleAnimation(1, 2, 1, 2,
					Animation.RELATIVE_TO_SELF, (float)pivotX,
					Animation.RELATIVE_TO_SELF, (float)pivotY);
			mZoomInDoubleTapAnimation.setDuration(400L);
		}

		if (this.mPendingZoomLevel == this.mZoomLevel) {
			this.mPendingZoomLevel++;
			if (mIsDoubleTap) {
				startAnimation(mZoomInDoubleTapAnimation);
			} else {
				startAnimation(mZoomInAnimation);
			}

			Tile[] tiles;
			
			if (mIsDoubleTap) {
				tiles = initializePendingTiles(mZoomLevel + 1, (getOffsetX()) * 2 - this.mTouchDownX, (getOffsetY()) * 2 - this.mTouchDownY);
			} else {
				tiles = initializePendingTiles(mZoomLevel + 1, (getOffsetX()) * 2 - (getWidth() / 2), (getOffsetY()) * 2 -(getHeight() / 2));
			}
			
			for (int i = (mTiles.length-1); i >= 0; i--) {
				mTilesProvider.getTileBitmap(tiles[i]);
			}
			
			
		}
		return true;
	}

	public boolean animateZoomOut() {

		if (mPendingZoomLevel <= mMinZoomLevel) {
			return false;
		}

		if (mPendingZoomLevel == this.mZoomLevel) {
			this.mPendingZoomLevel--;
			startAnimation(mZoomOutAnimation);
		}
		return true;
	}

	public GeoPoint getCenter() {
				
	    int offsetX = mOffsetX - (getWidth() / 2);
	    int offsetY = mOffsetY - (getHeight() / 2);
			    
	    GeoPoint g =  getProjectionFromPixels(offsetX, offsetY);
	    return g;
	}
	
	public GeoPoint getProjectionFromPixels(int x, int y) {
		return Projection.getProjectionFromPixels(x, y, mZoomLevel);
	}
		
	public void setCenter(double lat, double lon, int mapWidth, int mapHeight) {

	    int x = Projection.getXPixelFromLongitude(lon, mZoomLevel);
	    int y = Projection.getYPixelFromLatitude(lat, mZoomLevel);
	    
	    int offsetX = (0 - x) + (mapWidth / 2);
	    int offsetY = (0 - y) + (mapHeight / 2);
		
	    setOffsetX(offsetX);
	    setOffsetY(offsetY);

		invalidate();
	}
	
	public void setCenter(double lat, double lon) {

	    int screenWidth = getWidth();
	    int screenHeight = getHeight();
	    
	    if (screenHeight == 0 && screenWidth == 0)
	    	setMapCenterWhenViewSizeChange = new GeoPoint((int)(lat*1E6),(int)(lon*1E6));

	    int x = Projection.getXPixelFromLongitude(lon, mZoomLevel);
	    int y = Projection.getYPixelFromLatitude(lat, mZoomLevel);
	    
	    int offsetX = (0 - x) + (screenWidth / 2);
	    int offsetY = (0 - y) + (screenHeight / 2);
		
	    setOffsetX(offsetX);
	    setOffsetY(offsetY);

		invalidate();
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		// On view change we have enough room for all the tiles
		if (getWidth() > 0 && getHeight() > 0)
			initMapTileArrayCoordinate();
		//---
		
		if (setMapCenterWhenViewSizeChange != null) {
			double lat = setMapCenterWhenViewSizeChange.getLatitudeE6() / 1E6;
			double lon = setMapCenterWhenViewSizeChange.getLongitudeE6() / 1E6;
			setCenter(lat, lon);
			setMapCenterWhenViewSizeChange = null;
		}
	}
	
	public void setMapTileUnavailableBitmap(Bitmap bitmap){
		mMapTileUnavailableBitmap = bitmap;
	}
		
	private void initMapTileArrayCoordinate() {
		
		int xRows = (int) Math.ceil((double)(getWidth()+Tile.TILE_SIZE) / (double)Tile.TILE_SIZE);
		int yRows = (int) Math.ceil((double)(getHeight()+Tile.TILE_SIZE) / (double)Tile.TILE_SIZE);
		
		mTilesProvider.setResizeBitmapCacheSize(xRows*yRows*2);
		mTilesProvider.setBitmapMemoryCacheSize(xRows*yRows*2);
		mTilesProvider.setMapTileUnavailableBitmap(mMapTileUnavailableBitmap);
		
		mIncrementsX = new int[xRows*yRows];
		mIncrementsY = new int[xRows*yRows];
		
		Tile[] oldTiles = mTiles;
		mTiles = new Tile[xRows*yRows];
		if (oldTiles != null && oldTiles.length > 0) {
			for (int i = 0; i < oldTiles.length && i < mTiles.length; i++)
				mTiles[i] = oldTiles[i];
		}
		
		int pos = 0;
		for (int x = 0; x < (yRows); x++) {
			for (int i = 0; i < xRows; i++) {
				mIncrementsX[pos++] = i;
			}
		}
		
		pos = 0;
		for (int y = 0; y < (yRows); y++) {
			for (int i = 0; i < xRows; i++) {
				mIncrementsY[pos++] = y;
			}
		}
		
//		if (getWidth() > 0 && getHeight() > 0 && getWidth() > getHeight()) {
//			mIncrementsX = new int[] { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3 };
//			mIncrementsY = new int[] { 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2 };
//		} else {
//			mIncrementsX = new int[] { 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2 };
//			mIncrementsY = new int[] { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3 };
//		}
	}
	
	public void clear() {
		try {
			
			mTilesProvider.clearCache();
			mTilesProvider.clearResizeCache();
			mTilesProvider.stopThreads();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startTileThreads(boolean allowTileRequestViaHttp) {
		mTilesProvider = new TilesProvider(this.getContext(), mHandler, allowTileRequestViaHttp);
	}
	
	private int getMaxOffsetX() {
		return (int) (0 - (Math.pow(2, mZoomLevel)) * TILE_SIZE);
	}

	public int getOffsetX() {
		return mOffsetX;
	}

	public int getOffsetY() {
		return mOffsetY;
	}

	private Tile[] initializeCurrentTiles(int zoomLevel, int offsetX, int offsetY) {
		int mapX = (0 - offsetX) / TILE_SIZE;
		int mapY = (0 - offsetY) / TILE_SIZE;

		for (int index = 0; mTiles != null && index < mTiles.length; ++index) {
			if (mTiles[index] == null) {
				mTiles[index] = new Tile();
			}
			
			// try to save on string relocations
			if (mTiles[index].mapX != (mapX + mIncrementsX[index])
					|| mTiles[index].mapY != (mapY + mIncrementsY[index])
					|| mTiles[index].zoom != zoomLevel) {
				mTiles[index].mapX = mapX + mIncrementsX[index];
				mTiles[index].mapY = mapY + mIncrementsY[index];
				mTiles[index].offsetX = mTiles[index].mapX * TILE_SIZE;
				mTiles[index].offsetY = mTiles[index].mapY * TILE_SIZE;
				mTiles[index].zoom = zoomLevel;
				mTiles[index].key = (zoomLevel + "/" + mTiles[index].mapX + "/"
						+ mTiles[index].mapY + ".png").intern();
				mTiles[index].mapTypeId = mMaptTypeId;
			}
		}
		return mTiles;
	}
	
	private Tile[] initializePendingTiles(int zoomLevel, int offsetX, int offsetY) {
		int mapX = (0 - offsetX) / TILE_SIZE;
		int mapY = (0 - offsetY) / TILE_SIZE;
		
		Tile[] tiles = new Tile[mTiles.length];

		for (int index = 0; mTiles != null && index < mTiles.length; ++index) {
			tiles[index] = new Tile();
			tiles[index].mapX = mapX + mIncrementsX[index];
			tiles[index].mapY = mapY + mIncrementsY[index];
			tiles[index].offsetX = tiles[index].mapX * TILE_SIZE;
			tiles[index].offsetY = tiles[index].mapY * TILE_SIZE;
			tiles[index].zoom = zoomLevel;
			tiles[index].key = (zoomLevel + "/" + tiles[index].mapX + "/"
						+ tiles[index].mapY + ".png").intern();
			tiles[index].mapTypeId = mMaptTypeId;
		}
		
		return tiles;
	}

	private boolean isOnScreen(Tile tile) {
		if (tile == null) {
			return false;
		}

		int upperLeftX = tile.offsetX + this.mOffsetX;
		int upperLeftY = tile.offsetY + this.mOffsetY;
		int width = this.getWidth();
		int height = this.getHeight();

		if (((upperLeftX + TILE_SIZE) >= 0) && (upperLeftX < width)
				&& ((upperLeftY + TILE_SIZE) >= 0) && (upperLeftY < height)) {
			return isSane(tile);
		}
		return false;
	}

	private boolean isSane(Tile tile) {
		if (tile.mapX >= 0 && tile.mapY >= 0
				&& tile.mapX <= (Math.pow(2, mZoomLevel) - 1)
				&& tile.mapY <= (Math.pow(2, mZoomLevel) - 1)) {
			return true;
		}
		return false;
	}

	@Override
	protected void onAnimationEnd() {
		if (this.mZoomLevel > mPendingZoomLevel) {
			this.mZoomLevel = mPendingZoomLevel;
			zoomOut();
		} else if (this.mZoomLevel < mPendingZoomLevel) {
			this.mZoomLevel = mPendingZoomLevel;
			if(mIsDoubleTap)
				zoomInForDoubleTap();
			else
				zoomIn();
		}

		mTouchOffsetX = getOffsetX();
		mTouchOffsetY = getOffsetY();
		
		invalidate();
		
		super.onAnimationEnd();
	}

	private void onZoomLevelChanges() {
		for (OsmOverlay overlay : mOverlays) {
			overlay.onZoomLevelChanges(this);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		for (int i = (mTiles.length-1); i >= 0; i--) {
			if (isOnScreen(mTiles[i])) {
				
				Bitmap bitmap = mTilesProvider.getTileBitmap(mTiles[i]);

				if (bitmap != null) {
					canvas.drawBitmap(bitmap, mOffsetX + mTiles[i].offsetX, mOffsetY + mTiles[i].offsetY, null);
				}
			}
		}
		
		for (OsmOverlay overlay : mOverlays) {
			if (overlay == null)
				continue;
			overlay.draw(canvas, this);
		}
	}

	public void setOffsetX(int offsetX) {
		if (mPendingZoomLevel == this.mZoomLevel) {
			if ((this.getWidth() != 0) && ((offsetX + 255) > this.getWidth())) {
				this.mOffsetX = this.getWidth() - 255;
			} else if ((offsetX - 255) < getMaxOffsetX()) {
				this.mOffsetX = getMaxOffsetX() + 255;
			} else {
				this.mOffsetX = offsetX;
			}
			
			//Log.i("setOffsetX", "OffsetX= " + mOffsetX);
		}
	}

	public void setOffsetY(int offsetY) {
		if (mPendingZoomLevel == this.mZoomLevel) {
			if ((this.getHeight() != 0) && ((offsetY + 255) > this.getHeight())) {
				this.mOffsetY = this.getHeight() - 255;
			} else if ((offsetY - 255) < getMaxOffsetX()) {
				this.mOffsetY = getMaxOffsetX() + 255;
			} else {
				this.mOffsetY = offsetY;
			}
			initializeCurrentTiles(mZoomLevel, this.mOffsetX, this.mOffsetY);
			
			//Log.i("setOffsetY", "OffsetY= " + mOffsetY);
		}
	}

	public int setZoom(int zoomLevel) {
			
		if (zoomLevel > MAX_ZOOM_LEVEL)
			zoomLevel = MAX_ZOOM_LEVEL;

		if (zoomLevel < mMinZoomLevel)
			zoomLevel = mMinZoomLevel;
		
		this.mZoomLevel = zoomLevel;
		this.mPendingZoomLevel = zoomLevel;
		
		onZoomLevelChanges();
		
		return this.mZoomLevel;
	}

	private void zoomInForDoubleTap() {
		setOffsetX((getOffsetX()) * 2 - this.mTouchDownX);
		setOffsetY((getOffsetY()) * 2 - this.mTouchDownY);
//		mTilesProvider.clearResizeCache();
		mIsDoubleTap=false;
		
		onZoomLevelChanges();
	}
	public void zoomIn() {
		setOffsetX((getOffsetX()) * 2 - (getWidth() / 2));
		setOffsetY((getOffsetY()) * 2 -(getHeight() / 2));
//		mTilesProvider.clearResizeCache();
		
		onZoomLevelChanges();
	}

	public void zoomOut() {
		setOffsetX(getOffsetX() / 2 + (getWidth() / 4));
		setOffsetY(getOffsetY() / 2 + (getHeight() / 4));
//		mTilesProvider.clearResizeCache();
		
		onZoomLevelChanges();
	}
	
	public class Tiles extends Vector<Tile> {
		private static final long serialVersionUID = -6468659912600523042L;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		//ACTION_DOWN
		this.mTouchDownX = (int) event.getX();
		this.mTouchDownY = (int) event.getY();
		this.mTouchOffsetX = this.mOffsetX;
		this.mTouchOffsetY = this.mOffsetY;
		return true;
	}

	@Override
	public boolean onFling(MotionEvent downEvent, MotionEvent event, float distanceXf,
			float distanceYf) {
		//ACTION_UP
		setOffsetX(this.mTouchOffsetX  + (int) event.getX() - this.mTouchDownX);
		setOffsetY(this.mTouchOffsetY + (int) event.getY() - this.mTouchDownY);
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
	}

	@Override
	public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float arg2,
			float arg3) {
		// ACTION_MOVE Equivalent
		setOffsetX(this.mTouchOffsetX + (int) currentEvent.getX() - this.mTouchDownX);
		setOffsetY(this.mTouchOffsetY+ (int) currentEvent.getY() - this.mTouchDownY);
		invalidate();
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {

		for (OsmOverlay overlay : mOverlays) {
			if (overlay.onInterceptSingleTap(event, this)) {
				overlay.onSingleTap(event, this);
				invalidate();
				return true;
			}
		}
		for (OsmOverlay overlay : mOverlays) {
			if (overlay.onSingleTap(event, this)) {
				invalidate();
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void onShowPress(MotionEvent event) {
	}

}
