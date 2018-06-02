package com.trio.drone.data;

import android.util.Log;

/**
 * Created by Hp on 1.06.2018.
 */

public class LowPassData implements FilterData {
    private float[] history = new float[3];
    private static float smoothingPerc = 0f;

    public static void setSmoothingPerc(float smoothingPerc) {
        LowPassData.smoothingPerc = smoothingPerc / 100f;
    }

    @Override
    public float[] get(float[] newValues) {

        //Log.e("raw", Float.toString(newValues[2]));

        history[0] = smoothingPerc * history[0] + (1f - smoothingPerc) * newValues[0];
        history[1] = smoothingPerc * history[1] + (1f - smoothingPerc) * newValues[1];
        history[2] = smoothingPerc * history[2] + (1f - smoothingPerc) * newValues[2];


        //Log.e("filtered", Float.toString(history[2]));
        return history;
    }
}
