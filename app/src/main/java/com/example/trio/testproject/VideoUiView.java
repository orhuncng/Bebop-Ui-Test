package com.example.trio.testproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoUiView extends LinearLayout {

    private final UiUpdater uiUpdater = new UiUpdater();
    private CanvasQuad canvasQuad;

    VideoUiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @MainThread
    public static VideoUiView createForOpenGl(Context context, ViewGroup parent, CanvasQuad quad) {
        Context theme = new ContextThemeWrapper(context, R.style.AppTheme);

        VideoUiView view = (VideoUiView) View.inflate(theme, R.layout.video_ui, null);
        view.canvasQuad = quad;
        view.setLayoutParams(CanvasQuad.getLayoutParams());
        view.setVisibility(View.VISIBLE);
        parent.addView(view, 0);

        return view;
    }

    @Override
    public void dispatchDraw(Canvas androidUiCanvas) {
        if (canvasQuad == null) {
            // Handle non-VR rendering.
            super.dispatchDraw(androidUiCanvas);
            return;
        }

        // Handle VR rendering.
        Canvas glCanvas = canvasQuad.lockCanvas();
        if (glCanvas == null) {
            // This happens if Android tries to draw this View before GL initialization completes. We need
            // to retry until the draw call happens after GL invalidation.
            postInvalidate();
            return;
        }

        // Clear the canvas first.
        glCanvas.drawColor(Color.BLACK);
        // Have Android render the child views.
        super.dispatchDraw(glCanvas);
        // Commit the changes.
        canvasQuad.unlockCanvasAndPost(glCanvas);
    }

    public SurfaceTexture.OnFrameAvailableListener getFrameListener() {
        return uiUpdater;
    }

    private final class UiUpdater implements SurfaceTexture.OnFrameAvailableListener {
        // onFrameAvailable is called on an arbitrary thread, but we can only access mediaPlayer on the
        // main thread.
        private Runnable uiThreadUpdater = new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.status_text);
                tv.setText("asdas");

                if (canvasQuad != null) {
                    // When in VR, we will need to manually invalidate this View.
                    invalidate();
                }
            }
        };

        @AnyThread
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            post(uiThreadUpdater);
        }
    }
}
