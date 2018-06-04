package com.trio.drone.core;

import android.Manifest;
import android.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.graphics.GL20;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.trio.drone.bebop.BebopBro;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;
import com.trio.drone.vr.Scene;
import com.trio.drone.vr.util.AnimationState;
import com.trio.dronetest.DeviceSensorProvider;
import com.trio.dronetest.DeviceSensorViewModel;

import java.util.HashMap;

public class VRActivity extends CardBoardAndroidApplication
        implements CardBoardApplicationListener, BebopEventListener {
    private Scene scene;

    float currentTilt = 0f;
    float currentPan = 0f;
    float[] accelerationFilter = new float[3];

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
        
        if (accelerationFilter != null && accelerationFilter.length == 3) {

            if (BebopBro.get().getControlState() == ControlState.CAMERA_LOOKUP) {

                float interpolatedTilt = Math.round(-10 * accelerationFilter[2]);
                int tiltMovement = Math.round(currentTilt - interpolatedTilt);
                int toDegreeTilt = Math.round(interpolatedTilt);


                if (Math.abs(tiltMovement) > 5) {
                    BebopBro.get().move(0, toDegreeTilt, 0, 0);
                } else {
                    Log.e("No Cam current", Float.toString(currentTilt));
                }
                float interpolatedPan = Math.round(-10 * accelerationFilter[1]);
                int panMovement = Math.round(currentPan - interpolatedPan);
                int toDegreePan = Math.round(interpolatedPan);

                if (Math.abs(panMovement) > 5) {
                    BebopBro.get().move(-toDegreePan, 0, 0, 0);
                }
            } else if (BebopBro.get().getControlState() == ControlState.PILOTING) {
                //Log.e("Gidilen Yol", Float.toString(deltaX));

                int pitch = Math.round(accelerationFilter[2]);
                int roll = Math.round(accelerationFilter[1]);
                //int pitch = Math.round(accelerationFilter[0]);

                BebopBro.get().move(roll, pitch, 0, 0);
            }
        }


    }

    @Override
    public void onBackPressed() {
        BebopBro.get().setControlState(ControlState.CAMERA_LOOKUP);
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
    public void onBatteryStateChanged(int batteryLevel) {

    }

    @Override
    public void onWifiSignalChanged(int rssi) {

    }

    @Override
    public void onFlyingStateChanged(FlyingState flyingState) {

    }

    @Override
    public void onControlStateChanged(ControlState controlState) {

    }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude) {

    }

    @Override
    public void onSpeedChanged(float x, float y, float z) {

    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw) {

    }

    @Override
    public void onRelativeAltitudeChanged(float altitude) {

    }

    @Override
    public void onCameraOrientationChanged(float tiltPerc, float panPerc) {
        currentTilt = tiltPerc;
        currentPan = panPerc;
    }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result) {

    }

    @Override
    public void onControllerStateChanged(boolean isRunning) {

    }
}