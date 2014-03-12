package com.test.testprojectopenstreetmap;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationListenerHelper {

	protected Context				mContext;
    
	private LocationManager			mLocationManager;
	private IMyLocationListener		mMyLocationListener;
	private boolean					mGotGpsLocation;
	private	Integer					mHeading;
	private	double					mHeadingMagneticVariation;
	private Location				mLastLocation;
    
    public LocationListenerHelper(Context context) {
    	mContext = context;
    	mLastLocation = null;
    	mMyLocationListener = null;
    }
    
    public void startListeningLocation() {
    	startListeningLocation(null);
    }
    
    public void startListeningLocation(IMyLocationListener myLocationListener) {
    	startListeningLocation(myLocationListener, 4000, 0);
    }

    public void startListeningLocation(IMyLocationListener myLocationListener, int minTime, int minDistance) {
    	mMyLocationListener = myLocationListener;
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = mLocationManager.getProviders(true);
		
		for (String provider : providers) {
			try {
				if (LocationManager.GPS_PROVIDER.equals(provider))
					mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListenerGps);
				else if (LocationManager.NETWORK_PROVIDER.equals(provider))
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListenerNetwork);
				else if (LocationManager.PASSIVE_PROVIDER.equals(provider))
					mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, locationListenerPassive);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
		mGotGpsLocation = false;
    }
    
    public void stopListeningLocation() {
    	mLocationManager.removeUpdates(locationListenerGps);
    	mLocationManager.removeUpdates(locationListenerNetwork);
    	mLocationManager.removeUpdates(locationListenerPassive);
    	mMyLocationListener = null;
    	mGotGpsLocation = false;
    }    
    
	public void onNewLocation(Location loc) {
   		mLastLocation = loc;
		setMagneticVariation(loc);
		if (mMyLocationListener != null)
			mMyLocationListener.onNewLocation(loc);
	}
	
    public Location getLastKnownLocation() {
    	
    	if (mLastLocation != null)
    		return mLastLocation;
    	
    	try {
  
	    	if (mLocationManager == null)
	    		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	   
			List<String> providers = mLocationManager.getProviders(true);
			TreeMap<Long, Location> locations = new TreeMap<Long, Location>(Collections.reverseOrder());
			for (String provider : providers) {
				Location l = mLocationManager.getLastKnownLocation(provider);
			
				if (l != null)
					locations.put(l.getTime(), l);
				
			}
			
			for (Map.Entry<Long, Location> map : locations.entrySet()) {
				Location location = map.getValue();
				return location;
			}
		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
			
		return null;
    }

    LocationListener locationListenerGps = new LocationListener() {
        @Override
		public void onLocationChanged(Location location) {
    		if (mLocationManager != null && mGotGpsLocation == false) {
    			mGotGpsLocation = true;
    			mLocationManager.removeUpdates(locationListenerNetwork);
    			mLocationManager.removeUpdates(locationListenerPassive);
    		}
    		onNewLocation(location);
        }
        @Override
		public void onProviderDisabled(String provider) {}
        @Override
		public void onProviderEnabled(String provider) {}
        @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
		public void onLocationChanged(Location location) {
            if (mLocationManager != null && mGotGpsLocation) {
            	mLocationManager.removeUpdates(this);
            	return;
            }
            onNewLocation(location);
        }
        @Override
		public void onProviderDisabled(String provider) {}
        @Override
		public void onProviderEnabled(String provider) {}
        @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerPassive = new LocationListener() {
        @Override
		public void onLocationChanged(Location location) {
    		if (mLocationManager != null && mGotGpsLocation) {
    			mLocationManager.removeUpdates(this);
    			return;
    		}
    		onNewLocation(location);
        }
        @Override
		public void onProviderDisabled(String provider) {}
        @Override
		public void onProviderEnabled(String provider) {}
        @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
	public boolean setHeading(float magneticHeading) {
		double heading = magneticHeading + mHeadingMagneticVariation;
		int headingRounded = (((int)(heading / 20)) * 20); // round heading 
		
		if (mHeading != null && headingRounded == mHeading.intValue())
			return false;

		mHeading = headingRounded;
		return true;
	}
	
	public Integer getHeading() {
		return mHeading;
	}
	
	private void setMagneticVariation(Location location) {
		long timestamp = location.getTime();
		if (timestamp == 0) {
			// Hack for Samsung phones which don't populate the time field
			timestamp = System.currentTimeMillis();
		}

		GeomagneticField field = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), timestamp);
		mHeadingMagneticVariation = field.getDeclination();
	}
    
	public interface IMyLocationListener {
		public void onNewLocation(android.location.Location location);
	}
	
	public static boolean isGPSProvidersAvailable(Context c){
		
		try{
			LocationManager locationManager= (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
	    	
			for (String gpsDevices : locationManager.getProviders(true)){
				if(gpsDevices.equalsIgnoreCase(LocationManager.GPS_PROVIDER) || gpsDevices.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)){
					return true;
				}
			}
	    	
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
}
