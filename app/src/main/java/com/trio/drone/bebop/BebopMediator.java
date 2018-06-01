package com.trio.drone.bebop;

import android.content.Context;

public interface BebopMediator extends BebopEventListener
{
    void onCreate(Context context);

    void register(BebopEventListener listener);

    void unregister(BebopEventListener listener);

    void calibrateAccelerometerAndGyro();

    void moveToRelative(float dX, float dY, float dZ, float dRotation);

    void move(int rollPerc, int pitchPerc, int yawPerc, int gazPerc);

    void doEmergencyLanding();

    void takeOff();

    void land();
}
