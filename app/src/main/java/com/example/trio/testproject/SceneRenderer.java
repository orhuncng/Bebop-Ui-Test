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
import android.view.ViewGroup;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SceneRenderer {
    private static final String TAG = "SceneRenderer";

    // This is the primary interface between the Media Player and the GL Scene.
    private SurfaceTexture displayTexture;
    private final AtomicBoolean frameAvailable = new AtomicBoolean();
    // Used to notify clients that displayTexture has a new frame. This requires synchronized access.
    @Nullable
    private OnFrameAvailableListener externalFrameListener;

    private int displayTexId;
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
                    "varying vec2 textureCoordinate;                            \n" +
                    "uniform samplerExternalOES s_texture;               \n" +
                    "void main(void) {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
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


    // These are only valid if createForVR() has been called. In the 2D Activity, these are null
    // since the UI is rendered in the standard Android layout.
    @Nullable
    private final CanvasQuad canvasQuad;
    @Nullable
    private final VideoUiView videoUiView;
    @Nullable
    private final Handler uiHandler;

    SceneRenderer(
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

    public void glInit() {
        //GLUtils.checkGlError();

        // Set the background frame color. This is only visible if the display mesh isn't a full sphere.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GLUtils.checkGlError();



        /*ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
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
        GLES20.glLinkProgram(mProgram);*/


        // Create the texture used to render each frame of video.
        displayTexId = GLUtils.glCreateExternalTexture();
        displayTexture = new SurfaceTexture(displayTexId);
        GLUtils.checkGlError();


        // When the video decodes a new frame, tell the GL thread to update the image.
        displayTexture.setOnFrameAvailableListener(
                new OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        frameAvailable.set(true);

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
    }

    @AnyThread
    public synchronized @Nullable
    SurfaceTexture createDisplay(int width, int height) {
        if (displayTexture == null) {
            Log.e(TAG, ".createDisplay called before GL Initialization completed.");
            return null;
        }

        //displayTexture.setDefaultBufferSize(width, height);
        return displayTexture;
    }

    public int getTextureId() {
        return displayTexId;
    }

    public void updateTexture() {
        displayTexture.updateTexImage();
        GLUtils.checkGlError();
    }

    public void glDrawFrame(float[] viewProjectionMatrix, int eyeType) {
        // The uiQuad uses alpha.
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        canvasQuad.glDraw(viewProjectionMatrix, videoUiView.getAlpha());
        GLES20.glDisable(GLES20.GL_BLEND);
        // The uiQuad uses alpha.
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glEnable(GLES20.GL_BLEND);

        //if (frameAvailable.compareAndSet(true, false)) {
        //displayTexture.updateTexImage();
        //GLUtils.checkGlError();
        // }

        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        /*GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, displayTexId);

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

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);*/
        //GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        //GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //GLES20.glDisable(GLES20.GL_BLEND);
        //GLES20.glDisable(GLES20.GL_TEXTURE_2D);

        if (videoUiView != null) {
            //        canvasQuad.glDraw(viewProjectionMatrix, videoUiView.getAlpha());
        }
    }

    public void glShutdown() {
        if (canvasQuad != null) {
            canvasQuad.glShutdown();
        }
    }
}