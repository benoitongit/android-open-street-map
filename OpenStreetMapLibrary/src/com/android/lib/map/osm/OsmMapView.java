package com.android.lib.map.osm;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.android.lib.map.osm.controller.IMapInteractionListener;
import com.android.lib.map.osm.helpers.ScaleGestureHelper;
import com.android.lib.map.osm.helpers.ScaleGesturePreFroyoHelper;
import com.android.lib.map.osm.helpers.ScaleGesturePreFroyoHelper.IScaleGesturePreFroyo;
import com.android.lib.map.osm.overlay.MapMarker;
import com.android.lib.map.osm.overlay.MapPolygon;
import com.android.lib.map.osm.overlay.MapTrack;
import com.android.lib.map.osm.overlay.OsmMarkerOverlay;
import com.android.lib.map.osm.overlay.OsmOverlay;
import com.android.lib.map.osm.overlay.OsmPolygonOverlay;
import com.android.lib.map.osm.overlay.OsmTrackOverlay;
import com.android.lib.map.osm.utils.CountDownTimer;


public class OsmMapView extends OsmMapViewBase {
	
	private IMapInteractionListener mMapInteractionListener;
	private List<OsmOverlay> mMapOverlays;
	private GestureDetector mDetector;
	private ScaleGesturePreFroyoHelper mScaleGesturePreFroyo = null;
	private ScaleGestureHelper mScaleGesture = null;
	private int mZoomFactorForScaleGesture;
	private boolean mIsScrolling = false;
	private OsmMarkerOverlay mMarkerOverlay;
	private OsmTrackOverlay mTrackOverlay;
	private OsmPolygonOverlay mPolygonOverlay;
	private OsmMarkerOverlay mTrackStartEndMarkerOverlay;
	private float mActionDownEventX;
	private float mActionDownEventY;
	private float mActionMoveEventX;
	private float mActionMoveEventY;
	private float mAnimationOffsetRight;
	private float mAnimationOffsetLeft;
	private float mAnimationOffsetTop;
	private float mAnimationOffsetBottom;
	
	
	public OsmMapView(Context context, OsmMapViewBuilder mapbuilder, IMapInteractionListener mapInteractionListener) {
		super(context, mapbuilder.getMapTypeId());
		
		mMapOverlays = getOverlays();
		mMapOverlays.clear();
		setBackgroundColor(mapbuilder.getBackgrounColor()); // Map tile background color before loading tiles 

		try {
			if (android.os.Build.VERSION.SDK_INT >= 8)
				mScaleGesture = new ScaleGestureHelper(context, new MySimpleOnScaleGestureListener());
			else
				mScaleGesturePreFroyo = new ScaleGesturePreFroyoHelper(new MyScaleGestureListenerPreFroyo());
		} catch (Error e) {
			// catch error for 1.6 platform that doesn't handle multitouch
			mScaleGesturePreFroyo = null;
		}
		
		setMapTileUnavailableBitmap(mapbuilder.getMapTileUnavailableBitmap());
		
		startTileThreads(mapbuilder.getIsNetworkRequestAllowed());
		mTrackOverlay = new OsmTrackOverlay(this);
		mPolygonOverlay = new OsmPolygonOverlay(this);
		mTrackStartEndMarkerOverlay = new OsmMarkerOverlay(this, null);
		mMarkerOverlay = new OsmMarkerOverlay(this, null);
		mMapOverlays.add(mPolygonOverlay);
		mMapOverlays.add(mTrackOverlay);
		mMapOverlays.add(mTrackStartEndMarkerOverlay);
		mMapOverlays.add(mMarkerOverlay);

		mMapInteractionListener = mapInteractionListener;
		mDetector = new GestureDetector(context, this);
	}
	
	public void addMarker(MapMarker marker) {

		mMarkerOverlay.addMarker(marker);
		invalidate();
	}

	public void addMarkers(List<? extends MapMarker> markers) {
		mMarkerOverlay.addMarkers(markers);
		invalidate();
	}
	
	public void addMarkersFadeIn(List<? extends MapMarker> markers) {
		mMarkerOverlay.addMarkersFadeIn(markers);
	}
	
	public void removeMarkers(List<? extends MapMarker> markers) {
		mMarkerOverlay.removeMarkers(markers);
		invalidate();
	}

	public void removeMarkersFadeOut(List<? extends MapMarker> markers) {
		mMarkerOverlay.removeMarkersFadeOut(markers);
	}
	
	public void removeMarkers() {
		mMarkerOverlay.removeMarkers();
		invalidate();
	}
	
	public void removeMarker(MapMarker marker) {
		mMarkerOverlay.removeMarker(marker);
		invalidate();
	}
	
	public void addOverlay(OsmOverlay overlay) {
		mMapOverlays.add(overlay);
		invalidate();
	}

	public void removeOverlay(OsmOverlay overlay) {
		mMapOverlays.remove(overlay);
		invalidate();
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}
	
	public List<MapMarker> getMarkers() {
		return mMarkerOverlay.getMarkers();
	}
	
	public IMapInteractionListener getMapIntereractionListener() {
		return mMapInteractionListener;
	}

	public void addTracks(List<? extends MapTrack> tracks, boolean showStartEndMarkers) {
		
		MapTrack lastTrack = null;
		MapMarker startMarker = null;
		for (MapTrack track : tracks) {
			if (startMarker == null) {
				startMarker = track.getStartMarker();
			}
			mTrackOverlay.addTrack(track);
			lastTrack = track;
		}

		if (showStartEndMarkers) {
			if (lastTrack != null && lastTrack.getEndMarker() != null)
				mTrackStartEndMarkerOverlay.addMarker(lastTrack.getEndMarker());
			if (startMarker != null)
				mTrackStartEndMarkerOverlay.addMarker(startMarker);
		}
	}
		
	public void removeTracks() {
		mTrackOverlay.removeTracks();
		mTrackStartEndMarkerOverlay.removeMarkers();
	}
	

	@Override
	public void clear() {
		removeMarkers();
		removeTracks();
		removePolygons();
		mMapOverlays.clear();
		super.clear();
	}

	public int getLatitudeSpanE6(int mapWidth, int mapHeight) {
	    int top = getOffsetY();
	    int bottom;

	    bottom = getOffsetY() - mapHeight;
	    
	    GeoPoint gTop = getProjectionFromPixels(getOffsetX(), top);
	    GeoPoint gBottom = getProjectionFromPixels(getOffsetX(), bottom);
	    
	    return (gTop.getLatitudeE6() - gBottom.getLatitudeE6());
	}

	public int getLongitudeSpanE6(int mapWidth, int mapHeight) {
	    int left = getOffsetX();
	    int right;

	    right = getOffsetX() + mapWidth;
	    
	    GeoPoint gRight = getProjectionFromPixels(right, getOffsetY());
	    GeoPoint gLeft = getProjectionFromPixels(left, getOffsetY());
	   
	    return (gLeft.getLongitudeE6() - gRight.getLongitudeE6());
		

	}
	
	public boolean zoomInOneLevel() {
		boolean result = animateZoomIn();
		return result;
	}

	public boolean zoomOutOneLevel() {
		boolean result = animateZoomOut();
		return result;
	}

	public void setCenter(GeoPoint location) {
		if (location != null)
			setCenter(location.getLatitudeE6() / 1E6, location.getLongitudeE6() / 1E6);
	}

	
	public void setCenter(double maxLat, double maxLon, double minLat, double minLon) {

//		if (getWidth() == 0 && getHeight() == 0) {
//			mSetCenterForBBoxWhenGetView = new Double[4];
//			mSetCenterForBBoxWhenGetView[0] = maxLat;
//			mSetCenterForBBoxWhenGetView[1] = maxLon;
//			mSetCenterForBBoxWhenGetView[2] = minLat;
//			mSetCenterForBBoxWhenGetView[3] = minLon;
//			return;
//		}

		double centerLat = maxLat - ((maxLat - minLat) / 2);
		double centerLon = maxLon - ((maxLon - minLon) / 2);
		setCenter(centerLat, centerLon);
	}
		
	public void setZoom(double maxLat, double maxLon, double minLat, double minLon, int paddingWidth, int paddingHeight) {
		int zoom = Projection.getZoomLevelFromBox(maxLat, maxLon, minLat, minLon, 
				getWidth(), getHeight(), paddingWidth, paddingHeight);

		setZoom(zoom);
	}
	
//	@Override
//	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//		super.onSizeChanged(w, h, oldw, oldh);
//		
//		if (mSetCenterForBBoxWhenGetView != null && w > 0 && h > 0) {
//			setCenterAndZoom(mSetCenterForBBoxWhenGetView[0], mSetCenterForBBoxWhenGetView[1],
//					mSetCenterForBBoxWhenGetView[2], mSetCenterForBBoxWhenGetView[3]);
//			mSetCenterForBBoxWhenGetView = null;
//		}
//	}

	public int getZoomLevel() {
		return mZoomLevel;
	}

	public void translate(int pixelX, int pixelY) {
		setOffsetX(getOffsetX() + pixelX);
		setOffsetY(getOffsetY() + pixelY);
		invalidate();
	}
	
	public void animateTranslation(final int pixelX, final int pixelY, final int animationTime, 
			final OnAnimateTranslationListener listener) {
		int interval= 30;
		mAnimationOffsetBottom=0;
		mAnimationOffsetLeft=0;
		mAnimationOffsetRight=0;
		mAnimationOffsetTop=0;
		final double multiplier = ((float)animationTime / interval) / animationTime;
		new CountDownTimer(animationTime,  interval) {

			@Override
			public void onTick() {
				if ( mAnimationOffsetLeft < pixelX || mAnimationOffsetRight > pixelX 
						|| mAnimationOffsetBottom > pixelY  || mAnimationOffsetTop < pixelY){
					int progressPositionX = 0;
					if (mAnimationOffsetLeft < pixelX){
						progressPositionX = (int) Math.round(((pixelX) * multiplier));
						mAnimationOffsetLeft = mAnimationOffsetLeft + progressPositionX;
					}
					if (mAnimationOffsetRight > pixelX){
						progressPositionX = (int) Math.round(((pixelX) * multiplier));
						mAnimationOffsetRight = mAnimationOffsetRight + progressPositionX;
					}
					int progressPositionY=0;
					if (mAnimationOffsetBottom > pixelY){
						progressPositionY = (int) Math.round(((pixelY) * multiplier));
						mAnimationOffsetBottom = mAnimationOffsetBottom + progressPositionY;
					}
					
					if (mAnimationOffsetTop < pixelY){
						progressPositionY = (int) Math.round(((pixelY) * multiplier));
						mAnimationOffsetTop = mAnimationOffsetTop + progressPositionY;
					}

					translate(progressPositionX,  progressPositionY);
					invalidate();
					
					
				} else {
					this.cancel();
				}
			}
			
			@Override
			public void onFinish() {
								
				invalidate();
				
				if (listener != null)
					listener.onAnimateTranslationEnds();
			}
		}.start();
	}
	
	public GeoPoint pixelToGeoPointProjection(int x, int y) {
	    int offsetX = getOffsetX() - x;
	    int offsetY = getOffsetY() - y;
			    
	    GeoPoint g =  getProjectionFromPixels(offsetX, offsetY);
	    return g;
	}

	public Point geopointToPixelProjection(GeoPoint coordinate) {
		
		if (coordinate == null) {
			return new Point(0,0);
		}
		
		double lat = coordinate.getLatitudeE6() / 1E6;
		double lon = coordinate.getLongitudeE6() / 1E6;
		
	    double x = Projection.getXPixelFromLongitude(lon, getZoomLevel());
	    double y = Projection.getYPixelFromLatitude(lat, getZoomLevel());

		Point point = new Point();
		point.x = (int) (getOffsetX() - (0 - x));
		point.y = (int) (getOffsetY() - (0 - y));
		
		return point;
	}
	
	@Override
	protected void onDraw(Canvas canvas)  {
		super.onDraw(canvas);
		//mLocationOverlay.dispatchDraw(canvas, this, mLocation, mLocationHeading);
		mMapInteractionListener.onMapDraw(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mActionDownEventX = event.getX();
				mActionDownEventY = event.getY();
				mActionMoveEventX = event.getX();
				mActionMoveEventY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				mActionMoveEventX = event.getX();
				mActionMoveEventY = event.getY();
				mIsScrolling = true;
				break;
			case MotionEvent.ACTION_UP:
				if (mIsScrolling && mMapInteractionListener != null) {
					mMapInteractionListener.onMapStopPanning();
				}
				mIsScrolling = false;
				break;
		}
		
		if (mMapInteractionListener.onMapTouchEvent(event))
			return true;
		
		if (mScaleGesturePreFroyo != null && mScaleGesturePreFroyo.onTouchEvent(event))
			return true; // if true event handled by mScaleGesture
	
		if (mScaleGesture != null && mScaleGesture.onTouchEvent(event))
			return true;
		
		return mDetector.onTouchEvent(event);
	}

	public int getMaxZoomLevel() {
		return MAX_ZOOM_LEVEL;
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		mIsDoubleTap=true;
		animateZoomIn();
		//mMapInteractionListener.onMapZoomChanged(mPendingZoomLevel);
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		return false;
	}

	
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {
		mMapInteractionListener.onMapSingleTapConfirmed(event);
		return super.onSingleTapConfirmed(event);
	}

	@Override
	public void onLongPress(MotionEvent event) {
		super.onLongPress(event);
	
		try {
			int xDownRounded =  Math.round(mActionDownEventX / 20f);
			int yDownRounded =  Math.round(mActionDownEventY / 20f);
			int xMoveRounded =  Math.round(mActionMoveEventX / 20f);
			int yMoveRounded =  Math.round(mActionMoveEventY / 20f);			
			//Log.i("", "xDownRounded = " + xDownRounded + "  xMoveRounded = " + xMoveRounded);
			//Log.i("", "yDownRounded = " + yDownRounded + "  yMoveRounded = " + yMoveRounded);
			if (xDownRounded == xMoveRounded && yDownRounded == yMoveRounded 
					&& mMapInteractionListener != null) {
				mMapInteractionListener.onMapLongClick(event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean setCenterAndZoomOnTracksAndMarkers() {
		Double[] bbox = getBBoxForTracksAndMarkers();
		if (bbox != null) {
			setZoom(bbox[0], bbox[1], bbox[2], bbox[3], 0, 0);
			setCenter(bbox[0], bbox[1], bbox[2], bbox[3]);
			return true;
		}
		return false;
	}
	
	public Double[] getBBoxForTracksAndMarkers() {
		List<MapMarker> markers = getMarkers();
		
		int lat = 0;
		int lon = 0;
		Integer maxLat = null;
		Integer maxLon = null;
		Integer minLat = null;
		Integer minLon = null;
		
		for (MapMarker marker : markers) {
			GeoPoint geoPoint = marker.getCoordinate();
			lat = geoPoint.getLatitudeE6();
			lon = geoPoint.getLongitudeE6();
			
			if (maxLat == null || (maxLat < lat))
				maxLat = lat;
			if (maxLon == null || (maxLon < lon))
				maxLon = lon;
			if (minLat == null || (minLat > lat))
				minLat = lat;
			if (minLon == null || (minLon > lon))
				minLon = lon;
		}
		
		List<MapTrack> tracks = mTrackOverlay.getTracks();
		for (MapTrack track : tracks) {
			for (GeoPoint point : track.getTrack()) {
				
				lat = point.getLatitudeE6();
				lon = point.getLongitudeE6();
				
				if (maxLat == null || (maxLat < lat))
					maxLat = lat;
				if (maxLon == null || (maxLon < lon))
					maxLon = lon;
				if (minLat == null || (minLat > lat))
					minLat = lat;
				if (minLon == null || (minLon > lon))
					minLon = lon;
			}
		}
		
		
		if (maxLat != null && maxLon != null && minLat != null && minLon != null) {
			Double[] bbox = new Double[4];
			bbox[0] = maxLat/1E6;
			bbox[1] = maxLon/1E6;
			bbox[2] = minLat/1E6;
			bbox[3] = minLon/1E6;
			return bbox;
		}
		return null;
	}

	public int getMinZoomLevel() {
		return mMinZoomLevel;
	}

	public void setMinZoomLevel(int zoomLevel) {
		mMinZoomLevel = zoomLevel;
	}

	public void addPolygon(MapPolygon polygon) {
		mPolygonOverlay.addPolygon(polygon);
	}

	public void removePolygons() {
		mPolygonOverlay.removePolygons();
	}
	
	public List<MapPolygon> getPolygons() {
		return mPolygonOverlay.getPolygons();
	}	
	
	public void setCenterAndZoomOnMarkers(List<? extends MapMarker> markers) {
		int lat = 0;
		int lon = 0;
		Integer maxLat = null;
		Integer maxLon = null;
		Integer minLat = null;
		Integer minLon = null;
		
		for (MapMarker marker : markers) {
			GeoPoint geoPoint = marker.getCoordinate();
			lat = geoPoint.getLatitudeE6();
			lon = geoPoint.getLongitudeE6();
			
			if (maxLat == null || (maxLat < lat))
				maxLat = lat;
			if (maxLon == null || (maxLon < lon))
				maxLon = lon;
			if (minLat == null || (minLat > lat))
				minLat = lat;
			if (minLon == null || (minLon > lon))
				minLon = lon;
		}
		
		if (maxLat != null && maxLon != null && minLat != null && minLon != null) {
			Double[] bbox = new Double[4];
			bbox[0] = maxLat/1E6;
			bbox[1] = maxLon/1E6;
			bbox[2] = minLat/1E6;
			bbox[3] = minLon/1E6;
			setZoom(bbox[0], bbox[1], bbox[2], bbox[3], 0, 0);
			setCenter(bbox[0], bbox[1], bbox[2], bbox[3]);
		}
	}
	
	public void setCenterAndZoomOnPolygons() {
		List<MapPolygon> polygons = mPolygonOverlay.getPolygons();
		int lat = 0;
		int lon = 0;
		Integer maxLatE6 = null;
		Integer maxLonE6 = null;
		Integer minLatE6 = null;
		Integer minLonE6 = null;
		
		for (MapPolygon polygon : polygons) {
			List<GeoPoint> points = polygon.getPolygon();
			
			for (GeoPoint point : points) {
			
				lat = point.getLatitudeE6();
				lon = point.getLongitudeE6();
				
				if (maxLatE6 == null || (maxLatE6 < lat))
					maxLatE6 = lat;
				if (maxLonE6 == null || (maxLonE6 < lon))
					maxLonE6 = lon;
				if (minLatE6 == null || (minLatE6 > lat))
					minLatE6 = lat;
				if (minLonE6 == null || (minLonE6 > lon))
					minLonE6 = lon;
			}
		}
	
		if (maxLatE6 != null && maxLonE6 != null && minLatE6 != null && minLonE6 != null) {
			double maxLat = ((double)maxLatE6 / 1E6);
			double minLat = ((double)minLatE6 / 1E6);
			double maxLon = ((double)maxLonE6 / 1E6);
			double minLon = ((double)minLonE6 / 1E6);
			setZoom(maxLat, maxLon, minLat, minLon, 0, 0);
			setCenter(maxLat, maxLon, minLat, minLon);			
		}
	}
	
	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		
		if (mMapInteractionListener != null)
			mMapInteractionListener.onMapZoomChanged(mZoomLevel);
	}
	
	private class MySimpleOnScaleGestureListener implements ScaleGestureHelper.IScaleGestureListener {

		@Override
		public void onScale(float distanceF) {
			int distance = (int) distanceF;
			
			int zoomFactor = (int) Math.floor(distance / 100);
			
			//Log.i("", "zoomFactor = " + zoomFactor);
			
			if (mZoomFactorForScaleGesture == 0) {
				mZoomFactorForScaleGesture = zoomFactor;
				return;
			}
			
			if (zoomFactor > mZoomFactorForScaleGesture) {
				animateZoomIn();
			} else if (zoomFactor < mZoomFactorForScaleGesture) {
				animateZoomOut();
			}
			
			mZoomFactorForScaleGesture = zoomFactor;
		}

		@Override
		public void onScaleEnd() {
			mZoomFactorForScaleGesture = 0;
		}
	}
	
	private class MyScaleGestureListenerPreFroyo implements IScaleGesturePreFroyo {

		@Override
		public void onScaleBegin(MotionEvent event) {
			
		}
		
		@Override
		public void onScale(MotionEvent event,  float startDistance, float lastDistance) {
			//Log.i("onScale", "ScaleGesture   startDistance= " + startDistance + "  lastDistance= " + lastDistance);
			
			int distance = (int) (startDistance - lastDistance);
			
			int zoomFactor = distance / 100;
			if (zoomFactor < 1)
				zoomFactor *= -1;
			
			boolean changeZoom = false;
			
			if (mZoomFactorForScaleGesture != zoomFactor && zoomFactor > 0) {
				mZoomFactorForScaleGesture = zoomFactor;
				changeZoom = true;
			}
			
			if (distance > 0 && changeZoom) {
				animateZoomOut();
			} else if (changeZoom) {
				animateZoomIn();
			}
		}

		@Override
		public void onScaleEnd(float startDistance, float lastDistance) {
			mZoomFactorForScaleGesture = 0;
		}

	}

	public static class OsmMapViewBuilder {
		
		private int mBackgrounColor = Color.parseColor("#FFDADBD7");
		private Bitmap mMapTileUnavailableBitmap = null;
		private boolean mIsNetworkRequestAllowed = false;		
		private int mMapTypeId = 1;
		private int mPositionIndicatorDrawableId = R.drawable.blue_position_indicator;
		private int mArrowPositionIndicatorDrawableId = R.drawable.blue_arrow_indicator;
		
		
		public int getArrowPositionIndicatorDrawableId() {
			return mArrowPositionIndicatorDrawableId;
		}

		public void setArrowPositionIndicatorDrawableId(int arrowPositionIndicatorDrawableId) {
			this.mArrowPositionIndicatorDrawableId = arrowPositionIndicatorDrawableId;
		}

		public int getPositionIndicatorDrawableId() {
			return mPositionIndicatorDrawableId;
		}

		public void setPositionIndicatorDrawableId(int positionIndicatorDrawableId) {
			this.mPositionIndicatorDrawableId = positionIndicatorDrawableId;
		}
		
		public int getMapTypeId() {
			return mMapTypeId;
		}

		public void setMapTypeId(int mapTypeId) {
			this.mMapTypeId = mapTypeId;
		}

		public boolean getIsNetworkRequestAllowed() {
			return mIsNetworkRequestAllowed;
		}

		public void setIsNetworkRequestAllowed(boolean isNetworkRequestAllowed) {
			this.mIsNetworkRequestAllowed = isNetworkRequestAllowed;
		}

		public Bitmap getMapTileUnavailableBitmap() {
			return mMapTileUnavailableBitmap;
		}

		public void setMapTileUnavailableBitmap(Bitmap mapTileUnavailableBitmap) {
			this.mMapTileUnavailableBitmap = mapTileUnavailableBitmap;
		}

		public int getBackgrounColor() {
			return mBackgrounColor;
		}

		public void setBackgrounColor(int backgrounColor) {
			this.mBackgrounColor = backgrounColor;
		}

	}

	public interface OnAnimateTranslationListener {
		public void onAnimateTranslationEnds();
	}
}
