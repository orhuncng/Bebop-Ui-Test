package com.trio.drone.vr.util;

public class LimitedData {
    private static float hecticAlertLimit = 0.8f;
    private static float alertLimit = 0.65f;
    private float value = 0f;
    private float limit = 1f;
    private State state = State.NORMAL;

    public static float getHecticAlertLimit() {
        return hecticAlertLimit;
    }

    public static void setHecticAlertLimit(float limit) {
        hecticAlertLimit = limit / 100f;
    }

    public static float getAlertLimit() {
        return alertLimit;
    }

    public static void setAlertLimit(float limit) {
        alertLimit = limit / 100f;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        updateState();
    }

    public float getLimit() {
        return limit;
    }

    public void setLimit(float limit) {
        this.limit = limit;
        updateState();
    }

    public boolean inAlertState() {
        return state == State.IN_ALERT;
    }

    public boolean inHecticAlertState() {
        return state == State.IN_HECTIC_ALERT;
    }

    public void updateState() {
        float perc = getPerc();

        if (perc >= hecticAlertLimit) state = State.IN_HECTIC_ALERT;
        else if (perc >= alertLimit) state = State.IN_ALERT;
        else state = State.NORMAL;
    }

    public float getPerc() {
        return Math.abs(value / limit);
    }

    public enum State {
        NORMAL, IN_ALERT, IN_HECTIC_ALERT
    }

}
