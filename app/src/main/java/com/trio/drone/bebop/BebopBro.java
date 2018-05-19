package com.trio.drone.bebop;

import android.content.Context;
import android.view.Surface;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;

import java.util.ArrayList;
import java.util.List;


public class BebopBro implements BebopMediator
{
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;
    private static final BebopBro instance = new BebopBro();

    static { ARSDK.loadSDKLibs(); }

    private Context context;
    private DeviceController controller;
    private List<BebopEventListener> listeners;

    private BebopBro() { }

    public static BebopBro getInstance() { return instance; }

    public static int getVideoWidth() { return VIDEO_WIDTH; }

    public static int getVideoHeight() { return VIDEO_HEIGHT; }

    public void onCreate(Context context)
    {
        this.context = context;
        listeners = new ArrayList<>();
        controller = new DeviceController(this, VIDEO_WIDTH, VIDEO_HEIGHT);
    }

    public void register(BebopEventListener listener) {listeners.add(listener);}

    public void unRegister(BebopEventListener listener) {listeners.remove(listener);}

    public void setVideoSurface(Surface surface) {controller.setVideoSurface(surface);}

    @Override
    public Context getContext() { return context; }

    @Override
    public void onBatteryStateChanged(int batteryLevel)
    {
        for (BebopEventListener listener : listeners)
            listener.onBatteryStateChanged(batteryLevel);
    }

    @Override
    public void onWifiSignalChanged(int rssi)
    {
        for (BebopEventListener listener : listeners)
            listener.onWifiSignalChanged(rssi);
    }

    @Override
    public void onFlyingStateChanged(
            ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState)
    {
        for (BebopEventListener listener : listeners)
            listener.onFlyingStateChanged(flyingState);
    }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude)
    {
        for (BebopEventListener listener : listeners)
            listener.onPositionChanged(latitude, longitude, altitude);
    }

    @Override
    public void onSpeedChanged(float x, float y, float z)
    {
        for (BebopEventListener listener : listeners)
            listener.onSpeedChanged(x, y, z);
    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw)
    {
        for (BebopEventListener listener : listeners)
            listener.onOrientationChanged(roll, pitch, yaw);
    }

    @Override
    public void onRelativeAltitudeChanged(float altitude)
    {
        for (BebopEventListener listener : listeners)
            listener.onRelativeAltitudeChanged(altitude);
    }

    @Override
    public void onCameraOrientationChanged(int tiltPerc, int panPerc)
    {
        for (BebopEventListener listener : listeners)
            listener.onCameraOrientationChanged(tiltPerc, panPerc);
    }
}
