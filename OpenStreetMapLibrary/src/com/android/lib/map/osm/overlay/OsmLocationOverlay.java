package com.android.lib.map.osm.overlay;

import com.android.lib.map.osm.GeoPoint;
import com.android.lib.map.osm.OsmMapView;
import com.android.lib.map.osm.OsmMapView.OsmMapViewBuilder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Location;
import android.view.View;


public class OsmLocationOverlay extends OsmOverlay {
	
	private Bitmap mLocationDot;
	private Bitmap[] mArrowBitmaps;
	private OsmMapViewBuilder mOsmMapViewBuilder;
	private Context mContext;
	private Location mLocation;
	private Integer mHeading;
	private OsmMapView mMapView;
	
	
	public OsmLocationOverlay(Context c, OsmMapViewBuilder mapbuilder, OsmMapView mapView) {
		mLocationDot = BitmapFactory.decodeResource(c.getResources(), mapbuilder.getPositionIndicatorDrawableId());
		mOsmMapViewBuilder = mapbuilder;
		mContext = c;
		mMapView = mapView;
		initHeadingArrow();
	}

	@Override
	public void draw(android.graphics.Canvas canvas, View view) {
		GeoPoint g = null;
		
		if (mLocation != null) {
			g = new GeoPoint((int) (mLocation.getLatitude() * 1E6), (int) (mLocation.getLongitude() * 1E6));
		}

		if (g != null) {
						
			if (mHeading != null) {
				
				drawBearingArrow(canvas, mMapView, g, mHeading);
				
			} else {
				Point center = mMapView.geopointToPixelProjection(g);
				canvas.drawBitmap(mLocationDot, center.x - (mLocationDot.getWidth()  / 2), 
					center.y - (mLocationDot.getHeight() / 2),  null);
			}
		}
	}
	
	private void initHeadingArrow() {
		mArrowBitmaps = new Bitmap[20]; 
		mArrowBitmaps[0] = BitmapFactory.decodeResource(mContext.getResources(), mOsmMapViewBuilder.getArrowPositionIndicatorDrawableId());
		for (int i = 1, angle = 20; angle <= 360; i++, angle += 20) {
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			mArrowBitmaps[i] = Bitmap.createBitmap(mArrowBitmaps[0], 0, 0, mArrowBitmaps[0].getWidth(),
					mArrowBitmaps[0].getHeight(), matrix, true);
		}
	}
	
	public void setLocation(Location location) {
		mLocation = location;
	}
	
	public void setHeading(Integer heading) {
		mHeading = heading;
	}
	
	private void drawBearingArrow(Canvas canvas, OsmMapView mapView, GeoPoint currentLocation, int heading) {

		try {
		
			int indexArrowBitmap = heading/20;
			// translate the GeoPoint to screen pixels
			Point screenPts = mapView.geopointToPixelProjection(currentLocation);
	
			// add the rotated marker to the canvas
			canvas.drawBitmap(mArrowBitmaps[indexArrowBitmap],
					screenPts.x - (mArrowBitmaps[indexArrowBitmap].getWidth() / 2), screenPts.y
							- (mArrowBitmaps[indexArrowBitmap].getHeight() / 2), null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
