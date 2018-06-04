package com.trio.dronetest;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.trio.drone.data.FilterData;
import com.trio.drone.data.LowPassData;
import com.trio.drone.data.SensorSource;

import java.util.HashMap;

/**
 * Created by orhun on 4/15/2018.
 */

public class DeviceSensorProvider<T> extends LiveData<HashMap<String, float[]>>
        implements SensorEventListener {

    private HashMap<String, float[]> sensorMap = new HashMap();

    private SensorManager mSensorManager;

    private FilterData filterData = new LowPassData(SensorSource.PHONE);

    private float[] acceleration = new float[]{0.0F, 0.0F, 0.0F};

    //Config
    private int sensorFrequency = SensorManager.SENSOR_DELAY_NORMAL;
    private boolean filterActive = true;
    private boolean axisInverted = false;


    public DeviceSensorProvider(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }


    @Override
    protected void onActive() {
        registerSensors(sensorFrequency);
    }

    @Override
    protected void onInactive() {
        unregisterSensors();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (filterActive) {
                acceleration = filterData.get(event.values);

                sensorMap.put("accelerationFilter", acceleration);
                setValue(sensorMap);
            } else {
                sensorMap.put("acceleration", event.values);
                setValue(sensorMap);
            }

            //For testing
            sensorMap.put("acceleration", event.values);
            setValue(sensorMap);

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorMap.put("gyroscope", event.values);
            setValue(sensorMap);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerSensors(int sensorDelay) {
        // Register for sensor updates.
        mSensorManager
                .registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        sensorFrequency);
        mSensorManager
                .registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        sensorFrequency);
    }

    private void unregisterSensors() {
        // Unregister for sensor updates.
        mSensorManager.unregisterListener(this);
    }

    private float[] invert(float[] values) {
        for (int i = 0; i < 3; i++) {
            values[i] = -values[i];
        }
        return values;
    }

}
