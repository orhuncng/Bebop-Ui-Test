package com.trio.drone.bebop;

public interface BebopEventListener
{
    void onBatteryStateChanged(int batteryLevel);

    void onWifiSignalChanged(int rssi);

    void onFlyingStateChanged(FlyingState flyingState);

    void onControlStateChanged(ControlState controlState);

    void onPositionChanged(float latitude, float longitude, float altitude);

    void onSpeedChanged(float x, float y, float z);

    void onOrientationChanged(float roll, float pitch, float yaw);

    void onRelativeAltitudeChanged(float altitude);

    void onCameraOrientationChanged(float tiltPerc, float panPerc);

    void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result);

    void onControllerStateChanged(boolean isRunning);
}