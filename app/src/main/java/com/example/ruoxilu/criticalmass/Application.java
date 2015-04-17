package com.example.ruoxilu.criticalmass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
        Log.d("CriticalMassApplication", "On start the currentuser is " + ParseUser.getCurrentUser());
        ParseUser.getCurrentUser().saveInBackground();

        setParseACL();
    }

    public void setParseACL() {

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

    // Return true if network is connected.
    public static boolean networkConnected(Context c) {
        ConnectivityManager conManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conManager.getActiveNetworkInfo();

        boolean networkConnected = netInfo != null && netInfo.isConnected();

        if (!networkConnected) {
            internetAlert(c);
        }

        return networkConnected;
    }

    // If network is not connected, alert user with an alert dialog.
    public static void internetAlert (Context c) {

        final Context specificContext = c;

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Error: No Internet connection.");
        builder.setMessage("Sorry, it seems like no internet connection is available. \n" +
                "Please turn off airplane mode or turn on Wi-Fi/Celluar connection in Settings.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.setNegativeButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                specificContext.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS), null);
            }
        });

        AlertDialog internetAlertDialog = builder.create();

        internetAlertDialog.show();
    }
}

