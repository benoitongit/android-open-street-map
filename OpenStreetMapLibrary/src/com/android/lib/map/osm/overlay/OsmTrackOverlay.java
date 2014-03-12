package com.android.lib.map.osm.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.view.View;

import com.android.lib.map.osm.GeoPoint;
import com.android.lib.map.osm.OsmMapView;

public class OsmTrackOverlay extends OsmOverlay {

	private List<MapTrack> mTracks;
	private GeoPoint mStartLocation;
	private OsmMapView mMapView;
	private Map<Path, Point> mStartPoints;
	
	public OsmTrackOverlay(OsmMapView mapView) {

		mTracks = new ArrayList<MapTrack>();
		mMapView = mapView;
		mStartPoints = new HashMap<Path, Point>();
	}
	
	public void addTrack(MapTrack track) {
    	mTracks.add(track);
	}
	
	public void removeTracks() {
		mTracks.clear();
		mStartPoints.clear();
	}
	
	public List<MapTrack> getTracks() {
		return mTracks;
	}
	
	@Override
	public void draw(Canvas canvas, View view) {
		super.draw(canvas, view);

		for (MapTrack track :  mTracks) {
			if (track.getPath().isEmpty()) {
				drawTrack(track.getPath());
			}
		
			offsetPath(track.getPath());
			canvas.drawPath(track.getPath(), track.getPaint());
		}
	}

	private void drawTrack(Path path) {

		if (!path.isEmpty())
			path.rewind();

		GeoPoint previousPoint = null;
		
		Point startPoint = new Point();
		startPoint.x = mMapView.getOffsetX();
		startPoint.y = mMapView.getOffsetY();
		mStartPoints.put(path, startPoint);
		
		for (MapTrack track : mTracks) {
		
			for (GeoPoint g : track.getTrack()) {

				if (previousPoint == null) {
					previousPoint = g;
					if (mStartLocation == null)
						mStartLocation = g;

					continue;
				}

				drawLine(path, g, previousPoint);

				previousPoint = g;
			}
	
			previousPoint = null;
		}
	}

	@Override
	public void onZoomLevelChanges(View view) {
		super.onZoomLevelChanges(view);

		for (MapTrack track :  mTracks) {
			drawTrack(track.getPath());
		}	
	}
	
	private void offsetPath(Path path) {
				
		Point startPoint = mStartPoints.get(path);
		
		if (startPoint == null && path != null && !path.isEmpty()) {
			drawTrack(path);
		}
		
		if (startPoint != null && !path.isEmpty()) {
			int x = mMapView.getOffsetX() - startPoint.x;
			int y = mMapView.getOffsetY() - startPoint.y;

			path.offset(x, y);
			startPoint.x = mMapView.getOffsetX();
			startPoint.y = mMapView.getOffsetY();
		}
	}

	public void drawLine(Path path, GeoPoint gp1, GeoPoint gp2) {
		Point p1 = convertGeoPointToPixel(gp1);
		Point p2 = convertGeoPointToPixel(gp2);

		path.moveTo(p2.x, p2.y);
		path.lineTo(p1.x, p1.y);
	}

	private Point convertGeoPointToPixel(GeoPoint g) {
		return mMapView.geopointToPixelProjection(g);
	}

}
