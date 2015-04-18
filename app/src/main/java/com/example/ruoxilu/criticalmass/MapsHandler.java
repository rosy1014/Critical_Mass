package com.example.ruoxilu.criticalmass;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by RuoxiLu on 4/17/15.
 */
public class MapsHandler {
    public static Context mContext;
    public static Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    public static Map<Marker, String> markerIDs = new HashMap<Marker, String>();
    public static GoogleMap mMap;

    public static LocationRequest mLocationRequest;
    public static GoogleApiClient mGoogleApiClient;

    private static Location mCurrentLocation = Settings.getDefaultLocation();
    private static Location mLastLocation = Settings.getDefaultLocation();
    private int mostRecentMapUpdate = 0;

    public MapsHandler(Context context){
        this.mContext = context;
        //this.mMap = map;
    }

    public static void initLocationRequest() {
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        Log.i(Settings.APPTAG, "LOCATION REQUEST CREATED");
        // Set the update interval
        mLocationRequest.setInterval(Settings.UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(Settings.FAST_INTERVAL_CEILING_IN_MILLLISECONDS);

    }

    public static Location initialMapLocation(){
        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = mLocationManager.getBestProvider(criteria, true);

        // Get Current Location
        if (mLocationManager.getLastKnownLocation(provider) == null) {
            return Settings.getDefaultLocation();

        } else {
            return mLocationManager.getLastKnownLocation(provider);
        }
    }

    /*
     * Create map markers based on location and size
     * Size Criterion:
     *      10-20:
     *      20-50:
     *      50-100:
     *      100-500:
     *      >500:
     */
    // SIGN_MARKER_OBJECT
    public static MarkerOptions createMarkerOpt(MassEvent mEvent) {

        int size = mEvent.getEventSize();
        if (size < Settings.POPSIZE2) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2))
                    .title("Location: " + mEvent.getLocationName()).snippet("Size: " + size);
            return markerOpt;
        } else if (size < Settings.POPSIZE3) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3))
                    .title("Location: " + mEvent.getLocationName()).snippet("Size: " + size);
            return markerOpt;
        } else if (size < Settings.POPSIZE4) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker4))
                    .title("Location: " + mEvent.getLocationName()).snippet("Size: " + size);
            return markerOpt;
        } else if (size < Settings.POPSIZE5) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker5))
                    .title("Location: " + mEvent.getLocationName()).snippet("Size: " + size);
            return markerOpt;
        } else {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker6))
                    .title("Location: " + mEvent.getLocationName()).snippet("Size: " + size);
            return markerOpt;
        }

    }

    public void updateMarkers(HashSet<MassEvent> eventList) {
        HashSet<String> eventIdsToKeep = new HashSet<String>();
        for (MassEvent event : eventList) {
            if (event.getEventSize() > 10) {
                eventIdsToKeep.add(event.getObjectId());
                Marker marker = mMap.addMarker(createMarkerOpt(event));
                mapMarkers.put(event.getObjectId(), marker);
                markerIDs.put(marker, event.getObjectId());
            }
        }
        for(String objId: new HashSet<>(mapMarkers.keySet())){
            if (!eventIdsToKeep.contains(objId)){
                Marker marker= mapMarkers.get(objId);
                markerIDs.remove(marker);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }


    public static void cleanUpMarkers(HashMap<String, MarkerOptions> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.containsKey(objId)) {
                Marker marker = mapMarkers.get(objId);
                markerIDs.remove(marker);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }
    // TODO
//    // SIGN_MARKER_OBJECT
//    protected int populationLevel(int size) {
//        if (size < Settings.POPSIZE1) {
//            return Settings.POPLEVEL1;
//        } else if (size < Settings.POPSIZE2) {
//            return Settings.POPLEVEL2;
//        } else if (size < Settings.POPSIZE3) {
//            return Settings.POPLEVEL3;
//        } else if (size < Settings.POPSIZE4) {
//            return Settings.POPLEVEL4;
//        } else if (size < Settings.POPSIZE5) {
//            return Settings.POPLEVEL5;
//        } else {
//            return Settings.POPLEVEL6;
//        }
//    }

    /*
     * Define map marker color based on location and size
     * Size Criterion:
     *      10-20: yellow
     *      20-50: orange
     *      50-100: rose
     *      100-500:violet
     *      >500: red
     */
    // SIGN_MARKER_OBJECT
//    protected float markerColor(int size) {
//        if (size < Settings.POPSIZE2 && size >= Settings.POPSIZE1) {
//            return BitmapDescriptorFactory.HUE_YELLOW;
//        } else if (size < Settings.POPSIZE3) {
//            return BitmapDescriptorFactory.HUE_ORANGE;
//        } else if (size < Settings.POPSIZE4) {
//            return BitmapDescriptorFactory.HUE_ROSE;
//        } else if (size < Settings.POPSIZE5) {
//            return BitmapDescriptorFactory.HUE_VIOLET;
//        } else
//            return BitmapDescriptorFactory.HUE_RED;
//    }

}
