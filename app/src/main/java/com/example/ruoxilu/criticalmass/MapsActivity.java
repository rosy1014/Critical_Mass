package com.example.ruoxilu.criticalmass;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
import com.parse.ParseAnonymousUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
    public static MapsHandler mapsHandler;
    // Made static so that other activity can access location.
    public static Location mCurrentLocation = Settings.getDefaultLocation();
    public static Location mLastLocation = Settings.getDefaultLocation();
    // Fields for helping process the map and location changes
    private static Map<String, Marker> mapMarkers = new HashMap<String, Marker>(); // find marker based on Event ID
    private static Map<Marker, String> markerIDs = new HashMap<Marker, String>(); // find Event ID associated with marker
    private static ViewGroup mViewGroup;
    private static LinearLayout mMainScreen;
    protected MassUser mMassUser;  // Each user (i.e. application) only has one MassUser object.

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
   // private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private String mEventID;
    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;
    //  private String selectedPostObjectId;
    private int mostRecentMapUpdate;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Settings.APPTAG, "onCreate");
        super.onCreate(savedInstanceState);
        mapsHandler = new MapsHandler(this);

        MapsHandler.initLocationRequest(); // Helper function to initiate location request
        initGoogleApiClient(); // Helper function to initiate Google Api Client to "listen to" location change

        setContentView(R.layout.activity_maps);
        mDrawerButtons = getResources().getStringArray(R.array.drawer_buttons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerButtons));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        setUpMapIfNeeded();

//        Intent intent = new Intent(MapsActivity.this, DispatchActivity.class);
//        startActivity(intent);
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
            mapsHandler.mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mapsHandler.mMap.animateCamera(CameraUpdateFactory.zoomTo(Settings.ZOOM_LEVEL));
            //Log.d(Settings.APPTAG, "update camera on resume");
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
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapsHandler.mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapsHandler.mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mapsHandler.mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mapsHandler.mMap.setMyLocationEnabled(true);
        // Get LocationManager object from System Service LOCATION_SERVICE
        mCurrentLocation = mapsHandler.initialMapLocation();
        updateZoom(mCurrentLocation);

        // Get longitude of the current location
        double longitude = mCurrentLocation.getLongitude();
        double latitude = mCurrentLocation.getLatitude();
        Log.i(Settings.APPTAG, "my LatLng is " + latitude + ", " + longitude);
        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        mapsHandler.mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mapsHandler.mMap.animateCamera(CameraUpdateFactory.zoomTo(Settings.ZOOM_LEVEL));
        mapsHandler.mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                doMapQuery();
            }
        });
        mapsHandler.mMap.setOnInfoWindowClickListener(this);
        mapsHandler.mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClient.connect();
        mCurrentLocation = getLocation();
        if (mCurrentLocation==null){
            mCurrentLocation = Settings.getDefaultLocation();
        }
        if(mMassUser == null){
            mMassUser= ParseHandler.getDefaultMassUser();
        }

        starterPeriodicLocationUpdates();// connect googleFused api services
        ParseHandler.updateUserLocation(mMassUser.getLocation(), mMassUser);
        // update MassEvent
        ParseHandler.updateUserEvent(geoPointFromLocation(mCurrentLocation),mMassUser);
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
        ParseHandler.updateUserLocation(ParseHandler.geoPointFromLocation(location),mMassUser);
        updateZoom(location);
        doMapQuery();
        ParseHandler.updateUserEvent(ParseHandler.geoPointFromLocation(mCurrentLocation), mMassUser);//helper function to update event as location changes
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, Settings.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    private ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        // Log.i(Settings.APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
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
        // Move the camera to the location in interest and zoom to appropriate level
        mapsHandler.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, Settings.ZOOM_LEVEL));
    }

    // display events by markers on the map
    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;


        // 1
        Location myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
//        HashSet<MassEvent> nearbyEvents = ParseHandler.queryNearbyEvent(myLoc);
        ParseHandler.queryNearbyEvent(myLoc);
    }

    /*
     * Remove markers that are not in the Hashmap markersToKeep
     */
    // SIGN_MARKER_OBJECT
//    private void cleanUpMarkers(Set<String> markersToKeep) {
//        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
//            if (!markersToKeep.contains(objId)) {
//                Marker marker = mapMarkers.get(objId);
//                markerIDs.remove(marker);
//                marker.remove();
//                mapMarkers.get(objId).remove();
//                mapMarkers.remove(objId);
//            }
//        }
//    }


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
                        Intent intent = new Intent(MapsActivity.this, DispatchActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .show();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (mapsHandler.markerIDs.containsKey(marker)) {
            Intent eventDetailIntent = new Intent();
            eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
            String locationName = mapsHandler.markerNames.get(marker);
            String eventId = mapsHandler.markerIDs.get(marker);
            eventDetailIntent.putExtra("objectId", eventId);
            eventDetailIntent.putExtra("location", locationName);
//            Log.d(Settings.APPTAG, "On Marker Click, event object id is " + eventId);
            startActivity(eventDetailIntent);
            //return true;

        } else {
//            Log.d(Settings.APPTAG, "On Marker Click, unable to start eventActivity");
            // return false;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;

    }

    /*
    * Click on marker redirects user to eventActivity
    */

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class EventListFragment extends Fragment {
        public static final String ARG_MENU_OPTION = "menu_option";

        public EventListFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.activity_list, container, false);

            return rootView;
        }
    }
/*
 * Reference for customized info window
 * http://stackoverflow.com/questions/14123243/google-maps-android-api-v2-interactive-infowindow-like-in-original-android-go/15040761#15040761
 */
    /*
    * Click on marker redirects user to eventActivity
    */

    public static class ErrorDialogFragment extends DialogFragment {
        /*
         * Show user a message if Google Play services are not enabled on the
         * device.
         */
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

}