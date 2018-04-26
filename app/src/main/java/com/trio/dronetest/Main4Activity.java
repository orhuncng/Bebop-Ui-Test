package com.trio.dronetest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.CardBoardAndroidApplication;
import com.badlogic.gdx.backends.android.CardBoardApplicationListener;
import com.badlogic.gdx.backends.android.CardboardCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main4Activity extends CardBoardAndroidApplication
        implements CardBoardApplicationListener, SurfaceTexture.OnFrameAvailableListener
{
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.0f;
    private static final float CAMERA_Z = 0.01f;
    private CardboardCamera cam;
    private Model model;
    private ModelInstance instance;
    private ModelBatch batch;
    private Environment environment;
    static final int COORDS_PER_VERTEX = 2;
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
    private Surface mPhoneSurface;
    private SurfaceTexture mPhoneTexture;
    private ShapeRenderer shapeRenderer;
    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private short drawOrder[] = {0, 2, 1, 1, 2, 3};

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
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;
        initialize(this, config);
    }

    @Override
    public void create()
    {
        cam = new CardboardCamera();
        cam.position.set(0f, 0f, CAMERA_Z);
        cam.lookAt(0, 0, 0);
        cam.near = Z_NEAR;
        cam.far = Z_FAR;

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.translate(0, 0, -50);

        shapeRenderer = new ShapeRenderer();

        batch = new ModelBatch();

        onSurfaceCreated();
    }

    @Override
    public void resize(int width, int height)
    {
    }

    @Override
    public void render()
    {
    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void dispose()
    {
        batch.dispose();
        model.dispose();
    }

    @Override
    public void onNewFrame(HeadTransform paramHeadTransform)
    {
        instance.transform.rotate(0, 1, 0, Gdx.graphics.getDeltaTime() * 30);
        updateTexture();
    }

    @Override
    public void onDrawEye(Eye eye)
    {
        //Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
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


        // Apply the eye transformation to the camera.
        /*cam.setEyeViewAdjustMatrix(new Matrix4(eye.getEyeView()));

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        cam.setEyeProjection(new Matrix4(perspective));
        cam.update();*/

        //batch.begin(cam);
        //batch.render(instance, environment);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(500, 500, 300, 20);
        shapeRenderer.end();
        //batch.end();
    }

    @Override
    public void onFinishFrame(Viewport paramViewport)
    {

    }

    public void onSurfaceCreated()
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

        mPhoneSurface = new Surface(mPhoneTexture);

        camera = Camera.open();

        Camera.Size cSize = camera.getParameters().getPreviewSize();

        try {
            mPhoneTexture.setDefaultBufferSize(cSize.width, cSize.height);
            camera.setPreviewTexture(mPhoneTexture);
            camera.startPreview();
        } catch (IOException ioe) {
            Log.w("Main3Activity", "CAM LAUNCH FAILED");
        }
    }

    @Override
    public void onRendererShutdown()
    {

    }

    @Override
    public void onCardboardTrigger()
    {

    }

    public void updateTexture()
    {
        if (phoneFrameAvailable.get()) {
            mPhoneTexture.updateTexImage();
            phoneFrameAvailable.set(false);
        }
        GLUtils.checkGlError();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {

    }


}