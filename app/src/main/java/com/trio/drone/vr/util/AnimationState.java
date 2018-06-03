package com.trio.drone.vr.util;

import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class AnimationState
{
    private static final float BLINK_START = 1f;
    private static final float BLINK_END = 0.6f;
    private static final long BLINK_DURATION = 750;

    private static final float BLINK_HECTIC_START = 1f;
    private static final float BLINK_HECTIC_END = 0.2f;
    private static final long BLINK_HECTIC_DURATION = 500;

    private static final float SCALE_START = 0.98f;
    private static final float SCALE_END = 1.02f;
    private static final long SCALE_DURATION = 750;

    private static final float SCALE_HECTIC_START = 0.95f;
    private static final float SCALE_HECTIC_END = 1.05f;
    private static final long SCALE_HECTIC_DURATION = 500;
    private static AnimationState instance = new AnimationState();
    private ValueAnimator blink;
    private ValueAnimator hecticBlink;
    private ValueAnimator backstreets;
    private ValueAnimator hecticBackstreets;

    private AnimationState()
    {

        blink = ValueAnimator.ofFloat(BLINK_START, BLINK_END);
        blink.setRepeatCount(ValueAnimator.INFINITE);
        blink.setRepeatMode(ValueAnimator.REVERSE);
        blink.setDuration(BLINK_DURATION);
        blink.setInterpolator(new AccelerateDecelerateInterpolator());

        hecticBlink = ValueAnimator.ofFloat(BLINK_HECTIC_START, BLINK_HECTIC_END);
        hecticBlink.setRepeatCount(ValueAnimator.INFINITE);
        hecticBlink.setRepeatMode(ValueAnimator.REVERSE);
        hecticBlink.setDuration(BLINK_HECTIC_DURATION);
        hecticBlink.setInterpolator(new LinearInterpolator());

        backstreets = ValueAnimator.ofFloat(SCALE_START, SCALE_END);
        backstreets.setRepeatCount(ValueAnimator.INFINITE);
        backstreets.setRepeatMode(ValueAnimator.REVERSE);
        backstreets.setDuration(SCALE_DURATION);
        backstreets.setInterpolator(new AccelerateDecelerateInterpolator());

        hecticBackstreets = ValueAnimator.ofFloat(SCALE_HECTIC_START, SCALE_HECTIC_END);
        hecticBackstreets.setRepeatCount(ValueAnimator.INFINITE);
        hecticBackstreets.setRepeatMode(ValueAnimator.REVERSE);
        hecticBackstreets.setDuration(SCALE_HECTIC_DURATION);
        hecticBackstreets.setInterpolator(new LinearInterpolator());
    }

    public static AnimationState getInstance()
    {
        return instance;
    }

    public void start()
    {
        blink.start();
        hecticBlink.start();
        backstreets.start();
        hecticBackstreets.start();
    }

    public float getBlink()
    {
        return (float) blink.getAnimatedValue();
    }

    public float getHecticBlink() { return (float) instance.hecticBlink.getAnimatedValue();}

    public float getBackstreets() { return (float) instance.backstreets.getAnimatedValue();}

    public float getHecticBackstreets()
    {
        return (float) instance.hecticBackstreets.getAnimatedValue();
    }

}
