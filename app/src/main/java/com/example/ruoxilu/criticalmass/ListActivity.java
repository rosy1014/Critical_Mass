package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.ListView;
import android.content.Intent;

import android.app.Fragment;

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


        eventListAdapter = new ListActivityAdapter(this);

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

        // TODO Depend on whether we assign name to events. For now it uses ObjectId.
        eventListAdapter.setTextKey("objectId");
        // TODO Disable this until new icons are created. A good way to do this is if each user is
        // given a default profile image and an event image is created with users' images.
        // eventListAdapter.setImageKey("icons");


        // Disable paignation.
        eventListAdapter.setPaginationEnabled(false);

        mActivityOne = (ListView) findViewById(R.id.event_list);

        // Bind data from adapter to ListView.
        mActivityOne.setAdapter(eventListAdapter);
        eventListAdapter.loadObjects();

        // Load EventActivity when user clicks on a mass in the list
        mActivityOne.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent eventDetailIntent = new Intent();
                eventDetailIntent.setClass(getApplicationContext(), EventActivity.class);
                eventDetailIntent.putExtra("objectId", id);
                startActivity(eventDetailIntent);
            }
        });
    }


}
