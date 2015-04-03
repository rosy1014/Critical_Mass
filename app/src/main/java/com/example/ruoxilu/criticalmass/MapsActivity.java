package com.example.ruoxilu.criticalmass;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    /*
     * Constants for location update parameters
     */
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // the update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // the fast interval ceiling
    private static final int FAST_INTERVAL_CEILING_IN_SECONDS = 1;
    private static final int FAST_INTERVAL_CEILING_IN_MILLLISECONDS =
            FAST_INTERVAL_CEILING_IN_SECONDS * MILLISECONDS_PER_SECOND;
    private static final double UPDATE_PIVOT = 0.005; //update if move more than 5 meters
    private static final int SEARCH_DISTANCE = 5;
    private static final int ZOOM_LEVEL = 17; //city level
    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;
    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;
    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
    private static final String APPTAG = "CriticalMass";
    Button mMiddleBar;  // Directs to list activity
    Button mLeftBar;    // Placeholder for login
    Button mRightBar;   // Placeholder
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    // Made static so that other activity can access location.
    public static Location mCurrentLocation;
    public static Location mLastLocation;
    private MassUser mMassUser; // Each user (i.e. application) only has one MassUser object.

    private MassEvent mMassEvent; //
    private String mEventID;

    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;

    // Fields for helping process the map and location changes
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private String selectedPostObjectId;
    private int mostRecentMapUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(APPTAG,"onCreate");
        super.onCreate(savedInstanceState);

        initLocationRequest(); // Helper function to initiate location request
        initGoogleApiClient(); // Helper function to initiate Google Api Client to "listen to" location change

        mMassUser = new MassUser(); // Initialize mMassUser data object
        //Log.d(APPTAG, "mMassUser " + mMassUser);

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mMiddleBar = (Button)findViewById(R.id.map_middle_bar);
        mMiddleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {

                // Change color if pressed and reset after release
                if (ev.getAction() == MotionEvent.ACTION_DOWN ) {
                    mMiddleBar.setBackgroundColor(0xffffffff);
                    Intent i = new Intent(MapsActivity.this, ListActivity.class);
                    startActivityForResult(i, 0);

                } else {
                    mMiddleBar.setBackgroundColor(0xff9dadd6);
                }

                return true;
            }
        });
        mMiddleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start ListActivity
                Intent i = new Intent(MapsActivity.this, ListActivity.class);
                startActivityForResult(i, 0);

            }
        });

        //TODO Logout and delete mass user
//        deleteMassUser();
//        ParseUser.logOut();

        mLeftBar = (Button)findViewById(R.id.map_left_bar);
        mLeftBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {

                // Change color if pressed and reset after release
                if (ev.getAction() == MotionEvent.ACTION_DOWN ) {
                    deleteMassUser();
                    ParseUser.logOut();
                    mLeftBar.setBackgroundColor(0xff2a4a90);
                    Intent i = new Intent(MapsActivity.this, LoginSignupActivity.class);
                    startActivityForResult(i,0);
                } else {
                    mLeftBar.setBackgroundColor(0xff112645);
                }

                return true;
            }
        });

        mRightBar = (Button)findViewById(R.id.map_right_bar);
        mRightBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {

                // Change color if pressed and reset after release
                if (ev.getAction() == MotionEvent.ACTION_DOWN ) {
                    mRightBar.setBackgroundColor(0xff2a4a90);
                } else {
                    mRightBar.setBackgroundColor(0xff112645);
                }

                return true;
            }
        });



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

    protected void deleteMassUser(){
        ParseQuery<MassUser> query = MassUser.getQuery();
        final String user_id = mMassUser.getUser();
//        Log.d(APPTAG, obj_id);
        query.whereEqualTo("user", user_id);
        query.getFirstInBackground(new GetCallback<MassUser>(){
            @Override
            public void done(final MassUser massUser, ParseException e) {
                if(e==null){
                    massUser.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d(APPTAG, "Successfully deleted mass user " + user_id );
                            } else {
                                Log.d(APPTAG, "Failed to delete mass user " + e);
                            }
                        }
                    });
                } else {
                    Log.d(APPTAG, "Failed to find the current mass user");
                }
            }
        });
    }
    /*
     * Helper function for onCreate
     * Initialize the location request for maps activity
     */

    protected void initLocationRequest(){
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        Log.i(APPTAG,"LOCATION REQUEST CREATED");

        // Set the update interval
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLLISECONDS);

    }
    /*
     * Helper function for onCreate
     * Initialize the Goolge Api Client for maps activity
     */
    protected void initGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.i(APPTAG,"GOOGLE API CLIENT CREATED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        //Log.d(APPTAG,"On Resume, Google Api Client connect");
        //Log.d(APPTAG,"On Resume, my current location is " + mCurrentLocation);
        if(mCurrentLocation != null){
            // Create a LatLng object for the current location
            double latitude = mCurrentLocation.getLatitude();

            // Get longitude of the current location
            double longitude = mCurrentLocation.getLongitude();
            Log.i(APPTAG, "my LatLng is " + latitude + ", " + longitude );
            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude,longitude);

            // Move the camera to the place in interest
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
            //Log.d(APPTAG, "update camera on resume");
        }

    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()){
            stopPeriodicLocationUpdates();
        }
        mGoogleApiClient.disconnect();

        super.onStop();
        setUpMapIfNeeded();

    }

    @Override
    //TODO
    // Must call super.onDestroy() at the end.
    protected void onDestroy(){
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user", mMassUser.getUser());
        query.getFirstInBackground(new GetCallback<MassUser>(){
            @Override
            public void done(MassUser massUser, ParseException e) {
                if(e==null){
                    massUser.deleteInBackground();
                } else {
                    Log.d(APPTAG, "Failed to find the current mass user");
                }
            }
        });
        super.onDestroy();
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
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = mLocationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location mCurrentLocation = mLocationManager.getLastKnownLocation(provider);

        updateZoom(mCurrentLocation);

        // Get longitude of the current location
        double longitude = mCurrentLocation.getLongitude();
        double latitude = mCurrentLocation.getLatitude();
        Log.i(APPTAG, "my LatLng is " + latitude + ", " + longitude );
        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude,longitude);
        // Get the bounds to zoom to
     //   LatLngBounds bounds = calculateBoundsWithCenter(latLng);
        // Zoom to the given bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
        Log.i(APPTAG, "update camera");
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                doMapQuery();
            }
        });

       // mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        //mMap.addMarker(new MarkerOptions().position(latLng).title("me"));
        //CameraPosition mCameraPosition = new CameraPosition.Builder().build();

    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClient.connect();
        mCurrentLocation = getLocation();
        //Log.d(APPTAG,"ONCONNECTED");

        anonymousUserLogin(); // Helper function to log in the user anonymously if not alreadly logged in
        starterPeriodicLocationUpdates();// connect googleFused api services

        // set up mMassUser
        mMassUser.setUser(ParseUser.getCurrentUser());
        Log.d(APPTAG, "Current massuser is " + mMassUser);
        if(mCurrentLocation == null){
            Log.d(APPTAG,"mCurrentlocation is null");
            mMassUser.setLocation(null);
        } else {
            Log.d(APPTAG,"mCurrentlocation is NOT null");
            mMassUser.setLocation(geoPointFromLocation(mCurrentLocation));
        }

        Log.i(APPTAG, "Object Id of current user is " + ParseUser.getCurrentUser().getObjectId());
        mMassUser.setUser(ParseUser.getCurrentUser());
        updateUserLocation(mMassUser.getLocation());


        // update MassEvent
        Log.i(APPTAG, "Event ID of current user is " + mEventID);
        updateUserEvent(geoPointFromLocation(mCurrentLocation));
    }
    /*
     * Helper Function
     * Set up the ParseACL for the current user
     */
//    protected void setParseACL(){
//        ParseACL defaultACL = new ParseACL();
//
//        // Optionally enable public read access.
//        defaultACL.setPublicReadAccess(true);
//        defaultACL.setPublicWriteAccess(true);
//
//        ParseACL.setDefaultACL(defaultACL, true);
//
//        // allows read and write access to all users
//        ParseACL postACL = new ParseACL(ParseUser.getCurrentUser());
//        postACL.setPublicReadAccess(true);
//        postACL.setPublicWriteAccess(true);
//    }

    /*
     * Helper Function
     * Anonymous User login for phase 1, to be replaced with actual log-in activity
     * TODO
     */
    protected void anonymousUserLogin(){
        ParseUser.enableAutomaticUser();
        Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is "+ ParseUser.getCurrentUser());
        Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is null?"+ ParseUser.getCurrentUser().getObjectId());
        Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is null?"+ ParseUser.getCurrentUser().getCreatedAt());
        Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is null?"+ ParseUser.getCurrentUser().getUsername());
        ParseUser puser = ParseUser.getCurrentUser();
        String pid = puser.getObjectId();
        Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is "+ pid);
        if(pid == null) {
            Log.d(APPTAG,  " In anonymousUserLogin, in if!!!!");
            ParseAnonymousUtils.logInInBackground();
//            ParseAnonymousUtils.logIn(new LogInCallback() {
//
//                @Override
//                public void done(ParseUser user, ParseException e) {
//                    if (e != null) {
//                        Log.d(APPTAG, "Anonymous login failed.");
//                    } else {
//                        Log.d(APPTAG, "Anonymous user logged in.");
//                        Log.d(APPTAG,  " in callback, ParseUser is "+ user.getObjectId());
//                        Log.d(APPTAG,  " in callback, ParseUser is "+ ParseUser.getCurrentUser().getObjectId());
//                    }
//                }
//            });
            Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is "+ ParseUser.getCurrentUser().getObjectId());

        }
        //setParseACL();
        Log.d(APPTAG,  " In anonymousUserLogin, ParseUser is "+ ParseUser.getCurrentUser().getObjectId());
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    // passes in the user current location as input
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mLastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(mLastLocation)) < UPDATE_PIVOT) {
            return;
        }
        mLastLocation = location;
        updateUserLocation(geoPointFromLocation(location));
        updateZoom(location);
        doMapQuery();

        updateUserEvent(geoPointFromLocation(mCurrentLocation));//helper function to update event as location changes
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    /*
     * private helper functions
     */

    private ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
       // Log.i(APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }
    /*
     * Helper function to update user's location in the MassUser table in cloud
     *    if the user is not found in the table, save it to the cloud with the current location
     *    if the user is already in the table, then replace the obsolete location with the current location.
     */
    protected void updateUserLocation(ParseGeoPoint value) {
        final ParseGeoPoint geoPointValue = value; // need "final" type to pass in the callback function
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user",mMassUser.getUser());
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                Log.d(APPTAG, "Done with getFirstInBackground loc " + e);

                if (e == null) {
                    // no error exception, the user is found in the cloud, update the location in the cloud
                    Log.d(APPTAG, "massuser in updateUserLocation after query is " + massUser.getUser());
                    massUser.setLocation(geoPointValue);
                    massUser.saveInBackground(new SaveCallback() {

                        @Override
                        public void done(ParseException e) {
                            Log.d(APPTAG, "Done with getFirstInBackground loc");

                            if (e==null){
                                Log.d(APPTAG, "MassUser update saved successfully");
                            } else {
                                Log.d(APPTAG, "MassUser update were not saved");
                            }
                        }
                    });
                    Log.d(APPTAG, "Updated the parse user's location");
                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // The user has not been saved into the cloud, save it with current location
                    mMassUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null){
                                Log.d(APPTAG, "New MassUser saved successfully");
                            } else {
                                Log.d(APPTAG, "New MassUser were not saved");
                            }
                        }
                    });
                    Log.d(APPTAG, "Saved new MassUser.");
                } else {
                    // Do nothing
                }
            }
        });
        return;
    }

    /*
     * API calls to start/stop periodic location update, and get the current location.
     */


    // helper function to update the user's event by event type data(Xin)
    // pass in the current location
    protected void updateUserEvent(ParseGeoPoint value){
        // Find by ID the user's last event
        mEventID = mMassUser.getEvent();

        // the user current location
        final ParseGeoPoint currentLocation = value;

        //This is the first query to validate the user's last event
        final ParseQuery<MassEvent> query1 = MassEvent.getQuery();

        // This is the second query to find the user's new event
        final ParseQuery<MassEvent> query2 = MassEvent.getQuery();

        // check if the user's old event exists
        query1.whereEqualTo("event",mEventID);

        Log.i(APPTAG,"Mass User in updateUserEvent is " + mEventID);

        query1.getFirstInBackground(new GetCallback<MassEvent>() {
            @Override
            public void done(MassEvent massEvent, ParseException e) {
                Log.i(APPTAG, "Done with getFirstInBackground loc " + e);
                // the event ID is found
                if (e == null) {
                    Log.i(APPTAG, "massevent in updateUserLocation after query is " + massEvent.getEvent());

                    // check if the user is still within the event radius
                    double distance = currentLocation
                            .distanceInKilometersTo(massEvent.getLocation());
                    // the user is no longer inside the old event
                    if (distance > massEvent.getRadius()) {
                        // decrement the old event size as the user is no longer there
                        int size = massEvent.getEventSize();
                        size = size - 1;
                        massEvent.setEventSize(size);
                        massEvent.saveInBackground();

                        // search for new event, if any,  that includes the user
                        double maxDistance = 5;

                        // finding objects in "event" near the point given and within the maximum distance given.
                        query2.whereWithinKilometers("location", currentLocation, maxDistance);

                        // Since the user can only be in one event at a time, use getFirstInBackground
                        query2.getFirstInBackground(new GetCallback<MassEvent>() {
                            @Override
                            public void done(MassEvent massEvent, ParseException e) {
                                if (e == null) {
                                    Log.i(APPTAG, "the current massevent is " + massEvent.getEvent());
                                    int size = massEvent.getEventSize();
                                    size = size + 1;
                                    massEvent.setEventSize(size);
                                    massEvent.saveInBackground();
                                } else {
                                    // No new event found
                                    Log.i(APPTAG, "new event not found ");
                                }
                            }
                        });
                        return;
                    }
                }
            }
        });
    }





    private void starterPeriodicLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    private void stopPeriodicLocationUpdates(){
        LocationServices.FusedLocationApi
                .removeLocationUpdates(mGoogleApiClient, this);
    }

    private Location getLocation() {
        if(servicesConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            return null;
        }
    }

    /*
     * Zooms the map to show the area of interest based on the search radius
     */
    private void updateZoom(Location location) {
        LatLng myLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        // Move the camera to the location in interest and zoom to appropriate level
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, ZOOM_LEVEL));
    }

    // display events by markers on the map
    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;
        // 1
        Location myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }
        // 2
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // 3
        ParseQuery<MassEvent> mapQuery = MassEvent.getQuery();
        // 4
        mapQuery.whereWithinKilometers("location", myPoint, SEARCH_DISTANCE);
        // 5
        //mapQuery.include("objectId");
        mapQuery.orderByDescending("createdAt");
       // mapQuery.setLimit(MAX_MARKER_SEARCH_RESULTS);
        // 6
        mapQuery.findInBackground(new FindCallback<MassEvent>() {
            @Override
            public void done(List<MassEvent> objects, ParseException e) {
                if (e != null) {
                    Log.d(APPTAG, "An error occurred while querying for map posts.", e);
                    return;
                }   else {
                    Log.d(APPTAG, "Find Mass Event" + objects.get(0).getObjectId());
                }

                if (myUpdateNumber != mostRecentMapUpdate) {
                    return;
                }
                // Handle the results
                Set<String> toKeep = new HashSet<String>();
                // 2
                for (MassEvent mEvent : objects) {
                    // 3 check if the event size exceeds the threshold, tentatively set to 0
                    if (mEvent.getEventSize() > 10){
                        //Log.d(APPTAG, "valid mass event"+mEvent.getEventSize());

                        toKeep.add(mEvent.getObjectId());
                        // 4
                        Marker oldMarker = mapMarkers.get(mEvent.getObjectId());
                        // 5
                        MarkerOptions markerOpts =
                                new MarkerOptions().position(new LatLng(mEvent.getLocation().getLatitude(), mEvent
                                        .getLocation().getLongitude()));
                        // 6
                        if (mEvent.getLocation().distanceInKilometersTo(myPoint) > radius * METERS_PER_FEET
                                / METERS_PER_KILOMETER) {
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

                        }
                        else {
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
                        // 7
                        Marker marker = mMap.addMarker(markerOpts);
                        mapMarkers.put(mEvent.getObjectId(), marker);
                        // 8
                        if (mEvent.getObjectId().equals(selectedPostObjectId)) {
                            marker.showInfoWindow();
                            selectedPostObjectId = null;
                        }
                    }
                }

                // 9
                cleanUpMarkers(toKeep);
            }
        });
    }
    /*
     * Remove markers that are not in the Hashmap markersToKeep
     */
    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }



    /*
     * Show a dialog returned by Google Play services for the connection error code
     */

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog
                = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        if (errorDialog != null) {

            // Creatae a new DialogFracment in which to show the error dialog
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorDialogFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorDialogFragment.show(getSupportFragmentManager(), APPTAG);
        }

    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(this.getSupportFragmentManager(), APPTAG);
            }
            return false;
        }
    }

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
}

