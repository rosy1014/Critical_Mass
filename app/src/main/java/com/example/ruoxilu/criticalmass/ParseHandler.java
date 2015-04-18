package com.example.ruoxilu.criticalmass;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by RuoxiLu on 4/17/15.
 */
public class ParseHandler {
    public static ParseUser mUser = ParseUser.getCurrentUser();

    public static void deleteMassUser(MassUser user){
        ParseQuery<MassUser> query = MassUser.getQuery();
        final String user_id = user.getUser();
//        Log.d(Settings.APPTAG, obj_id);
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



}
