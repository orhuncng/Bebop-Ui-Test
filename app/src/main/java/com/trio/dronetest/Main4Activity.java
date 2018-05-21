package com.trio.dronetest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Surface;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.vr.OverlayTexture;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main4Activity extends CardBoardAndroidApplication
        implements CardBoardApplicationListener
{
    BitmapFont font24;
    private long frameTime;
    private final AtomicBoolean videoFrameAvailable = new AtomicBoolean();
    String fps;
    private float counter;
    private Sprite sprite;
    private SpriteBatch batch;
    Surface surface;
    private OverlayTexture videoTexture;
    private Lock mReadyLock;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;
        config.numSamples = 2;
        initialize(this, config);

        BebopBro.getInstance().onCreate(getApplicationContext());

        mReadyLock = new ReentrantLock();
    }

    @Override
    public void create()
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

        batch = new SpriteBatch();

        onSurfaceCreated();
    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void render() { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() { }

    @Override
    public void onNewFrame(HeadTransform paramHeadTransform)
    {
        long currentFrame = SystemClock.elapsedRealtime();
        fps = String.valueOf(1000.0f / (currentFrame - frameTime));
        frameTime = currentFrame;

        counter += 0.5f;
    }

    @Override
    public void onDrawEye(Eye eye)
    {
        GLES20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        synchronized (surface) {
            if (videoFrameAvailable.compareAndSet(true, false)) {
                videoTexture.updateTexImage();
            }
        }

        videoTexture.draw();
        drawUI();
    }

    public void drawUI()
    {
        float cPitch = 1.5f * 500;
        batch.begin();
        for (int i = 0; i < 30; i++) {
            float cRelPitch = cPitch + (75.0f * (i - 15));
            sprite.setCenter(500f, cRelPitch);
            sprite.setOrigin(128, 4 + 500 - cRelPitch);
            sprite.setRotation(counter);
            sprite.draw(batch);
        }
        batch.setColor(1f);
        font24.draw(batch, fps, 500f, 500f);
        batch.end();
    }

    @Override
    public void onFinishFrame(Viewport paramViewport) { }

    @Override
    public void onRendererShutdown() { }

    public void onSurfaceCreated()
    {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLES20.glEnable(GL20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        videoTexture = new OverlayTexture(false, 0.01f, metrics.widthPixels / 2, metrics
                .heightPixels, BebopBro.getVideoWidth(), BebopBro.getVideoHeight());

        SurfaceTexture.OnFrameAvailableListener listener =
                new SurfaceTexture.OnFrameAvailableListener()
                {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture)
                    {
                        videoFrameAvailable.set(true);
                    }
                };

        surface = videoTexture.createSurface(getResources(), listener);

        BebopBro.getInstance().setVideoSurface(surface);
    }

    @Override
    public void onCardboardTrigger() { }
}