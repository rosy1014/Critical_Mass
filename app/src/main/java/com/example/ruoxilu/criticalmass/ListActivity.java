package com.example.ruoxilu.criticalmass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by tingyu on 2/23/15.
 */
public class ListActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    Button mButton;
    EditText mEditText;
    ListView mListView;
    ArrayAdapter mArrayAdapter;
    ArrayList mEventlist = new ArrayList();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mButton = (Button) findViewById(R.id.create_button);
        mButton.setOnClickListener(this);

        mEditText = (EditText) findViewById(R.id.add_event_text);

        mListView = (ListView) findViewById(R.id.event_listview);

        mArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mEventlist);

        mListView.setAdapter(mArrayAdapter);

        mListView.setOnItemClickListener(this);


    }

    @Override
    public void onClick(View v) {

        mEventlist.add(mEditText.getText().toString());
        mArrayAdapter.notifyDataSetChanged();

        // After a mass event is created, set EditText to empty
        // so that the user does not have to delete the text manually
        mEditText.setText(R.string.empty);

        // Notify the user that the event has been created successfully
        Toast.makeText(this,
                R.string.creation_toast,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        // When a mass event is clicked, open the event activity
        // Currently the event activity is static

        Intent i = new Intent(ListActivity.this, EventActivity.class);
        startActivityForResult(i, 0);
    }

}
