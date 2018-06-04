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

import com.trio.drone.R;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;
import com.trio.drone.core.SettingsActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements WatchServiceCallbacks, BebopEventListener {
    Context mContext;
    float[] acceleration = new float[2];
    float[] accelerationFilter = new float[3];
    float[] gyroscope = new float[2];
    float deltaX = 0;
    float currentTilt = 0f;
    float currentPan = 0f;
    private DeviceSensorProvider<HashMap<String, float[]>> liveData;
    private int count = 0;
    private boolean mIsBound = false;
    float watchDataZ = 0;
    private ConsumerService mConsumerService = null;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConsumerService = ((ConsumerService.LocalBinder) service).getService();
            //updateTextView("onServiceConnected");
            Log.e("MainAct", "onServiceConnected");
            if (mIsBound && mConsumerService != null) {
                Log.e("MainAct", "oncreate bind and consumer");
                mConsumerService.findPeers();
                mConsumerService.setCallbacks(MainActivity.this); // register
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            mIsBound = false;
            //updateTextView("onServiceDisconnected");
            Log.e("MainAct", "onServicedisonnected");
        }
    };
    private int myState = 0;

    public void takeOffDrone(View view) {
        Log.e("TakeOffDrone", "TakeOff fonksiyonunda");
        BebopBro.get().takeOff();
    }

    public void landDrone(View view) {
        Log.e("landDrone", "landDrone fonksiyonunda");
        BebopBro.get().land();
    }

    public void cancelFlight(View view) {
        Log.e("cancelFlight", "cancelFlight fonksiyonunda");
        BebopBro.get().doEmergencyLanding();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        SurfaceView videoTextureView = findViewById(R.id.video_texture_view);

        BebopBro.get().onCreate(getApplicationContext());
        //Surface surface = new Surface(videoTextureView.getHolder().getSurface());
        BebopBro.get().setVideoSurface(videoTextureView.getHolder().getSurface());

        BebopBro.get().register(this);

        System.out.println("ON CREATE");
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mIsBound = bindService(new Intent(MainActivity.this, ConsumerService.class), mConnection,
                Context.BIND_AUTO_CREATE);

        DeviceSensorViewModel model = ViewModelProviders.of(this).get(DeviceSensorViewModel.class);
        liveData = model.getDeviceSensorListener();

        liveData.observe(this, new Observer<HashMap<String, float[]>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, float[]> sensorData) {
                acceleration = sensorData.get("acceleration");
                accelerationFilter = sensorData.get("accelerationFilter");
                gyroscope = sensorData.get("gyroscope");

                count++;

                if (acceleration != null && acceleration.length == 3) {
                    //Log.e("raw", Float.toString(acceleration[0]));
                }

                if (accelerationFilter != null && accelerationFilter.length == 3) {

                    if (BebopBro.get().getControlState() == ControlState.CAMERA_LOOKUP) {

                        float interpolatedTilt = Math.round(-10 * accelerationFilter[2]);
                        int tiltMovement = Math.round(currentTilt - interpolatedTilt);
                        int toDegreeTilt = Math.round(interpolatedTilt);


                        if (Math.abs(tiltMovement) > 5) {
                            //BebopBro.get().move(0, toDegreeTilt, 0, 0);
                        } else {
                            //Log.e("No Cam current", Float.toString(currentTilt));
                        }
                        float interpolatedPan = Math.round(-10 * accelerationFilter[1]);
                        int panMovement = Math.round(currentPan - interpolatedPan);
                        int toDegreePan = Math.round(interpolatedPan);

                        if (Math.abs(panMovement) > 5) {
                            //BebopBro.get().move(-toDegreePan, 0, 0, 0);
                        }
                    } else if (BebopBro.get().getControlState() == ControlState.PILOTING) {
                        deltaX = deltaX + accelerationFilter[2];
                        //Log.e("Gidilen Yol", Float.toString(deltaX));

                        int pitch = Math.round(accelerationFilter[2]);
                        int roll = Math.round(accelerationFilter[1]);
                        //int pitch = Math.round(accelerationFilter[0]);

                        //Log.e("Piloting roll", Integer.toString(roll));
                        //Log.e("Piloting pitch", Integer.toString(pitch));
                        //BebopBro.get().move(roll, pitch, 0, 0);
                    }
                }

            }
        });
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
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openSensor(View view) {
        Log.e("openSensor", "OpenSensore basıldı");

        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

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

    public void openGearSensor(View view) {
        Log.e("openGearSensor", "OpenGearSensore basıldı");

        Intent intent = new Intent(this, GearSensorActivity.class);
        startActivity(intent);
    }

    public void changeControlState(View view) {
        Log.e("Change State", "State tuşu basıldı");
        if (BebopBro.get().getControlState() == ControlState.CAMERA_LOOKUP)
            BebopBro.get().setControlState(ControlState.PILOTING);
        else if (BebopBro.get().getControlState() == ControlState.PILOTING)
            BebopBro.get().setControlState(ControlState.CAMERA_LOOKUP);
    }

    @Override
    public void watchRotateDrone(int dir) {
        Log.e("watchRotateDron", "rotate drone received " + dir);
        if (watchDataZ > 4.5f) {
            BebopBro.get().move(0, 0, dir, 0);
        } else {
            BebopBro.get().move(0, 0, 0, dir);
        }


    }

    @Override
    public void watchTakeOffDrone() {
        Log.e("watchTakeOff", "takeoff received ");
        BebopBro.get().takeOff();
    }

    @Override
    public void watchLandDrone() {
        Log.e("watchTakeOff", "land received ");
        BebopBro.get().land();
    }

    @Override
    public void watchEmergencyDrone() {
        Log.e("watchEmergency", "emergency received ");
        BebopBro.get().doEmergencyLanding();
    }

    @Override
    public void watchAcceleroMoveDrone(float[] watchData) {
        //Log.e("watchAccelero", "accelero received ");
        watchDataZ = watchData[2];
        if (BebopBro.get().getControlState() == ControlState.CAMERA_LOOKUP) {

            float interpolatedTilt = Math.round(10 * watchData[0]);
            int tiltMovement = Math.round(currentTilt - interpolatedTilt);
            int toDegreeTilt = Math.round(interpolatedTilt);

            //Log.e("interpolated", Float.toString(interpolatedTilt));
            //Log.e("tiltMovement", Integer.toString(tiltMovement));
            //Log.e("toDegree", Integer.toString(toDegreeTilt));
            //Log.e("Tilt Move", Float.toString(currentTilt));

            if (Math.abs(tiltMovement) > 5) {
                BebopBro.get().move(0, toDegreeTilt, 0, 0);
                Log.e("Move", "bigger 5");
            } else
                Log.e("No Cam current", Float.toString(currentTilt));

            float interpolatedPan = Math.round(-10 * watchData[1]);
            int panMovement = Math.round(currentPan - interpolatedPan);
            int toDegreePan = Math.round(interpolatedPan);

            if (Math.abs(panMovement) > 5) {
                BebopBro.get().move(-toDegreePan, 0, 0, 0);
            }
        } else if (BebopBro.get().getControlState() == ControlState.PILOTING) {
            deltaX = deltaX + watchData[0];
            //Log.e("Gidilen Yol", Float.toString(deltaX));

            int pitch = 5 * Math.round(watchData[0]);
            int roll = Math.round(watchData[1]);
            //int pitch = Math.round(accelerationFilter[0]);

            //Log.e("Piloting roll", Integer.toString(roll));
            //Log.e("Piloting pitch", Integer.toString(pitch));
            BebopBro.get().move(roll, -pitch, 0, 0);
        }
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
    public void onControlStateChanged(ControlState controlState) {

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
    public void onCameraOrientationChanged(float tiltPerc, float panPerc) {
        //Log.e("Camera tilt: ", Integer.toString(tiltPerc));
        //Log.e("Camera pan: ", Integer.toString(panPerc));
        currentTilt = tiltPerc;
        Log.e("onCameraorient", Float.toString(currentTilt));
        currentPan = panPerc;
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
