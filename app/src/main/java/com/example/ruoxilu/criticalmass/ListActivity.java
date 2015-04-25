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
import android.widget.ListView;

import com.parse.ParseGeoPoint;

/**
 * Created by tingyu on 2/23/15.
 * The ListActivity class displays a list of masses nearby. ListActivity fetches data from parse
 * through the ParseQueryAdapter and bind it to the ListView.
 */
public class ListActivity extends Activity {

    private EventListAdapter mEventListAdapter;
    private SwipeRefreshLayout mScrollList;
    private ListView mEventListView;

    private ParseGeoPoint mLocationPoint;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Application.networkConnected(this)) {

            setContentView(R.layout.activity_list);
            mLocationPoint = getLocationPoint();
            mEventListView = (ListView) findViewById(R.id.event_list);
            initEventInfo();

            mScrollList = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            mScrollList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshEventInfo();
                }
            });

            // Load EventActivity when user clicks on a mass in the
            mEventListView.setOnItemClickListener(
                    new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            MassEvent event = mEventListAdapter.getItem(position);

                            String locationName = event.getLocationName();

                            Intent eventDetailIntent = new Intent();
                            eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);

                            String eventId = event.getObjectId();
                            eventDetailIntent.putExtra("objectId", eventId);
                            eventDetailIntent.putExtra("location", locationName);

                            Log.d(Settings.APPTAG, "event object id is " + id);

                            startActivity(eventDetailIntent);
                        }
                    }

            );
        }
    }

    private void refreshEventInfo() {

        // Get updated user location
        mLocationPoint = getLocationPoint();
        initEventInfo();

        // Reset the ParseQueryAapter using the new location
        mScrollList.setRefreshing(false);

    }

    protected ParseGeoPoint getLocationPoint() {

        Location userLocation;


        if (MapsActivity.mCurrentLocation != null) {
            userLocation = MapsActivity.mCurrentLocation;

        } else if (MapsActivity.mLastLocation != null) {
            userLocation = MapsActivity.mLastLocation;

        } else {
            userLocation = Settings.getDefaultLocation();

        }

        return new ParseGeoPoint(userLocation.getLatitude(),
                userLocation.getLongitude());

    }


    protected void initEventInfo() {

        // Bind data from adapter to ListView
        mEventListAdapter = new EventListAdapter(this, mLocationPoint);
        mEventListView.setAdapter(mEventListAdapter);

    }

}
