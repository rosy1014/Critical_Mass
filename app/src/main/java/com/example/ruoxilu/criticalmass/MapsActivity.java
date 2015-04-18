package com.example.ruoxilu.criticalmass;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    // Made static so that other activity can access location.
    public static Location mCurrentLocation = Settings.getDefaultLocation();
    public static Location mLastLocation = Settings.getDefaultLocation();
    public static Context mContext;
    // Fields for helping process the map and location changes
    private static Map<String, Marker> mapMarkers = new HashMap<String, Marker>(); // find marker based on Event ID
    private static Map<Marker, String> markerIDs = new HashMap<Marker, String>(); // find Event ID associated with marker
    private static ViewGroup mViewGroup;
    private static LinearLayout mMainScreen;
    protected MassUser mMassUser; // Each user (i.e. application) only has one MassUser object.

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

        MapsHandler.initLocationRequest(); // Helper function to initiate location request
        initGoogleApiClient(); // Helper function to initiate Google Api Client to "listen to" location change

        mMassUser = new MassUser(); // Initialize mMassUser data object
        //Log.d(Settings.APPTAG, "mMassUser " + mMassUser);

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

    @Override
    //TODO
    // Must call super.onDestroy() at the end.
    protected void onDestroy() {
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user", mMassUser.getUser());
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                if (e == null) {
                    massUser.deleteInBackground();
                } else {
                    Log.d(Settings.APPTAG, "Failed to find the current mass user");
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
     * This is where we can add markers or lines, add listeners or move the camera.
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
        if (mLocationManager.getLastKnownLocation(provider) == null) {
            mCurrentLocation = Settings.getDefaultLocation();
//            mCurrentLocation.setLongitude(37.0);
//            mCurrentLocation.setLatitude(-63.0);

        } else {
            mCurrentLocation = mLocationManager.getLastKnownLocation(provider);
        }


        updateZoom(mCurrentLocation);

        // Get longitude of the current location
        double longitude = mCurrentLocation.getLongitude();
        double latitude = mCurrentLocation.getLatitude();
        Log.i(Settings.APPTAG, "my LatLng is " + latitude + ", " + longitude);
        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        // Get the bounds to zoom to
        //   LatLngBounds bounds = calculateBoundsWithCenter(latLng);
        // Zoom to the given bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(Settings.ZOOM_LEVEL));
        Log.i(Settings.APPTAG, "update camera");
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                doMapQuery();
            }
        });
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        //mMap.addMarker(new MarkerOptions().position(latLng).title("me"));
        //CameraPosition mCameraPosition = new CameraPosition.Builder().build();

    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClient.connect();
        mCurrentLocation = getLocation();

        anonymousUserLogin(); // Helper function to log in the user anonymously if not already logged in
        starterPeriodicLocationUpdates();// connect googleFused api services

        // set up mMassUser
        mMassUser.setUser(ParseUser.getCurrentUser());
        Log.d(Settings.APPTAG, "Current massuser is " + ParseUser.getCurrentUser());
        if (mCurrentLocation == null) {
            Log.d(Settings.APPTAG, "mCurrentlocation is null");
            mMassUser.setLocation(null);
        } else {
            Log.d(Settings.APPTAG, "mCurrentlocation is NOT null");
            mMassUser.setLocation(geoPointFromLocation(mCurrentLocation));
        }

        Log.i(Settings.APPTAG, "Object Id of current user is " + ParseUser.getCurrentUser().getObjectId());
        mMassUser.setUser(ParseUser.getCurrentUser());
        updateUserLocation(mMassUser.getLocation());


        // update MassEvent
        Log.i(Settings.APPTAG, "Event ID of current user is " + mEventID);
        updateUserEvent(geoPointFromLocation(mCurrentLocation));
    }

    /*
     * Helper Function
     * Anonymous User login for phase 1, to be replaced with actual log-in activity
     * TODO
     */
    protected void anonymousUserLogin() {
        ParseUser.enableAutomaticUser();
        Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is null? " + ParseUser.getCurrentUser().getObjectId());

        ParseUser puser = ParseUser.getCurrentUser();
        String pid = puser.getObjectId();
        Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is " + pid);
        if (pid == null) {
            Log.d(Settings.APPTAG, " In anonymousUserLogin, in if!!!!");
            ParseAnonymousUtils.logInInBackground();
//            ParseAnonymousUtils.logIn(new LogInCallback() {
//
//                @Override
//                public void done(ParseUser user, ParseException e) {
//                    if (e != null) {
//                        Log.d(Settings.APPTAG, "Anonymous login failed.");
//                    } else {
//                        Log.d(Settings.APPTAG, "Anonymous user logged in.");
//                        Log.d(Settings.APPTAG,  " in callback, ParseUser is "+ user.getObjectId());
//                        Log.d(Settings.APPTAG,  " in callback, ParseUser is "+ ParseUser.getCurrentUser().getObjectId());
//                    }
//                }
//            });
            Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is " + ParseUser.getCurrentUser().getObjectId());

        }
        //setParseACL();
        Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is " + ParseUser.getCurrentUser().getObjectId());
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
                .distanceInKilometersTo(geoPointFromLocation(mLastLocation)) < Settings.UPDATE_PIVOT) {
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
     * Helper function to update user's location in the MassUser table in cloud
     *    if the user is not found in the table, save it to the cloud with the current location
     *    if the user is already in the table, then replace the obsolete location with the current location.
     */
    protected void updateUserLocation(ParseGeoPoint value) {
        final ParseGeoPoint geoPointValue = value; // need "final" type to pass in the callback function
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user", mMassUser.getUser());
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                Log.d(Settings.APPTAG, "Done with getFirstInBackground loc " + e);

                if (e == null) {
                    // no error exception, the user is found in the cloud, update the location in the cloud
                    Log.d(Settings.APPTAG, "massuser in updateUserLocation after query is " + massUser.getUser());
                    massUser.setLocation(geoPointValue);
                    massUser.saveInBackground(new SaveCallback() {

                        @Override
                        public void done(ParseException e) {
                            Log.d(Settings.APPTAG, "Done with getFirstInBackground loc");

                            if (e == null) {
                                Log.d(Settings.APPTAG, "MassUser update saved successfully");
                            } else {
                                Log.d(Settings.APPTAG, "MassUser update were not saved");
                            }
                        }
                    });
                    Log.d(Settings.APPTAG, "Updated the parse user's location");
                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // The user has not been saved into the cloud, save it with current location
                    mMassUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(Settings.APPTAG, "New MassUser saved successfully");
                            } else {
                                Log.d(Settings.APPTAG, "New MassUser were not saved");
                            }
                        }
                    });
                    Log.d(Settings.APPTAG, "Saved new MassUser.");
                } else {
                    // Do nothing
                }
            }
        });
        return;
    }

    /*
     * private helper functions
     */

    // helper function to update the user's event by event type data(Xin)
    // pass in the current location
    protected void updateUserEvent(ParseGeoPoint value) {
        // Find by ID the user's last event
        mEventID = mMassUser.getEvent();

        // the user current location
        final ParseGeoPoint currentLocation = value;

        //This is the first query to validate the user's last event
        final ParseQuery<MassEvent> query1 = MassEvent.getQuery();

        // This is the second query to find the user's new event
        final ParseQuery<MassEvent> query2 = MassEvent.getQuery();

        // check if the user's old event exists
        query1.whereEqualTo("event", mEventID);

        Log.i(Settings.APPTAG, "Mass User in updateUserEvent is " + mEventID);

        query1.getFirstInBackground(new GetCallback<MassEvent>() {
            @Override
            public void done(MassEvent massEvent, ParseException e) {
                Log.i(Settings.APPTAG, "Done with getFirstInBackground loc " + e);
                // the event ID is found
                if (e == null) {
                    Log.i(Settings.APPTAG, "massevent in updateUserLocation after query is " + massEvent.getEvent());

                    // check if the user is still within the event radius
                    double distance = currentLocation
                            .distanceInKilometersTo(massEvent.getLocation());
                    // the user is no longer inside the old event
                    if (distance > massEvent.getRadius()) {
                        // decrement the old event size as the user is no longer there
                        int size = massEvent.getEventSize();
                        size = size - 1;
                        Log.d(Settings.APPTAG, "decrement event size");
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
                                    Log.i(Settings.APPTAG, "the current massevent is " + massEvent.getEvent());
                                    int size = massEvent.getEventSize();
                                    size = size + 1;
                                    massEvent.setEventSize(size);
                                    massEvent.saveInBackground();
                                    mMassUser.setEvent(massEvent);
                                    mMassUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Log.d(Settings.APPTAG, "update user event error: " + e);
                                        }
                                    });
                                } else {
                                    // No new event found
                                    Log.i(Settings.APPTAG, "new event not found ");
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, Settings.ZOOM_LEVEL));
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
        Log.d(Settings.APPTAG, "myloc is " + myLoc);
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // 3
        ParseQuery<MassEvent> mapQuery = MassEvent.getQuery();
        // 4
        mapQuery.whereWithinKilometers("location", myPoint, Settings.SEARCH_DISTANCE);
        // 5
        //mapQuery.include("objectId");
        mapQuery.orderByDescending("createdAt");
        // mapQuery.setLimit(MAX_MARKER_SEARCH_RESULTS);
        // 6
        mapQuery.findInBackground(new FindCallback<MassEvent>() {
            @Override
            public void done(List<MassEvent> objects, ParseException e) {
                if (e != null) {
                    Log.d(Settings.APPTAG, "An error occurred while querying for map posts.", e);
                    return;
                } else {
                    Log.d(Settings.APPTAG, "Find Mass Event " + e);
                    //  Log.d(Settings.APPTAG, "Find Mass Event " + objects.get(0).getObjectId());
                }

                if (myUpdateNumber != mostRecentMapUpdate) {
                    return;
                }
                // Handle the results
                Set<String> toKeep = new HashSet<String>();
                // 2
                for (final MassEvent mEvent : objects) {
                    // 3 check if the event size exceeds the threshold, tentatively set to 0
                    if (mEvent.getEventSize() > 10) {
                        //Log.d(Settings.APPTAG, "valid mass event"+mEvent.getEventSize());

                        toKeep.add(mEvent.getObjectId());
                        // 4
                        Marker oldMarker = mapMarkers.get(mEvent.getObjectId());
                        // 5
                        MarkerOptions markerOpts = createMarkerOpt(mEvent);
                        // 6
                        if (mEvent.getLocation().distanceInKilometersTo(myPoint) > radius * Settings.METERS_PER_FEET
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
                                    Log.d(Settings.APPTAG, "Removed oldmarker: " + oldMarker);
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
                        // 7
                        Marker marker = mMap.addMarker(markerOpts);
                        //marker.showInfoWindow();
                        Log.d(Settings.APPTAG, "Showed info Window");
                        // update markerIDs hash map and mapMarkers hash map.
                        markerIDs.put(marker, mEvent.getObjectId());
                        mapMarkers.put(mEvent.getObjectId(), marker);
                        // 8
//                        if (mEvent.getObjectId().equals(selectedPostObjectId)) {
//                            marker.showInfoWindow();
//                            selectedPostObjectId = null;
//                        }
                    }
                }

                // 9
                cleanUpMarkers(toKeep);
                Log.d(Settings.APPTAG, "After clean up markers");
            }
        });
    }

    /*
     * Remove markers that are not in the Hashmap markersToKeep
     */
    // SIGN_MARKER_OBJECT
    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                markerIDs.remove(marker);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
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
    protected MarkerOptions createMarkerOpt(MassEvent mEvent) {

        int size = mEvent.getEventSize();
        if (size < Settings.POPSIZE2) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2))
                    .title("Location: " + mEvent.getLocation()).snippet("Size: " + size);
            return markerOpt;
        } else if (size < Settings.POPSIZE3) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3))
                    .title("Location: " + mEvent.getLocation()).snippet("Size: " + size);
            return markerOpt;
        } else if (size < Settings.POPSIZE4) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker4))
                    .title("Location: " + mEvent.getLocation()).snippet("Size: " + size);
            return markerOpt;
        } else if (size < Settings.POPSIZE5) {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker5))
                    .title("Location: " + mEvent.getLocation()).snippet("Size: " + size);
            return markerOpt;
        } else {
            MarkerOptions markerOpt = new MarkerOptions().position(
                    new LatLng(mEvent.getLocation().getLatitude(), mEvent
                            .getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker6))
                    .title("Location: " + mEvent.getLocation()).snippet("Size: " + size);
            return markerOpt;
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

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog
                = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                Settings.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        if (errorDialog != null) {

            // Creatae a new DialogFracment in which to show the error dialog
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorDialogFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorDialogFragment.show(getSupportFragmentManager(), Settings.APPTAG);
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
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(this.getSupportFragmentManager(), Settings.APPTAG);
            }
            return false;
        }
    }


    /*
     * Show a dialog returned by Google Play services for the connection error code
     */

    public void confirmLogOut() {


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure you want to log out?");
        alert.setMessage("Logged out users will no longer be shown on the map.");

        // Make an "OK" button to confirm log out
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                ParseHandler.deleteMassUser(mMassUser);
                ParseUser.logOut();
                Intent intent = new Intent(MapsActivity.this, DispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
//                Intent i = new Intent(MapsActivity.this, LoginSignupActivity.class);
//                startActivityForResult(i, 0);

                Toast.makeText(getApplicationContext(), "You have successfully logged out!", Toast.LENGTH_LONG).show();
            }
        });

        // Make a "Cancel" button
        // that simply dismisses the alert
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (markerIDs.containsKey(marker)) {
            Intent eventDetailIntent = new Intent();
            eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
            String eventId = markerIDs.get(marker);
            eventDetailIntent.putExtra("objectId", eventId);
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