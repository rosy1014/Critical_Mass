package com.example.ruoxilu.criticalmass;

import android.location.Location;
import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by RuoxiLu on 4/17/15.
 */
public class ParseHandler {

    public static void deleteMassUser(MassUser user){
        ParseQuery<MassUser> massUserQuery = MassUser.getQuery();
        ParseQuery<MassEvent> massEventQuery = MassEvent.getQuery();
        final String objId = user.getObjectId();
        final String eventId = user.getEvent();

        massUserQuery.getInBackground(objId, new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                if (e == null) {
                    massUser.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(Settings.PARSEHANDLER, "Successfully deleted mass user " + objId);
                            } else {
                                Log.d(Settings.PARSEHANDLER, "Failed to delete mass user " + e);
                            }
                        }
                    });
                } else {
                    Log.d(Settings.PARSEHANDLER, "Failed to find the current mass user");
                }
            }
        });
        if(eventId != null){
            massEventQuery.getInBackground(eventId, new GetCallback<MassEvent>() {
                @Override
                public void done(MassEvent massEvent, ParseException e) {
                    Log.d(Settings.PARSEHANDLER, "in get in background mass event, error is: " + e);
                    if(e == null){
                        int size = massEvent.getEventSize();
                        size = size - 1;
                        massEvent.setEventSize(size);
                        massEvent.saveInBackground();
                    }
                }
            });
        }
    }


    public static MassUser getDefaultMassUser(){

        if(ParseUser.getCurrentUser() == null){
            anonymousUserLogin();
        }
        MassUser massUser = new MassUser();
        massUser.setUser(ParseUser.getCurrentUser());
        massUser.setLocation(geoPointFromLocation(Settings.getDefaultLocation()));
        massUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d(Settings.APPTAG, "in save in background for getdefault mass user, error is: " + e);
            }
        });
        return massUser;

    }

    public static ParseGeoPoint geoPointFromLocation(Location location) {
        return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    public static void updateUserLocation(ParseGeoPoint value, MassUser mMassUser){
//        final ParseGeoPoint geoPointValue = value;
//        final String massUserId = mMassUser.getObjectId();
        mMassUser.setLocation(value);
        mMassUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.d(Settings.PARSEHANDLER, "Failed to update user location, error is:  " + e);
                } else {
                    Log.d(Settings.PARSEHANDLER, "Successfully updated user's location");
                }
            }
        });
//        final MassUser massUser = mMassUser;
//        ParseQuery<MassUser> query = MassUser.getQuery();
//        query.getInBackground(massUserId, new GetCallback<MassUser>() {
//            @Override
//            public void done(MassUser massUser, ParseException e) {
//
//                if (e == null) {
//                    massUser.setLocation(geoPointValue);
//                    massUser.saveInBackground();
//                    Log.d(Settings.PARSEHANDLER, "updated mass user location ", e);
//                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
//                    Log.d(Settings.PARSEHANDLER, "mass user is not found in database");
//                }
//            }
//        });
    }

    // helper function to update the user's event by event type data
    // pass in the current location
    public static void updateUserEvent(ParseGeoPoint value, MassUser mMassUser) {
        // Find by ID the user's last event
        String EventId = mMassUser.getEvent();

        final String massUserId = mMassUser.getObjectId();

        // the user current location
        final ParseGeoPoint currentLocation = value;

        //This is the first query to validate the user's last event
        ParseQuery<MassEvent> eventQuery= MassEvent.getQuery();

        // check if the user's old event exists

        Log.i(Settings.PARSEHANDLER, "Mass Event in updateUserEvent is " + EventId);

        // The user was previously in a Mass Event, update the size of the event first,
        // then check if the user has moved to a new mass event.
        if(EventId != null){
            eventQuery.getInBackground(EventId, new GetCallback<MassEvent>() {
                @Override
                public void done(MassEvent massEvent, ParseException e) {
                    Log.i(Settings.PARSEHANDLER, "Done with getFirstInBackground loc " + e);
                    // the event ID is found
                    if (e == null) {
                        Log.i(Settings.PARSEHANDLER, "massevent in updateUserLocation after query is " + massEvent.getObjectId());
                        // check if the user is still within the event radius
                        double distance = currentLocation
                                .distanceInKilometersTo(massEvent.getLocation());
                        // the user is no longer inside the old event
                        if (distance > Settings.RADIUS) {
                            // decrement the old event size as the user is no longer there
                            int size = massEvent.getEventSize();
                            size = size - 1;
                            Log.d(Settings.PARSEHANDLER, "decrement event size");
                            massEvent.setEventSize(size);
                            massEvent.saveInBackground();

                            ParseQuery<MassUser> massUserQuery = MassUser.getQuery();
                            massUserQuery.getInBackground(massUserId, new GetCallback<MassUser>() {
                                @Override
                                public void done(MassUser massUser, ParseException e) {

                                    Log.d(Settings.PARSEHANDLER, "in UpdateUserEvent done " + e.getMessage());
                                    if (e == null) {
                                        massUser.setEvent(null);
                                        massUser.saveInBackground();
                                    } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                        massUser.saveInBackground();
                                    }
                                }
                            });

                            ParseQuery newEventQuery = MassEvent.getQuery();

                            // finding objects in "event" near the point given and within the maximum distance given.
                            newEventQuery.whereWithinKilometers("location", currentLocation, Settings.RADIUS);

                            // Since the user can only be in one event at a time, use getFirstInBackground
                            newEventQuery.getFirstInBackground(new GetCallback<MassEvent>() {
                                @Override
                                public void done(final MassEvent massEvent, ParseException e) {
                                    if (e == null) {
                                        Log.i(Settings.PARSEHANDLER, "the current massevent is " + massEvent.getObjectId() + "testing for onconnect");
                                        int size = massEvent.getEventSize();
                                        size = size + 1;
                                        massEvent.setEventSize(size);
                                        massEvent.saveInBackground();
                                        ParseQuery<MassUser> massUserQuery = MassUser.getQuery();
                                        massUserQuery.getInBackground(massUserId, new GetCallback<MassUser>() {
                                            @Override
                                            public void done(MassUser massUser, ParseException e) {
                                                massUser.setEvent(massEvent);
                                                massUser.saveInBackground();
                                                Log.d(Settings.PARSEHANDLER, "after save in query 2");
                                            }
                                        });
                                    } else {
                                        Log.d(Settings.PARSEHANDLER, e.getMessage() + "testing for onconnect, query3 callback");
                                    }
                                }
                            });
                        }
                    } else {

                    }
                }
            });
        } else {
            // The user was not in a Mass Event, go ahead and check if the user is now
            // in a Mass Event
            ParseQuery newEventQuery = MassEvent.getQuery();

            // finding objects in "event" near the point given and within the maximum distance given.
            newEventQuery.whereWithinKilometers("location", currentLocation, Settings.RADIUS);

            // Since the user can only be in one event at a time, use getFirstInBackground
            newEventQuery.getFirstInBackground(new GetCallback<MassEvent>() {
                @Override
                public void done(final MassEvent massEvent, ParseException e) {
                    if (e == null) {
                        Log.i(Settings.PARSEHANDLER, "the current massevent is " + massEvent.getObjectId() + "testing for onconnect");
                        int size = massEvent.getEventSize();
                        size = size + 1;
                        massEvent.setEventSize(size);
                        massEvent.saveInBackground();
                        ParseQuery<MassUser> massUserQuery = MassUser.getQuery();
                        massUserQuery.getInBackground(massUserId, new GetCallback<MassUser>() {
                            @Override
                            public void done(MassUser massUser, ParseException e) {
                                Log.d(Settings.PARSEHANDLER, "after save in query 2," + e);
                                if(e == null){
                                    massUser.setEvent(massEvent);
                                    massUser.saveInBackground();
                                    Log.d(Settings.PARSEHANDLER, "after save in query 2");
                                }
                            }
                        });
                    } else {
                        Log.d(Settings.PARSEHANDLER, e.getMessage() + "testing for onconnect, query3 callback");
                    }
                }
            });

        }

    }



    protected static void anonymousUserLogin() {
        ParseUser.enableAutomaticUser();
        if (ParseUser.getCurrentUser().getObjectId() == null) {
            ParseAnonymousUtils.logInInBackground();
        }
    }

}
