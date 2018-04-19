package com.example.trio.testproject;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ARDeviceControllerListener, ARDeviceControllerStreamListener, WatchServiceCallbacks {
    public static final String TESTMSG = "com.example.trio.testproject";
    public ArrayList<String> DeviceNames = new ArrayList<>();
    ARDiscoveryDevice trioDrone;
    Context mContext;
    ARDiscoveryServicesDevicesListUpdatedReceiver receiver;
    ARDeviceController deviceController;
    private H264VideoView mVideoView;
    private ARCONTROLLER_DEVICE_STATE_ENUM deviceState = ARCONTROLLER_DEVICE_STATE_ENUM.eARCONTROLLER_DEVICE_STATE_UNKNOWN_ENUM_VALUE;

    float[] acceleration = new float[2];
    float[] accelerationFilter = new float[3];
    float[] gyroscope = new float[2];

    private DeviceSensorProvider<HashMap<String, float[]>> liveData;

    float deltaX = 0;
    private int count = 0;

    private boolean mIsBound = false;
    private ConsumerService mConsumerService = null;

    static {
        ARSDK.loadSDKLibs();
    }

    String[] mobileArray = {"Android", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        System.out.println("ON CREATE");

        initDiscoveryService();
        registerReceivers();

        mVideoView = (H264VideoView) findViewById(R.id.videoView);
        mIsBound = bindService(new Intent(MainActivity.this, ConsumerService.class), mConnection, Context.BIND_AUTO_CREATE);


        DeviceSensorViewModel model = ViewModelProviders.of(this).get(DeviceSensorViewModel.class);
        liveData = model.getDeviceSensorListener();

        liveData.observe(this, new Observer<HashMap<String, float[]>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, float[]> sensorData) {
                acceleration = sensorData.get("acceleration");
                accelerationFilter = sensorData.get("accelerationFilter");
                gyroscope = sensorData.get("gyroscope");

                count++;

                if (acceleration != null && acceleration.length == 3) {
                    Log.e("raw", Float.toString(acceleration[0]));
                }

                if (accelerationFilter != null && accelerationFilter.length == 3) {
                    Log.e("filtered", Float.toString(accelerationFilter[0]));
                }

                if (count >= 100) {
                    Log.e("count", "Ayar yapıldı");
                    deltaX = deltaX + accelerationFilter[2];
                    Log.e("Gidilen Yol", Float.toString(deltaX));

                    int yaw = Math.round(accelerationFilter[2] * 10);

                    setYawFromAcclSensor(yaw);

                }

            }
        });


    }

    public void showDevices(View view) {
        System.out.println("TUŞA BASILDI");

        if (DeviceNames.isEmpty()) {
            DeviceNames.add("Deneme1");
            DeviceNames.add("Deneme2");
            DeviceNames.add("Deneme3");
            DeviceNames.add("Deneme4");
            DeviceNames.add("Deneme5");
            DeviceNames.add("Deneme6");
            DeviceNames.add("Deneme7");
            DeviceNames.add("Deneme8");
        }

        Intent intent = new Intent(this, Main2Activity.class);
        intent.putStringArrayListExtra(TESTMSG, DeviceNames);
        startActivity(intent);
    }

    public void takeOffDrone(View view) {
        Log.e("TakeOffDrone", "TakeOff fonksiyonunda");
        takeoff();
    }

    public void landDrone(View view) {
        Log.e("landDrone", "landDrone fonksiyonunda");
        land();
    }

    public void cancelFlight(View view) {
        Log.e("cancelFlight", "cancelFlight fonksiyonunda");
        deviceController.getFeatureARDrone3().sendPilotingEmergency();
    }

    public void stopControl(View view) {

        Log.e("stopControl", "stopControl fonksiyonunda");
        if (deviceController != null) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.stop();
            // only when the deviceController is stopped
            deviceController.dispose();
        }
    }

    public void setYawFromBezelRotate(int dir) {
        Log.e("MainActRotateDrone", "" + dir);

        if (deviceController != null) {

            deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);

            Log.e("SetYawpilotingState", getDeviceState().toString());
            if ((getPilotingState().toString().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
                deviceController.getFeatureARDrone3().setPilotingPCMDYaw((byte) dir);
                Log.e("MainActRotateDrone", "dirSent");
            } else {
                Log.e("MainActRotateDrone", "else ye girdi");

            }
        }
    }

    public void setYawFromAcclSensor(int dir) {
        Log.e("MainActRotateDrone", "" + dir);

        if (deviceController != null) {

            deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);

            Log.e("SetYawpilotingState", getDeviceState().toString());
            if ((getPilotingState().toString().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
                deviceController.getFeatureARDrone3().setPilotingPCMDYaw((byte) dir);
                Log.e("MainActRotateDrone", "dirSent");
            } else {
                Log.e("MainActRotateDrone", "else ye girdi");

            }
        }
    }



    public void openSensor(View view) {
        Log.e("openSensor", "OpenSensore basıldı");

        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

    public void openGearSensor(View view) {
        Log.e("openGearSensor", "OpenGearSensore basıldı");

        Intent intent = new Intent(this, GearSensorActivity.class);
        startActivity(intent);
    }

    public void startVideo(View view) {
        deviceController.startVideoStream();
    }


    public void stopVideo(View view) {
        try {
            deviceController.stopVideoStream();
        } catch (ARControllerException e) {
            e.printStackTrace();
        }
    }

    public void openVR(View view) {
        unregisterReceivers();
        closeServices();
        Intent intent = new Intent(this, Main3Activity.class);
        startActivity(intent);
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

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        deviceState = newState;
        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                Log.e("onStateChanged", "ARCONTROLLER_DEVICE_STATE_RUNNING: ");
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                Log.e("onStateChanged", "ARCONTROLLER_DEVICE_STATE_STOPPED: ");
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                Log.e("onStateChanged", "ARCONTROLLER_DEVICE_STATE_STARTING: ");
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                Log.e("onStateChanged", "ARCONTROLLER_DEVICE_STATE_STOPPING: ");
                break;

            default:
                break;
        }

    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if (elementDictionary != null) {
            // if the command received is a battery state changed
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null) {
                    Integer batValue = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    Log.e("Battery", String.valueOf(batValue));
                    // do what you want with the battery level
                }
            }

            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                    ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                    Log.e("Battery", String.valueOf(flyingStateInt));
                }
            }
        } else {
            Log.e("OnCommandReceive", "elementDictionary is null");
        }

    }

    private ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM getPilotingState() {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.eARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_UNKNOWN_ENUM_VALUE;
        if (deviceController != null) {
            try {
                ARControllerDictionary dict = deviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED);
                if (dict != null) {
                    ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null) {
                        Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                        flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                    }
                }
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }
        return flyingState;
    }

    public ARCONTROLLER_DEVICE_STATE_ENUM getDeviceState() {
        return deviceState;
    }

    private void takeoff() {
        Log.e("TAKE OFF state", getPilotingState().toString());
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED.equals(getPilotingState())) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureARDrone3().sendPilotingTakeOff();
            Log.e("TAKE OFF", "TakeOff called");

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)) {
                Log.e("TAKE OFF", "Error while sending take off: " + error);
            }
        }
    }

    private void land() {
        Log.e("Landing state", getPilotingState().toString());
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING.equals(flyingState) ||
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState)) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureARDrone3().sendPilotingLanding();
            Log.e("LANDING", "Landing called");

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)) {
                Log.e("LANDING", "Error while sending take off: " + error);
            }
        }
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, ARControllerCodec codec) {
        Log.e("configureDecoder", "codec received");
        mVideoView.configureDecoder(codec);
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, ARFrame frame) {
        mVideoView.displayFrame(frame);
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {

    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConsumerService = ((ConsumerService.LocalBinder) service).getService();
            //updateTextView("onServiceConnected");
            Log.e("MainAct", "onServiceConnected");
            if (mIsBound == true && mConsumerService != null) {
                Log.e("MainAct", "oncreate bind and consumer");
                mConsumerService.findPeers();
                mConsumerService.setCallbacks(MainActivity.this); // register
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            mIsBound = false;
            //updateTextView("onServiceDisconnected");
            Log.e("MainAct", "onServicedisonnected");
        }
    };

    @Override
    public void doSomething(int dir) {
        setYawFromBezelRotate(dir);
    }
}
