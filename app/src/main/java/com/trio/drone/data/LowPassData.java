package com.trio.drone.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hp on 1.06.2018.
 */

public class LowPassData implements FilterData
{
    private static Map<SensorSource, Float> smoothingCoeffs = new HashMap<>();
    private float[] history = new float[3];
    private SensorSource source;

    public LowPassData(SensorSource source) {
        this.source = source;
    }

    public static void setSmoothingCoeff(float coeff, SensorSource source)
    {
        smoothingCoeffs.put(source, coeff / 100f);
    }

    @Override
    public float[] get(float[] newValues)
    {
        get(newValues[0], 0);
        get(newValues[1], 1);
        get(newValues[2], 2);

        return history;
    }

    @Override
    public float get(float newValue, int index) {
        float coeff = smoothingCoeffs.get(source);
        history[index] = coeff * history[index] + (1f - coeff) * newValue;
        return history[index];
    }

    @Override
    public float get(float newValue) {
        return get(newValue, 0);
    }
}
