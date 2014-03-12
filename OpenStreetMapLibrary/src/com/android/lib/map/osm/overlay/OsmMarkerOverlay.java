package com.android.lib.map.osm.overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.android.lib.map.osm.GeoPoint;
import com.android.lib.map.osm.OsmMapView;
import com.android.lib.map.osm.controller.IMapInteractionListener;
import com.android.lib.map.osm.utils.CountDownTimer;


public class OsmMarkerOverlay extends OsmOverlay {
		
	private final List<MapMarker> mMarkers = new ArrayList<MapMarker>();
	private final Paint mPaint = new Paint();
	private Drawable mDrawable;
	private OsmMapView mOsmMapView;
	
	
	public OsmMarkerOverlay(OsmMapView mapView, Drawable defaultMarker) {
		mDrawable = defaultMarker;
		mOsmMapView = mapView;
	}

	
	public void addMarkers(List<? extends MapMarker> markers) {
		mMarkers.addAll(markers);
		sortItems();				
	}
		
	public void addMarker(MapMarker mapMarker) {
		mMarkers.add(mapMarker);
		sortItems();
	}
		
	public void addMarkersFadeIn(final List<? extends MapMarker> markers) {
		
		for (MapMarker marker : markers)
			marker.setAlpha(0);
		
		addMarkers(markers);
		
		new CountDownTimer(300, 30) {
			@Override
			public void onTick() {
				int alpha;
				for (MapMarker marker : markers) {
					alpha = marker.getAlpha() + 25;
					if (alpha > 255)
						alpha = 255;
					marker.setAlpha(alpha);
				}
				if (mOsmMapView != null)
					mOsmMapView.invalidate();
			}
			
			@Override
			public void onFinish() {
				for (MapMarker marker : markers)
					marker.setAlpha(255);
				if (mOsmMapView != null)
					mOsmMapView.invalidate();
				
			}
		}.start();
	}
	
	public void removeMarkers() {
		mMarkers.clear();
	}

	public void removeMarker(MapMarker markerToRemove) {
		Iterator<MapMarker> it = mMarkers.iterator();
		while (it.hasNext()) {
			MapMarker marker = it.next();
			if (markerToRemove == marker) {
				it.remove();
			}
		}
	}
	
	public void removeMarkersFadeOut(final List<? extends MapMarker> markers) {
		
		for (MapMarker marker : markers)
			marker.setAlpha(255);
				
		new CountDownTimer(500, 50) {
			@Override
			public void onTick() {
				int alpha;
				for (MapMarker marker : markers) {
					alpha = marker.getAlpha() - 25;
					if (alpha < 0)
						alpha = 0;
					marker.setAlpha(alpha);
				}
				if (mOsmMapView != null)
					mOsmMapView.invalidate();
			}
			
			@Override
			public void onFinish() {
				removeMarkers(markers);
				if (mOsmMapView != null)
					mOsmMapView.invalidate();
				
			}
		}.start();
	}
	
	public void removeMarkers(List<? extends MapMarker> markersToRemove) {
	//	mMarkers.removeAll(markersToRemove);
		Iterator<MapMarker> it = mMarkers.iterator();
		while (it.hasNext()) {
			MapMarker marker = it.next();
			if (markersToRemove.contains(marker)) {
				it.remove();
			}
		}
	}

	public List<MapMarker> getMarkers() {
		return mMarkers;
	}
	
	public void onTap(MapMarker markerTapped) {
		
		try {
	
			IMapInteractionListener mapListener = mOsmMapView.getMapIntereractionListener();
			
			Iterator<MapMarker> it = mMarkers.iterator();
			
			while(it.hasNext()){
				MapMarker mapMarker = it.next();
				if (mapMarker == markerTapped && mapMarker.isClickable()) {
					mapListener.onMapMarkerTap(mapMarker);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void draw(android.graphics.Canvas canvas, View view) {
		super.draw(canvas, view);
				
		Point p;
		Bitmap bitmap;
		GeoPoint g;
		MapMarker selectedMarker = null;
		for (MapMarker marker : mMarkers) {
			
			g = marker.getCoordinate();
			p = mOsmMapView.geopointToPixelProjection(g);
			
			if (marker.isFocused()) {
				selectedMarker = marker;
				continue;
			}
			
			if (marker.getDrawable() != null)
				bitmap = ((BitmapDrawable) marker.getDrawable()).getBitmap();
			else if (mDrawable != null)
				bitmap = ((BitmapDrawable) mDrawable).getBitmap();
			else
				continue;
				
			setBoundForBitmap(marker.getDrawableBound(), bitmap, p, marker);
			
			mPaint.setAlpha(marker.getAlpha());
			canvas.drawBitmap(bitmap, p.x, p.y, mPaint);
		}
		
		if (selectedMarker != null)
			drawMarker(view.getContext(), canvas, selectedMarker);
	}
	
	private void drawMarker(Context context, android.graphics.Canvas canvas, MapMarker selectedMarker) {
		Bitmap bitmap=null;
		GeoPoint g = selectedMarker.getCoordinate();
		Point p = mOsmMapView.geopointToPixelProjection(g);
	
		bitmap =  ((BitmapDrawable) selectedMarker.getDrawableFocused()).getBitmap();
		
		setBoundForBitmap(selectedMarker.getDrawableFocusedBound(), bitmap, p, selectedMarker);
		
		mPaint.setAlpha(selectedMarker.getAlpha());
		canvas.drawBitmap(bitmap, p.x, p.y, mPaint);	
	}
	
	private void setBoundForBitmap(short bound, Bitmap bitmap, Point p, MapMarker marker) {
		
		if (bound == MapMarker.BOUND_CENTER) {
			p.x -= bitmap.getWidth() / 2;
			p.y -= bitmap.getHeight() / 2;				
		} else if (bound == MapMarker.BOUND_CENTER_BOTTOM) {
			p.x -= bitmap.getWidth() / 2;
			p.y -= bitmap.getHeight();
		} else if (bound == MapMarker.BOUND_CUSTOM) {
			Point customBound = (marker.isFocused()) ? marker.getDrawableFocusedCustomBound() : marker.getDrawableCustomBound();
			p.x -= bitmap.getWidth() - customBound.x;
			p.y -= bitmap.getHeight() - customBound.y;
		}
	}
	
	@Override
	public boolean onSingleTap(final MotionEvent event, View view) {
		super.onSingleTap(event, view);
		
		Map<MapMarker, Integer> tappedItems = new HashMap<MapMarker, Integer>();
		
		for (MapMarker marker : mMarkers) {
			
			GeoPoint itemCoord = marker.getCoordinate();
			Point itemPoint = mOsmMapView.geopointToPixelProjection(itemCoord);
			
			int itemWidth = 0;
			int itemHeight = 0;
			
			
			if (marker.getDrawable() != null) {
				itemWidth = marker.getDrawable().getIntrinsicWidth();
				itemHeight = marker.getDrawable().getIntrinsicHeight();
			} else if (mDrawable != null) {
				itemWidth = mDrawable.getIntrinsicWidth();
				itemHeight = mDrawable.getIntrinsicHeight();
			}
			
			//Log.i("", "itemWidth = " + itemWidth+ " itemHeight=" + itemHeight);
			
			if (itemWidth == 0 || itemHeight == 0)
				continue;

			int XboundaryMax = itemPoint.x + (itemWidth / 2);
			int XboundaryMin = itemPoint.x - (itemWidth / 2);
			int YboundaryMax = 0;
			int YboundaryMin = 0;
			
			if (marker.getDrawableBound() == MapMarker.BOUND_CENTER) {
				
				YboundaryMax = itemPoint.y + (itemHeight / 2);
				YboundaryMin = itemPoint.y - (itemHeight / 2);				
								
			} else {
				
				YboundaryMax = itemPoint.y;
				YboundaryMin = itemPoint.y - itemHeight;				
			
			}
			
			//Log.i("", "getX = " + event.getX() + " XboundaryMax=" + XboundaryMax);
			//Log.i("", "event.getY()" + event.getY() + " YboundaryMax=" + YboundaryMax);
			
			if (event.getX() <= XboundaryMax && event.getX() >= XboundaryMin
					&& event.getY() <= YboundaryMax && event.getY() >= YboundaryMin) {
				
				int score = 0;
				float x1 = event.getX() - XboundaryMin;
				float x2 = XboundaryMax - event.getX();
				float y1 = event.getY() - YboundaryMin;
				float y2 = YboundaryMax - event.getY();
				
				if (x1 < x2 && y1 < y2)
					score = (int)(x1 * y1);
				else if (x2 < x1 && y2 < y1)
					score = (int)(x2 * y2);
				else if (x2 < x1 && y1 < y2)
					score = (int)(x2 * y1);
				else if (x1 < x2 && y2 < y1)
					score = (int)(x1 * y2);
				
				tappedItems.put(marker, score);
			}
			
		}
		
		if (tappedItems.size() == 0){
			return false;
		}
		
		if (tappedItems.size() == 1) {
			for (MapMarker marker : tappedItems.keySet()) {
				onTap(marker);
				return true;
			}
		}
		
		Map<MapMarker, Integer> tappedItemSorted = sortByValue(tappedItems, false);

		//Log.i("", "Items tapped size = " + tappedItems.size());
	
		for (MapMarker marker : tappedItemSorted.keySet()) {
			onTap(marker);
			return true;
		}
				
		return false;
	}
	
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, final boolean sortASC) {
		
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				if (sortASC)
					return (o1.getValue()).compareTo(o2.getValue());
				else
					return ((o1.getValue()).compareTo(o2.getValue()) * -1);
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	protected void sortItems() {
		
		List<MapMarker> markerOverlays = new ArrayList<MapMarker>();
		
		for (int i = 0; i < mMarkers.size(); i++) {
			MapMarker item = mMarkers.get(i);
			if (item == null)
				continue;
			GeoPoint itemCoord = item.getCoordinate();
			if (itemCoord != null) {
				markerOverlays.add(item);
			}
		}

    	Collections.sort(markerOverlays, new Comparator<MapMarker>() {
            @Override
			public int compare(MapMarker o1, MapMarker o2) {
            	int o1Lat = o1.getCoordinate().getLatitudeE6();
                int o2Lat = o2.getCoordinate().getLatitudeE6();

                if (o1.getZIndex() > o2.getZIndex())
                	return 1;
                else if (o2.getZIndex() > o1.getZIndex())
                	return -1;

                if (o1Lat > o2Lat)
                	return -1;
                else if (o1Lat == o2Lat)
                	return 0;
                else
                	return 1;            
            }
        });
			
    	mMarkers.clear();
    	mMarkers.addAll(markerOverlays);
	}
	
}
