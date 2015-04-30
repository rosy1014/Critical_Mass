package com.example.ruoxilu.criticalmass;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Application extends android.app.Application {


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
    public static void internetAlert(Context c) {

        final Context specificContext = c;

        new SweetAlertDialog(c, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("No internet connection available. Please check your internet settings.")
                .setConfirmText("Settings")
                .setCancelText("Cancel")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        specificContext.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS), null);
                    }
                })
                .show();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(MassUser.class);
        ParseObject.registerSubclass(MassEvent.class);
        ParseObject.registerSubclass(Comment.class);

        Parse.initialize(this, "ADIzf9tA1P4KQFL1AyyAKoCjLKhgaCmaZTmp96CL", "PcefekoiDoE3uR2yUd932HRbPPqrEGJyaE61aPVF");
        ParseUser.enableAutomaticUser();
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

    }

}

