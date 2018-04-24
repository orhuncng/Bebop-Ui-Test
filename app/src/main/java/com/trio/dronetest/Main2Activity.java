package com.trio.dronetest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.trio.testproject.R;

import java.util.ArrayList;

public class Main2Activity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Intent listIntent = getIntent();
        ArrayList<String> listMobile;
        listMobile = listIntent.getStringArrayListExtra(MainActivity.TESTMSG);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.activity_listview, listMobile);

        ListView listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
    }

}
