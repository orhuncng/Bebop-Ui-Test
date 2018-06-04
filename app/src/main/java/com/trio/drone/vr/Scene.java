package com.trio.drone.vr;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.view.Surface;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;
import com.trio.drone.vr.elements.*;

import java.util.ArrayList;
import java.util.List;

public class Scene implements SceneMediator, BebopEventListener
{
    static final float BATCH_SCALE = 0.75f;

    private List<SceneListener> listeners = new ArrayList<>();
    private OverlayTexture background;
    private SpriteBatch batch;

    private ADI adi = new ADI();
    private Ring altitudeRing = new Ring(1.5f, 0.4f, "ALTITUDE", "m", "VERT SPD", "m/s", false);
    private Ring speedRing = new Ring(0.7f, 1f, "SPEED", "m/s", "ACCEL", "", true);
    private Battery battery = new Battery();
    private Wifi wifi = new Wifi();
    private Location location = new Location();
    private OperatingState operatingState = new OperatingState();

    private float prevSpeed;

    public Scene(int width, int height)
    {
        batch = new SpriteBatch();
        background = new OverlayTexture(false, .01f, width, height);

        register(background);
        register(adi);
        register(altitudeRing);
        register(speedRing);
        register(battery);
        register(wifi);
        register(location);
        register(operatingState);
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

        float posX = (1f - BATCH_SCALE) * metrics.widthPixels / 2f;
        float posY = (1f - BATCH_SCALE) * metrics.heightPixels / 2f;

        batch.getTransformMatrix().setToTranslationAndScaling(
                posX, posY, 0f, BATCH_SCALE, BATCH_SCALE, 1f);

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
    public void onBatteryStateChanged(int batteryLevel) { battery.setLevel((float) batteryLevel / 100f); }

    @Override
    public void onWifiSignalChanged(int rssi) { wifi.setRssi(rssi); }

    @Override
    public void onFlyingStateChanged(FlyingState flyingState)
    {
        operatingState.setFlyingState(flyingState);
    }

    @Override
    public void onControlStateChanged(ControlState controlState)
    {
        operatingState.setControlState(controlState);
    }

    @Override
    public void onPositionChanged(float latitude, float longitude, float altitude)
    {
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
    }

    @Override
    public void onSpeedChanged(float x, float y, float z)
    {
        float currentSpeed = (float) Math.sqrt(((double) (x * x + y * y)));
        speedRing.setValue(currentSpeed);
        speedRing.setOutlierValue(currentSpeed - prevSpeed);
        prevSpeed = currentSpeed;
        altitudeRing.setOutlierValue(z);
    }

    @Override
    public void onOrientationChanged(float roll, float pitch, float yaw)
    {
        adi.setPitch((pitch / (float) Math.PI) * 180f);
        adi.setRoll((roll / (float) Math.PI) * 180f);
        adi.setYaw((yaw / (float) Math.PI) * 180f);
    }

    @Override
    public void onRelativeAltitudeChanged(float altitude) { altitudeRing.setValue(altitude); }

    @Override
    public void onCameraOrientationChanged(float tiltPerc, float panPerc) { }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result) { }

    @Override
    public void onControllerStateChanged(boolean isRunning) { }
}
