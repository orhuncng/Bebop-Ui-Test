package com.trio.drone.data;

/**
 * Created by Hp on 1.06.2018.
 */

public interface FilterData
{
    float[] get(float[] newValues);

    float get(float newValue, int index);

    float get(float newValue);
}