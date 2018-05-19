package com.trio.dronetest;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.trio.drone.R;

import java.util.HashMap;
import java.util.List;

public class SensorActivity extends AppCompatActivity implements SensorEventListener
{

    float DEFAULT_TIME_CONSTANT = 0.18f;

    private SensorManager mSensorManager;

    float deltaZ = 0;

    // Sensors to used
    private Sensor mGyro;
    private Sensor mAccl;
    private Sensor mLinearAccl;
    private Sensor mMagnet;

    LinearLayout sensorLayout;

    // Gyro Text Views
    TextView gyroXTextView;
    TextView gyroYTextView;
    TextView gyroZTextView;

    // Acceleration Text Views
    TextView acclXTextView;
    TextView acclYTextView;
    TextView acclZTextView;

    // Linear Acceleration Views
    TextView linearAcclXTextView;
    TextView linearAcclYTextView;
    TextView linearAcclZTextView;

    // Linear Magnetometer Views
    TextView magnetometerXTextView;
    TextView magnetometerYTextView;
    TextView magnetometerZTextView;

    // Seperator Text
    TextView seperateGroups;

    // Seperator Text
    TextView averageLast;

    // Acceleration with lowPassFilter Text Views
    TextView velocityZ;
    TextView lowPassAcclYTextView;
    TextView lowPassAcclZTextView;

    float[] linear_acceleration = new float[3];
    float[] gravity = new float[3];

    float[] acceleration = new float[2];
    float[] accelerationFilter = new float[3];
    float[] gyroscope = new float[2];

    private DeviceSensorProvider<HashMap<String, float[]>> liveData;

    float deltaX = 0;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // Set sensorLayout to related layout
        sensorLayout = findViewById(R.id.sensorLayout);
        addLinearAcclViews(true);
        addAcclViews(true);

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
                    linearAcclXTextView.setText(Float.toString(acceleration[0]));
                    linearAcclYTextView.setText(Float.toString(acceleration[1]));
                    linearAcclZTextView.setText(Float.toString(acceleration[2]));

                    Log.e("raw", Float.toString(acceleration[0]));
                }


                if (accelerationFilter != null && accelerationFilter.length == 3) {
                    acclXTextView.setText(Float.toString(accelerationFilter[0]));
                    acclYTextView.setText(Float.toString(accelerationFilter[1]));
                    acclZTextView.setText(Float.toString(accelerationFilter[2]));

                    Log.e("filtered", Float.toString(accelerationFilter[0]));
                }

                if (count >= 100) {
                    Log.e("count", "Ayar yapıldı");

                    deltaX = deltaX + accelerationFilter[2];

                    velocityZ.setText("Gidilen Yol: " + Float.toString(deltaX));


                }


            }
        });


/*
        // Set Sensor Manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Get Gyro Sensor from device
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //Adding Gyro Text Views
        addGyroViews(mGyro != null);

        // Get Acceleration Sensor from device
        mAccl = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Adding Acceleration Text Views
        addAcclViews(mAccl != null);

        // Get Linear Acceleration Sensor from device
        mLinearAccl = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //Adding Linear Acceleration Text Views
        addLinearAcclViews(mLinearAccl != null);

        // Get Magnetic Sensor from device
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //Adding magnetic Text Views
        addMagnetoViews(mMagnet != null);

        //listSensorData();
        */
    }

    private void addLinearAcclViews(boolean hasLinAcc)
    {
        if (hasLinAcc) {
            linearAcclXTextView = new TextView(this);
            linearAcclYTextView = new TextView(this);
            linearAcclZTextView = new TextView(this);
            sensorLayout.addView(linearAcclXTextView);
            sensorLayout.addView(linearAcclYTextView);
            sensorLayout.addView(linearAcclZTextView);

            seperateGroups = new TextView(this);
            seperateGroups.setText("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            sensorLayout.addView(seperateGroups);

            Log.e("LinearAccl", "Added");
        }
        else {
            TextView noLinAccl = new TextView(this);
            noLinAccl.setText("Dont Have Linear Acceleration Sorry :(");
            sensorLayout.addView(noLinAccl);
            Log.e("LinearAccl", "Cant added");

        }
    }

    private void addAcclViews(boolean hasAccl)
    {
        if (hasAccl) {

            acclXTextView = new TextView(this);
            acclYTextView = new TextView(this);
            acclZTextView = new TextView(this);
            sensorLayout.addView(acclXTextView);
            sensorLayout.addView(acclYTextView);
            sensorLayout.addView(acclZTextView);

            seperateGroups = new TextView(this);
            seperateGroups.setText("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            sensorLayout.addView(seperateGroups);

            velocityZ = new TextView(this);
            sensorLayout.addView(velocityZ);
/*
            lowPassAcclYTextView = new TextView(this);
            lowPassAcclZTextView = new TextView(this);
            sensorLayout.addView(lowPassAcclYTextView);
            sensorLayout.addView(lowPassAcclZTextView);
*/
            seperateGroups = new TextView(this);
            seperateGroups.setText("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            sensorLayout.addView(seperateGroups);

            Log.e("Velocity", "Added");

        }
        else {
            TextView noAccleration = new TextView(this);
            noAccleration.setText("Dont Have Acceleration Sorry :(");
            sensorLayout.addView(noAccleration);

            Log.e("Accl", "Cant added");
        }
    }

    private void addGyroViews(Boolean hasGyro)
    {
        if (hasGyro) {
            gyroXTextView = new TextView(this);
            gyroYTextView = new TextView(this);
            gyroZTextView = new TextView(this);
            sensorLayout.addView(gyroXTextView);
            sensorLayout.addView(gyroYTextView);
            sensorLayout.addView(gyroZTextView);

            seperateGroups = new TextView(this);
            seperateGroups.setText("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            sensorLayout.addView(seperateGroups);

            Log.e("Gyro", "Added");
        }
        else {
            TextView noGyro = new TextView(this);
            noGyro.setText("Dont Have Gyro Sorry :(");
            sensorLayout.addView(noGyro);

            Log.e("Gyro", "Cant added");
        }
    }

    private void addMagnetoViews(boolean hasMagnet)
    {
        if (hasMagnet) {
            magnetometerXTextView = new TextView(this);
            magnetometerYTextView = new TextView(this);
            magnetometerZTextView = new TextView(this);
            sensorLayout.addView(magnetometerXTextView);
            sensorLayout.addView(magnetometerYTextView);
            sensorLayout.addView(magnetometerZTextView);

            seperateGroups = new TextView(this);
            seperateGroups.setText("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            sensorLayout.addView(seperateGroups);

            Log.e("Magneto", "Added");
        }
        else {
            TextView noMagnetoAccl = new TextView(this);
            noMagnetoAccl.setText("Dont Have Linear Acceleration Sorry :(");
            sensorLayout.addView(noMagnetoAccl);
            Log.e("Magneto", "Cant added");

        }
    }

    private List<Sensor> getSensorList()
    {

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < deviceSensors.size(); i++) {
            Log.e("Sensor",
                    deviceSensors.get(i).getName() + "Vendor: " + deviceSensors.get(i).getVendor() +
                            "Power: " + deviceSensors.get(i).getPower());
        }
        return deviceSensors;
    }

    private void listSensorData()
    {
        List<Sensor> deviceSensors = getSensorList();

        final int N = deviceSensors.size(); // total number of textviews to add

        sensorLayout = findViewById(R.id.sensorLayout);
        for (int i = 0; i < N; i++) {
            // create a new textview
            gyroXTextView = new TextView(this);

            // set some properties of gyroXTextView or something
            String sensorInfo = "SENSOR: " + deviceSensors.get(i).getName();
            gyroXTextView.setText(sensorInfo);

            // add the textview to the linearlayout
            sensorLayout.addView(gyroXTextView);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        // Gyro sensor to detect turn movements
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            String gyroDataX = "GyroX:" + Float.toString((float) (event.values[0] * 57.2957795));
            String gyroDataY = "GyroY:" + Float.toString((float) (event.values[1] * 57.2957795));
            String gyroDataZ = "GyroZ:" + Float.toString((float) (event.values[2] * 57.2957795));

            gyroXTextView.setText(gyroDataX);
            gyroYTextView.setText(gyroDataY);
            gyroZTextView.setText(gyroDataZ);
        }


        // Acceleration sensor for detect head movements along x-y-z
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float alpha = (float) 0.8;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            String lowPassacclX = "LowPasAcclX:" + Float.toString(linear_acceleration[0]);
            String lowPassacclY = "LowPassAcclY:" + Float.toString(linear_acceleration[1]);
            String lowPassacclZ = "LowPassAcclZ:" + Float.toString(linear_acceleration[2]);

            velocityZ.setText(lowPassacclX);
            lowPassAcclYTextView.setText(lowPassacclY);
            lowPassAcclZTextView.setText(lowPassacclZ);

            String acclX = "AcclX:" + Float.toString(event.values[0]);
            String acclY = "AcclY:" + Float.toString(event.values[1]);
            String acclZ = "AcclZ:" + Float.toString(event.values[2]);

            acclXTextView.setText(acclX);
            acclYTextView.setText(acclY);
            acclZTextView.setText(acclZ);
        }

        // Acceleration sensor for detect head movements along x-y-z
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            String linAcclX = "LinAcclX:" + Float.toString(event.values[0]);
            String linAcclY = "LinAcclY:" + Float.toString(event.values[1]);
            String linAcclZ = "LinAcclZ:" + Float.toString(event.values[2]);

            linearAcclXTextView.setText(linAcclX);
            linearAcclYTextView.setText(linAcclY);
            linearAcclZTextView.setText(linAcclZ);

            // event.values[0]

        }

        // Acceleration sensor for detect head movements along x-y-z
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            String magneticAcclX = "MagneticX:" + Float.toString(event.values[0]);
            String magneticAcclY = "MagneticY:" + Float.toString(event.values[1]);
            String magneticAcclZ = "MagneticZ:" + Float.toString(event.values[2]);

            magnetometerXTextView.setText(magneticAcclX);
            magnetometerYTextView.setText(magneticAcclY);
            magnetometerZTextView.setText(magneticAcclZ);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        /*
        mSensorManager.unregisterListener(this);
        */
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e("OnResume", "Resume Etti");
        /*
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccl, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLinearAccl, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnet, SensorManager.SENSOR_DELAY_NORMAL);
        */
    }
}
