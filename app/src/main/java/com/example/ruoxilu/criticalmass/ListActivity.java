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

import android.widget.Toast;

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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        ParseQuery<ParseObject> testQuery = ParseQuery.getQuery("MassEvent");

        Location userCurrentLocation = MapsActivity.mCurrentLocation;
        Location userLastLocation = MapsActivity.mLastLocation;
        if (userCurrentLocation == null) {
            Log.i(Application.APPTAG, "the current location is null");
            userCurrentLocation = userLastLocation;
        }

        ArrayList<String> mNearbyList = new ArrayList<String>();

        testQuery.whereNear("location", geoPointFromLocation(userCurrentLocation));
        testQuery.setLimit(10);

        List<ParseObject> parseObjects;

        try {
            // Use find instead of findInBackground because of a potential thread problem.
            parseObjects = testQuery.find();
            for (ParseObject mass : parseObjects) {
                String eventObjectId = mass.getObjectId();
                mNearbyList.add(eventObjectId);
            }
        } catch (ParseException e) {
            Log.d(Application.APPTAG, e.getMessage());
        }

        mListArray = new String[(mNearbyList.size())];
        mListArray = mNearbyList.toArray(mListArray);

//        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, mListArray);

        ArrayAdapter<String> adapter = new ListActivityAdapter(this, mListArray);

        mActivityOne = (ListView) findViewById(R.id.event_list);
        // Bind data from adapter to ListView.
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

    // The "static" keyword was added so that the constructor can call this function.
    private static ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        Log.i(Application.APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }


}
