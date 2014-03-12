package com.test.testprojectopenstreetmap;

import java.io.File;

import com.android.lib.map.osm.OsmMapView;
import com.android.lib.map.osm.controller.IMapInteractionListener;
import com.android.lib.map.osm.helpers.OsmDatabaseHelper;
import com.android.lib.map.osm.models.OsmModel;
import com.android.lib.map.osm.overlay.MapMarker;
import com.android.lib.map.osm.overlay.OsmLocationOverlay;
import com.test.testprojectopenstreetmap.LocationListenerHelper.IMyLocationListener;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MapActivity extends Activity implements IMapInteractionListener,
		IMyLocationListener {

	private LocationListenerHelper mLocationListener;
	private OsmMapView mOsmMapView;
	private OsmLocationOverlay mOsmLocationOverlay;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mLocationListener = new LocationListenerHelper(this);

		initOsmDatabase();

		initMap();
	
	}

	@Override
	protected void onResume() {
		super.onResume();

		mLocationListener.startListeningLocation(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mLocationListener.stopListeningLocation();
	}

	@Override
	protected void onDestroy() {
		mOsmMapView.clear();
		super.onDestroy();
	}

	private void initMap() {
		OsmMapView.OsmMapViewBuilder mapBuilder = new OsmMapView.OsmMapViewBuilder();

		mapBuilder.setIsNetworkRequestAllowed(true);
		mapBuilder.setPositionIndicatorDrawableId(R.drawable.blue_position_indicator);
		
		mOsmMapView = new OsmMapView(getApplicationContext(), mapBuilder, this);

		mOsmLocationOverlay = new OsmLocationOverlay(getApplicationContext(), mapBuilder, mOsmMapView);
		
		mOsmMapView.addOverlay(mOsmLocationOverlay);
		
		ViewGroup mapLayout = (ViewGroup) findViewById(R.id.mapLayout);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
		mapLayout.addView(mOsmMapView, layoutParams);

		mOsmMapView.setCenter(37.7793, -122.4192);
		mOsmMapView.setZoom(12);
	}
	
	private void initOsmDatabase() {

		File destFile = new File(getFilesDir(), "osm_db.sqlite");

		OsmDatabaseHelper osmDbHelper = new OsmDatabaseHelper(this);

		osmDbHelper.setDatabaseFile(destFile);

		boolean success = osmDbHelper.openOrCreateDatabase(this, destFile);

		if (success) {
			OsmModel.mDbHelper = osmDbHelper;
		}
	}

	@Override
	public boolean onMapTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void onMapDraw(Canvas canvas) {

	}

	@Override
	public void onMapSingleTapConfirmed(MotionEvent event) {

	}

	@Override
	public void onMapStopPanning() {

	}

	@Override
	public void onMapZoomChanged(int zoomLevel) {

	}

	@Override
	public void onMapLongClick(MotionEvent event) {
		
	}

	@Override
	public void onMapMarkerTap(MapMarker overlayItem) {
		
	}

	@Override
	public void onMapCalloutTap(MotionEvent event) {
		
	}
	
	@Override
	public void onNewLocation(Location location) {
		if (mOsmLocationOverlay != null && location != null) {
			mOsmLocationOverlay.setLocation(location);
		}
	}

}
