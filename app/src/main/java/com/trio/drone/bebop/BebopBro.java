package com.trio.drone.bebop;

import android.content.Context;
import android.view.Surface;
import com.parrot.arsdk.ARSDK;
import com.trio.drone.bebop.controller.DeviceController;
import com.trio.drone.bebop.strategy.CameraLookupControlStrategy;
import com.trio.drone.bebop.strategy.DroneControlStrategy;
import com.trio.drone.bebop.strategy.PilotingControlStrategy;

import java.util.ArrayList;
import java.util.List;

public class BebopBro implements BebopMediator
{
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;
    private static BebopBro instance = new BebopBro();

    static { ARSDK.loadSDKLibs(); }

    private DeviceController controller;
    private ControlState controlState = ControlState.CAMERA_LOOKUP;
    private List<BebopEventListener> listeners = new ArrayList<>();
    private DroneControlStrategy controlStrategy = new CameraLookupControlStrategy();

    private BebopBro() { }

    public static BebopBro getInstance() { return instance; }

    public static int getVideoWidth() { return VIDEO_WIDTH; }

    // Listener subscription

    public static int getVideoHeight() { return VIDEO_HEIGHT; }

    @Override
    public void onCreate(Context context)
    {
        if (controller == null)
            controller = new DeviceController(context, this, VIDEO_WIDTH, VIDEO_HEIGHT);
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

    public void setVideoSurface(Surface surface) {controller.setVideoSurface(surface);}

    @Override
    public void move(int rollPerc, int pitchPerc, int yawPerc, int gazPerc)
    {
        controlStrategy.move(controller, rollPerc, pitchPerc, yawPerc, gazPerc);
    }

    @Override
    public void doEmergencyLanding() {controlStrategy.doEmergencyLanding(controller);}

    // Piloting commands

    @Override
    public void takeOff() {controlStrategy.takeOff(controller);}

    @Override
    public void land() {controlStrategy.land(controller);}

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
        }
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
