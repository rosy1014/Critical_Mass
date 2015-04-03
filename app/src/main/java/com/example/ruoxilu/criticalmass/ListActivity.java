package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import android.content.Intent;

import android.util.Log;

import android.app.Fragment;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by tingyu on 2/23/15.
 * The ListActivity class displays a list of masses nearby. ListActivity fetches data from parse
 * through the ParseQueryAdapter and bind it to the ListView.
 */
public class ListActivity extends Activity {


    private ListActivityAdapter eventListAdapter;
    ListView mActivityOne;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

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

        // TODO: Depend on whether we assign name to events. For now it uses ObjectId.
        // eventListAdapter.setTextKey("objectId");
        // TODO Disable this until new icons are created. A good way to do this is if each user is
        // given a default profile image and an event image is created with users' images.
        // eventListAdapter.setImageKey("icons");



        eventListAdapter = new ListActivityAdapter(this);
//        final ParseQueryAdapter<ParseObject> eventListAdapter = new ParseQueryAdapter<ParseObject>(this, "MassUser");
//        eventListAdapter.setTextKey("objectId");



        ParseQuery<ParseObject> testQuery = ParseQuery.getQuery("MassEvent");

        Location userCurrentLocation = MapsActivity.mCurrentLocation;
        Location userLastLocation = MapsActivity.mLastLocation;
        if (userCurrentLocation == null) {
            Log.i(Application.APPTAG, "the current location is null");
            userCurrentLocation = userLastLocation;
        }

        testQuery.whereNear("location", geoPointFromLocation(userCurrentLocation));
        testQuery.setLimit(5);
        testQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                for (ParseObject mass : parseObjects) {
                    String eventObjectId = mass.getObjectId();
                    int eventSize = mass.getInt("EventSize");
                    Log.d(Application.APPTAG, "event object id is "+eventObjectId);
                    Log.d(Application.APPTAG, "event size is "+eventSize);
                }
            }
        });

        // Disable paignation.
        eventListAdapter.setPaginationEnabled(false);

        mActivityOne = (ListView) findViewById(R.id.event_list);

        String userObjectId = ParseUser.getCurrentUser().toString();
        Log.d(Application.APPTAG, "user object id is "+userObjectId);

        // Bind data from adapter to ListView.
        mActivityOne.setAdapter(eventListAdapter);
        eventListAdapter.loadObjects();
        eventListAdapter.setAutoload(true);



        // Load EventActivity when user clicks on a mass in the list
        mActivityOne.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent eventDetailIntent = new Intent();
                eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
//                ParseObject eventObject = eventListAdapter.getItem(position);
//                String eventId = eventObject.getString("objectId");
                String tempId = "xCCyqr84Nz";
                eventDetailIntent.putExtra("objectId", tempId);
                Log.d(Application.APPTAG, "event object id is "+id);
                startActivity(eventDetailIntent);
            }
        });
    }

    // The "static" keyword was added so that the constructor can call this function.
    private static ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        Log.i(Application.APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }


}
