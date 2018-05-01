package com.trio.dronetest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.support.annotation.AnyThread;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.trio.drone.R;

public class OverlayView extends RelativeLayout
{
    private static Paint paintWhite = new Paint();
    private static Paint paintCyan = new Paint();
    private static Paint paintOrange = new Paint();
    private static Paint paintRed = new Paint();
    private GLCanvas glCanvas;
    private float counter = 0.0f;
    private UiUpdater uiUpdater;

    public OverlayView(Context context, AttributeSet attr) { super(context, attr); }

    public void onCreate(ViewGroup parent)
    {
        paintWhite.setColor(Color.WHITE);
        paintWhite.setStrokeWidth(12.0f);
        paintCyan.setColor(Color.CYAN);
        paintCyan.setStrokeWidth(12.0f);
        paintOrange.setColor(Color.argb(255, 250, 160, 0));
        paintOrange.setStrokeWidth(6.0f);
        paintOrange.setAntiAlias(true);
        paintRed.setColor(Color.RED);
        paintRed.setStrokeWidth(12.0f);
        uiUpdater = new UiUpdater();
        glCanvas = new GLCanvas();
        setLayoutParams(GLCanvas.getLayoutParams());
        setVisibility(View.VISIBLE);
        parent.addView(this, 0);
    }

    @Override
    public void dispatchDraw(Canvas androidUiCanvas)
    {
        if (glCanvas == null) {
            super.dispatchDraw(androidUiCanvas);
            return;
        }

        Canvas canvas = glCanvas.lock();

        if (canvas == null) {
            postInvalidate();
            return;
        }

        canvas.drawARGB(255, 0, 0, 0);

        canvas.save();
        canvas.translate(500, 500);
        canvas.rotate(counter);
        canvas.drawRoundRect(500.0f, 500.0f, -500.0f, -500.0f, 20.0f, 20.0f, paintWhite);
        float cPitch = 1.5f * 500;
        canvas.drawLine(-500, cPitch, 500, cPitch, paintRed);
        for (int i = 0; i < 2; i++) {
            float cRelPitch = cPitch + (75.0f * (i - 15));
            canvas.drawLine(-100, cRelPitch, 100, cRelPitch, paintOrange);
        }
        canvas.drawLine(-1.5f * 500, -500, -1.5f * 500, 500, paintCyan);
        canvas.restore();

        // draws children
        super.dispatchDraw(canvas);

        glCanvas.unlockAndPost(canvas);
    }

    public void initCanvas()
    {
        glCanvas.init(getResources(), uiUpdater);
    }

    public void draw() { glCanvas.draw(); }

    public void shutdown() { glCanvas.shutdown(); }

    private final class UiUpdater implements SurfaceTexture.OnFrameAvailableListener
    {
        private Runnable uiThreadUpdater = new Runnable()
        {
            @Override
            public void run()
            {
                if (glCanvas != null) {

                    counter += 0.25;
                    invalidate();
                    ((TextView) findViewById(R.id.textView17)).setText("asdasd");
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