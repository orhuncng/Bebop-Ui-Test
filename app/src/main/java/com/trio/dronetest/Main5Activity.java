package com.trio.dronetest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import com.badlogic.gdx.graphics.GL20;
import com.google.vr.sdk.base.*;
import com.trio.drone.R;
import com.trio.drone.vr.GLUtils;
import com.trio.drone.vr.OverlayTexture;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main5Activity extends GvrActivity implements GvrView.StereoRenderer
{
    private long frameTime;
    private OverlayView overlayView;
    private final AtomicBoolean phoneFrameAvailable = new AtomicBoolean();
    private OverlayTexture overlayTexture;

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

        setContentView(com.trio.drone.R.layout.activity_main5);

        GvrView gvrView = findViewById(R.id.hud5view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setRenderer(this);

        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
        Context theme = new ContextThemeWrapper(this, R.style.AppTheme);
        overlayView = (OverlayView) View.inflate(theme, R.layout.overlay_ui, null);
        overlayView.onCreate(gvrView);

        //overlayTexture = new OverlayTexture(true, 0.01f);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform)
    {
        long currentFrame = SystemClock.elapsedRealtime();
        //Log.w("Main5Activity fps:", String.valueOf(1000.0f / (currentFrame - frameTime)));
        frameTime = currentFrame;

        if (phoneFrameAvailable.compareAndSet(true, false))
            overlayTexture.updateTexImage();
    }

    @Override
    public void onDrawEye(Eye eye)
    {
        GLES20.glEnable(GL20.GL_DEPTH_TEST);
        GLES20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        GLES20.glDisable(GLES20.GL_BLEND);
        overlayTexture.draw();

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        overlayView.draw();
    }

    @Override
    public void onFinishFrame(Viewport viewport)
    {

    }

    @Override
    public void onSurfaceChanged(int width, int height)
    {

    }

    @Override
    public void onSurfaceCreated(EGLConfig config)
    {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        overlayView.initCanvas();
        GLUtils.checkGlError();

        SurfaceTexture.OnFrameAvailableListener listener =
                new SurfaceTexture.OnFrameAvailableListener()
                {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture)
                    {
                        phoneFrameAvailable.set(true);
                    }
                };


        Camera camera = Camera.open();
        Camera.Size cSize = camera.getParameters().getPreviewSize();

        //overlayTexture.createSurface(getResources(), listener, cSize.width, cSize.height);

        try {
            camera.setPreviewTexture(overlayTexture.getTexture());
            camera.startPreview();
        } catch (IOException ioe) {
            Log.w("Main5Activity", "CAM LAUNCH FAILED");
        }
    }

    @Override
    public void onRendererShutdown()
    {
        overlayView.shutdown();
    }
}