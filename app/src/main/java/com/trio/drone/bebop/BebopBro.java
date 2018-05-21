package com.trio.drone.bebop;

import android.content.Context;
import android.view.Surface;
import com.parrot.arsdk.ARSDK;

import java.util.ArrayList;
import java.util.List;

public class BebopBro implements BebopMediator
{
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;
    private static BebopBro instance = new BebopBro();

    static { ARSDK.loadSDKLibs(); }

    private Context context;
    private DeviceController controller;
    private List<BebopEventListener> listeners;

    private BebopBro() { }

    public static BebopBro getInstance() { return instance; }

    public void onCreate(Context context)
    {
        this.context = context;
        listeners = new ArrayList<>();
        controller = new DeviceController(this, VIDEO_WIDTH, VIDEO_HEIGHT);
    }

    // controller and drone state

    public static int getVideoWidth() { return VIDEO_WIDTH; }

    public static int getVideoHeight() { return VIDEO_HEIGHT; }

    // Listener subscription

    public void register(BebopEventListener listener) {listeners.add(listener);}

    public void unRegister(BebopEventListener listener) {listeners.remove(listener);}

    // Video related

    public void setVideoSurface(Surface surface) {controller.setVideoSurface(surface);}

    public boolean IsRunning() { return controller.IsRunning(); }

    public FlyingState GetFlyingState() { return controller.GetFlyingState(); }

    // Piloting commands

    public void calibrateAccelerometerAndGyro() { controller.calibrateAccelerometerAndGyro(); }

    public void moveToRelative(float dX, float dY, float dZ, float dRotation)
    {
        controller.moveToRelative(dX, dY, dZ, dRotation);
    }

    public void move(int rollPerc, int pitchPerc, int yawPerc, int gazPerc)
    {
        controller.move(rollPerc, pitchPerc, yawPerc, gazPerc);
    }

    public void doEmergencyLanding() {controller.doEmergencyLanding();}

    public void takeOff() {controller.takeOff();}

    public void land() {controller.land();}

    // Notifications to listeners

    @Override
    public Context getContext() { return context; }

    @Override
    public void onBatteryStateChanged(int batteryLevel)
    {
        for (BebopEventListener ls : listeners) ls.onBatteryStateChanged(batteryLevel);
    }

    @Override
    public void onWifiSignalChanged(int rssi)
    {
        for (BebopEventListener ls : listeners) ls.onWifiSignalChanged(rssi);
    }

    @Override
    public void onFlyingStateChanged(FlyingState flyingState)
    {
        for (BebopEventListener ls : listeners) ls.onFlyingStateChanged(flyingState);
    }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude)
    {
        for (BebopEventListener ls : listeners) ls.onPositionChanged(latitude, longitude, altitude);
    }

    @Override
    public void onSpeedChanged(float x, float y, float z)
    {
        for (BebopEventListener ls : listeners) ls.onSpeedChanged(x, y, z);
    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw)
    {
        for (BebopEventListener ls : listeners) ls.onOrientationChanged(roll, pitch, yaw);
    }

    @Override
    public void onRelativeAltitudeChanged(float altitude)
    {
        for (BebopEventListener ls : listeners) ls.onRelativeAltitudeChanged(altitude);
    }

    @Override
    public void onCameraOrientationChanged(int tiltPerc, int panPerc)
    {
        for (BebopEventListener ls : listeners) ls.onCameraOrientationChanged(tiltPerc, panPerc);
    }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result)
    {
        for (BebopEventListener ls : listeners) ls.onRelativeMotionEnded(dX, dY, dZ, result);
    }

    @Override
    public void onControllerStateChanged(boolean isRunning)
    {
        for (BebopEventListener ls : listeners) ls.onControllerStateChanged(isRunning);
    }
}
