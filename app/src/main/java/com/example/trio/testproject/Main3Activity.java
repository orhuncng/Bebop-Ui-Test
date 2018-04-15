package com.example.trio.testproject;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

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

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

public class Main3Activity extends GvrActivity implements GvrView.StereoRenderer,
        SurfaceTexture.OnFrameAvailableListener,
        ARDeviceControllerListener, ARDeviceControllerStreamListener {

    private static final String TAG = "Main3Activity";
    private VideoUiView uiView;
    private SceneRenderer scene;
    public ArrayList<String> DeviceNames = new ArrayList<>();
    private Camera camera;
    private boolean videoStarted;
    ARDiscoveryDevice trioDrone;
    ARDiscoveryServicesDevicesListUpdatedReceiver receiver;
    ARDeviceController deviceController;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final int VIDEO_WIDTH = 640;
    private static final int VIDEO_HEIGHT = 368;

    Context mContext;

    private H264VideoProvider h264VideoProvider;

    static {
        ARSDK.loadSDKLibs();
    }

    private float[] viewProjectionMatrix;

    public void initializeGvrView() {
        setContentView(R.layout.activity_main3);

        GvrView gvrView = findViewById(R.id.hudView2);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        //gvrView.setTransitionViewEnabled(true);

        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);

        Pair<SceneRenderer, VideoUiView> pair = SceneRenderer.createForVR(this, gvrView);
        scene = pair.first;
        uiView = pair.second;

        viewProjectionMatrix = new float[16];
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deviceController.startVideoStream();
                Log.e("videostream", "starting video stream");
            }
        }, 10000);
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
        if ((!videoStarted) && (deviceController != null)) {
            //deviceController.startVideoStream();
            videoStarted = true;
        }

        scene.updateTexture();
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.multiplyMM(
                viewProjectionMatrix, 0, eye.getPerspective(Z_NEAR, Z_FAR), 0, eye.getEyeView(), 0);
        scene.draw(viewProjectionMatrix);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        scene.onSurfaceCreated();
        h264VideoProvider.init(scene.createDisplay(VIDEO_WIDTH, VIDEO_HEIGHT));
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

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }
}