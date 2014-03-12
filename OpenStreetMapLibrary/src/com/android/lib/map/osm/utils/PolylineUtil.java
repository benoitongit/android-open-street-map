package com.android.lib.map.osm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.lib.map.osm.GeoPoint;

public class PolylineUtil {

    public static List<GeoPoint> decodePoly(String encoded) {

        List<GeoPoint> poly = new ArrayList<GeoPoint>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            GeoPoint p = new GeoPoint((int) ((lat / 1E5) * 1E6),
                 (int) ((lng / 1E5) * 1E6));
            poly.add(p);
        }

        return poly;
    }

	public static HashMap<String, String> encodePoly(List<GeoPoint> geopoints, int level, int step) {

		HashMap<String, String> resultMap = new HashMap<String, String>();
		StringBuffer encodedPoints = new StringBuffer();
		StringBuffer encodedLevels = new StringBuffer();

		int plat = 0;
		int plng = 0;

		for (GeoPoint g : geopoints) {
			int late5 = floor1e5(g.getLatitudeE6() / 1E6);
			int lnge5 = floor1e5(g.getLongitudeE6() / 1E6);
			
			int dlat = late5 - plat;
			int dlng = lnge5 - plng;

			plat = late5;
			plng = lnge5;

			encodedPoints.append(encodeSignedNumber(dlat)).append(encodeSignedNumber(dlng));
			encodedLevels.append(encodeNumber(level));

		}
		resultMap.put("encodedPoints", encodedPoints.toString());
		resultMap.put("encodedLevels", encodedLevels.toString());

		return resultMap;
	}
	
	private static String encodeSignedNumber(int num) {
		int sgn_num = num << 1;
		if (num < 0) {
			sgn_num = ~(sgn_num);
		}
		return (encodeNumber(sgn_num));
	}
	
	private static String encodeNumber(int num) {

		StringBuffer encodeString = new StringBuffer();

		while (num >= 0x20) {
			int nextValue = (0x20 | (num & 0x1f)) + 63;
			encodeString.append((char) (nextValue));
			num >>= 5;
		}

		num += 63;
		encodeString.append((char) (num));

		return encodeString.toString();
	}
	
	private static int floor1e5(double coordinate) {
		return (int) Math.floor(coordinate * 1e5);
	}
	
}
