package com.example.ruoxilu.criticalmass;

import android.app.Dialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.parse.Parse;
import com.parse.ParseGeoPoint;


public class MainActivity extends FragmentActivity implements LocationListener,
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

    private static final double UPDATE_PIVOT = 0.01;

    private LocationRequest mLocationRequest;
    //private LocationClient locationClient;
    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;
    private Location mCurrentLocation;

    private static String APPTAG = "Critical Mass";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        // Set the update interval
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLLISECONDS);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    /*
     * Methods used in activity lifecycle
     */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = getLocation();
        starterPeriodicLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDisconnected() {
        return;
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()){
            stopPeriodicLocationUpdates();
        }
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
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
    }

    private ParseGeoPoint geoPointFromLocation(Location location) {
        return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//    }

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
