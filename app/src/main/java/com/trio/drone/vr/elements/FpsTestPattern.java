package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.trio.drone.vr.SceneListener;

public class FpsTestPattern implements SceneListener
{
    private BitmapFont font24;
    private long frameTime;
    private String fps;
    private float counter;
    private Sprite sprite;

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        Pixmap pixmap = new Pixmap(256, 8, Pixmap.Format.RGBA8888);

        pixmap.setColor(1f, 0.7f, 0f, 0.33f);
        pixmap.fillRectangle(0, 0, 256, 8);

        pixmap.setColor(1f, 0.7f, 0f, 0.67f);
        pixmap.fillRectangle(1, 1, 254, 6);

        pixmap.setColor(1f, 0.7f, 0f, 1f);
        pixmap.fillRectangle(2, 2, 252, 4);

        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sprite = new Sprite(tex);
        pixmap.dispose();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("font/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        font24 = generator.generateFont(parameter);
        font24.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture
                .TextureFilter.Linear);
        generator.dispose();
    }

    @Override
    public void update()
    {
        long currentFrame = SystemClock.elapsedRealtime();
        fps = String.valueOf(1000f / (currentFrame - frameTime));
        frameTime = currentFrame;
        counter += .5f;
    }

    @Override
    public void draw(SpriteBatch batch)
    {
        float cPitch = 1.5f * 500;
        for (int i = 0; i < 30; i++) {
            float cRelPitch = cPitch + (75.0f * (i - 15));
            sprite.setCenter(500f, cRelPitch);
            sprite.setOrigin(128, 4 + 500 - cRelPitch);
            sprite.setRotation(counter);
            sprite.draw(batch);
        }

        batch.setColor(1f);
        font24.draw(batch, fps, 500f, 500f);
    }

    @Override
    public void shutdown()
    {

    }
}
