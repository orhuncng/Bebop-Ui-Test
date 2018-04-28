package com.trio.dronetest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.support.annotation.AnyThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import com.trio.drone.R;

public class OverlayView extends View
{
    private static Paint paintWhite = new Paint();
    private static Paint paintCyan = new Paint();
    private static Paint paintOrange = new Paint();
    private static Paint paintRed = new Paint();
    private CanvasQuad canvasQuad;
    private float counter = 0.0f;
    private UiUpdater uiUpdater;

    public OverlayView(Context context, AttributeSet attrs) { super(context, attrs); }

    public OverlayView(Context context, ViewGroup parent)
    {
        super(context);
        Context theme = new ContextThemeWrapper(context, R.style.AppTheme);
        inflate(theme, R.layout.video_ui, null);
        parent.addView(this);
        paintWhite.setColor(Color.WHITE);

        paintWhite.setStrokeWidth(12.0f);
        paintCyan.setColor(Color.CYAN);
        paintCyan.setStrokeWidth(12.0f);
        paintOrange.setColor(Color.argb(255, 250, 160, 0));
        paintOrange.setStrokeWidth(6.0f);
        paintRed.setColor(Color.RED);
        paintRed.setStrokeWidth(12.0f);
        uiUpdater = new UiUpdater();
        canvasQuad = new CanvasQuad();
        setLayoutParams(CanvasQuad.getLayoutParams());
        setVisibility(View.VISIBLE);
    }

    @Override
    public void dispatchDraw(Canvas androidUiCanvas)
    {
        if (canvasQuad == null) {
            super.dispatchDraw(androidUiCanvas);
            return;
        }

        Canvas glCanvas = canvasQuad.lockCanvas();

        if (glCanvas == null) {
            postInvalidate();
            return;
        }

        glCanvas.drawARGB(255, 0, 0, 0);

        Log.e("canvas is ha", String.valueOf(glCanvas.isHardwareAccelerated()));
        glCanvas.save();
        glCanvas.translate(500, 500);
        glCanvas.rotate(counter);
        glCanvas.drawRoundRect(500.0f, 500.0f, -500.0f, -500.0f, 20.0f, 20.0f, paintWhite);
        float cPitch = 1.5f * 500;
        glCanvas.drawLine(-500, cPitch, 500, cPitch, paintRed);
        for (int i = 0; i < 30; i++) {
            float cRelPitch = cPitch + (75.0f * (i - 15));
            glCanvas.drawLine(-100, cRelPitch, 100, cRelPitch, paintOrange);
        }
        glCanvas.drawLine(-1.5f * 500, -500, -1.5f * 500, 500, paintCyan);
        glCanvas.restore();

        super.dispatchDraw(glCanvas);

        canvasQuad.unlockCanvasAndPost(glCanvas);
    }

    public void glInit()
    {
        canvasQuad.glInit(uiUpdater);
    }

    public void glDraw() { canvasQuad.glDraw(0.7f); }

    public void glShutdown() { canvasQuad.glShutdown(); }

    private final class UiUpdater implements SurfaceTexture.OnFrameAvailableListener
    {
        // onFrameAvailable is called on an arbitrary thread, but we can only access mediaPlayer
        // on the
        // main thread.
        private Runnable uiThreadUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                if (canvasQuad != null) {

                    counter += 0.1;
                    invalidate();
                }
            }
        };

        @AnyThread
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture)
        {
            post(uiThreadUpdater);
        }
    }
}
