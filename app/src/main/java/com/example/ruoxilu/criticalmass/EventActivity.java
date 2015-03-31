package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.content.Intent;

import android.app.Fragment;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by tingyu on 2/26/15.
 */
public class EventActivity extends Activity {

    String eventObjectId;
    int eventSize;

    TextView mTitleTextView;
    TextView mEventSizeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);

        Bundle extras = getIntent().getExtras();
        eventObjectId = extras.getString("objectId");

        mTitleTextView = (TextView) findViewById(R.id.activity_name);
        mTitleTextView.setText(eventObjectId);

        mEventSizeView = (TextView) findViewById(R.id.event_size);

        ParseQuery<ParseObject> queryMassEvent = ParseQuery.getQuery("MassEvent");
        queryMassEvent.getInBackground(eventObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    eventSize = object.getInt("EventSize");
                    mEventSizeView.setText(eventSize);
                }
            }
        });

    }

    // If the activity is resumed


}
