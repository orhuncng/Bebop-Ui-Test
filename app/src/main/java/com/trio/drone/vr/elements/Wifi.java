package com.trio.drone.vr.elements;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.vr.SceneListener;
import com.trio.drone.vr.util.GdxUtils;

import java.util.HashMap;
import java.util.Map;

public class Wifi implements SceneListener
{
    private Sprite sprite;
    private int rssi;
    private RssiLevel rssiLevel;
    private Map<RssiLevel, String> rssiLabels;
    private SparseArray<RssiLevel> rssiLevelLimits;

    public Wifi()
    {
        rssiLevel = RssiLevel.AMAZING;

        rssiLabels = new HashMap<>();

        rssiLabels.put(RssiLevel.POOR, "POOR");
        rssiLabels.put(RssiLevel.NOT_GOOD, "NOT GOOD");
        rssiLabels.put(RssiLevel.NORMAL, "NORMAL");
        rssiLabels.put(RssiLevel.VERY_GOOD, "VERY GOOD");
        rssiLabels.put(RssiLevel.AMAZING, "AMAZING");

        rssiLevelLimits = new SparseArray<>();

        rssiLevelLimits.put(-90, RssiLevel.POOR);
        rssiLevelLimits.put(-80, RssiLevel.NOT_GOOD);
        rssiLevelLimits.put(-70, RssiLevel.NORMAL);
        rssiLevelLimits.put(-58, RssiLevel.VERY_GOOD);
        rssiLevelLimits.put(-40, RssiLevel.AMAZING);
    }

    public void setRssi(int rssi)
    {
        this.rssi = rssi;

        rssiLevel = RssiLevel.POOR;

        for (int i = 0; i < rssiLevelLimits.size(); i++)
            if (rssi > rssiLevelLimits.keyAt(i))
                rssiLevel = rssiLevelLimits.get(rssiLevelLimits.keyAt(i));
    }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        float centerX = (metrics.widthPixels / 2f) + 130f;
        float centerY = (metrics.heightPixels / 2f) + 5f;

        sprite = new Sprite(GdxUtils.getInstance().createSprite("wifi"));
        sprite.setPosition(centerX, centerY);
    }

    @Override
    public void update() { }

    @Override
    public void draw(SpriteBatch batch)
    {
        sprite.draw(batch);

        GdxUtils.getInstance().getFont18().draw(batch, "RSSI: " + String.valueOf(rssi),
                sprite.getX() + 40f, sprite.getY() + 10f);

        if (rssiLevel == RssiLevel.POOR)
            GdxUtils.getInstance().getFont18().setColor(Color.SCARLET);
        else if (rssiLevel == RssiLevel.NOT_GOOD)
            GdxUtils.getInstance().getFont18().setColor(Color.ORANGE);
        else
            GdxUtils.getInstance().getFont18().setColor(Color.LIME);

        GdxUtils.getInstance().getFont18().draw(batch, rssiLabels.get(rssiLevel),
                sprite.getX() + 40f, sprite.getY() + 30f);

        GdxUtils.getInstance().resetFont18Color();
    }

    @Override
    public void shutdown() { }

    private enum RssiLevel
    {
        AMAZING, VERY_GOOD, NORMAL, NOT_GOOD, POOR
    }
}
