package com.trio.drone.bebop.strategy;

import com.trio.drone.bebop.controller.DeviceController;

public class CameraLookupControlStrategy implements DroneControlStrategy
{
    @Override
    public void calibrateAccelerometerAndGyro(DeviceController c)
    {
        c.calibrateAccelerometerAndGyro();
    }

    @Override
    public void moveToRelative(DeviceController c, float dX, float dY, float dZ, float dRotation)
    { }

    @Override
    public void move(DeviceController c, int rollPerc, int pitchPerc, int yawPerc, int gazPerc)
    {
        c.moveCamera(pitchPerc * 1.8f, rollPerc * 1.8f);
    }

    @Override
    public void doEmergencyLanding(DeviceController c) { c.doEmergencyLanding(); }

    @Override
    public void takeOff(DeviceController c) { c.takeOff(); }

    @Override
    public void land(DeviceController c) { c.land(); }
}
