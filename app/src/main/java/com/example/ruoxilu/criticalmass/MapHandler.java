package com.example.ruoxilu.criticalmass;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RuoxiLu on 4/17/15.
 */
public class MapHandler {
    public static Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    public static Map<Marker, String> markerIDs = new HashMap<Marker, String>();
    public static GoogleMap mMap;

    public static LocationRequest mLocationRequest;

    private static Location mCurrentLocation = Settings.getDefaultLocation();
    private static Location mLastLocation = Settings.getDefaultLocation();


    public static void initLocationRequest() {
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        Log.i(Settings.APPTAG, "LOCATION REQUEST CREATED");
        // Set the update interval
        mLocationRequest.setInterval(Settings.UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(Settings.FAST_INTERVAL_CEILING_IN_MILLLISECONDS);

    }

}
