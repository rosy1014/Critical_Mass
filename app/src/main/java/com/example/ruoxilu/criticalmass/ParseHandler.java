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
 //   public static ParseUser mUser = ParseUser.getCurrentUser();

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

    private static ParseGeoPoint geoPointFromLocation(Location location) {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        // Log.i(Settings.APPTAG, "geoPoint is " + geoPoint);
        return geoPoint;
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
