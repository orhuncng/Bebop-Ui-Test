package com.trio.drone.bebop.controller;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.*;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.trio.drone.bebop.BebopMediator;
import com.trio.drone.bebop.FlyingState;
import com.trio.drone.bebop.RelativeMotionResult;

public class DeviceController implements ARDeviceControllerListener,
        ARDeviceControllerStreamListener, ARDiscoveryServicesDevicesListUpdatedReceiverDelegate
{
    private DiscoveryService discoveryService;
    private H264VideoController videoController;
    private ARDeviceController controller;
    private BebopMediator mediator;
    private boolean isRunning = false;
    private FlyingState flyingState = FlyingState.LANDED;

    public DeviceController(Context context, BebopMediator mediator, int videoWidth, int
            videoHeight)
    {
        this.mediator = mediator;
        discoveryService = new DiscoveryService(context);
        discoveryService.registerReceiver(this);
        videoController = new H264VideoController(videoWidth, videoHeight);
    }

    public void setVideoSurface(Surface surface) {videoController.setVideoSurface(surface);}

    public boolean IsRunning() { return isRunning; }

    public FlyingState GetFlyingState() { return flyingState; }

    public void calibrateAccelerometerAndGyro()
    {
        if (isRunning && flyingState == FlyingState.LANDED)
            controller.getFeatureARDrone3().sendPilotingFlatTrim();
    }

    public void moveToRelative(float dX, float dY, float dZ, float dRotation)
    {
        if (isRunning) controller.getFeatureARDrone3().sendPilotingMoveBy(dX, dY, dZ, dRotation);
    }

    public void move(int rollPerc, int pitchPerc, int yawPerc, int gazPerc)
    {
        if (isRunning) controller.getFeatureARDrone3().setPilotingPCMD((byte) 1, (byte) rollPerc,
                (byte) pitchPerc, (byte) yawPerc, (byte) gazPerc, 0);
    }

    public void moveCamera(float tilt, float pan)
    {
        if (isRunning) controller.getFeatureARDrone3().sendCameraOrientationV2(tilt, pan);
    }

    public void doEmergencyLanding()
    {
        if (isRunning) controller.getFeatureARDrone3().sendPilotingEmergency();
    }

    public void takeOff() {if (isRunning) controller.getFeatureARDrone3().sendPilotingTakeOff();}

    public void land() {if (isRunning) controller.getFeatureARDrone3().sendPilotingLanding();}

    @Override
    public void onStateChanged(ARDeviceController deviceController,
            ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error)
    {
        isRunning =
                (newState == ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING);

        if (isRunning) { controller.startVideoStream(); }
        else {
            try {
                controller.stopVideoStream();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }

        mediator.onControllerStateChanged(isRunning);
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController,
            ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name,
            ARCONTROLLER_ERROR_ENUM error)
    { }

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
                    ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state =
                            ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM
                                    .getFromValue((Integer) args.get(ARFeatureARDrone3
                                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));

                    switch (state) {
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_EMERGENCY:
                            flyingState = FlyingState.EMERGENCY;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_EMERGENCY_LANDING:
                            flyingState = FlyingState.EMERGENCY_LANDING;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_USERTAKEOFF:
                            flyingState = FlyingState.USERTAKEOFF;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_TAKINGOFF:
                            flyingState = FlyingState.TAKINGOFF;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_MOTOR_RAMPING:
                            flyingState = FlyingState.MOTOR_RAMPING;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDING:
                            flyingState = FlyingState.LANDING;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                            flyingState = FlyingState.LANDED;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                            flyingState = FlyingState.HOVERING;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                            flyingState = FlyingState.FLYING;
                            break;
                        default:
                            flyingState = FlyingState.LANDED;
                            break;
                    }

                    mediator.onFlyingStateChanged(flyingState);
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

                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATIONV2: {
                    mediator.onCameraOrientationChanged(
                            (float) ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATIONV2_TILT))
                                    .doubleValue(),
                            (float) ((Double) args.get(ARFeatureARDrone3
                                    .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATIONV2_PAN))
                                    .doubleValue());
                    break;
                }

                case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND: {
                    float dX = (float) ((Double) args.get(ARFeatureARDrone3
                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DX))
                            .doubleValue();
                    float dY = (float) ((Double) args.get(ARFeatureARDrone3
                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DY))
                            .doubleValue();
                    float dZ = (float) ((Double) args.get(ARFeatureARDrone3
                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DZ))
                            .doubleValue();
                    float dPsi = (float) ((Double) args.get(ARFeatureARDrone3
                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DPSI))
                            .doubleValue();

                    RelativeMotionResult result;

                    ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM error =
                            ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM.getFromValue(
                                    (Integer) args.get(ARFeatureARDrone3
                                            .ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR));

                    switch (error) {
                        case ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_BUSY:
                            result = RelativeMotionResult.BUSY;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_INTERRUPTED:
                            result = RelativeMotionResult.INTERRUPTED;
                            break;
                        case ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_OK:
                            result = RelativeMotionResult.OK;
                            break;
                        default:
                            result = RelativeMotionResult.UNKNOWN;
                            break;
                    }

                    mediator.onRelativeMotionEnded(dX, dY, dZ, result);
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
    public void onFrameTimeout(ARDeviceController deviceController) { }

    @Override
    public void onServicesDevicesListUpdated()
    {
        ARDiscoveryDevice device = discoveryService.getDevice();

        if (device != null) {
            try {
                controller = new ARDeviceController(device);
                device.dispose();

                controller.addListener(this);
                controller.addStreamListener(this);
                controller.start();
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }
    }
}
