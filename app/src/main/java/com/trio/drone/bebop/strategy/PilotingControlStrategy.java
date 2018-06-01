package com.trio.drone.bebop.strategy;

import com.trio.drone.bebop.controller.DeviceController;

public class PilotingControlStrategy implements DroneControlStrategy
{
    @Override
    public void calibrateAccelerometerAndGyro(DeviceController c) { }

    @Override
    public void moveToRelative(DeviceController c, float dX, float dY, float dZ, float dRotation)
    {
        c.moveToRelative(dX, dY, dZ, dRotation);
    }

    @Override
    public void move(DeviceController c, int rollPerc, int pitchPerc, int yawPerc, int gazPerc)
    {
        c.move(rollPerc, pitchPerc, yawPerc, gazPerc);
    }

    @Override
    public void doEmergencyLanding(DeviceController c) { c.doEmergencyLanding(); }

    @Override
    public void takeOff(DeviceController c) { }

    @Override
    public void land(DeviceController c) { c.land(); }
}
