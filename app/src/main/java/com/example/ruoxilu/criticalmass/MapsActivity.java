package com.example.ruoxilu.criticalmass;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    public static MapsHandler mMapsHandler;
    // Made static so that other activity can access location.
    public static Location mCurrentLocation = Settings.getDefaultLocation();
    public static Location mLastLocation = Settings.getDefaultLocation();

    // Fields for helping process the map and location changes
    protected static Map<String, Marker> mMapMarkers = new HashMap<>(); // find marker based on Event ID
    protected static Map<Marker, String> mMarkerIDs = new HashMap<>(); // find Event ID associated with marker
    protected static Map<Marker, String> mMarkerNames = new HashMap<>();
    protected MassUser mMassUser = Application.mMassUser;  // Each user (i.e. application) only has one MassUser object.

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    // private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    // private String selectedPostObjectId;
    private int mMostRecentMapUpdate;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerButtons;

    /*
     * Remove markers that are not in the Hashmap markersToKeep
     */
    public static void cleanUpMarkers(HashSet<String> markersToKeep) {
        for (String objId : new HashSet<>(mMapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mMapMarkers.get(objId);
                mMarkerIDs.remove(marker);
                marker.remove();
                mMapMarkers.get(objId).remove();
                mMapMarkers.remove(objId);
                mMarkerNames.remove(objId);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Settings.APPTAG, "onCreate");
        super.onCreate(savedInstanceState);
        mMapsHandler = new MapsHandler(this);

        MapsHandler.initLocationRequest(); // Helper function to initiate location request
        initGoogleApiClient(); // Helper function to initiate Google Api Client to "listen to" location change

        setContentView(R.layout.activity_maps);

        mDrawerButtons = getResources().getStringArray(R.array.drawer_buttons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mDrawerButtons));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        setUpMapIfNeeded();

        checkLoginStatus();


    }

    private void selectItem(int position) {
        // update the main content by replacing fragments

        if (position == 1) {
            confirmLogOut();

        } else {

            Intent i = new Intent(MapsActivity.this, ListActivity.class);
            startActivityForResult(i, 0);


//            android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.list_fragment);
//            Bundle args = new Bundle();
//            args.putInt(EventListFragment.ARG_MENU_OPTION, position);
//            fragment.setArguments(args);
//
//            FragmentManager fragmentManager = getFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.map, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    // TODO repeat the functionality of the dispatchActivity
    protected void checkLoginStatus() {

        //(Xin)
        // determine whether the current user is an anonymous user and
        // if the user has previously signed up and logged into the application
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // If user is anonymous, send the user to LoginSignupActivity.class
            Intent intent = new Intent(MapsActivity.this,
                    LoginSignupActivity.class);
            startActivity(intent);
            finish();
        } else {
            // If current user is NOT anonymous user
            // Get current user data from Parse.com
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                Intent intent = new Intent(MapsActivity.this,
                        LoginSignupActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    /*
     * Helper function for onCreate
     * Initialize the Goolge Api Client for maps activity
     * TODO refactor to MapHandler
     */
    protected void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
//        Log.i(Settings.APPTAG, "GOOGLE API CLIENT CREATED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (mCurrentLocation != null) {

            // Create a LatLng object for the current location
            double latitude = mCurrentLocation.getLatitude();

            // Get longitude of the current location
            double longitude = mCurrentLocation.getLongitude();
            Log.i(Settings.APPTAG, "my LatLng is " + latitude + ", " + longitude);
            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Move the camera to the place in interest
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(Settings.ZOOM_LEVEL));
        }

    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            stopPeriodicLocationUpdates();
        }
        mGoogleApiClient.disconnect();

        super.onStop();
        setUpMapIfNeeded();

    }

    /**
     * Sets up the map if it is possible to do so
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        // Get LocationManager object from System Service LOCATION_SERVICE
        mCurrentLocation = mMapsHandler.initialMapLocation();
        updateZoom(mCurrentLocation);

        // Get longitude of the current location
        double longitude = mCurrentLocation.getLongitude();
        double latitude = mCurrentLocation.getLatitude();
        Log.i(Settings.APPTAG, "my LatLng is " + latitude + ", " + longitude);
        // Create a LatLng object for the current location

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                doMapQuery();
            }
        });
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {

        mGoogleApiClient.connect();

        starterPeriodicLocationUpdates();// connect googleFused api services
        setUpMapIfNeeded();

        mCurrentLocation = getLocation();

        Log.d(Settings.APPTAG, "onConnected " + "lat: " + mCurrentLocation.getLatitude() + "lng: " + mCurrentLocation.getLongitude());

        if (mCurrentLocation == null) {
            mCurrentLocation = Settings.getDefaultLocation();
        }
//
//        if (mMassUser == null) {
//            Log.d(Settings.APPTAG,"in Maps Activity, mMassUser is null");
////            mMassUser = ParseHandler.getDefaultMassUser();
//        }

        // 20150425
        mMassUser.setLocation(geoPointFromLocation(mCurrentLocation));

        //starterPeriodicLocationUpdates();// connect googleFused api services
        ParseHandler.updateUserLocation(mMassUser.getLocation(), mMassUser);


        // update MassEvent
        ParseHandler.updateUserEvent(geoPointFromLocation(mCurrentLocation), mMassUser);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    // passes in the user current location as input
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mLastLocation != null
                && ParseHandler.geoPointFromLocation(location)
                .distanceInKilometersTo(ParseHandler.geoPointFromLocation(mLastLocation)) < Settings.UPDATE_PIVOT) {
            return;
        }
        mLastLocation = location;

        mMassUser.setLocation(geoPointFromLocation(location));

        ParseHandler.updateUserLocation(ParseHandler.geoPointFromLocation(location), mMassUser);
        updateZoom(location);
        doMapQuery();
        ParseHandler.updateUserEvent(ParseHandler.geoPointFromLocation(mCurrentLocation), mMassUser);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) try {
            connectionResult.startResolutionForResult(this, Settings.CONNECTION_FAILURE_RESOLUTION_REQUEST);
        } catch (IntentSender.SendIntentException ignored) {
        }
        else {
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    private ParseGeoPoint geoPointFromLocation(Location location) {
        return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }
    /*
     * private helper functions
     */

    private void starterPeriodicLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, MapsHandler.mLocationRequest, this);
    }

    /*
     * API calls to start/stop periodic location update, and get the current location.
     */
    private void stopPeriodicLocationUpdates() {
        LocationServices.FusedLocationApi
                .removeLocationUpdates(mGoogleApiClient, this);
    }

    private Location getLocation() {
        if (servicesConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            return null;
        }
    }

    /*
     * Zooms the map to show the area of interest based on the search radius
     */
    private void updateZoom(Location location) {
        LatLng myLatLng = (location == null) ? new LatLng(0, 0) : new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, Settings.ZOOM_LEVEL));
    }

    /*
     *  Query MassEvent and add map markers
     */
    private void doMapQuery() {
        final int myUpdateNumber = ++mMostRecentMapUpdate;


        // 1
        Location myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }
        final ParseGeoPoint myPoint = ParseHandler.geoPointFromLocation(myLoc);
        ParseQuery<MassEvent> mapQuery = MassEvent.getQuery();
        mapQuery.whereWithinKilometers("location", myPoint, Settings.SEARCH_DISTANCE);
        mapQuery.orderByDescending("createdAt");
        mapQuery.findInBackground(new FindCallback<MassEvent>() {
            @Override
            public void done(List<MassEvent> objects, ParseException e) {
                if (e != null) {
                    Log.d(Settings.APPTAG, "An error occurred while querying for map posts.", e);
                    return;
                }
                if (myUpdateNumber != mMostRecentMapUpdate) {
                    return;
                }
                // Handle the results
                HashSet<String> toKeep = new HashSet<>();
                for (final MassEvent mEvent : objects) {
                    // check if the event size exceeds the threshold, tentatively set to 0
                    if (mEvent.getEventSize() > 10) {
                        toKeep.add(mEvent.getObjectId());
                        Marker oldMarker = mMapMarkers.get(mEvent.getObjectId());
                        MarkerOptions markerOpts = MapsHandler.createMarkerOpt(mEvent);

                        if (mEvent.getLocation().distanceInKilometersTo(myPoint) > Settings.RADIUS * Settings.METERS_PER_FEET
                                / Settings.METERS_PER_KILOMETER) {
                            // Set up an out-of-range marker
                            // Check for an existing out of range marker
                            if (oldMarker != null) {
                                if (oldMarker.getSnippet() == null) {
                                    // Out of range marker already exists, skip adding it
                                    continue;
                                } else {
                                    // Marker now out of range, needs to be refreshed
                                    oldMarker.remove();
                                }
                            }

                        } else {
                            // Set up an in-range marker
                            // Check for an existing in range marker
                            if (oldMarker != null) {
                                if (oldMarker.getSnippet() != null) {
                                    // In range marker already exists, skip adding it
                                    continue;
                                } else {
                                    // Marker now in range, needs to be refreshed
                                    oldMarker.remove();
                                }
                            }
                        }
                        Marker marker = mMap.addMarker(markerOpts);

                        mMarkerIDs.put(marker, mEvent.getObjectId());
                        mMapMarkers.put(mEvent.getObjectId(), marker);
                        mMarkerNames.put(marker, mEvent.getLocationName());
                    }
                }
                cleanUpMarkers(toKeep);
                Log.d(Settings.APPTAG, "After clean up markers");
            }
        });
    }

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog
                = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                Settings.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        if (errorDialog != null) {

            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText(errorDialog.toString())
                    .show();
        }

    }

    // SIGN_BACKGROUND_SERVICE
    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText(dialog.toString())
                        .show();
            }
            return false;
        }
    }


    /*
     * Show a dialog returned by Google Play services for the connection error code
     */

    public void confirmLogOut() {

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Logged out users will no longer be shown on the map.")
                .setCancelText("No")
                .setConfirmText("Yes")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        ParseHandler.deleteMassUser(mMassUser);
                        ParseUser.logOut();
                        sDialog.dismiss();
                        mGoogleApiClient.disconnect();
                        Intent intent = new Intent(MapsActivity.this, LoginSignupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (mMarkerIDs.containsKey(marker)) {
            Intent eventDetailIntent = new Intent();
            eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
            String locationName = mMarkerNames.get(marker);
            String eventId = mMarkerIDs.get(marker);
            eventDetailIntent.putExtra("objectId", eventId);
            eventDetailIntent.putExtra("location", locationName);
            startActivity(eventDetailIntent);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;

    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        Log.d(Settings.APPTAG, "called finish");
        super.finish();
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

}
