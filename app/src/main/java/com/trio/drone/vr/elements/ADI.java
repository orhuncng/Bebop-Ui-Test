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

public class ADI implements SceneListener
{
    private static final float PITCH_BLINK_LIMIT = 45f;
    private static final float PITCH_HECTIC_BLINK_LIMIT = 65f;
    private static final float ROLL_BLINK_LIMIT = 30f;
    private static final float ROLL_HECTIC_BLINK_LIMIT = 45f;
    private static final float YAW_BLINK_LIMIT = 25f;
    private static final float YAW_HECTIC_BLINK_LIMIT = 35f;

    private static final float TOTAL_DEGREES = 230f;
    private static final float SIZE = 300f;
    private static final float WIDTH_COEFF = 0.75f;
    private static final float ROLL_POINTER_MARGIN = 15f;

    private Sprite pitchSprite;
    private Sprite rollSprite;
    private Sprite rollPointerSprite;
    private Sprite yawSprite;

    private float roll;
    private float pitch;
    private float yaw;

    private float pitchRange;
    private float pitchCenter;
    private float degreeInV;
    private float centerX;

    private float centerV = 0f;

    private ShaderProgram pitchShader;

    public void setRoll(float roll) { if (roll <= 180f && roll >= -180f) this.roll = roll; }

    public void setPitch(float pitch) { if (pitch <= 90f && pitch >= -90f) this.pitch = pitch; }

    public void setYaw(float yaw) { if (yaw <= 180f && yaw >= -180f) this.yaw = yaw; }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        centerX = metrics.widthPixels / 2f;
        float centerY = metrics.heightPixels / 2f;

        pitchSprite = new Sprite(GdxUtils.getInstance().createSprite("pitch"));

        centerV = (pitchSprite.getV() + pitchSprite.getV2()) / 2f;
        degreeInV = (pitchSprite.getV2() - pitchSprite.getV()) / TOTAL_DEGREES;

        pitchSprite.setSize(SIZE * WIDTH_COEFF, SIZE);
        pitchSprite.setCenter(centerX, centerY);
        pitchSprite.setOriginCenter();

        pitchRange = (pitchSprite.getHeight() / pitchSprite.getRegionHeight()) / 2f;

        float rollPosY = centerY - (SIZE * .6f);
        rollSprite = new Sprite(GdxUtils.getInstance().createSprite("roll"));
        rollSprite.setCenter(centerX, rollPosY);

        rollPointerSprite = new Sprite(GdxUtils.getInstance().createSprite("roll_pointer"));
        rollPointerSprite.setCenter(centerX, rollPosY + ROLL_POINTER_MARGIN);
        rollPointerSprite.setOriginCenter();
        rollPointerSprite.setOrigin(rollPointerSprite.getOriginX(),
                centerY - rollPosY + ROLL_POINTER_MARGIN);

        yawSprite = new Sprite((GdxUtils.getInstance().createSprite("aircraft")));
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
        pitchCenter = centerV - (pitch * degreeInV);
        pitchSprite.setV(pitchCenter - pitchRange);
        pitchSprite.setV2(pitchCenter + pitchRange);

        pitchSprite.setRotation(roll);
        rollPointerSprite.setRotation(roll);
        yawSprite.setCenterX(centerX + SIZE * (yaw / 180f));

        if (Math.abs(pitch) > PITCH_HECTIC_BLINK_LIMIT) {
            pitchSprite.setAlpha(AnimationState.getInstance().getHecticBlink());
            pitchSprite.setScale(AnimationState.getInstance().getHecticBackstreets());
        }
        else if (Math.abs(pitch) > PITCH_BLINK_LIMIT) {
            pitchSprite.setAlpha(AnimationState.getInstance().getBlink());
            pitchSprite.setScale(AnimationState.getInstance().getBackstreets());
        }
        else {
            pitchSprite.setAlpha(1f);
            pitchSprite.setScale(1f);
        }

        if (Math.abs(roll) > ROLL_HECTIC_BLINK_LIMIT) {
            rollSprite.setAlpha(AnimationState.getInstance().getHecticBlink());
            rollPointerSprite.setScale(AnimationState.getInstance().getHecticBackstreets());
            rollPointerSprite.setColor(0.8f, 0.2f, 0f,
                    AnimationState.getInstance().getHecticBlink());
        }
        else if (Math.abs(roll) > ROLL_BLINK_LIMIT) {
            rollSprite.setAlpha(AnimationState.getInstance().getHecticBlink());
            rollPointerSprite.setScale(AnimationState.getInstance().getBackstreets());
            rollPointerSprite.setColor(0.7f, 0.3f, 0f, AnimationState.getInstance().getBlink());
        }
        else {
            rollSprite.setAlpha(1f);
            rollPointerSprite.setAlpha(1f);
            rollPointerSprite.setScale(1f);
            rollPointerSprite.setColor(Color.WHITE);
        }

        if (Math.abs(yaw) > YAW_HECTIC_BLINK_LIMIT) {
            yawSprite.setColor(0.8f, 0.2f, 0f, AnimationState.getInstance().getHecticBlink());
            yawSprite.setScale(AnimationState.getInstance().getHecticBackstreets());
        }
        else if (Math.abs(yaw) > YAW_BLINK_LIMIT) {
            yawSprite.setColor(0.7f, 0.3f, 0f, AnimationState.getInstance().getBlink());
            yawSprite.setScale(AnimationState.getInstance().getBackstreets());
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

        batch.setShader(GdxUtils.getInstance().getDefaultShader());

        rollSprite.draw(batch);
        rollPointerSprite.draw(batch);
        yawSprite.draw(batch);
    }

    @Override
    public void shutdown() { }
}
