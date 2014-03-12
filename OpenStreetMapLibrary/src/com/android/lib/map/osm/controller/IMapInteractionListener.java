package com.android.lib.map.osm.controller;

import com.android.lib.map.osm.overlay.MapMarker;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface IMapInteractionListener {
	public boolean onMapTouchEvent(MotionEvent event);
	public void onMapDraw(Canvas canvas);
	public void onMapSingleTapConfirmed(MotionEvent event);
	public void onMapLongClick(MotionEvent event);
	public void onMapMarkerTap(MapMarker overlayItem);
	public void onMapStopPanning();
	public void onMapZoomChanged(int zoomLevel);
	public void onMapCalloutTap(MotionEvent event);
}
