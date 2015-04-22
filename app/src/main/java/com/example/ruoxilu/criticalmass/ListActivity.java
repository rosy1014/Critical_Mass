package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tingyu on 2/23/15.
 * The ListActivity class displays a list of masses nearby. ListActivity fetches data from parse
 * through the ParseQueryAdapter and bind it to the ListView.
 */
public class ListActivity extends Activity {

    ArrayAdapter<String> mAdapter;
    List<MassEvent> parseObjects;
    private SwipeRefreshLayout mScrollList;
    private ArrayList<String> mNearbyList;
    private ListView mActivityOne;
    private String[] mListArray;
    private ParseGeoPoint userLocationPoint;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Application.networkConnected(this)) {

            setContentView(R.layout.activity_list);
            userLocationPoint = getLocationPoint();
            mActivityOne = (ListView) findViewById(R.id.event_list);
            mListArray = getEventInfo();

            mScrollList = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            mScrollList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshContent();

                }
            });


            // Bind data from adapter to ListView.
            mAdapter = new ListActivityAdapter(this, mListArray);
            mActivityOne.setAdapter(mAdapter);


            // Load EventActivity when user clicks on a mass in the
            mActivityOne.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent eventDetailIntent = new Intent();
                    eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
                    String eventId = parseObjects.get(position).getObjectId();
                    String locationName = parseObjects.get(position).getLocationName();
                    eventDetailIntent.putExtra("objectId", eventId);
                    eventDetailIntent.putExtra("location", locationName);
                    Log.d(Settings.APPTAG, "event object id is " + id);
                    startActivity(eventDetailIntent);
                }
            });
        }
    }

    private void refreshContent() {
        userLocationPoint = getLocationPoint();
        mListArray = getEventInfo();
        mAdapter = new ListActivityAdapter(this, mListArray);
        mActivityOne.setAdapter(mAdapter);


        mScrollList.setRefreshing(false);

        Log.d(Settings.APPTAG, "refreshContent and getEventInfo!!!!");

    }

    protected ParseGeoPoint getLocationPoint() {

        Location userLocation;
        if (MapsActivity.mCurrentLocation == null) {
            Log.i(Settings.APPTAG, "the current location is null");
            userLocation = MapsActivity.mLastLocation;
        } else {
            userLocation = MapsActivity.mCurrentLocation;
        }

        ParseGeoPoint geoPoint = new ParseGeoPoint(userLocation.getLatitude(),
                userLocation.getLongitude());

        return geoPoint;
    }


    protected String[] getEventInfo() {

        ParseQuery<MassEvent> eventsQuery = ParseQuery.getQuery("MassEvent");

        eventsQuery.whereNear("location", userLocationPoint);
        eventsQuery.setLimit(Settings.MAX_EVENT_NUMBER);

        ArrayList<String> mNearbyList = new ArrayList<String>();

        try {
            // Use find instead of findInBackground because of a potential thread problem.
            parseObjects = eventsQuery.find();
            for (MassEvent mass : parseObjects) {
                String locationName = mass.getLocationName();
                mNearbyList.add(locationName);
            }
        } catch (ParseException e) {
            Log.d(Settings.APPTAG, e.getMessage());
        }

        String[] listArray = new String[mNearbyList.size()];
        listArray = mNearbyList.toArray(listArray);

        return listArray;
    }

}
