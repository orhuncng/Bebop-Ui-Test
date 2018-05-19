package com.trio.dronetest;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;
import android.widget.FrameLayout;

import java.util.concurrent.atomic.AtomicBoolean;

public class GLCanvas
{
    private OverlayTexture overlayTexture;
    private Surface surface;
    private final AtomicBoolean surfaceDirty = new AtomicBoolean();

    public static FrameLayout.LayoutParams getLayoutParams()
    {
        return null;//OverlayTexture.getLayoutParams();
    }

    public Canvas lock()
    {
        if (surface == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return surface.lockHardwareCanvas();
        else return surface.lockCanvas(null);
    }

    public void unlockAndPost(Canvas canvas)
    {
        if (canvas == null || surface == null) {return;}

        surface.unlockCanvasAndPost(canvas);
        surfaceDirty.set(true);
    }

    void init() { }

    void init(Resources res, SurfaceTexture.OnFrameAvailableListener listener)
    {
        //overlayTexture = new OverlayTexture(true, 0);
        surface = overlayTexture.createSurface(res, listener);
    }

    void draw()
    {
        if (surfaceDirty.compareAndSet(true, false))
            overlayTexture.updateTexImage();

        overlayTexture.draw();
    }

    void shutdown() { overlayTexture.shutdown(); }
}
