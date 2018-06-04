package com.trio.drone.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.graphics.GL20;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.phone.SensorDroneDriver;
import com.trio.drone.phone.SensorProvider;
import com.trio.drone.vr.Scene;
import com.trio.drone.vr.util.AnimationState;

public class VRActivity extends CardBoardAndroidApplication
        implements CardBoardApplicationListener {
    private Scene scene;
    private SensorDroneDriver sensorDroneDriver;
    private SensorProvider sensorProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        BebopBro.get().onCreate(getApplicationContext());
        AnimationState.get().start();

        sensorProvider = new SensorProvider(this);
        sensorDroneDriver = new SensorDroneDriver();
        sensorProvider.register(sensorDroneDriver);
    }

    @Override
    public void onBackPressed() {
        BebopBro.get().setControlState(ControlState.CAMERA_LOOKUP);
        sensorProvider.unregister(sensorDroneDriver);
        super.onBackPressed();
        startActivity(new Intent(VRActivity.this, SettingsActivity.class));
        finish();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onNewFrame(HeadTransform paramHeadTransform) {
        scene.update();
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        scene.draw();
    }

    @Override
    public void onFinishFrame(Viewport paramViewport) {
    }

    @Override
    public void onRendererShutdown() {
        scene.shutdown();
    }

    @Override
    public void onCardboardTrigger() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
            BebopBro.get().toggleControlState();
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (BebopBro.get().getFlyingState() == FlyingState.LANDED)
                BebopBro.get().takeOff();
            else BebopBro.get().land();
        } else return super.dispatchKeyEvent(event);

        return true;
    }

    @Override
    public void create() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLES20.glEnable(GL20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        scene = new Scene(BebopBro.getVideoWidth(), BebopBro.getVideoHeight());
        scene.create(metrics, getResources());

        BebopBro.get().register(scene);
        BebopBro.get().setVideoSurface(scene.getBackgroundSurface());

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(scene);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorProvider.unregister(sensorDroneDriver);
    }
}