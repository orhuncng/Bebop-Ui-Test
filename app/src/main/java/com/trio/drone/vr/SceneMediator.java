package com.trio.drone.vr;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public interface SceneMediator
{
    void register(SceneListener listener);

    void unregister(SceneListener listener);

    void create(DisplayMetrics metrics, Resources res);

    void update();

    void draw();

    void shutdown();
}
