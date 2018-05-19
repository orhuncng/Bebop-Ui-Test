package com.trio.drone.bebop;

import android.util.Log;
import android.view.Surface;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.*;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

public class DeviceController implements ARDeviceControllerListener,
        ARDeviceControllerStreamListener, ARDiscoveryServicesDevicesListUpdatedReceiverDelegate
{
    private DiscoveryService discoveryService;
    private H264VideoController videoController;
    private ARDeviceController controller;
    private BebopMediator mediator;
    private boolean droneRunning = false;

    DeviceController(BebopMediator mediator, int videoWidth, int videoHeight)
    {
        this.mediator = mediator;
        discoveryService = new DiscoveryService(mediator.getContext());
        discoveryService.registerReceiver(this);
        videoController = new H264VideoController(videoWidth, videoHeight);
    }

    public void setVideoSurface(Surface surface) {videoController.setVideoSurface(surface);}

    @Override
    public void onStateChanged(ARDeviceController deviceController,
            ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error)
    {
        droneRunning =
                (newState == ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING);

        if (droneRunning) { controller.startVideoStream(); }
        else {
            try {
                controller.stopVideoStream();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController,
            ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name,
            ARCONTROLLER_ERROR_ENUM error)
    {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController,
            ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary)
    {
        ARControllerArgumentDictionary<Object> args = (elementDictionary == null) ? null :
                elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

        if (args != null) {
            switch (commandKey) {

                case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED: {
                    mediator.onBatteryStateChanged((int) args.get(ARFeatureCommon
                            .ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT));
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_WIFISIGNALCHANGED: {
                    mediator.onWifiSignalChanged((Integer) args.get(ARFeatureCommon
                            .ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_WIFISIGNALCHANGED_RSSI));
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED: {
                    mediator.onFlyingStateChanged(
                            ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM
                                    .getFromValue((Integer) args.get(ARFeatureARDrone3
                                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE)));
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED: {
                    mediator.onPositionChanged(
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE))
                                    .floatValue(),
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE))
                                    .floatValue(),
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE))
                                    .floatValue());
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED: {
                    mediator.onSpeedChanged(
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDX))
                                    .floatValue(),
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDY))
                                    .floatValue(),
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDZ))
                                    .floatValue());
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED: {
                    mediator.onOrientationChanged(
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_ROLL))
                                    .floatValue(),
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_PITCH))
                                    .floatValue(),
                            ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_YAW))
                                    .floatValue());
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALTITUDECHANGED: {
                    mediator.onRelativeAltitudeChanged(((Double) args.get(ARFeatureARDrone3
                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALTITUDECHANGED_ALTITUDE))
                            .floatValue());
                    break;
                }
                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION: {
                    mediator.onCameraOrientationChanged(
                            (Integer) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_TILT),
                            (Integer) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_PAN));
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController,
            ARControllerCodec codec)
    {
        Log.e("configureDecoder", "codec received");
        videoController.configureDecoder(codec);
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController,
            ARFrame frame)
    {
        videoController.displayFrame(frame);
        return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
    }

    @Override
    public void onFrameTimeout(ARDeviceController deviceController)
    {

    }

    @Override
    public void onServicesDevicesListUpdated()
    {
        ARDiscoveryDevice device = discoveryService.getDevice();

        if (device != null) {
            try {
                controller = new ARDeviceController(device);
                device.dispose();
                Log.e("DeviceCOntrollerYaratma", "trioDrone Null Değil");
                controller.addListener(this);
                ARCONTROLLER_ERROR_ENUM error = controller.start();
                controller.addStreamListener(this);
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }
    }
}
