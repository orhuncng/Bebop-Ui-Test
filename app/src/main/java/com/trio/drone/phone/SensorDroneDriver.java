package com.trio.drone.phone;

import android.util.Log;

import com.trio.drone.bebop.BebopBro;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;

/**
 * Created by Hp on 5.06.2018.
 */

public class SensorDroneDriver implements PhoneSensorListener, BebopEventListener {
    float currentTilt = 0f;
    float currentPan = 0f;

    public SensorDroneDriver() {
        BebopBro.get().register(this);
    }

    @Override
    public void onAccelerometerSensorChanged(float[] values) {
        Log.e("Girmemeli", "-----------------------WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW--------------------------------------------------");
        if (BebopBro.get().getControlState() == ControlState.CAMERA_LOOKUP) {

            float interpolatedTilt = Math.round(-10 * values[2]);
            int tiltMovement = Math.round(currentTilt - interpolatedTilt);
            int toDegreeTilt = Math.round(interpolatedTilt);

            if (Math.abs(tiltMovement) > 5) {
                BebopBro.get().move(0, toDegreeTilt, 0, 0);
            } else {
                Log.e("No Cam current", Float.toString(currentTilt));
            }
            float interpolatedPan = Math.round(-10 * values[1]);
            int panMovement = Math.round(currentPan - interpolatedPan);
            int toDegreePan = Math.round(interpolatedPan);

            if (Math.abs(panMovement) > 5) {
                BebopBro.get().move(-toDegreePan, 0, 0, 0);
            }
        } else if (BebopBro.get().getControlState() == ControlState.PILOTING) {
            //Log.e("Gidilen Yol", Float.toString(deltaX));

            int pitch = Math.round(values[2]);
            int roll = Math.round(values[1]);
            //int pitch = Math.round(accelerationFilter[0]);

            BebopBro.get().move(roll, pitch, 0, 0);
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
        currentTilt = tiltPerc;
        currentPan = panPerc;
    }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result) {

    }

    @Override
    public void onControllerStateChanged(boolean isRunning) {

    }
}
