package com.example.ruoxilu.criticalmass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.util.Log;

import java.lang.Integer;
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
 * Created by angeloliao on 3/28/15.
 */
public class ListActivityAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] values;
    private Integer[] sizes;
    private com.parse.ParseFile[] eventIconsArray;

    public ListActivityAdapter(Context context, String[] values, Integer[] sizes,
                               com.parse.ParseFile[] eventIconsArray) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
        this.sizes = sizes;
        this.eventIconsArray = eventIconsArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.title_text);

        TextView sizeTextView = (TextView) rowView.findViewById(R.id.event_size);
        ParseImageView eventIconView = (ParseImageView) rowView.findViewById(R.id.activity_image);

        titleTextView.setText(values[position]);
        eventIconView.setParseFile(eventIconsArray[position]);
        eventIconView.setPlaceholder(context.getResources().getDrawable(R.drawable.giraffe));
        eventIconView.loadInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, com.parse.ParseException e) {
                Log.d(Settings.APPTAG,
                        "Fetched image");
            }
        });

        sizeTextView.setText(String.valueOf(sizes[position]));

        return rowView;
    }

}