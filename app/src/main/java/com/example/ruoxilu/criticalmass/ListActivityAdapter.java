package com.example.ruoxilu.criticalmass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.util.Log;

import java.lang.Integer;

/**
 * Created by angeloliao on 3/28/15.
 */
public class ListActivityAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] values;
    private Integer[] sizes;

    public ListActivityAdapter(Context context, String[] values, Integer[] sizes) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
        this.sizes = sizes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView titleTextView = (TextView) rowView.findViewById(R.id.title_text);
        TextView sizeTextView = (TextView) rowView.findViewById(R.id.event_size);

        titleTextView.setText(values[position]);

        sizeTextView.setText(String.valueOf(sizes[position]));

        return rowView;
    }

}