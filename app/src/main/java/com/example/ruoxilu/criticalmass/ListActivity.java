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
    private String[] listArray;
    private com.parse.ParseFile[] eventIconsArray;
    private ParseGeoPoint userLocationPoint;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Application.networkConnected(this)) {

            setContentView(R.layout.activity_list);
            userLocationPoint = getLocationPoint();
            mActivityOne = (ListView) findViewById(R.id.event_list);
            getEventInfo();


            // Bind data from adapter to ListView.
            ArrayAdapter<String> adapter = new ListActivityAdapter(this, listArray);
            mActivityOne.setAdapter(adapter);


            // Load EventActivity when user clicks on a mass in the
            mActivityOne.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent eventDetailIntent = new Intent();
                    eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
                    String eventId = listArray[position];
                    eventDetailIntent.putExtra("objectId", eventId);
                    Log.d(Application.APPTAG, "event object id is "+id);
                    startActivity(eventDetailIntent);
                }
            });
        }
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


    protected void getEventInfo() {

        ParseQuery<ParseObject> eventsQuery = ParseQuery.getQuery("MassEvent");

        eventsQuery.whereNear("location", userLocationPoint);
        eventsQuery.setLimit(10);

        // ArrayList<String> mNearbyList = new ArrayList<String>();

        eventIconsArray = new com.parse.ParseFile[10];
//        String[] listArray = new String[mNearbyList.size()];
//        listArray = mNearbyList.toArray(listArray);
        listArray = new String[10];
        List<ParseObject> parseObjects;

        try {
            // Use find instead of findInBackground because of a potential thread problem.
            parseObjects = eventsQuery.find();
            int i = 0;
            for (ParseObject mass : parseObjects) {
                String eventObjectId = mass.getObjectId();
                com.parse.ParseFile eventIcon = mass.getParseFile("image");

//                mNearbyList.add(eventObjectId);
                listArray[i] = eventObjectId;
                eventIconsArray[i] = eventIcon;
                i++;
            }
        } catch (ParseException e) {
            Log.d(Application.APPTAG, e.getMessage());
        }

    }

}
