package com.example.ruoxilu.criticalmass;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    Button mMiddleBar;  // Directs to list activity
    Button mLeftBar;    // Placeholder for login
    Button mRightBar;   // Placeholder
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

}
