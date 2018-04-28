package com.trio.dronetest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import com.badlogic.gdx.graphics.GL20;
import com.google.vr.sdk.base.*;
import com.trio.drone.R;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main5Activity extends GvrActivity
        implements GvrView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener
{
    static final int COORDS_PER_VERTEX = 2;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;
    private static final float CAMERA_Z = 0.01f;
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    static float squareVertices[] = { // in counterclockwise order:
            -1.0f, -1.0f,   // 0.left - mid
            1.0f, -1.0f,   // 1. right - mid
            -1.0f, 1.0f,   // 2. left - top
            1.0f, 1.0f,   // 3. right - top
    };
    static float textureVertices[] = {0.0f, 1.0f,  // A. left-bottom
            1.0f, 1.0f,  // B. right-bottom
            0.0f, 0.0f,  // C. left-top
            1.0f, 0.0f   // D. right-top
    };
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final AtomicBoolean phoneFrameAvailable = new AtomicBoolean();
    private final String vertexShaderCode =
            "attribute vec4 position;" + "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" + "void main()" + "{" +
                    "gl_Position = position;" + "textureCoordinate = inputTextureCoordinate;" + "}";
    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" + "precision mediump float;" +
                    "varying vec2 textureCoordinate;" + "uniform samplerExternalOES s_texture;" +
                    "void main(void) {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );" + "}";
    private Camera camera;
    private int phoneTexId;
    private SurfaceTexture mPhoneTexture;
    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private short drawOrder[] = {0, 2, 1, 1, 2, 3};
    private long frameTime;
    private OverlayView overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        setContentView(com.trio.drone.R.layout.activity_main5);

        GvrView gvrView = findViewById(R.id.hud5view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setRenderer(this);

        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
        overlayView = new OverlayView(this, gvrView);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {

    }

    @Override
    public void onNewFrame(HeadTransform headTransform)
    {
        if (phoneFrameAvailable.get()) {
            mPhoneTexture.updateTexImage();
            phoneFrameAvailable.set(false);
        }

        long currentFrame = SystemClock.elapsedRealtime();
        Log.w("Main5Activity fps:", String.valueOf(1000.0f / (currentFrame - frameTime)));
        frameTime = currentFrame;
    }

    @Override
    public void onDrawEye(Eye eye)
    {
        GLES20.glEnable(GL20.GL_DEPTH_TEST);
        GLES20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, phoneTexId);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);


        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, textureVerticesBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT,
                drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        overlayView.glDraw();
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void onFinishFrame(Viewport viewport)
    {

    }

    @Override
    public void onSurfaceChanged(int width, int height)
    {

    }

    @Override
    public void onSurfaceCreated(EGLConfig config)
    {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLUtils.checkGlError();

        ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareVertices);
        vertexBuffer.position(0);


        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        int vertexShader = GLUtils.loadGLShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLUtils.loadGLShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);

        // Create the texture used to render each frame of video.
        phoneTexId = GLUtils.glCreateExternalTexture();
        mPhoneTexture = new SurfaceTexture(phoneTexId);
        GLUtils.checkGlError();

        // When the video decodes a new frame, tell the GL thread to update the image.
        mPhoneTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener()
        {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture)
            {
                phoneFrameAvailable.set(true);
            }
        });

        camera = Camera.open();

        Camera.Size cSize = camera.getParameters().getPreviewSize();

        try {
            mPhoneTexture.setDefaultBufferSize(cSize.width, cSize.height);
            camera.setPreviewTexture(mPhoneTexture);
            camera.startPreview();
        } catch (IOException ioe) {
            Log.w("Main5Activity", "CAM LAUNCH FAILED");
        }

        overlayView.glInit();
    }

    @Override
    public void onRendererShutdown()
    {
        overlayView.glShutdown();
    }
}