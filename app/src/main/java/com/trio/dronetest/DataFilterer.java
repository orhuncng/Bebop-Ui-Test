package com.trio.dronetest;

import android.util.Log;

/**
 * Created by orhun on 4/17/2018.
 */

public class DataFilterer
{

    static final float ALPHA = 0.8f;

    public float[] lowPassFilter(float[] output, float[] input)
    {
        Log.e("raw", Float.toString(input[0]));

        output[0] = ALPHA * output[0] + (1 - ALPHA) * input[0];
        output[1] = ALPHA * output[1] + (1 - ALPHA) * input[1];
        output[2] = ALPHA * output[2] + (1 - ALPHA) * input[2];

        Log.e("filtered", Float.toString(output[0]));

        return output;

    }
}
