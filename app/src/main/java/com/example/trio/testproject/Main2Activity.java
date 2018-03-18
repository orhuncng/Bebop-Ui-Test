package com.example.trio.testproject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

public class Main2Activity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Intent listIntent = getIntent();
            ArrayList<String> listMobile;
            listMobile = listIntent.getStringArrayListExtra(MainActivity.TESTMSG);

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, listMobile);

            ListView listView = (ListView) findViewById(R.id.mobile_list);
            listView.setAdapter(adapter);
        }

}
