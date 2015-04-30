package com.example.ruoxilu.criticalmass;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by SEAN on 3/6/15.
 */
@ParseClassName("MassEvent")
public class MassEvent extends ParseObject {

    // The constructor is currently not used
    public MassEvent() {

    }

    public static ParseQuery<MassEvent> getQuery() {
        return ParseQuery.getQuery(MassEvent.class);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public int getEventSize() {
        return getInt("EventSize");
    }

    public void setEventSize(int eventSize) {
        put("EventSize", eventSize);
    }

    public String getLocationName() {
        return getString("locationName");
    }

    public ParseFile getEventImage() {
        return getParseFile("image");
    }

}
