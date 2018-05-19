package com.trio.drone.bebop;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;

public interface BebopEventListener
{
    void onBatteryStateChanged(int batteryLevel);

    void onWifiSignalChanged(int rssi);

    void onFlyingStateChanged(
            ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState);

    void onPositionChanged(float latitude, float longitude, float altitude);

    void onSpeedChanged(float x, float y, float z);

    void onOrientationChanged(float roll, float pitch, float yaw);

    void onRelativeAltitudeChanged(float altitude);

    void onCameraOrientationChanged(int tiltPerc, int panPerc);
}