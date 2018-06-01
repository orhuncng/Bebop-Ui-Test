package com.trio.drone.vr;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface SceneListener
{
    void create(DisplayMetrics metrics, Resources res);

    void update();

    void draw(SpriteBatch batch);

    void shutdown();
}
