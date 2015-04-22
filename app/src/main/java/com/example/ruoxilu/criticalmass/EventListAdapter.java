package com.example.ruoxilu.criticalmass;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by tingyu on 4/21/15.
 */
public class EventListAdapter extends ParseQueryAdapter<MassEvent> {

    public EventListAdapter(Context c, final ParseGeoPoint userLocationPoint) {

        super(c, new ParseQueryAdapter.QueryFactory<MassEvent>() {
            public ParseQuery<MassEvent> create() {
                ParseQuery queryMassEvent = new ParseQuery("MassEvent");
                queryMassEvent.whereNear("location", userLocationPoint);
                queryMassEvent.setLimit(Settings.MAX_EVENT_NUMBER);
                return queryMassEvent;
            }
        });


    }

    @Override
    public View getItemView(MassEvent massEvent,
                            View v,
                            ViewGroup parent) {

        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_item, null);
        }

        super.getItemView(massEvent, v, parent);

        // Set event image
        ParseImageView eventImageView = (ParseImageView) v.findViewById(R.id.event_image);
        ParseFile eventImage = massEvent.getEventImage();

        if (eventImage != null) {
            eventImageView.setParseFile(eventImage);
            eventImageView.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    // nothing to do
                }
            });
        }

        // Set event name
        TextView eventName = (TextView) v.findViewById(R.id.event_name);
        eventName.setText(massEvent.getLocationName());

        // Set event size
        TextView eventSize = (TextView) v.findViewById(R.id.event_size);
        eventSize.setText(massEvent.getEventSize());

        return v;

    }


}