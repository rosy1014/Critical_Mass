package com.example.ruoxilu.criticalmass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


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