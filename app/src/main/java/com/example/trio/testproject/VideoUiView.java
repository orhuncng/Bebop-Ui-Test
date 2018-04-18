package com.example.trio.testproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_EMERGENCY;

public class VideoUiView extends LinearLayout {

    private final UiUpdater uiUpdater = new UiUpdater();
    private CanvasQuad canvasQuad;
    private static Paint paintWhite = new Paint();
    private static Paint paintCyan = new Paint();
    private static String batteryLevel;
    private static String wifiSignal;
    private static String pilotingState;
    private static String latitude;
    private static String longitude;
    private static String posAlt;
    private static String speedX;
    private static String speedY;
    private static String speedZ;
    private static String roll;
    private static String pitch;
    private static String yaw;
    private static String altitude;
    private static String cameraTilt;
    private static String cameraPan;

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
        paintWhite.setColor(Color.WHITE);
        paintWhite.setStrokeWidth(4.0f);
        paintCyan.setColor(Color.CYAN);
        paintCyan.setStrokeWidth(4.0f);

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
        glCanvas.drawARGB(255, 0, 0, 0);
        // Have Android render the child views.

       // glCanvas.drawLine(10, 10, 500, 500, paintWhite);
      //  glCanvas.drawLine(500, 20, 90, 500, paintCyan);

        super.dispatchDraw(glCanvas);
        // Commit the changes.
        canvasQuad.unlockCanvasAndPost(glCanvas);
    }

    public SurfaceTexture.OnFrameAvailableListener getFrameListener() {
        return uiUpdater;
    }

    public void setBatteryLevel(Integer batteryLevel) { this.batteryLevel = String.valueOf(batteryLevel); }
    public void setWifiSignal(short wifiSignal) { this.wifiSignal = String.valueOf(wifiSignal) +" dbm"; }

    public void setPilotingState(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM pilotingState) {
        switch (pilotingState) {
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_EMERGENCY: {this.pilotingState = ("Emergency"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_EMERGENCY_LANDING: {this.pilotingState=("Emergency Landing"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING: {this.pilotingState=("Flying"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING: {this.pilotingState=("Hovering"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED: {this.pilotingState=("Landed"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDING: {this.pilotingState=("Landing"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_MOTOR_RAMPING: {this.pilotingState=("Motor Ramping"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_TAKINGOFF: {this.pilotingState=("Taking Off"); break;}
            case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_USERTAKEOFF: {this.pilotingState=("User Take Off"); break;}
        }
    }
    public void setLatitude(double latitude) { this.latitude=(String.valueOf(latitude)); }
    public void setLongitude(double longitude) { this.longitude=(String.valueOf(longitude)); }
    public void setPosAlt(double posAlt) { this.posAlt=(String.valueOf(posAlt)); }
    public void setSpeedX(float speedX) { this.speedX=(String.valueOf(speedX)); }
    public void setSpeedY(float speedY) { this.speedY=(String.valueOf(speedY)); }
    public void setSpeedZ(float speedZ) { this.speedZ=(String.valueOf(speedZ)); }
    public void setRoll(float roll) { this.roll=(String.valueOf(roll)); }
    public void setPitch(float pitch) { this.pitch=(String.valueOf(pitch)); }
    public void setYaw(float yaw) { this.yaw=(String.valueOf(yaw)); }
    public void setAltitude(double altitude) { this.altitude=(String.valueOf(altitude)); }
    public void setCameraTilt(byte cameraTilt) { this.cameraTilt=(String.valueOf(cameraTilt)); }
    public void setCameraPan(byte cameraPan) { this.cameraPan=(String.valueOf(cameraPan)); }

    private final class UiUpdater implements SurfaceTexture.OnFrameAvailableListener {
        // onFrameAvailable is called on an arbitrary thread, but we can only access mediaPlayer on the
        // main thread.
        private Runnable uiThreadUpdater = new Runnable() {
            @Override
            public void run() {
                if (canvasQuad != null) {

                    ((TextView)findViewById(R.id.twBattery)).setText(batteryLevel);
                    ((TextView)findViewById(R.id.twWifi)).setText(wifiSignal);
                    ((TextView)findViewById(R.id.twPilotingState)).setText(pilotingState);
                    ((TextView)findViewById(R.id.twLat)).setText(latitude);
                    ((TextView)findViewById(R.id.twLong)).setText(longitude);
                    ((TextView)findViewById(R.id.twPosAlt)).setText(posAlt);
                    ((TextView)findViewById(R.id.twSpeedX)).setText(speedX);
                    ((TextView)findViewById(R.id.twSpeedY)).setText(speedY);
                    ((TextView)findViewById(R.id.twSpeedZ)).setText(speedZ);
                    ((TextView)findViewById(R.id.twRoll)).setText(roll);
                    ((TextView)findViewById(R.id.twPitch)).setText(pitch);
                    ((TextView)findViewById(R.id.twYaw)).setText(yaw);
                    ((TextView)findViewById(R.id.twAltitude)).setText(altitude);
                    ((TextView)findViewById(R.id.twTilt)).setText(cameraTilt);
                    ((TextView)findViewById(R.id.twPan)).setText(cameraPan);


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
