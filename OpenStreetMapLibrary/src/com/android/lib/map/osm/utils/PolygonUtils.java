package com.android.lib.map.osm.utils;

import java.util.List;

import com.android.lib.map.osm.GeoPoint;

public class PolygonUtils {

	public static boolean isPointInsidePolygon(List<GeoPoint> polygon, GeoPoint g) {
	
		if (polygon == null || polygon.size() == 0)
			return false;
		
		int[] x = new int[polygon.size()];
		int[] y = new int[polygon.size()];
		
		int i = 0;
		for (GeoPoint point : polygon) {
			x[i] = point.getLongitudeE6();
			y[i] = point.getLatitudeE6();	
			i++;
		}		
		return isPointInsidePolygon(x, y, g.getLongitudeE6(), g.getLatitudeE6());
	}

	public static boolean isPointInsidePolygon(int X[], int Y[], int x, int y) {
		
		int i, j;
		boolean c = false;
		for (i = 0, j = X.length-2; i < X.length-1; j = i++) {
			
			if (( ((Y[i]<=y) && (y<Y[j])) || ((Y[j]<=y) && (y<Y[i])) ) &&
			(x < (X[j] - X[i]) * (y - Y[i]) / (Y[j] - Y[i]) + X[i]))
			c = !c;
			
		}
		
		return c;
	}
	
}
