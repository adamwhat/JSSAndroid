package com.jinshisong;

import android.content.Context;  
import android.location.Location;  
import android.location.LocationListener;  
import android.location.LocationManager;  
import android.os.Bundle;  
  
public class GPS {  
  
    private Context con;  
    private double mLatitude = 0;  
    private double mLongitude = 0;  
    private LocationManager mLocationManager;  
  
    public GPS(Context con) {  
        this.con = con;  
    }  
  
    public static GPS mGps;  
  
    public static GPS getGpsInstance(Context mCon) {  
        if (mGps == null) {  
            mGps = new GPS(mCon);  
        }  
        return mGps;  
    }  
  
    /** Get the locationManager object and set GPS update listener. */  
    public void openGps() {  
        mLocationManager = (LocationManager) con  
                .getSystemService(Context.LOCATION_SERVICE);  
        /** 
         * Register the listener with the Location Manager to receive 
         * location updates 
         */  
        mLocationManager.requestLocationUpdates(  
                LocationManager.NETWORK_PROVIDER, 6000, 0, locationListener);  
    }  
  
    /** Close the gps service. */  
    public void closeGps() {  
        if (mLocationManager != null) {  
            mLocationManager.removeUpdates(locationListener);  
        }  
    }  
  
    /** Get the latitude location. */  
    public double getLatitude() {  
        return mLatitude;  
    }  
  
    /** Get the longitude location. */  
    public double getLongitude() {  
        return mLongitude;  
    }  
  
    private void updateWithNewLocation(Location location) {  
        if (location != null) {  
            mLatitude = location.getLatitude();  
            mLongitude = location.getLongitude();  
        }  
    }  
  
    private final LocationListener locationListener = new LocationListener() {  
        public void onLocationChanged(Location location) {  
            updateWithNewLocation(location);  
        }  
  
        public void onProviderDisabled(String provider) {  
        }  
  
        public void onProviderEnabled(String provider) {  
        }  
  
        public void onStatusChanged(String provider, int status, Bundle extras) {  
        }  
    };  
  
}  
