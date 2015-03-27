package com.example.ruoxilu.criticalmass;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.util.Log;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.ListView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseGeoPoint;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import java.text.ParseException;

/**
 * Created by tingyu on 2/23/15.
 */
public class ListActivity extends Activity {

    private ParseQueryAdapter<ParseObject> eventListAdapter;
    ListView mActivityOne;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ParseQueryAdapter.QueryFactory<ParseObject> factoryNearbyPost =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {
                        Location userCurrentLocation = MapsActivity.mCurrentLocation;
                        Location userLastLocation = MapsActivity.mLastLocation;

                        if (userCurrentLocation == null) {
                            Log.i(MapsActivity.APPTAG, "the current location is null");
                            userCurrentLocation = userLastLocation;
                        }

                        ParseQuery queryNearbyEvents = new ParseQuery("MassEvent");
                        queryNearbyEvents.whereNear("location",
                                geoPointFromLocation(userCurrentLocation));

                        queryNearbyEvents.setLimit(10);

                        return queryNearbyEvents;

                    }
                };

        eventListAdapter = new ParseQueryAdapter<ParseObject>(this, factoryNearbyPost);

//        eventListAdapter.
//                addOnQueryLoadListener(
//                        new OnQueryLoadListener<ParseObject>() {
//                            public void onLoading() {
//                                // TODO: Trigger loading UI if needed.
//                            }
//
//                            public void onLoaded(List<ParseObject> objects, ParseException e) {
//                                // TODO: Execute any post-loading logic, hide "loading" UI
//                            }
//                        }
//                );

        // TODO Depend on whether we assign name to events.
        // eventListAdapter.setTextKey("name");
        // TODO Disable this until new icons are created
        // eventListAdapter.setImageKey("icons");

        // Disable paignation.
        eventListAdapter.setPaginationEnabled(false);

        mActivityOne = (ListView) findViewById(R.id.activity_1);

//        mActivityOne.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(ListActivity.this, EventActivity.class);
//                startActivityForResult(i, 0);
//            }
//        });
    }




    private ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        // Log.i(APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }

}
