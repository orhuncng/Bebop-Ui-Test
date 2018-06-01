package com.trio.drone.bebop.strategy;

import com.trio.drone.bebop.controller.DeviceController;

public interface DroneControlStrategy
{
    void calibrateAccelerometerAndGyro(DeviceController c);

    void moveToRelative(DeviceController c, float dX, float dY, float dZ, float dRotation);

    void move(DeviceController c, int rollPerc, int pitchPerc, int yawPerc, int gazPerc);

    void doEmergencyLanding(DeviceController c);

    void takeOff(DeviceController c);

    void land(DeviceController c);
}
