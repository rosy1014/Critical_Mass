package com.example.ruoxilu.criticalmass;

import android.location.Location;
import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashSet;
import java.util.List;

/**
 * Created by RuoxiLu on 4/17/15.
 */
public class ParseHandler {
 //   public static ParseUser mUser = ParseUser.getCurrentUser();
    public static double radius = 1.0;

    public static void deleteMassUser(MassUser user){
        ParseQuery<MassUser> query = MassUser.getQuery();
        final String user_id = user.getUser();
         Log.d(Settings.APPTAG, user_id);
        query.whereEqualTo("user", user_id);
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(final MassUser massUser, ParseException e) {
                if (e == null) {
                    massUser.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(Settings.APPTAG, "Successfully deleted mass user " + user_id);
                            } else {
                                Log.d(Settings.APPTAG, "Failed to delete mass user " + e);
                            }
                        }
                    });
                } else {
                    Log.d(Settings.APPTAG, "Failed to find the current mass user");
                }
            }
        });
    }


    public static MassUser getDefaultMassUser(){

        if(ParseUser.getCurrentUser() == null){
            anonymousUserLogin();
        }
        MassUser massUser = new MassUser();
        massUser.setUser(ParseUser.getCurrentUser());
        massUser.setLocation(geoPointFromLocation(Settings.getDefaultLocation()));
        massUser.saveInBackground();
        return massUser;

    }

    public static ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        // Log.i(Settings.APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
    }


    public static void updateUserLocation(ParseGeoPoint value, MassUser mMassUser) {
        final ParseGeoPoint geoPointValue = value; // need "final" type to pass in the callback function
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user", mMassUser.getUser());
        Log.d(Settings.APPTAG, "in UpdateUserLocation, user is " + mMassUser.getUser());
        Log.d(Settings.APPTAG, "in UpdateUserLocation, user is " + geoPointValue);
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                Log.d(Settings.APPTAG, "Done with getFirstInBackground loc " + e);

                if (e == null) {
                    // no error exception, the user is found in the cloud, update the location in the cloud
                    massUser.setLocation(geoPointValue);
                    massUser.saveInBackground(new SaveCallback() {

                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(Settings.APPTAG, "MassUser update saved successfully");
                            } else {
                                Log.d(Settings.APPTAG, "MassUser update were not saved");
                            }
                        }
                    });
                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // The user has not been saved into the cloud, save it with current location
                    massUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(Settings.APPTAG, "New MassUser saved successfully");
                            } else {
                                Log.d(Settings.APPTAG, "New MassUser were not saved");
                            }
                        }
                    });
                } else {
                    // Do nothing
                }
            }
        });
        return;
    }

    // helper function to update the user's event by event type data(Xin)
    // pass in the current location
    public static void updateUserEvent(ParseGeoPoint value, final MassUser mMassUser) {
        // Find by ID the user's last event
        String mEventID = mMassUser.getEvent();

        // the user current location
        final ParseGeoPoint currentLocation = value;

        //This is the first query to validate the user's last event
        final ParseQuery<MassEvent> query1 = MassEvent.getQuery();

        // This is the second query to find the user's new event
        final ParseQuery<MassEvent> query2 = MassEvent.getQuery();

        // check if the user's old event exists
        query1.whereEqualTo("event", mEventID);

        Log.i(Settings.APPTAG, "Mass User in updateUserEvent is " + mEventID);

        query1.getFirstInBackground(new GetCallback<MassEvent>() {
            @Override
            public void done(MassEvent massEvent, ParseException e) {
                Log.i(Settings.APPTAG, "Done with getFirstInBackground loc " + e);
                // the event ID is found
                if (e == null) {
                    Log.i(Settings.APPTAG, "massevent in updateUserLocation after query is " + massEvent.getObjectId());

                    // check if the user is still within the event radius
                    double distance = currentLocation
                            .distanceInKilometersTo(massEvent.getLocation());
                    // the user is no longer inside the old event
                    if (distance > massEvent.getRadius()) {
                        // decrement the old event size as the user is no longer there
                        int size = massEvent.getEventSize();
                        size = size - 1;
                        Log.d(Settings.APPTAG, "decrement event size");
                        massEvent.setEventSize(size);
                        massEvent.saveInBackground();

                        // search for new event, if any,  that includes the user
                        double maxDistance = 5;

                        // finding objects in "event" near the point given and within the maximum distance given.
                        query2.whereWithinKilometers("location", currentLocation, maxDistance);

                        // Since the user can only be in one event at a time, use getFirstInBackground
                        query2.getFirstInBackground(new GetCallback<MassEvent>() {
                            @Override
                            public void done(MassEvent massEvent, ParseException e) {
                                if (e == null) {
                                    Log.i(Settings.APPTAG, "the current massevent is " + massEvent.getObjectId());
                                    int size = massEvent.getEventSize();
                                    size = size + 1;
                                    massEvent.setEventSize(size);
                                    massEvent.saveInBackground();
                                    mMassUser.setEvent(massEvent);
                                    mMassUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Log.d(Settings.APPTAG, "update user event error: " + e);
                                        }
                                    });
                                } else {
                                    // No new event found
                                    Log.i(Settings.APPTAG, "new event not found ");
                                }
                            }
                        });
                        return;
                    }
                }
            }
        });
    }

    public static void queryNearbyEvent(Location location) {
        Log.d(Settings.APPTAG, "in queryNearbyEvent, " + location);
        if (location == null) {
            Log.d(Settings.APPTAG, "No events found");
        } else {
            final ParseGeoPoint myPoint = ParseHandler.geoPointFromLocation(location);
            //final HashMap<String, MarkerOptions> toKeep = new HashMap<String, MarkerOptions>();
            ParseQuery<MassEvent> mapQuery = MassEvent.getQuery();
            mapQuery.whereWithinKilometers("location", myPoint, Settings.SEARCH_DISTANCE);
            // 5
            mapQuery.orderByDescending("EventSize");
            // mapQuery.setLimit(MAX_MARKER_SEARCH_RESULTS);

            final HashSet<MassEvent> nearbyEvents;
            mapQuery.findInBackground(new FindCallback<MassEvent>() {
                @Override
                public void done(List<MassEvent> massEvents, ParseException e) {
                    HashSet<MassEvent> events = new HashSet<MassEvent>();
                    HashSet<String> eventIds = new HashSet<String>();
                    for (MassEvent event: massEvents){
                        events.add(event);
                        Log.d(Settings.APPTAG,"in nearbyEvents " + event.getLocationName());
                    }
                    MapsActivity.updateMarkers(events);
//                    MapsActivity.cleanUpMarkers(eventIds);
                }
            });
//            nearbyEvents.addAll(eventList);
//            Log.d(Settings.APPTAG, "in nearbyEvents, number of events " + nearbyEvents.size() );
//            return nearbyEvents;
        }
    }


    protected static void anonymousUserLogin() {
        ParseUser.enableAutomaticUser();
        Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is null? " + ParseUser.getCurrentUser().getObjectId());

        ParseUser puser = ParseUser.getCurrentUser();
        String pid = puser.getObjectId();
        Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is " + pid);
        if (pid == null) {
//            Log.d(Settings.APPTAG, " In anonymousUserLogin, in if!!!!");
            ParseAnonymousUtils.logInInBackground();
            Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is " + ParseUser.getCurrentUser().getObjectId());

        }
        Log.d(Settings.APPTAG, " In anonymousUserLogin, ParseUser is " + ParseUser.getCurrentUser().getObjectId());
    }


}
