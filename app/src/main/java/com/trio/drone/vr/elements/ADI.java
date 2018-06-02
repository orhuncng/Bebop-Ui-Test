package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.trio.drone.vr.GLUtils;
import com.trio.drone.vr.SceneListener;

public class ADI implements SceneListener
{
    private static final float PITCH_DEGREE_IN_V = 1f / 230f;
    private static float RADIUS = 250f;
    private Sprite sprite;
    private float pitch;
    private float pitchRange = 0f;
    private float pitchCenter = 0.5f;
    private float roll = 0f;

    private ShaderProgram shader;

    public void setRoll(float roll) { this.roll = roll; }

    public void setPitch(float pitch) { pitchCenter = (pitch * PITCH_DEGREE_IN_V) + 0.5f; }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        Texture tex = new Texture(Gdx.files.internal("images/vr/adi_pitch.png"));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sprite = new Sprite(tex);

        pitchRange = RADIUS / tex.getHeight();

        sprite.setSize(RADIUS, RADIUS * 2f);
        sprite.setCenter(metrics.widthPixels / 2f, metrics.heightPixels / 2f);
        sprite.setOriginCenter();
        sprite.setV(0.5f - pitchRange);
        sprite.setV2(0.5f + pitchRange);

        shader = new ShaderProgram(Gdx.files.internal("shaders/default_vert.glsl"),
                Gdx.files.internal("shaders/fading_frag.glsl"));
    }

    @Override
    public void update()
    {
        sprite.setRotation(roll);
        sprite.setV(pitchCenter - pitchRange);
        sprite.setV2(pitchCenter + pitchRange);
    }

    @Override
    public void draw(SpriteBatch batch)
    {
        batch.setShader(shader);
        shader.setUniformf("center", pitchCenter);
        shader.setUniformf("alphaRadius", pitchRange);
        sprite.draw(batch);
        batch.setShader(GLUtils.getDefaultLibgdxShader());
    }

    @Override
    public void shutdown() { }
}
