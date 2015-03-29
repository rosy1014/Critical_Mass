package com.example.ruoxilu.criticalmass;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.AdapterView.OnItemClickListener;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.ListView;
import android.widget.Toolbar;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseGeoPoint;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import java.text.ParseException;

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

        // TODO Depend on whether we assign name to events.
        // eventListAdapter.setTextKey("name");
        // TODO Disable this until new icons are created
        // eventListAdapter.setImageKey("icons");

        // Disable paignation.
        eventListAdapter.setPaginationEnabled(false);

        mActivityOne = (ListView) findViewById(R.id.simple_list_view);

        // Bind data from adapter to ListView.
        mActivityOne.setAdapter(eventListAdapter);
        eventListAdapter.loadObjects();

        // Load EventActivity when user clicks on a mass in the list
        mActivityOne.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
            }
        });
    }



}
