package com.example.ruoxilu.criticalmass;

import java.util.List;

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
public class ListActivityAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] values;

    public ListActivityAdapter(Context context, String[] values) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.title_text);

        // TODO: add event size
        // TextView eventSizeTextView = (TextView) rowView.findViewById(R.id.event_size);
        titleTextView.setText(values[position]);

        // TODO: different background color for different row
//        if (position % 2 == 0) {
//
//        }
//        else {
//
//        }
        return rowView;
    }

}