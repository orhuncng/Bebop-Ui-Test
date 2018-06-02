package com.trio.drone.vr;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.view.Surface;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;
import com.trio.drone.vr.elements.ADI;
import com.trio.drone.vr.elements.OverlayTexture;

import java.util.ArrayList;
import java.util.List;

public class Scene implements SceneMediator, BebopEventListener
{
    private List<SceneListener> listeners = new ArrayList<>();
    private OverlayTexture background;
    private SpriteBatch batch;

    private ADI adi = new ADI();

    public Scene(int width, int height)
    {
        batch = new SpriteBatch();
        background = new OverlayTexture(false, .01f, width, height);

        // fill listener array
        register(background);
        register(new ADI());
    }

    public SurfaceTexture getBackgroundTexture() { return background.getTexture(); }

    public Surface getBackgroundSurface() { return background.getSurface(); }

    @Override
    public void register(SceneListener listener) { listeners.add(listener); }

    @Override
    public void unregister(SceneListener listener) { listeners.remove(listener); }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        metrics.widthPixels /= 2f;
        for (SceneListener l : listeners) l.create(metrics, res);
    }

    @Override
    public void update() { for (SceneListener l : listeners) l.update(); }

    @Override
    public void draw()
    {
        background.draw();

        batch.begin();
        for (SceneListener l : listeners) l.draw(batch);
        batch.end();
    }

    @Override
    public void shutdown()
    {
        for (SceneListener l : listeners) l.shutdown();
    }

    @Override
    public void onBatteryStateChanged(int batteryLevel)
    {

    }

    @Override
    public void onWifiSignalChanged(int rssi)
    {

    }

    @Override
    public void onFlyingStateChanged(FlyingState flyingState)
    {

    }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude)
    {

    }

    @Override
    public void onSpeedChanged(float x, float y, float z)
    {

    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw)
    {
        adi.setPitch((pitch / (float) Math.PI) * 180f);
        adi.setRoll((roll / (float) Math.PI) * 180f);
    }

    @Override
    public void onRelativeAltitudeChanged(float altitude)
    {

    }

    @Override
    public void onCameraOrientationChanged(float tiltPerc, float panPerc)
    {

    }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result)
    {

    }

    @Override
    public void onControllerStateChanged(boolean isRunning)
    {

    }
}
