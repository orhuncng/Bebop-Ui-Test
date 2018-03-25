package com.example.trio.testproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mGyro;

    LinearLayout sensorLayout;
    TextView gyroXTextView;
    TextView gyroYTextView;
    TextView gyroZTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorLayout = (LinearLayout) findViewById(R.id.sensorLayout);

        //Adding Gyro Text Views
        addGyroViews();


        Log.e("Gyro: ", mGyro.getName());
        //listSensorData();
    }

    private void addGyroViews() {
        gyroXTextView = new TextView(this);
        gyroYTextView = new TextView(this);
        gyroZTextView = new TextView(this);
        sensorLayout.addView(gyroXTextView);
        sensorLayout.addView(gyroYTextView);
        sensorLayout.addView(gyroZTextView);
    }

    private List<Sensor> getSensorList(){

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i < deviceSensors.size(); i++) {
            Log.e("Sensor", deviceSensors.get(i).getName() + "Vendor: " + deviceSensors.get(i).getVendor() + "Power: " + deviceSensors.get(i).getPower());
        }
        return deviceSensors;
    }

    private void listSensorData(){
        List<Sensor> deviceSensors = getSensorList();

        final int N = deviceSensors.size(); // total number of textviews to add

        sensorLayout = (LinearLayout) findViewById(R.id.sensorLayout);
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
    public void onSensorChanged(SensorEvent event) {

        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            String gyroDataX = "GyroX:" + Float.toString((float) (event.values[0] * 57.2957795));
            String gyroDataY = "GyroY:" + Float.toString((float) (event.values[1] * 57.2957795));
            String gyroDataZ = "GyroZ:" + Float.toString((float) (event.values[2] * 57.2957795));

            gyroXTextView.setText(gyroDataX);
            gyroYTextView.setText(gyroDataY);
            gyroZTextView.setText(gyroDataZ);
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("OnResume" ,"Resume Etti");
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
