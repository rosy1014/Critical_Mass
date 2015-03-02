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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Button mMiddleBar;  // Directs to list activity
    Button mLeftBar;    // Placeholder for login
    Button mRightBar;   // Placeholder
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Location mLastLocation;
    private MassUser mMassUser;

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
    private static final double UPDATE_PIVOT = 0.01;
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
    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;

    private static final String APPTAG = "CriticalMass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(APPTAG,"onCreate");
        super.onCreate(savedInstanceState);
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        Log.i(APPTAG,"LOCATION REQUEST CREATED");

        // Set the update interval
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLLISECONDS);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.i(APPTAG,"GOOGLE API CLIENT CREATED");
        mMassUser = new MassUser();
        Log.i(APPTAG, "mMassUser " + mMassUser);
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

        mLeftBar = (Button)findViewById(R.id.map_left_bar);
        mLeftBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {

                // Change color if pressed and reset after release
                if (ev.getAction() == MotionEvent.ACTION_DOWN ) {
                    mLeftBar.setBackgroundColor(0xff2a4a90);
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


    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        Log.i(APPTAG,"On Resume, Google Api Client connect");
        Log.i(APPTAG,"On Resume, my current location is " + mCurrentLocation);
        if(mCurrentLocation != null){
            double latitude = mCurrentLocation.getLatitude();

            // Get longitude of the current location
            double longitude = mCurrentLocation.getLongitude();
            Log.i(APPTAG, "my LatLng is " + latitude + ", " + longitude );
            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude,longitude);
            // Get the bounds to zoom to
            //   LatLngBounds bounds = calculateBoundsWithCenter(latLng);
            // Zoom to the given bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
            Log.i(APPTAG, "update camera on resume");

            // mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            mMap.addMarker(new MarkerOptions().position(latLng).title("me"));
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

        Log.i(APPTAG,"mCurrentLocation is " + mCurrentLocation );
        double latitude = mCurrentLocation.getLatitude();

        // Get longitude of the current location
        double longitude = mCurrentLocation.getLongitude();
        Log.i(APPTAG, "my LatLng is " + latitude + ", " + longitude );
        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude,longitude);
        // Get the bounds to zoom to
     //   LatLngBounds bounds = calculateBoundsWithCenter(latLng);
        // Zoom to the given bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
        Log.i(APPTAG, "update camera");

       // mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        mMap.addMarker(new MarkerOptions().position(latLng).title("me"));
        //CameraPosition mCameraPosition = new CameraPosition.Builder().build();

        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClient.connect();
        mCurrentLocation = getLocation();
        Log.i(APPTAG,"ONCONNECTED");
        ParseUser.enableAutomaticUser();
        if(ParseUser.getCurrentUser()== null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Log.d(APPTAG, "Anonymous login failed.");
                    } else {
                        Log.d(APPTAG, "Anonymous user logged in.");
                    }
                }
            });
            Log.i(APPTAG,"ANONYMOUS USER LOGGED IN");
        }



        ParseACL defaultACL = new ParseACL();
//        Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);

        // allows read and write access to all users
        ParseACL postACL = new ParseACL(ParseUser.getCurrentUser());
        postACL.setPublicReadAccess(true);
        postACL.setPublicWriteAccess(true);
        starterPeriodicLocationUpdates();
        Log.i(APPTAG, "Current massuser is " + mMassUser);
        if(mCurrentLocation == null){
            Log.i(APPTAG,"mCurrentlocation is null");
            mMassUser.setLocation(null);
        } else {
            Log.i(APPTAG,"mCurrentlocation is NOT null");
            mMassUser.setLocation(geoPointFromLocation(mCurrentLocation));
        }
        mMassUser.setUser(ParseUser.getCurrentUser());
        mMassUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(APPTAG, "MassUser saved successfully.");
                } else {
                    Log.d(APPTAG, "MassUser failed to save.");
                }
            }
        });
        Log.i(APPTAG, "saved mMassuser");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mLastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(mLastLocation)) < UPDATE_PIVOT) {
            return;
        }
        mLastLocation = location;
        updateUserLocation(location);
        updateZoom(location);

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
        Log.i(APPTAG,"Latitude = "+ location.getLatitude() + " Longitude = " + location.getLongitude());
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        Log.i(APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }

    // TODO
    // Call cloud function
    protected void updateUserLocation(Location value) {
        final ParseGeoPoint geoPointValue = geoPointFromLocation(value);
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user",mMassUser.getUser());
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                massUser.setLocation(geoPointValue);
                massUser.saveInBackground();
                if(e==null){
                    return;
                }
            }
        });
        return;
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
      /*
   * Zooms the map to show the area of interest based on the search radius
   */
    private void updateZoom(Location location) {
        LatLng myLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17));
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

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if(dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(this.getSupportFragmentManager(), APPTAG);
            }
            return false;
        }
    }
}

