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

/**
 * Created by RuoxiLu on 4/17/15.
 */
public class ParseHandler {

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
        return massUser;

    }

    public static ParseGeoPoint geoPointFromLocation(Location location) {
        return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    public static void updateUserLocation(ParseGeoPoint value, final MassUser mMassUser){
        final ParseGeoPoint geoPointValue = value;
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.whereEqualTo("user", mMassUser.getUser());
        query.getFirstInBackground(new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {

                if (e == null) {
                    final String objectId = massUser.getObjectId();
                    ParseQuery<MassUser> query1 = MassUser.getQuery();
                    query1.getInBackground(objectId, new GetCallback<MassUser>() {
                        @Override
                        public void done(MassUser massUser, ParseException e) {
                            if (e == null) {
                                massUser.setLocation(geoPointValue);
                                massUser.saveInBackground();
                            }

                            Log.d(Settings.APPTAG, "updated mass user location ", e);
                        }
                    });
                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    mMassUser.saveInBackground();
                }
            }
        });
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
        query1.whereEqualTo("objectID", mEventID);

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
                    if (distance > Settings.RADIUS) {
                        // decrement the old event size as the user is no longer there
                        int size = massEvent.getEventSize();
                        size = size - 1;
                        Log.d(Settings.APPTAG, "decrement event size");
                        massEvent.setEventSize(size);
                        massEvent.saveInBackground();

                        ParseQuery<MassUser> query4 = MassUser.getQuery();
                        query4.whereEqualTo("user", mMassUser.getUser());
                        query4.getFirstInBackground(new GetCallback<MassUser>() {
                            @Override
                            public void done(MassUser massUser, ParseException e) {

                                Log.d(Settings.APPTAG, "in UpdateUserEvent done " + e.getMessage());
                                if (e == null) {
                                    massUser.setEvent(null);
                                    massUser.saveInBackground();
                                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    massUser.saveInBackground();
                                }

                            }
                        });

                        // finding objects in "event" near the point given and within the maximum distance given.
                        query2.whereWithinKilometers("location", currentLocation, Settings.RADIUS);

                        // Since the user can only be in one event at a time, use getFirstInBackground
                        query2.getFirstInBackground(new GetCallback<MassEvent>() {
                            @Override
                            public void done(final MassEvent massEvent, ParseException e) {
                                if (e == null) {
                                    Log.i(Settings.APPTAG, "the current massevent is " + massEvent.getObjectId() + "testing for onconnect");
                                    int size = massEvent.getEventSize();
                                    size = size + 1;
                                    massEvent.setEventSize(size);
                                    massEvent.saveInBackground();
                                    ParseQuery<MassUser> query3 = MassUser.getQuery();
                                    query3.whereEqualTo("user", mMassUser.getUser());
                                    query3.getFirstInBackground(new GetCallback<MassUser>() {
                                        @Override
                                        public void done(MassUser massUser, ParseException e) {
                                            massUser.setEvent(massEvent);
                                            massUser.saveInBackground();
                                        }
                                    });
                                } else {
                                    Log.d(Settings.APPTAG, e.getMessage() + "testing for onconnect, query3 callback");
                                }
                            }
                        });
                    }
                } else {

                    // finding objects in "event" near the point given and within the maximum distance given.
                    query2.whereWithinKilometers("location", currentLocation, Settings.RADIUS);

                    // Since the user can only be in one event at a time, use getFirstInBackground
                    query2.getFirstInBackground(new GetCallback<MassEvent>() {
                        @Override
                        public void done(final MassEvent massEvent, ParseException e) {
                            if (e == null) {
                                Log.i(Settings.APPTAG, "the current massevent is " + massEvent.getObjectId() + "testing for onconnect");
                                int size = massEvent.getEventSize();
                                size = size + 1;
                                massEvent.setEventSize(size);
                                massEvent.saveInBackground();
                                ParseQuery<MassUser> query3 = MassUser.getQuery();
                                query3.whereEqualTo("user", mMassUser.getUser());
                                query3.getFirstInBackground(new GetCallback<MassUser>() {
                                    @Override
                                    public void done(MassUser massUser, ParseException e) {
                                        massUser.setEvent(massEvent);
                                        massUser.saveInBackground();
                                    }
                                });
                            } else {
                                Log.d(Settings.APPTAG, e.getMessage() + "testing for onconnect, query3 callback");
                            }
                        }
                    });

                }
            }
        });
    }



    protected static void anonymousUserLogin() {
        ParseUser.enableAutomaticUser();
        ParseUser puser = ParseUser.getCurrentUser();
        String pid = puser.getObjectId();
        if (pid == null) {
            ParseAnonymousUtils.logInInBackground();
        }
    }

}
