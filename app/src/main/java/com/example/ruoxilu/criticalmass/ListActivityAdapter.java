package com.example.ruoxilu.criticalmass;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.AdapterView.OnItemClickListener;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.ListView;
import android.widget.Toolbar;
import android.content.Context;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseGeoPoint;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.parse.ParseFile;

import java.text.ParseException;



/**
 * Created by angeloliao on 3/28/15.
 */
public class ListActivityAdapter extends ParseQueryAdapter<ParseObject> {

    public ListActivityAdapter(Context context) {
        // Use the QueryFactory to create a customized query adapter, which only displays nearby
        // 10 events.
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery create() {
                        Location userCurrentLocation = MapsActivity.mCurrentLocation;
                        Location userLastLocation = MapsActivity.mLastLocation;

                        if (userCurrentLocation == null) {
                            Log.i(Application.APPTAG, "the current location is null");
                            userCurrentLocation = userLastLocation;
                        }

                        ParseQuery queryNearbyEvents = new ParseQuery("MassEvent");
                        queryNearbyEvents.whereNear("location",
                                geoPointFromLocation(userCurrentLocation));

                        queryNearbyEvents.setLimit(10);

                        queryNearbyEvents.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                for (ParseObject mass : parseObjects) {
                                    String eventObjectId = mass.getObjectId();
                                    int eventSize = mass.getInt("EventSize");
                                    Log.d(Application.APPTAG, "ListActivityAdapter, create(), event object id is "+eventObjectId);
                                    Log.d(Application.APPTAG, "ListActivityAdapter, create(), event size is "+eventSize);
                                }
                            }
                        });


                        ParseQuery queryEvents = new ParseQuery("MassEvent");
//                        queryEvents.whereNear("location",
//                                geoPointFromLocation(userCurrentLocation));
//
//                        queryEvents.setLimit(10);

                        return queryEvents;
                    }
                }
        );
    }

    // Customize the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_item, null);
        }

        super.getItemView(object, v, parent);

        TextView titleTextView = (TextView) v.findViewById(R.id.title_text);
        String eventObjectId = object.getString("objectId");
        Log.d (Application.APPTAG, "getItemView, the object id is "+ eventObjectId);

//        String userObjectId = ParseUser.getCurrentUser().toString();
//        Log.d(Application.APPTAG, "ListActivityAdapter, getItemView, user object id is "+userObjectId);

        titleTextView.setText(object.getString("objectId"));

//        titleTextView.setText("iED9ctulkS");

//        TextView eventSizeTextView = (TextView) v.findViewById(R.id.event_size);
//        eventSizeTextView.setText(object.getString("EventSize"));
        ParseImageView giraffeView = (ParseImageView) v.findViewById(R.id.activity_image);
        ParseFile imageFile = object.getParseFile("image");


        return v;
    }

    // TODO: handle the edge case when nearby events have been changed
    // eventListAdapter.notifyDataSetChanged();
//    @Override
//    public notifyDataSetChanged() {
//
//    }

    // The "static" keyword was added so that the constructor can call this function.
    private static ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        Log.i(Application.APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }
}
