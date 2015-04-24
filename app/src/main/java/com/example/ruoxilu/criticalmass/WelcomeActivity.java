package com.example.ruoxilu.criticalmass;

/**
 * Created by SEAN on 4/14/15.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {

    // Declare Variable
    Button GoToApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from welcome.xml
        setContentView(R.layout.welcome);

        // Locate TextView in welcome.xml
        TextView content = (TextView) findViewById(R.id.content);

        // Set the tutorial content into TextView
        content.setText("The map displays your current location as well as nearby critical mass. " +
                "You can click on the marker or the top middle button to view event details. " +
                "Press top left button to log out.");
        content.setTextSize(30);

        GoToApp = (Button) findViewById(R.id.gotoapp);

        // Gotoapp Button Click Listener
        GoToApp.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // direct to main map view
                Intent intent = new Intent(
                        WelcomeActivity.this,
                        MapsActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),
                        "You have successfully entered the app!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
