package com.android.lib.map.osm;


public class Projection {

	// OrignShift  2 * math.pi * 6378137 / 2.0
	private final static double MAX_X = 20037508.342789244;
	private final static double MAX_Y = 20037508.342789244;
	
	public static double getMapSize(int zoomLevel) {
		double mapSize = (Math.pow(2, zoomLevel) * Tile.TILE_SIZE);
		return mapSize;
	}
	
	public static int getXPixelFromLongitude(double lon, int zoomLevel) {
		double mapWidth = getMapSize(zoomLevel);

		// Converts given lon in WGS84 Datum to XY in Spherical Mercator EPSG:900913
		double x = lon * MAX_X / 180.0;
		
		// Scale to map
		x = ((x + MAX_X) / (MAX_X * 2)) * mapWidth;
		
		return (int) x;
	}

	public static int getYPixelFromLatitude(double lat, int zoomLevel) {
		double mapWidth = getMapSize(zoomLevel);		
		
		//Converts given lat in WGS84 Datum to XY in Spherical Mercator EPSG:900913
	    double y= Math.log( Math.tan((90 + lat) * Math.PI / 360.0 )) / (Math.PI / 180.0);
	    y = y * MAX_Y / 180.0;
	    
	    // Scale to map
	    y = mapWidth - (((y + MAX_Y) / (MAX_Y * 2)) * mapWidth);
	    
	    return (int) y;
	}
	
	public static GeoPoint getProjectionFromPixels(int x, int y, int zoom) {
		//Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum
		
		double mapWidth = getMapSize(zoom);
		// Convert Map pixel to mercator coord
	    double mercatorX = MAX_X - ((MAX_X * 2) * (x / mapWidth));
	    double mercatorY = - (MAX_Y - ((MAX_Y * 2) * ((y + mapWidth) / mapWidth)));
		
		double lon = (mercatorX / MAX_X) * 180.0;
        double lat = (mercatorY / MAX_Y) * 180.0;

        lat = 180 / Math.PI * (2 * Math.atan( Math.exp( lat * Math.PI / 180.0)) - Math.PI / 2.0);
		
		return new GeoPoint((int) (lat * 1E6) , (int) (lon * 1E6));
	}

	public static int getZoomLevelFromBox(double maxLat, double maxLon, double minLat,
			double minLon, int viewWidth, int viewHeight) {
		
		return getZoomLevelFromBox(maxLat, maxLon, minLat, minLon, viewWidth, viewHeight, 0, 0);
	}
	
	public static int getZoomLevelFromBox(double maxLat, double maxLon, double minLat,
			double minLon, int viewWidth, int viewHeight, int viewPaddingWidth, int viewPaddingHeight) {

		// Reduce view by XX pixels to not have markers or tracks on edge of screen 
		viewWidth -= viewPaddingWidth;
		viewHeight -= viewPaddingHeight;
		//---
		
		int zoomLevel;
		int maxLatOffset;
		int minLonOffset;
		int maxLatE6 = (int) (maxLat * 1E6);
		int maxLonE6 = (int) (maxLon * 1E6);
		int minLatE6 = (int) (minLat * 1E6);
		int minLonE6 = (int) (minLon * 1E6);
		
	//	Log.i("getZoomLevelFromBox", "maxLatE6:" + maxLatE6 + "  maxLonE6:" + maxLonE6 + "  minLatE6:" + minLatE6 + "  minLonE6:" + minLonE6);
		
		for (zoomLevel = OsmMapViewBase.MIN_ZOOM_LEVEL_FOR_TILES; zoomLevel > 0; zoomLevel--) {
			
			maxLatOffset = (0 - Projection.getYPixelFromLatitude(maxLat, zoomLevel));
			minLonOffset = (0 - Projection.getXPixelFromLongitude(minLon, zoomLevel));
			
			GeoPoint upperLeftScreen = Projection.getProjectionFromPixels(minLonOffset, maxLatOffset, zoomLevel);
			GeoPoint lowerRightcreen = Projection.getProjectionFromPixels(
					minLonOffset - viewWidth, 
					maxLatOffset - viewHeight, 
					zoomLevel);
		
		//	Log.i("getZoomLevelFromBox", "upperLeftScreen.getLatitudeE6():" + upperLeftScreen.getLatitudeE6() + "   upperLeftScreen.getLongitudeE6():" + upperLeftScreen.getLongitudeE6() 
		//			+ "   lowerRightcreen.getLatitudeE6():" + lowerRightcreen.getLatitudeE6() + "   lowerRightcreen.getLongitudeE6():" + lowerRightcreen.getLongitudeE6());
			
			if (upperLeftScreen.getLatitudeE6() >= maxLatE6
					&& upperLeftScreen.getLongitudeE6() <= minLonE6
					&& lowerRightcreen.getLatitudeE6() <= minLatE6
					&& lowerRightcreen.getLongitudeE6() >= maxLonE6) {
				
				//fit in screen
				break;
			}
		}
		
		return zoomLevel;
	}
	
	public static Tile getMapTileFromCoordinates(final double lat, final double lon, final int zoom) {
		final int y = (int) Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
		final int x = (int) Math.floor((lon + 180) / 360 * (1 << zoom));

		Tile tile = new Tile(x, y, zoom);
		return tile;
	}
}
