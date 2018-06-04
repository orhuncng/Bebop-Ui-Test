package com.trio.drone.vr;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.view.Surface;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.R;
import com.trio.drone.bebop.BebopEventListener;
import com.trio.drone.bebop.ControlState;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;
import com.trio.drone.vr.elements.*;
import com.trio.drone.vr.util.LimitedData;

import java.util.ArrayList;
import java.util.List;

public class Scene implements SceneMediator, BebopEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    static final float BATCH_SCALE = 0.75f;

    private List<SceneListener> listeners = new ArrayList<>();
    private OverlayTexture background;
    private SpriteBatch batch;

    private ADI adi = new ADI();
    private Ring altitudeRing = new Ring("ALTITUDE", "m", "VERT SPD", "m/s", false);
    private Ring speedRing = new Ring("SPEED", "m/s", "ACCEL", "m/s", true);
    private Battery battery = new Battery();
    private Wifi wifi = new Wifi();
    private Location location = new Location();
    private OperatingState operatingState = new OperatingState();

    private long speedEventTime = System.currentTimeMillis();
    private float prevSpeed = 0f;

    private Resources resources;

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

        resources = res;

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
    public void onBatteryStateChanged(int batteryLevel)
    {
        battery.setLevel((float) batteryLevel / 100f);
    }

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
        speedRing.setRingValue(currentSpeed);

        long elapsedTime = System.currentTimeMillis() - speedEventTime;
        speedRing.setOutlierValue((currentSpeed - prevSpeed) / (elapsedTime / 1000.f));

        speedEventTime = elapsedTime;
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
    public void onRelativeAltitudeChanged(float altitude) { altitudeRing.setRingValue(altitude); }

    @Override
    public void onCameraOrientationChanged(float tiltPerc, float panPerc) { }

    @Override
    public void onRelativeMotionEnded(float dX, float dY, float dZ, RelativeMotionResult result) { }

    @Override
    public void onControllerStateChanged(boolean isRunning) { }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(resources.getString(R.string.pref_key_ui_roll_limit)))
            adi.setRollLimit((float) sharedPreferences.getInt(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_pitch_limit)))
            adi.setPitchLimit((float) sharedPreferences.getInt(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_yaw_limit)))
            adi.setYawLimit((float) sharedPreferences.getInt(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_speed_limit)))
            speedRing.setRingValueLimit(sharedPreferences.getFloat(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_accel_limit)))
            speedRing.setOutlierValueLimit(sharedPreferences.getFloat(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_altitude_limit)))
            altitudeRing.setRingValueLimit(sharedPreferences.getFloat(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_vert_speed_limit)))
            LimitedData.setAlertLimit((float) sharedPreferences.getInt(key, 1));
        else if (key.equals(resources.getString(R.string.pref_key_ui_hectic_alert_perc)))
            LimitedData.setHecticAlertLimit((float) sharedPreferences.getInt(key, 1));
    }
}
