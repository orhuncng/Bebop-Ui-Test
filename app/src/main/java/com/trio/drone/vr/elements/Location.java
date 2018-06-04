package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.vr.SceneListener;
import com.trio.drone.vr.util.GdxUtils;

public class Location implements SceneListener
{
    private float latitude = 0.0004353f;
    private float longitude = 0.0004353f;
    private float altitude = 3405;

    private float centerX;
    private float centerY;

    public void setLatitude(float latitude) { this.latitude = latitude; }

    public void setLongitude(float longitude) { this.longitude = longitude; }

    public void setAltitude(float altitude) { this.altitude = altitude; }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        centerX = (metrics.widthPixels / 2f) - 100f;
        centerY = (metrics.heightPixels / 2f) - 450f;
    }

    @Override
    public void update() { }

    @Override
    public void draw(SpriteBatch batch)
    {
        GdxUtils.getInstance().getFont18().draw(batch, "LATITUDE: " + String.valueOf(latitude),
                centerX, centerY + 60f);

        GdxUtils.getInstance().getFont18().draw(batch, "LONGITUDE: " + String.valueOf(longitude),
                centerX - 18f, centerY + 30f);

        GdxUtils.getInstance().getFont18().draw(batch, "GPS ALTITUDE: " + String.valueOf(altitude),
                centerX - 40f, centerY);
    }

    @Override
    public void shutdown() { }
}
