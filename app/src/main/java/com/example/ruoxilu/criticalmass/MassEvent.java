package com.example.ruoxilu.criticalmass;

import com.parse.ParseClassName;
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

    // Every event is associated with an event ID
    public String getEvent() {
        return getString("event");
    }

    // event field takes ObjectId of Parse Object
    public void setUser(ParseObject value) {
        put("event", value.getObjectId());
    }

    // retrieve the event radius
    public int getRadius() {
        return getInt("radius");
    }

    public void setRadius(int value) {
        put("radius", value);
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

    public String getEventName() {
        return getString("locationName");
    }

    public void setEventName(String newName) {
        put("locationName", newName);
    }
}
