package com.example.ruoxilu.criticalmass;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.parse.GetDataCallback;
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
 * Created by angeloliao on 4/16/15.
 */
public class EventActivityAdapter extends ArrayAdapter<String> {

    private Context context;
    private String eventId;
    private ArrayList<String> comments;
    private ArrayList<String> username;

    public EventActivityAdapter(Context context, ArrayList<String> comments,
                                ArrayList<String> username) {

        super(context, R.layout.list_item);
        this.context = context;
        this.eventId = eventId;

    }

//    private void getComments(String eventId) {
//        ParseQuery queryEventComment = new ParseQuery("EventComment");
//        queryEventComment.whereEqualTo("EventId", eventId);
//        comments = new ArrayList<String>();
//
//        List<ParseObject> parseObjects;
//
//        try {
//            // Use find instead of findInBackground because of a potential thread problem.
//            parseObjects = queryEventComment.find();
//
//            for (ParseObject commentInfo : parseObjects) {
//                comments.add(commentInfo.getString("UserComment"));
//                username.add(commentInfo.getString("username"));
//            }
//
//        } catch (com.parse.ParseException e) {
//            Log.d(Application.APPTAG, e.getMessage());
//        }
//
//    }
}


// If a username is more than or equal to 24 characters, change the username.