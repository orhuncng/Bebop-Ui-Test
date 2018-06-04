package com.trio.drone.bebop;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.Surface;

import com.parrot.arsdk.ARSDK;
import com.trio.drone.R;
import com.trio.drone.bebop.controller.DeviceController;
import com.trio.drone.bebop.strategy.CameraLookupControlStrategy;
import com.trio.drone.bebop.strategy.DroneControlStrategy;
import com.trio.drone.bebop.strategy.PilotingControlStrategy;
import com.trio.drone.data.LowPassData;
import com.trio.drone.data.SensorSource;

import java.util.ArrayList;
import java.util.List;

public class BebopBro implements BebopMediator, SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;
    private static BebopBro instance = new BebopBro();

    static { ARSDK.loadSDKLibs(); }

    private DeviceController controller;
    private ControlState controlState = ControlState.CAMERA_LOOKUP;
    private List<BebopEventListener> listeners = new ArrayList<>();
    private DroneControlStrategy controlStrategy = new CameraLookupControlStrategy();
    private Resources resources;

    private LowPassData lpSpeed = new LowPassData(SensorSource.DRONE);
    private LowPassData lpOrient = new LowPassData(SensorSource.DRONE);
    private LowPassData lpAltitude = new LowPassData(SensorSource.DRONE);

    private BebopBro() { }

    public static BebopBro get() {
        return instance;
    }

    public static int getVideoWidth() { return VIDEO_WIDTH; }

    // Listener subscription

    public static int getVideoHeight() { return VIDEO_HEIGHT; }

    @Override
    public void onCreate(Context context)
    {
        if (controller == null) {
            controller = new DeviceController(context, this, VIDEO_WIDTH, VIDEO_HEIGHT);
            resources = context.getResources();
            PreferenceManager.getDefaultSharedPreferences(context)
                    .registerOnSharedPreferenceChangeListener(this);
        }
    }

    // controller and drone state

    @Override
    public void register(BebopEventListener listener) {listeners.add(listener);}

    @Override
    public void unregister(BebopEventListener listener) {listeners.remove(listener);}

    @Override
    public void calibrateAccelerometerAndGyro()
    {
        controlStrategy.calibrateAccelerometerAndGyro(controller);
    }

    @Override
    public void moveToRelative(float dX, float dY, float dZ, float dRotation)
    {
        controlStrategy.moveToRelative(controller, dX, dY, dZ, dRotation);
    }

    // Video related

    @Override
    public void move(int rollPerc, int pitchPerc, int yawPerc, int gazPerc)
    {
        controlStrategy.move(controller, rollPerc, pitchPerc, yawPerc, gazPerc);
    }

    @Override
    public void doEmergencyLanding() { controlStrategy.doEmergencyLanding(controller); }

    @Override
    public void takeOff() { controlStrategy.takeOff(controller); }

    // Piloting commands

    @Override
    public void land() { controlStrategy.land(controller); }

    public void setVideoSurface(Surface surface) { controller.setVideoSurface(surface); }

    public boolean isRunning() { return controller.IsRunning(); }

    public FlyingState getFlyingState() { return controller.GetFlyingState(); }

    public ControlState getControlState()
    {
        return controlState;
    }

    public void setControlState(ControlState controlState)
    {
        if (this.controlState != controlState) {
            this.controlState = controlState;

            if (controlState == ControlState.CAMERA_LOOKUP)
                controlStrategy = new CameraLookupControlStrategy();
            else if (controlState == ControlState.PILOTING)
                controlStrategy = new PilotingControlStrategy();

            for (BebopEventListener ls : listeners) ls.onControlStateChanged(controlState);
        }
    }

    public void toggleControlState()
    {
        setControlState(controlState == ControlState.PILOTING ?
                ControlState.CAMERA_LOOKUP : ControlState.PILOTING);
    }

    // Notifications to listeners

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
    public void onControlStateChanged(ControlState controlState) { }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude)
    {
        for (BebopEventListener ls : listeners) ls.onPositionChanged(latitude, longitude, altitude);
    }

    @Override
    public void onSpeedChanged(float x, float y, float z)
    {
        for (BebopEventListener ls : listeners)
            ls.onSpeedChanged(lpSpeed.get(x), lpSpeed.get(y), lpSpeed.get(z));
    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw)
    {
        for (BebopEventListener ls : listeners)
            ls.onOrientationChanged(lpOrient.get(roll), lpOrient.get(pitch), lpOrient.get(yaw));
    }

    @Override
    public void onRelativeAltitudeChanged(float altitude)
    {
        for (BebopEventListener ls : listeners)
            ls.onRelativeAltitudeChanged(lpAltitude.get(altitude));
    }

    @Override
    public void onCameraOrientationChanged(float tiltPerc, float panPerc)
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(resources.getString(R.string.pref_key_max_altitude)))
            controller.setMaxAltitude(sharedPreferences.getFloat(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_max_vert_speed)))
            controller.setMaxVertSpeed(sharedPreferences.getFloat(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_max_pitch_roll)))
            controller.setMaxPitchRoll(sharedPreferences.getInt(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_max_rot_speed)))
            controller.setMaxRotationSpeed(sharedPreferences.getInt(key, 1));
    }
}
