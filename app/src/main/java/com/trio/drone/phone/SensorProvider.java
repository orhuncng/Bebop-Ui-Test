package com.trio.drone.phone;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.trio.drone.data.FilterData;
import com.trio.drone.data.LowPassData;
import com.trio.drone.data.SensorSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hp on 5.06.2018.
 */

public class SensorProvider implements SensorEventListener {

    private SensorManager manager;
    private static final int DELAY = SensorManager.SENSOR_DELAY_NORMAL;
    private FilterData accelData = new LowPassData(SensorSource.PHONE);

    private List<PhoneSensorListener> listeners = new ArrayList<>();

    public SensorProvider(Context context) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void register(PhoneSensorListener listener) {
        if (listeners.isEmpty())
            registerSensors(DELAY);
        listeners.add(listener);
    }

    public void unregister(PhoneSensorListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty())
            unregisterSensors();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            for (PhoneSensorListener l : listeners)
                l.onAccelerometerSensorChanged(accelData.get(event.values));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void registerSensors(int sensorDelay) {
        // Register for sensor updates.
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), DELAY);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), DELAY);
    }

    private void unregisterSensors() {
        // Unregister for sensor updates.
        manager.unregisterListener(this);
    }
}
