package com.trio.drone.bebop.controller;

import android.content.*;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;

public class DiscoveryService
{
    private ARDiscoveryServicesDevicesListUpdatedReceiver receiver;
    private ARDiscoveryService service;
    private ServiceConnection serviceConnection;
    private Context context;

    DiscoveryService(Context context)
    {
        this.context = context;

        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service)
                {
                    DiscoveryService.this.service =
                            ((ARDiscoveryService.LocalBinder) service).getService();
                    startService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {service = null;}
            };
        }

        if (service == null) {
            Intent i = new Intent(context, ARDiscoveryService.class);
            context.bindService(i, serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        else {
            startService();
        }
    }

    public void registerReceiver(ARDiscoveryServicesDevicesListUpdatedReceiverDelegate delegate)
    {
        if (receiver == null) {
            receiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(delegate);

            LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
            localBroadcastMgr.registerReceiver(receiver, new IntentFilter(
                    ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
        }
    }

    public void unregisterReceiver()
    {
        if (receiver != null) {
            LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(context);
            localBroadcastMgr.unregisterReceiver(receiver);
        }
    }

    private void startService() {service.start();}

    private void closeService()
    {
        Log.d("ServiceClose", "closeServices ...");

        if (service != null) {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    service.stop();

                    context.unbindService(serviceConnection);
                    service = null;
                }
            }).start();
        }
    }

    public ARDiscoveryDevice getDevice()
    {
        ARDiscoveryDevice device = null;

        if (service != null) {
            List<ARDiscoveryDeviceService> services = service.getDeviceServicesArray();

            if (services.size() > 0) {
                try {
                    device = new ARDiscoveryDevice(context, services.get(0));
                } catch (ARDiscoveryException e) {
                    Log.e("ARDiscoveryException", "Exception", e);
                }
            }
        }

        return device;
    }
}
