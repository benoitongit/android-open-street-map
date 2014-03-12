package com.android.lib.map.osm.overlay;

import java.util.List;

import android.graphics.Paint;
import android.graphics.Path;

import com.android.lib.map.osm.GeoPoint;

public class MapTrack {

	private List<GeoPoint> mTrack;
	private MapMarker mTrackStartMarker;
	private MapMarker mTrackEndMarker;
	private Paint mPaint;
	private Path mPath;
	
	public MapTrack() {
		mPath = new Path();
	}
	
	public List<GeoPoint> getTrack() {
		return mTrack;
	}

	public void setTrack(List<GeoPoint> track) {
		mTrack = track;
	}
	
	public MapMarker getStartMarker() {
		return mTrackStartMarker;
	}

	public void setStartMarker(MapMarker startMapMarker) {
		mTrackStartMarker = startMapMarker;
	}
	
	public MapMarker getEndMarker() {
		return mTrackEndMarker;
	}
	
	public void setEndMarker(MapMarker endMapMarker) {
		mTrackEndMarker = endMapMarker;
	}

	public Paint getPaint() {
		return mPaint;
	}
	
	public void setPaint(Paint paint) {
		mPaint = paint;
	}
	
	public Path getPath() {
		return mPath;
	}
}
