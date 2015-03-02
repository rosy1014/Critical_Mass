package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by tingyu on 2/23/15.
 */
public class ListActivity extends Activity {

    LinearLayout mActivityOne;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mActivityOne = (LinearLayout)findViewById(R.id.activity_1);
        mActivityOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ListActivity.this, EventActivity.class);
                startActivityForResult(i, 0);
            }
        });


    }

}
