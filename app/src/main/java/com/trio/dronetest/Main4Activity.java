package com.trio.dronetest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.graphics.GL20;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.vr.Scene;
import com.trio.drone.vr.util.AnimationState;

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

        AnimationState.getInstance().start();
    }

    @Override
    public void create()
    {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLES20.glEnable(GL20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        scene = new Scene(BebopBro.getVideoWidth(), BebopBro.getVideoHeight());
        scene.create(metrics, getResources());
        BebopBro.getInstance().register(scene);
        BebopBro.getInstance().setVideoSurface(scene.getBackgroundSurface());

        /*Camera camera = Camera.open();
        Camera.Size cSize = camera.getParameters().getPreviewSize();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        scene = new Scene(cSize.width, cSize.height);
        scene.create(metrics, getResources());

        try {
            camera.setPreviewTexture(scene.getBackgroundTexture());
            camera.startPreview();
        } catch (IOException ignored) {
        }*/
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