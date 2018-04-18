package com.example.trio.testproject;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.ViewGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SceneRenderer {
    private static final String TAG = "SceneRenderer";

    // This is the primary interface between the Media Player and the GL Scene.
    private Surface mDroneSurface;
    private SurfaceTexture mDroneTexture;
    private Surface mPhoneSurface;
    private SurfaceTexture mPhoneTexture;
    private final AtomicBoolean droneFrameAvailable = new AtomicBoolean();
    private final AtomicBoolean phoneFrameAvailable = new AtomicBoolean();

    @Nullable
    private OnFrameAvailableListener externalFrameListener;

    private int droneTexId;
    private int phoneTexId;
    private final String vertexShaderCode =
            "attribute vec4 position;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{" +
                    "gl_Position = position;" +
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;" +
                    "uniform samplerExternalOES s_texture;" +
                    "void main(void) {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );" +
                    "}";

    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    static final int COORDS_PER_VERTEX = 2;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;

    static float squareVertices[] = { // in counterclockwise order:
            -1.0f, -1.0f,   // 0.left - mid
            1.0f, -1.0f,   // 1. right - mid
            -1.0f, 1.0f,   // 2. left - top
            1.0f, 1.0f,   // 3. right - top
    };

    private short drawOrder[] = {0, 2, 1, 1, 2, 3};

    static float textureVertices[] = {
            0.0f, 1.0f,  // A. left-bottom
            1.0f, 1.0f,  // B. right-bottom
            0.0f, 0.0f,  // C. left-top
            1.0f, 0.0f   // D. right-top
    };


    @Nullable
    private final CanvasQuad canvasQuad;
    @Nullable
    private final VideoUiView videoUiView;
    @Nullable
    private final Handler uiHandler;

    public void toggleDroneCameraEnabled() {
        droneCameraEnabled = !droneCameraEnabled;
    }

    private boolean droneCameraEnabled;

    private SceneRenderer(
            CanvasQuad canvasQuad, VideoUiView videoUiView, Handler uiHandler,
            SurfaceTexture.OnFrameAvailableListener externalFrameListener) {
        this.canvasQuad = canvasQuad;
        this.videoUiView = videoUiView;
        this.uiHandler = uiHandler;
        this.externalFrameListener = externalFrameListener;
    }

    @MainThread
    public static Pair<SceneRenderer, VideoUiView> createForVR(Context context, ViewGroup parent) {
        CanvasQuad canvasQuad = new CanvasQuad();
        VideoUiView videoUiView = VideoUiView.createForOpenGl(context, parent, canvasQuad);
        OnFrameAvailableListener externalFrameListener = videoUiView.getFrameListener();

        SceneRenderer scene = new SceneRenderer(
                canvasQuad, videoUiView, new Handler(Looper.getMainLooper()), externalFrameListener);
        return Pair.create(scene, videoUiView);
    }

    public void onSurfaceCreated() {
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
        droneTexId = GLUtils.glCreateExternalTexture();
        mDroneTexture = new SurfaceTexture(droneTexId);
        GLUtils.checkGlError();

        // When the video decodes a new frame, tell the GL thread to update the image.
        mDroneTexture.setOnFrameAvailableListener(
                new OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        droneFrameAvailable.set(true);

                        synchronized (SceneRenderer.this) {
                            if (externalFrameListener != null) {
                                externalFrameListener.onFrameAvailable(surfaceTexture);
                            }
                        }
                    }
                });

        if (canvasQuad != null) {
            canvasQuad.glInit();
        }

        mDroneSurface = new Surface(mDroneTexture);

        // Create the texture used to render each frame of video.
        phoneTexId = GLUtils.glCreateExternalTexture();
        mPhoneTexture = new SurfaceTexture(phoneTexId);
        GLUtils.checkGlError();

        // When the video decodes a new frame, tell the GL thread to update the image.
        mPhoneTexture.setOnFrameAvailableListener(
                new OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        phoneFrameAvailable.set(true);

                        synchronized (SceneRenderer.this) {
                            if (externalFrameListener != null) {
                                externalFrameListener.onFrameAvailable(surfaceTexture);
                            }
                        }
                    }
                });

        mPhoneSurface = new Surface(mPhoneTexture);

        Log.d("scene renderer", "initialized");

    }

    @AnyThread
    public synchronized @Nullable
    Surface getDroneCamTexture(int width, int height) {
        if (mDroneTexture == null) {
            Log.e(TAG, ".getDroneCamTexture called before GL Initialization completed.");
            return null;
        }

        mDroneTexture.setDefaultBufferSize(width, height);
        return mDroneSurface;
    }

    @AnyThread
    public synchronized @Nullable
    SurfaceTexture getPhoneCamTexture(int width, int height) {
        if (mPhoneTexture == null) {
            Log.e(TAG, ".getPhoneCamTexture called before GL Initialization completed.");
            return null;
        }

        mPhoneTexture.setDefaultBufferSize(width, height);
        return mPhoneTexture;
    }

    public void updateTexture() {
        if (droneCameraEnabled)
            mDroneTexture.updateTexImage();
        else
            mPhoneTexture.updateTexImage();
        GLUtils.checkGlError();
    }

    public void draw(float[] viewProjectionMatrix) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        if (droneCameraEnabled)
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, droneTexId);
        else
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, phoneTexId);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer);


        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, textureVerticesBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        canvasQuad.glDraw(0.7f);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void glShutdown() {
        if (canvasQuad != null) {
            canvasQuad.glShutdown();
        }
    }
}