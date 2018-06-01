package com.trio.dronetest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.graphics.GL20;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.trio.drone.vr.Scene;

import java.io.IOException;

public class Main4Activity extends CardBoardAndroidApplication
        implements CardBoardApplicationListener
{
    private Scene scene;

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
    }

    @Override
    public void create()
    {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLES20.glEnable(GL20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
/*
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

        BebopBro.getInstance().setVideoSurface(surface);*/

        Camera camera = Camera.open();
        Camera.Size cSize = camera.getParameters().getPreviewSize();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        scene = new Scene(cSize.width, cSize.height);
        scene.create(metrics, getResources());

        try {
            camera.setPreviewTexture(scene.getBackgroundTexture());
            camera.startPreview();
        } catch (IOException ioe) {
            Log.w("Main5Activity", "CAM LAUNCH FAILED");
        }
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
    public void onNewFrame(HeadTransform paramHeadTransform) { scene.update(); }

    @Override
    public void onDrawEye(Eye eye)
    {
        GLES20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        scene.draw();
    }

    @Override
    public void onFinishFrame(Viewport paramViewport) { }

    @Override
    public void onRendererShutdown() { scene.shutdown(); }

    @Override
    public void onCardboardTrigger() { }
}