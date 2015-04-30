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
        massUser.saveInBackground();
        return massUser;

    }

    public static ParseGeoPoint geoPointFromLocation(Location location) {
        return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    public static void updateUserLocation(ParseGeoPoint value, MassUser mMassUser){

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
    }

    // helper function to update the user's event by event type data
    // pass in the current location
    public static void updateUserEvent(ParseGeoPoint value, final MassUser mMassUser) {
        // Find by ID the user's last event
        String EventId = mMassUser.getEvent();

        final String massUserId = mMassUser.getObjectId();

        // the user current location
        final ParseGeoPoint currentLocation = value;


        ParseQuery newEventQuery = MassEvent.getQuery();

        // finding objects in "event" near the point given and within the maximum distance given.
        newEventQuery.whereWithinKilometers("location", currentLocation, Settings.RADIUS);

        // Since the user can only be in one event at a time, use getFirstInBackground
        newEventQuery.getFirstInBackground(new GetCallback<MassEvent>() {
            @Override
            public void done(final MassEvent massEvent, ParseException e) {
                if (e == null) {

                    ParseQuery<MassUser> massUserQuery = MassUser.getQuery();
                    massUserQuery.getInBackground(massUserId, new GetCallback<MassUser>() {
                        @Override
                        public void done(MassUser massUser, ParseException e) {

                            if (e == null) {
                                massUser.setEvent(massEvent);
                                massUser.saveInBackground();
                                Log.d(Settings.PARSEHANDLER, "after save in query 2");
                            } else {
                                mMassUser.saveInBackground();
                            }

                        }
                    });
                } else {
                    Log.d(Settings.PARSEHANDLER, e.getMessage() + "testing for onconnect, query3 callback");
                }
            }
        });

    }

    /**
     * Check whether the mass user is already stored in the database.
     * @param user
     */
    public static void checkMassUser(final MassUser user){
        ParseQuery<MassUser> query = MassUser.getQuery();
        query.getInBackground(user.getObjectId(),new GetCallback<MassUser>() {
            @Override
            public void done(MassUser massUser, ParseException e) {
                if(e.getCode() == ParseException.OBJECT_NOT_FOUND){
                    user.saveInBackground();
                }
            }
        });
    }



    protected static void anonymousUserLogin() {
        ParseUser.enableAutomaticUser();
        if (ParseUser.getCurrentUser().getObjectId() == null) {
            ParseAnonymousUtils.logInInBackground();
        }
    }

}
