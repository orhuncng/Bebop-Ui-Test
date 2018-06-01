package com.trio.drone.vr;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trio.drone.vr.elements.FpsTestPattern;
import com.trio.drone.vr.elements.OverlayTexture;

import java.util.ArrayList;
import java.util.List;

public class Scene implements SceneMediator
{
    private List<SceneListener> listeners = new ArrayList<>();
    private OverlayTexture background;
    private SpriteBatch batch;

    public Scene(int backgroundTexWidth, int backgroundTextHeight)
    {
        batch = new SpriteBatch();
        background = new OverlayTexture(false, .01f, backgroundTexWidth, backgroundTextHeight);

        // fill listener array
        register(new FpsTestPattern());
    }

    public SurfaceTexture getBackgroundTexture() { return background.getTexture(); }

    @Override
    public void register(SceneListener listener) { listeners.add(listener); }

    @Override
    public void unregister(SceneListener listener) { listeners.remove(listener); }

    @Override
    public void create(DisplayMetrics metrics, Resources res)
    {
        metrics.widthPixels /= 2f;
        background.create(metrics, res);
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
        background.shutdown();
        for (SceneListener l : listeners) l.shutdown();
    }

}
