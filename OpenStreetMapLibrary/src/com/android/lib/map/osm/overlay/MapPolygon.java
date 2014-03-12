package com.android.lib.map.osm.overlay;

import java.util.List;

import android.graphics.Paint;
import android.graphics.Path;

import com.android.lib.map.osm.GeoPoint;

public class MapPolygon {

	private List<GeoPoint> mPolygon;
	private Paint mPaintStroke;
	private Paint mPaint;
	private Path mPath;
	
	public MapPolygon() {
		mPath = new Path();
	}
	
	public void setPaintStroke(Paint paint) {
		mPaintStroke = paint;
	}
	
	public Paint getPaintStroke() {
		return mPaintStroke;
	}

	public void setPaint(Paint paint) {
		mPaint = paint;
	}
	
	public Paint getPaint() {
		return mPaint;
	}
	
	public void setPath(Path path) {
		mPath = path;
	}
	
	public Path getPath() {
		return mPath;
	}
	
	public void setPolygon(List<GeoPoint> polygon) {
		mPolygon = polygon;
	}

	public List<GeoPoint> getPolygon() {
		return mPolygon;
	}

}
