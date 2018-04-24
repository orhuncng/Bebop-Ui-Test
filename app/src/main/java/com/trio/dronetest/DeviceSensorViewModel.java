package com.trio.dronetest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.HashMap;

public class DeviceSensorViewModel extends AndroidViewModel
{
    DeviceSensorProvider<HashMap<String, float[]>> deviceSensorListener;

    public DeviceSensorViewModel(Application application)
    {
        super(application);
        deviceSensorListener = new DeviceSensorProvider(application);
    }

    public DeviceSensorProvider<HashMap<String, float[]>> getDeviceSensorListener()
    {
        return deviceSensorListener;
    }

}
