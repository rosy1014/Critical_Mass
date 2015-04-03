package com.example.ruoxilu.criticalmass;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(MassUser.class);
        ParseObject.registerSubclass(MassEvent.class);

        Parse.initialize(this, "ADIzf9tA1P4KQFL1AyyAKoCjLKhgaCmaZTmp96CL", "PcefekoiDoE3uR2yUd932HRbPPqrEGJyaE61aPVF");
        ParseUser.enableAutomaticUser();
        Log.d("CriticalMassApplication", "On start the currentuser is "+ ParseUser.getCurrentUser());
        ParseUser.getCurrentUser().saveInBackground();

        ParseACL defaultACL = new ParseACL();

        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);

        // allows read and write access to all users
        ParseACL postACL = new ParseACL(ParseUser.getCurrentUser());
        postACL.setPublicReadAccess(true);
        postACL.setPublicWriteAccess(true);
        Log.d("CriticalMassApplication",  " In anonymousUserLogin, ParseUser is null?"+ ParseUser.getCurrentUser().getObjectId());
        Log.d("CriticalMassApplication",  " In anonymousUserLogin, ParseUser is null?"+ ParseUser.getCurrentUser().getCreatedAt());
        Log.d("CriticalMassApplication",  " In anonymousUserLogin, ParseUser is null?"+ ParseUser.getCurrentUser().getUsername());

    }
}

