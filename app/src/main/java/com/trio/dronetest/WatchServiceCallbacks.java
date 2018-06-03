package com.trio.dronetest;

/**
 * Created by Hp on 19.04.2018.
 */
public interface WatchServiceCallbacks
{
    void watchRotateDrone(int dir);

    void watchTakeOffDrone();

    void watchLandDrone();

    void watchEmergencyDrone();

    void watchAcceleroMoveDrone(float[] watchData);

}
