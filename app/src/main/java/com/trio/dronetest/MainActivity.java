package com.trio.dronetest;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;

import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.trio.drone.R;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;
import com.trio.drone.core.SettingsActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements WatchServiceCallbacks, BebopEventListener
{
    Context mContext;
    ARDiscoveryServicesDevicesListUpdatedReceiver receiver;

    private final ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mConsumerService = ((ConsumerService.LocalBinder) service).getService();
            //updateTextView("onServiceConnected");
            Log.e("MainAct", "onServiceConnected");
            if (mIsBound == true && mConsumerService != null) {
                Log.e("MainAct", "oncreate bind and consumer");
                mConsumerService.findPeers();
                mConsumerService.setCallbacks(MainActivity.this); // register
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            mConsumerService = null;
            mIsBound = false;
            //updateTextView("onServiceDisconnected");
            Log.e("MainAct", "onServicedisonnected");
        }
    };

    public void takeOffDrone(View view) {
        Log.e("TakeOffDrone", "TakeOff fonksiyonunda");
        BebopBro.getInstance().takeOff();
    }

    public void landDrone(View view) {
        Log.e("landDrone", "landDrone fonksiyonunda");
        BebopBro.getInstance().land();
    }

    public void cancelFlight(View view) {
        Log.e("cancelFlight", "cancelFlight fonksiyonunda");
        BebopBro.getInstance().doEmergencyLanding();
    }

    float[] acceleration = new float[2];
    float[] accelerationFilter = new float[3];
    float[] gyroscope = new float[2];

    private DeviceSensorProvider<HashMap<String, float[]>> liveData;

    float deltaX = 0;
    private int count = 0;

    private boolean mIsBound = false;
    private ConsumerService mConsumerService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        SurfaceView videoTextureView = findViewById(R.id.video_texture_view);

        BebopBro.getInstance().onCreate(getApplicationContext());
        //Surface surface = new Surface(videoTextureView.getHolder().getSurface());
        BebopBro.getInstance().setVideoSurface(videoTextureView.getHolder().getSurface());

        BebopBro.getInstance().register(this);

        System.out.println("ON CREATE");
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mIsBound = bindService(new Intent(MainActivity.this, ConsumerService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        DeviceSensorViewModel model = ViewModelProviders.of(this).get(DeviceSensorViewModel.class);
        liveData = model.getDeviceSensorListener();

        liveData.observe(this, new Observer<HashMap<String, float[]>>()
        {
            @Override
            public void onChanged(@Nullable HashMap<String, float[]> sensorData)
            {
                acceleration = sensorData.get("acceleration");
                accelerationFilter = sensorData.get("accelerationFilter");
                gyroscope = sensorData.get("gyroscope");

                count++;

                if (acceleration != null && acceleration.length == 3) {
                    //Log.e("raw", Float.toString(acceleration[0]));
                }

                if (accelerationFilter != null && accelerationFilter.length == 3) {
                    //Log.e("filtered", Float.toString(accelerationFilter[0]));
                }

                if (count >= 100) {
                    //Log.e("count", "Ayar yapıldı");
                    deltaX = deltaX + accelerationFilter[2];
                    //Log.e("Gidilen Yol", Float.toString(deltaX));

                    int yaw = Math.round(accelerationFilter[2] * 10);

                    //BebopBro.getInstance().move(0,0,yaw,0);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSensor(View view)
    {
        Log.e("openSensor", "OpenSensore basıldı");

        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

    private int myState = 0;

    public void sendState(View view) {
        Log.e("sendState", "SendState basıldı");
        String deneme = "";
        if (myState == 0) {
            myState = 1;
            deneme = "1";
        } else {
            myState = 0;
            deneme = "0";
        }

        if (mIsBound == true && mConsumerService != null) {
            if (mConsumerService.sendState(deneme)) {
                Log.e("sendState", "SendState gitti");
            } else {
                Log.e("sendState", "SendState gitmedi");
            }
        }
    }

    public void openGearSensor(View view)
    {
        Log.e("openGearSensor", "OpenGearSensore basıldı");

        Intent intent = new Intent(this, GearSensorActivity.class);
        startActivity(intent);
    }

    @Override
    public void watchRotateDrone(int dir) {
        Log.e("watchRotateDron", "rotate drone received " + dir);
        BebopBro.getInstance().move(0, 0, dir, 0);
    }

    @Override
    public void watchTakeOffDrone() {
        Log.e("watchTakeOff", "takeoff received ");
        BebopBro.getInstance().takeOff();
    }

    @Override
    public void watchLandDrone() {
        Log.e("watchTakeOff", "land received ");
        BebopBro.getInstance().land();
    }

    @Override
    public void watchEmergencyDrone() {
        Log.e("watchEmergency", "emergency received ");
        BebopBro.getInstance().doEmergencyLanding();
    }

    @Override
    public void onBatteryStateChanged(int batteryLevel) {

    }

    @Override
    public void onWifiSignalChanged(int rssi) {

    }

    @Override
    public void onFlyingStateChanged(FlyingState flyingState) {

        String state = (flyingState == FlyingState.LANDED) ? "1" : "0";

        if (mIsBound == true && mConsumerService != null) {
            if (mConsumerService.sendState(state)) {
                Log.e("sendFlyingState", "SendState gitti");
            } else {
                Log.e("sendFlyingState", "SendState gitmedi");
            }
        }
    }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude) {

    }

    @Override
    public void onSpeedChanged(float x, float y, float z) {

    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw) {

    }

    @Override
    public void onRelativeAltitudeChanged(float altitude) {

    }

    @Override
    public void onCameraOrientationChanged(int tiltPerc, int panPerc) {

    }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result) {

    }

    @Override
    public void onControllerStateChanged(boolean isRunning) {

    }

    public void stopVideo(View view) {
    }


    public void startVideo(View view) {
    }
}
