package com.trio.drone.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.trio.drone.R;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.bebop.ControlState;

public class VrMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_menu);

    }

    public void takeOffDrone(View view) {
        Log.e("TakeOffDrone", "TakeOff fonksiyonunda");
        Intent intent = new Intent(this, VRActivity.class);
        startActivity(intent);
    }

    public void landDrone(View view) {
        Log.e("landDrone", "landDrone fonksiyonunda");
        BebopBro.get().land();
    }

    public void cancelFlight(View view) {
        Log.e("cancelFlight", "cancelFlight fonksiyonunda");
        BebopBro.get().doEmergencyLanding();
    }

    public void changeControlState(View view) {
        Log.e("Change State", "State tuşu basıldı");
        if (BebopBro.get().getControlState() == ControlState.CAMERA_LOOKUP)
            BebopBro.get().setControlState(ControlState.PILOTING);
        else if (BebopBro.get().getControlState() == ControlState.PILOTING)
            BebopBro.get().setControlState(ControlState.CAMERA_LOOKUP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(VrMenuActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
