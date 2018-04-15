package com.example.trio.testproject;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class Main3Activity extends GvrActivity
        implements GvrView.StereoRenderer, SurfaceTexture.OnFrameAvailableListener,
        ARDeviceControllerListener, ARDeviceControllerStreamListener {

    private static final String TAG = "Main3Activity";
    private VideoUiView uiView;
    private SceneRenderer scene;
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private final float[] viewProjectionMatrix = new float[16];
    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;
    public ArrayList<String> DeviceNames = new ArrayList<>();
    ARDiscoveryDevice trioDrone;
    ARDiscoveryServicesDevicesListUpdatedReceiver receiver;
    ARDeviceController deviceController;

    Context mContext;

    private H264VideoProvider h264VideoProvider;

    static {
        ARSDK.loadSDKLibs();
    }

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
                    //"  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
                    "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer, vertexBuffer2;
    private ShortBuffer drawListBuffer, buf2;
    private int mProgram;
    private int mPositionHandle, mPositionHandle2;
    private int mColorHandle;
    private int mTextureCoordHandle;
    private static final float Z_NEAR = .1f;
    private static final float Z_FAR = 100;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static float squareVertices[] = { // in counterclockwise order:
            -1.0f, -1.0f,   // 0.left - mid
            1.0f, -1.0f,   // 1. right - mid
            -1.0f, 1.0f,   // 2. left - top
            1.0f, 1.0f,   // 3. right - top
//
//    	 -1.0f, -1.0f, //4. left - bottom
//    	 1.0f , -1.0f, //5. right - bottom


//       -1.0f, -1.0f,  // 0. left-bottom
//        0.0f, -1.0f,   // 1. mid-bottom
//       -1.0f,  1.0f,   // 2. left-top
//        0.0f,  1.0f,   // 3. mid-top

            //1.0f, -1.0f,  // 4. right-bottom
            //1.0f, 1.0f,   // 5. right-top

    };


    //, 1, 4, 3, 4, 5, 3
//    private short drawOrder[] =  {0, 1, 2, 1, 3, 2 };//, 4, 5, 0, 5, 0, 1 }; // order to draw vertices
    private short drawOrder[] = {0, 2, 1, 1, 2, 3}; // order to draw vertices
    private short drawOrder2[] = {2, 0, 3, 3, 0, 1}; // order to draw vertices

    static float textureVertices[] = {
            0.0f, 1.0f,  // A. left-bottom
            1.0f, 1.0f,  // B. right-bottom
            0.0f, 0.0f,  // C. left-top
            1.0f, 0.0f   // D. right-top

//        1.0f,  1.0f,
//        1.0f,  0.0f,
//        0.0f,  1.0f,
//        0.0f,  0.0f
    };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private ByteBuffer indexBuffer;    // Buffer for index-array

    private float[] mView;
    private float[] mCamera;
    private boolean videoStarted = false;
    private int displayTexId;
    private SurfaceTexture mSurfaceTexture;
    private Boolean updateSurface = new Boolean(false);
    private Surface mSurface;

    private int loadGLShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_main3);

        GvrView gvrView = (GvrView) findViewById(R.id.hudView2);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        //gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);

        Pair<SceneRenderer, VideoUiView> pair = SceneRenderer.createForVR(this, gvrView);
        scene = pair.first;
        uiView = pair.second;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        initializeGvrView();

        mContext = this;
        h264VideoProvider = new H264VideoProvider();

        initDiscoveryService();
        registerReceivers();

        mCamera = new float[16];
    }

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;

    private void initDiscoveryService() {
        Log.e("initDiscoveryService", "İçerde");
        // create the service connection
        if (mArdiscoveryServiceConnection == null) {
            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();

                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mArdiscoveryService = null;
                }
            };
        }

        if (mArdiscoveryService == null) {
            // if the discovery service doesn't exists, bind to it
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (mArdiscoveryService != null) {
            mArdiscoveryService.start();
        }
    }

    private void registerReceivers() {
        Log.e("registerReceivers", "İçerde");

        receiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(mDiscoveryDelegate);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(receiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));

    }

    private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate mDiscoveryDelegate = new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {
        @Override
        public void onServicesDevicesListUpdated() {
            Log.e("DeviceListUpdater", "İçerde");
            if (mArdiscoveryService != null) {
                List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();

                DeviceNames.clear();
                for (int i = 0; i < deviceList.size(); i++) {
                    DeviceNames.add(deviceList.get(i).getName());
                }

                if (deviceList.size() > 0) {
                    Log.e("DeviceCreateCall", "Null değil create et!");
                    trioDrone = createDiscoveryDevice(deviceList.get(0));
                    if (trioDrone != null) {
                        try {
                            deviceController = new ARDeviceController(trioDrone);
                            trioDrone.dispose();
                            Log.e("DeviceCOntrollerYaratma", "trioDrone Null Değil");
                            deviceController.addListener((ARDeviceControllerListener) mContext);
                            ARCONTROLLER_ERROR_ENUM error = deviceController.start();
                            deviceController.addStreamListener((ARDeviceControllerStreamListener) mContext);
                        } catch (ARControllerException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e("DeviceCreateCall", "Null geldi!");
                }
            }
        }
    };


    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service) {
        ARDiscoveryDevice device = null;
        try {
            device = new ARDiscoveryDevice(mContext, service);
            Log.e("Device Var", device.toString());
        } catch (ARDiscoveryException e) {
            Log.e("ARDiscoveryException", "Exception", e);
        }

        return device;
    }


    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.unregisterReceiver(receiver);
    }

    private void closeServices() {
        Log.d("ServiceClose", "closeServices ...");

        if (mArdiscoveryService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mArdiscoveryService.stop();

                    getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }

    public void stopVideo() {
        try {
            deviceController.stopVideoStream();
        } catch (ARControllerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        synchronized (updateSurface) {
            if (updateSurface) {
                mSurfaceTexture.updateTexImage(); // update surfacetexture if available
                updateSurface = false;
            }
        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
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


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
/*
        Matrix.multiplyMM(
                viewProjectionMatrix, 0, eye.getPerspective(Z_NEAR, Z_FAR), 0, eye.getEyeView(), 0);
        scene.glDrawFrame(viewProjectionMatrix, eye.getType());
  */
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well

        //scene.glInit();
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

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);

        displayTexId = GLUtils.glCreateExternalTexture();
        mSurfaceTexture = new SurfaceTexture(displayTexId);
        GLUtils.checkGlError();


        // When the video decodes a new frame, tell the GL thread to update the image.
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                synchronized (updateSurface) {
                    updateSurface = true;
                }
            }
        });

        mSurface = new Surface(mSurfaceTexture);



        startCamera();
    }


    public void startCamera() {
        //h264VideoProvider.setSurface(scene.createDisplay(VIDEO_WIDTH, VIDEO_HEIGHT));
        /*
        camera = Camera.open();

        Camera.Size cSize = camera.getParameters().getPreviewSize();

        try {
            camera.setPreviewTexture(scene.createDisplay(cSize.width, cSize.height));
            camera.startPreview();
        } catch (IOException ioe) {
            Log.w("Main3Activity", "CAM LAUNCH FAILED");
        }*/
    }

    @Override
    public void onRendererShutdown() {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture arg0) {

    }

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {

    }

    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec) {
        Log.e("configureDecoder", "codec received");
        h264VideoProvider.configureDecoder(codec);
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame) {
        h264VideoProvider.displayFrame(frame);
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {

    }
}