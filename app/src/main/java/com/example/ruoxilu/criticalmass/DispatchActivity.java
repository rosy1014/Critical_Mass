package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;

/**
 * Created by RuoxiLu on 4/8/15.
 */
public class DispatchActivity extends Activity {
    public DispatchActivity(){

    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(ParseUser.getCurrentUser()!=null){
            Log.d(Settings.APPTAG, "Zoom Issue: Dispatch Activity intent: MapsActivity");

            startActivity(new Intent(this, MapsActivity.class));
        }
        else {

            Log.d(Settings.APPTAG, "Zoom Issue: Dispatch Activity intent: LoginSignupActivity");
            startActivity(new Intent(this, LoginSignupActivity.class));
        }

    }
}
