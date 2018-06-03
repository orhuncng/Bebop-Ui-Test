package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.vr.SceneListener;
import com.trio.drone.vr.util.GdxUtils;

import java.util.HashMap;
import java.util.Map;

public class OperatingState implements SceneListener
{
    private FlyingState flyingState;
    private ControlState controlState;

    private float centerX;
    private float centerY;
    private Map<FlyingState, String> flyingStateLabels;
    private Map<FlyingState, Color> flyingStateColors;
    private Map<ControlState, String> controlStateLabels;

    public OperatingState()
    {
        flyingState = FlyingState.LANDED;
        controlState = ControlState.CAMERA_LOOKUP;

        flyingStateLabels = new HashMap<>();

        flyingStateLabels.put(FlyingState.LANDED, "LANDED");
        flyingStateLabels.put(FlyingState.LANDING, "LANDING");
        flyingStateLabels.put(FlyingState.HOVERING, "HOVERING");
        flyingStateLabels.put(FlyingState.FLYING, "FLYING");
        flyingStateLabels.put(FlyingState.EMERGENCY_LANDING, "EMERGENCY LANDING");
        flyingStateLabels.put(FlyingState.EMERGENCY, "EMERGENCY");
        flyingStateLabels.put(FlyingState.MOTOR_RAMPING, "MOTOR RAMPING");
        flyingStateLabels.put(FlyingState.TAKINGOFF, "TAKING OFF");
        flyingStateLabels.put(FlyingState.USERTAKEOFF, "USER TAKE OFF");

        flyingStateColors = new HashMap<>();

        flyingStateColors.put(FlyingState.LANDED, Color.GOLDENROD);
        flyingStateColors.put(FlyingState.LANDING, Color.GOLDENROD);
        flyingStateColors.put(FlyingState.HOVERING, Color.FOREST);
        flyingStateColors.put(FlyingState.FLYING, Color.LIME);
        flyingStateColors.put(FlyingState.EMERGENCY_LANDING, Color.SCARLET);
        flyingStateColors.put(FlyingState.EMERGENCY, Color.SCARLET);
        flyingStateColors.put(FlyingState.MOTOR_RAMPING, Color.SKY);
        flyingStateColors.put(FlyingState.TAKINGOFF, Color.TEAL);
        flyingStateColors.put(FlyingState.USERTAKEOFF, Color.TEAL);

        controlStateLabels = new HashMap<>();

        controlStateLabels.put(ControlState.CAMERA_LOOKUP, "CAMERA LOOKUP");
        controlStateLabels.put(ControlState.PILOTING, "PILOTING");
    }

    public void setFlyingState(FlyingState flyingState) { this.flyingState = flyingState; }

    public void setControlState(ControlState controlState) { this.controlState = controlState; }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        centerX = (metrics.widthPixels / 2f) - 50f;
        centerY = metrics.heightPixels * 0.8f;
    }

    @Override
    public void update() { }

    @Override
    public void draw(SpriteBatch batch)
    {
        GdxUtils.getInstance().getFont24().setColor(flyingStateColors.get(flyingState));
        GdxUtils.getInstance().getFont24().draw(batch, flyingStateLabels.get(flyingState),
                centerX - 5f, centerY);

        GdxUtils.getInstance().getFont18().setColor(
                controlState == ControlState.PILOTING ? Color.LIME : Color.ORANGE);
        GdxUtils.getInstance().getFont18().draw(batch, controlStateLabels.get(controlState),
                centerX - 30f, centerY - 35f);

        GdxUtils.getInstance().resetFont18Color();
        GdxUtils.getInstance().resetFont24Color();
    }

    @Override
    public void shutdown() { }
}
