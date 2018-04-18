/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that 
 * the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright notice, 
 *       this list of conditions and the following disclaimer in the documentation and/or 
 *       other materials provided with the distribution. 
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its contributors may be used to endorse or 
 *       promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.trio.testproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Accessory SDK - Supporting Android O OS
 * <p>
 * According to "Google's official guides", there are background execution limits in Android O.
 * Activity Manager will kill background services in 5 seconds after started by starForegroundService().
 * So, with below codes, you have to change background service to foreground service with notification.
 * You can change those codes to whatever your application needs.
 * <p>
 * If you don't need to keep the service in foreground over 5 seconds,
 * or if you build the project under Android SDK 26, you can erase below codes.
 * <p>
 * Example codes for startForeground() at SAAgent.onCreate().
 * <code>
 * if (Build.VERSION.SDK_INT >= 26) {
 * NotificationManager notificationManager = null;
 * String channel_id = "sample_channel_01";
 * if(notificationManager == null) {
 * String channel_name = "Accessory_SDK_Sample";
 * notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 * NotificationChannel notiChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_LOW);
 * notificationManager.createNotificationChannel(notiChannel);
 * }
 * <p>
 * int notifyID = 1;
 * Notification notification = new Notification.Builder(this.getBaseContext(),channel_id)
 * .setContentTitle(TAG)
 * .setContentText("")
 * .setChannelId(channel_id)
 * .build();
 * startForeground(notifyID, notification);
 * }
 * </code>
 * <p>
 * Example codes for stopForeground() at SAAgent.onDestroy().
 * <code>
 * if (Build.VERSION.SDK_INT >= 26) {
 * stopForeground(true);
 * }
 * </code>
 */

public class ConsumerService extends SAAgent {
    private static final String TAG = "HelloAccessory(C)";
    private static final Class<ServiceConnection> SASOCKET_CLASS = ServiceConnection.class;
    private final IBinder mBinder = new LocalBinder();
    private ServiceConnection mConnectionHandler = null;
    Handler mHandler = new Handler();
    private ArrayList<String> gyroData = new ArrayList<String>();
    private ArrayList<String> accelData = new ArrayList<String>();
    private ArrayList<String> gravityData = new ArrayList<String>();

    public ConsumerService() {
        super(TAG, SASOCKET_CLASS);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /****************************************************
         * Example codes for Android O OS (startForeground) *
         ****************************************************/
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = null;
            String channel_id = "sample_channel_01";

            if (notificationManager == null) {
                String channel_name = "Accessory_SDK_Sample";
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel notiChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(notiChannel);
            }

            int notifyID = 1;
            Notification notification = new Notification.Builder(this.getBaseContext(), channel_id)
                    .setContentTitle(TAG)
                    .setContentText("")
                    .setChannelId(channel_id)
                    .build();

            startForeground(notifyID, notification);
        }

        SA mAccessory = new SA();
        try {
            mAccessory.initialize(this);
        } catch (SsdkUnsupportedException e) {
            // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service threads, close UI thread, etc.)
             */
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        /***************************************************
         * Example codes for Android O OS (stopForeground) *
         ***************************************************/
        if (Build.VERSION.SDK_INT >= 26) {
            stopForeground(true);
        }
        super.onDestroy();
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        if ((result == SAAgent.PEER_AGENT_FOUND) && (peerAgents != null)) {
            for (SAPeerAgent peerAgent : peerAgents)
                requestServiceConnection(peerAgent);
        } else if (result == SAAgent.FINDPEER_DEVICE_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "FINDPEER_DEVICE_NOT_CONNECTED", Toast.LENGTH_LONG).show();
            updateTextView("Disconnected");
        } else if (result == SAAgent.FINDPEER_SERVICE_NOT_FOUND) {
            Toast.makeText(getApplicationContext(), "FINDPEER_SERVICE_NOT_FOUND", Toast.LENGTH_LONG).show();
            updateTextView("Disconnected");
        } else {
            Toast.makeText(getApplicationContext(), R.string.NoPeersFound, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        if (peerAgent != null) {
            acceptServiceConnectionRequest(peerAgent);
        }
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result) {
        if (result == SAAgent.CONNECTION_SUCCESS) {
            this.mConnectionHandler = (ServiceConnection) socket;
            updateTextView("Connected");
        } else if (result == SAAgent.CONNECTION_ALREADY_EXIST) {
            updateTextView("Connected");
            Toast.makeText(getBaseContext(), "CONNECTION_ALREADY_EXIST", Toast.LENGTH_LONG).show();
        } else if (result == SAAgent.CONNECTION_DUPLICATE_REQUEST) {
            Toast.makeText(getBaseContext(), "CONNECTION_DUPLICATE_REQUEST", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), R.string.ConnectionFailure, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
    }

    @Override
    protected void onPeerAgentsUpdated(SAPeerAgent[] peerAgents, int result) {
        final SAPeerAgent[] peers = peerAgents;
        final int status = result;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (peers != null) {
                    if (status == SAAgent.PEER_AGENT_AVAILABLE) {
                        Toast.makeText(getApplicationContext(), "PEER_AGENT_AVAILABLE", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "PEER_AGENT_UNAVAILABLE", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public class ServiceConnection extends SASocket {
        public ServiceConnection() {
            super(ServiceConnection.class.getName());
        }

        @Override
        public void onError(int channelId, String errorMessage, int errorCode) {
        }

        @Override
        public void onReceive(int channelId, byte[] data) {
            final String message = new String(data);

            try {
                JSONObject obj = new JSONObject(message);

                Iterator<String> it = obj.keys();

                while (it.hasNext()) {
                    try {
                        addMessage("Received: ", obj.getString(it.next()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   /* String key = it.next();
                    if ( obj.getString(key).equals("gyro")) {
                        gyroData.add(obj.get(obj.names().getString(1)).toString());
                        gyroData.add(obj.get(obj.names().getString(2)).toString());
                        gyroData.add(obj.get(obj.names().getString(3)).toString());
                        addGyroMessage(gyroData);
                        gyroData.clear();
                    }
                    else if (obj.getString(it.next()).equals("accelero"))
                    {
                        accelData.add(obj.get(obj.names().getString(1)).toString());
                        accelData.add(obj.get(obj.names().getString(2)).toString());
                        accelData.add(obj.get(obj.names().getString(3)).toString());
                        addAcceleroMessage(accelData);
                        accelData.clear();
                    }
                    else if (obj.getString(it.next()).equals("gravity"))
                    {
                        gravityData.add(obj.get(obj.names().getString(1)).toString());
                        gravityData.add(obj.get(obj.names().getString(2)).toString());
                        gravityData.add(obj.get(obj.names().getString(3)).toString());
                        addGravityMessage(gravityData);
                        gravityData.clear();
                    }*/


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onServiceConnectionLost(int reason) {
            updateTextView("Disconnected");
            closeConnection();
        }
    }

    public class LocalBinder extends Binder {
        public ConsumerService getService() {
            return ConsumerService.this;
        }
    }

    public void findPeers() {
        findPeerAgents();
    }

    public boolean sendData(final String data) {
        boolean retvalue = false;
        if (mConnectionHandler != null) {
            try {
                mConnectionHandler.send(getServiceChannelId(0), data.getBytes());
                retvalue = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            addMessage("Sent: ", data);
        }
        return retvalue;
    }

    public boolean closeConnection() {
        if (mConnectionHandler != null) {
            mConnectionHandler.close();
            mConnectionHandler = null;
            return true;
        } else {
            return false;
        }
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
            stopSelf();
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

    private void updateTextView(final String str) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                GearSensorActivity.updateTextView(str);
            }
        });
    }

    private void addMessage(final String prefix, final String data) {
        //final String strToUI = prefix.concat(data);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                GearSensorActivity.addMessage(data);
            }
        });
    }
}
