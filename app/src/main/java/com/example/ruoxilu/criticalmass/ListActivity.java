package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.content.Intent;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by tingyu on 2/23/15.
 * The ListActivity class displays a list of masses nearby. ListActivity fetches data from parse
 * through the ParseQueryAdapter and bind it to the ListView.
 */
public class ListActivity extends Activity {

    private ArrayList<String> mNearbyList;
    private ListView mActivityOne;
    private String[] mListArray;
    private ParseGeoPoint userLocationPoint;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);
        userLocationPoint = getLocationPoint();
        mActivityOne = (ListView) findViewById(R.id.event_list);

        ParseQuery<ParseObject> eventsQuery = ParseQuery.getQuery("MassEvent");

        ArrayList<String> mNearbyList = new ArrayList<String>();

        eventsQuery.whereNear("location", userLocationPoint);
        eventsQuery.setLimit(10);

        List<ParseObject> parseObjects;

        try {
            // Use find instead of findInBackground because of a potential thread problem.
            parseObjects = eventsQuery.find();
            for (ParseObject mass : parseObjects) {
                String eventObjectId = mass.getObjectId();
                mNearbyList.add(eventObjectId);
            }
        } catch (ParseException e) {
            Log.d(Application.APPTAG, e.getMessage());
        }

        mListArray = mNearbyList.toArray(mListArray);

        // Bind data from adapter to ListView.
        ArrayAdapter<String> adapter = new ListActivityAdapter(this, mListArray);
        mActivityOne.setAdapter(adapter);


        // Load EventActivity when user clicks on a mass in the
        mActivityOne.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent eventDetailIntent = new Intent();
                eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
                String eventId = mListArray[position];
                eventDetailIntent.putExtra("objectId", eventId);
                Log.d(Application.APPTAG, "event object id is "+id);
                startActivity(eventDetailIntent);
            }
        });
    }

    protected ParseGeoPoint getLocationPoint() {

        Location userLocation;
        if (MapsActivity.mCurrentLocation == null) {
            Log.i(Application.APPTAG, "the current location is null");
            userLocation = MapsActivity.mLastLocation;
        }
        else {
            userLocation = MapsActivity.mCurrentLocation;
        }

        ParseGeoPoint geoPoint = new ParseGeoPoint(userLocation.getLatitude(),
                userLocation.getLongitude());

        return geoPoint;
    }


    protected String[] getEventInfo() {

        ParseQuery<ParseObject> eventsQuery = ParseQuery.getQuery("MassEvent");

        ArrayList<String> mNearbyList = new ArrayList<String>();

        eventsQuery.whereNear("location", userLocationPoint);
        eventsQuery.setLimit(10);

        List<ParseObject> parseObjects;

        try {
            // Use find instead of findInBackground because of a potential thread problem.
            parseObjects = eventsQuery.find();
            for (ParseObject mass : parseObjects) {
                String eventObjectId = mass.getObjectId();
                mNearbyList.add(eventObjectId);
            }
        } catch (ParseException e) {
            Log.d(Application.APPTAG, e.getMessage());
        }

        String[] listArray = mNearbyList.toArray(mListArray);

        return listArray;
    }

}
