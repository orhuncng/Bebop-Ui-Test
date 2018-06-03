package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.badlogic.gdx.Gdx;
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

    private static final float PITCH_DEGREE_IN_V = 1f / 230f;
    private static final float SIZE = 300f;
    private static final float WIDTH_COEFF = 0.75f;

    private Sprite sprite;

    private float pitchRange;
    private float roll;
    private float pitch;
    private float pitchCenter;

    private float centerV = 0f;

    private ShaderProgram shader;

    public void setRoll(float roll) { this.roll = roll; }

    public void setPitch(float pitch)
    {
        if (pitch <= 90f && pitch >= -90f) {
            this.pitch = pitch;
            pitchCenter = centerV - (pitch * PITCH_DEGREE_IN_V);
            sprite.setV(pitchCenter - pitchRange);
            sprite.setV2(pitchCenter + pitchRange);
        }
    }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        sprite = new Sprite(GdxUtils.getInstance().createSprite("adi_pitch"));
        sprite.setSize(SIZE * WIDTH_COEFF, SIZE);
        sprite.setCenter(metrics.widthPixels / 2f, metrics.heightPixels / 2f);
        sprite.setOriginCenter();

        pitchRange = (sprite.getHeight() / sprite.getRegionHeight()) / 2f;
        centerV = (sprite.getV() + sprite.getV2()) / 2f;

        setPitch(90f);

        shader = new ShaderProgram(Gdx.files.internal("shaders/default_vert.glsl"),
                Gdx.files.internal("shaders/fading_frag.glsl"));
    }

    @Override
    public void update()
    {
        sprite.setRotation(roll);
        setPitch(0);

        if (Math.abs(pitch) > PITCH_HECTIC_BLINK_LIMIT) {
            sprite.setAlpha(AnimationState.getInstance().getHecticBlink());
            sprite.setScale(AnimationState.getInstance().getHecticBackstreets());
        }
        else if (Math.abs(pitch) > PITCH_BLINK_LIMIT) {
            sprite.setAlpha(AnimationState.getInstance().getBlink());
            sprite.setScale(AnimationState.getInstance().getBackstreets());
        }
        else {
            sprite.setAlpha(1f);
            sprite.setScale(1f);
        }
    }

    @Override
    public void draw(SpriteBatch batch)
    {
        batch.setShader(shader);
        shader.setUniformf("center", pitchCenter);
        shader.setUniformf("alphaRadius", pitchRange);
        sprite.draw(batch);
        batch.setShader(GdxUtils.getInstance().getDefaultShader());
    }

    @Override
    public void shutdown() { }
}
