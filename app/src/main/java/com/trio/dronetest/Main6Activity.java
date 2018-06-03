package com.trio.dronetest;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.trio.drone.R;
import com.trio.drone.core.SettingsActivity;


public class Main6Activity extends AppCompatActivity {

    private static final String TAB = "Activity 6";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        Log.d("onCreate:", "Starting");

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "Gear Controller");
        adapter.addFragment(new Tab2Fragment(), "Vr Controller");
        //adapter.addFragment(new Tab3Fragment(), "TAB3");
        viewPager.setAdapter(adapter);
    }

    public void onClickBtn1(View view){
        Log.e("Onclick", "Bastın");
        Toast.makeText(this,"FLY WITH GEAR",Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, GearIntroActivity.class);
        startActivity(intent);

    }

    public void onClickBtn2(View view){
        Toast.makeText(this,"FLY VR",Toast.LENGTH_SHORT).show();
    }

    public void onClickBtn3(View view){
        Toast.makeText(this,"Button3 basıldı",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(Main6Activity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
