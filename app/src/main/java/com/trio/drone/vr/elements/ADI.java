package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.trio.drone.vr.SceneListener;
import com.trio.drone.vr.util.AnimationState;
import com.trio.drone.vr.util.GdxUtils;
import com.trio.drone.vr.util.LimitedData;

public class ADI implements SceneListener
{
    private static final float TOTAL_DEGREES = 230f;
    private static final float SIZE = 300f;
    private static final float WIDTH_COEFF = 0.75f;
    private static final float ROLL_POINTER_MARGIN = 15f;

    private Sprite pitchSprite;
    private Sprite rollSprite;
    private Sprite rollPointerSprite;
    private Sprite yawSprite;

    private LimitedData roll = new LimitedData();
    private LimitedData pitch = new LimitedData();
    private LimitedData yaw = new LimitedData();

    private float pitchRange;
    private float pitchCenter;
    private float degreeInV;
    private float centerX;

    private float centerV = 0f;

    private ShaderProgram pitchShader;

    public void setRoll(float value) {
        if (value <= 180f && value >= -180f) roll.setValue(value);
    }

    public void setPitch(float value) {
        if (value <= 90f && value >= -90f) pitch.setValue(value);
    }

    public void setYaw(float value) {
        if (value <= 180f && value >= -180f) yaw.setValue(value);
    }

    public void setRollLimit(float limit) {
        roll.setLimit(limit);
    }

    public void setPitchLimit(float limit) {
        pitch.setLimit(limit);
    }

    public void setYawLimit(float limit) {
        yaw.setLimit(limit);
    }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        centerX = metrics.widthPixels / 2f;
        float centerY = (metrics.heightPixels / 2f) - 150f;

        pitchSprite = new Sprite(GdxUtils.get().createSprite("pitch"));

        centerV = (pitchSprite.getV() + pitchSprite.getV2()) / 2f;
        degreeInV = (pitchSprite.getV2() - pitchSprite.getV()) / TOTAL_DEGREES;

        pitchSprite.setSize(SIZE * WIDTH_COEFF, SIZE);
        pitchSprite.setCenter(centerX, centerY);
        pitchSprite.setOriginCenter();

        pitchRange = (pitchSprite.getHeight() / pitchSprite.getRegionHeight()) / 2f;

        float rollPosY = centerY - (SIZE * .6f);
        rollSprite = new Sprite(GdxUtils.get().createSprite("roll"));
        rollSprite.setCenter(centerX, rollPosY);

        rollPointerSprite = new Sprite(GdxUtils.get().createSprite("roll_pointer"));
        rollPointerSprite.setCenter(centerX, rollPosY + ROLL_POINTER_MARGIN);
        rollPointerSprite.setOriginCenter();
        rollPointerSprite.setOrigin(rollPointerSprite.getOriginX(),
                centerY - rollPosY + ROLL_POINTER_MARGIN);

        yawSprite = new Sprite((GdxUtils.get().createSprite("aircraft")));
        yawSprite.setCenter(centerX, centerY);
        yawSprite.setOriginCenter();

        setPitch(0f);
        setRoll(0f);
        setYaw(0f);

        pitchShader = new ShaderProgram(Gdx.files.internal("shaders/default_vert.glsl"),
                Gdx.files.internal("shaders/fading_frag.glsl"));
    }

    @Override
    public void update()
    {
        pitchCenter = centerV - (pitch.getValue() * degreeInV);
        pitchSprite.setV(pitchCenter - pitchRange);
        pitchSprite.setV2(pitchCenter + pitchRange);

        pitchSprite.setRotation(roll.getValue());
        rollPointerSprite.setRotation(roll.getValue());
        yawSprite.setCenterX(centerX + SIZE * (yaw.getValue() / 180f));

        if (pitch.inHecticAlertState()) {
            pitchSprite.setAlpha(AnimationState.get().getHecticBlink());
            pitchSprite.setScale(AnimationState.get().getHecticBackstreets());
        } else if (pitch.inAlertState()) {
            pitchSprite.setAlpha(AnimationState.get().getBlink());
            pitchSprite.setScale(AnimationState.get().getBackstreets());
        }
        else {
            pitchSprite.setAlpha(1f);
            pitchSprite.setScale(1f);
        }

        if (roll.inHecticAlertState()) {
            rollSprite.setAlpha(AnimationState.get().getHecticBlink());
            rollPointerSprite.setScale(AnimationState.get().getHecticBackstreets());
            rollPointerSprite.setColor(0.8f, 0.2f, 0f,
                    AnimationState.get().getHecticBlink());
        } else if (roll.inAlertState()) {
            rollSprite.setAlpha(AnimationState.get().getHecticBlink());
            rollPointerSprite.setScale(AnimationState.get().getBackstreets());
            rollPointerSprite.setColor(0.7f, 0.3f, 0f, AnimationState.get().getBlink());
        }
        else {
            rollSprite.setAlpha(1f);
            rollPointerSprite.setAlpha(1f);
            rollPointerSprite.setScale(1f);
            rollPointerSprite.setColor(Color.WHITE);
        }

        if (yaw.inHecticAlertState()) {
            yawSprite.setColor(0.8f, 0.2f, 0f, AnimationState.get().getHecticBlink());
            yawSprite.setScale(AnimationState.get().getHecticBackstreets());
        } else if (yaw.inAlertState()) {
            yawSprite.setColor(0.7f, 0.3f, 0f, AnimationState.get().getBlink());
            yawSprite.setScale(AnimationState.get().getBackstreets());
        }
        else {
            yawSprite.setColor(Color.WHITE);
            yawSprite.setScale(1f);
        }
    }

    @Override
    public void draw(SpriteBatch batch)
    {
        batch.setShader(pitchShader);

        pitchShader.setUniformf("center", pitchCenter);
        pitchShader.setUniformf("alphaRadius", pitchRange);
        pitchSprite.draw(batch);

        batch.setShader(GdxUtils.get().getDefaultShader());

        rollSprite.draw(batch);
        rollPointerSprite.draw(batch);
        yawSprite.draw(batch);
    }

    @Override
    public void shutdown() { }
}
