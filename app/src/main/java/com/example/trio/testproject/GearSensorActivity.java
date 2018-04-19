package com.example.trio.testproject;
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


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GearSensorActivity extends Activity {
    private static TextView mTextView;

    private static TextView mTextGyroX;
    private static TextView mTextGyroY;
    private static TextView mTextGyroZ;

    private static TextView mTextAccelX;
    private static TextView mTextAccelY;
    private static TextView mTextAccelZ;

    private static TextView mTextGravityX;
    private static TextView mTextGravityY;
    private static TextView mTextGravityZ;

    private MainActivity droneCtrlObj = new MainActivity();

    private boolean mIsBound = false;
    private ConsumerService mConsumerService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gear_sensor);
        mTextView = (TextView) findViewById(R.id.tvStatus);
        // Bind service

        mTextGyroX = (TextView) findViewById(R.id.GyroX);
        mTextGyroY = (TextView) findViewById(R.id.GyroY);
        mTextGyroZ = (TextView) findViewById(R.id.GyroZ);

        /*mTextAccelX = (TextView) findViewById(R.id.AccelX);
        mTextAccelY = (TextView) findViewById(R.id.AccelY);
        mTextAccelZ = (TextView) findViewById(R.id.AccelZ);

        mTextGravityX = (TextView) findViewById(R.id.GravityX);
        mTextGravityY = (TextView) findViewById(R.id.GravityY);
        mTextGravityZ = (TextView) findViewById(R.id.GravityZ);*/

        mIsBound = bindService(new Intent(GearSensorActivity.this, ConsumerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        // Clean up connections
        if (mIsBound == true && mConsumerService != null) {
            if (mConsumerService.closeConnection() == false) {
                updateTextView("Disconnected");
                //mMessageAdapter.clear();
            }
        }
        // Un-bind service
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        super.onDestroy();
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.buttonConnect: {
                if (mIsBound == true && mConsumerService != null) {
                    mConsumerService.findPeers();
                }
                break;
            }
            case R.id.buttonDisconnect: {
                if (mIsBound == true && mConsumerService != null) {
                    if (mConsumerService.closeConnection() == false) {
                        updateTextView("Disconnected");
                        Toast.makeText(getApplicationContext(), R.string.ConnectionAlreadyDisconnected, Toast.LENGTH_LONG).show();
                        //mMessageAdapter.clear();
                    }
                }
                break;
            }
            case R.id.buttonSend: {
                if (mIsBound == true && mConsumerService != null) {
                    if (mConsumerService.sendData("Hello Accessory!")) {
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.ConnectionAlreadyDisconnected, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
            default:
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mConsumerService = ((ConsumerService.LocalBinder) service).getService();
            updateTextView("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConsumerService = null;
            mIsBound = false;
            updateTextView("onServiceDisconnected");
        }
    };

    public void rotateDrone(final String str) {
        int dir = 10;
        if (str.equals("CCW")) {
            dir *= -1;
        }

        droneCtrlObj.setYawFromBezelRotate(dir);

    }

    public static void addGyroXMessage(final String str) {
        mTextGyroX.setText(str);

    }

    public static void addGyroYMessage(final String str) {
        mTextGyroY.setText(str);

    }

    public static void addGyroZMessage(final String str) {
        mTextGyroZ.setText(str);

    }

    public static void addGravityMessage(ArrayList<String> gravityData) {
        mTextGravityX.setText(gravityData.get(0));
        mTextGravityY.setText(gravityData.get(1));
        mTextGravityZ.setText(gravityData.get(2));

    }

    public static void addAcceleroMessage(ArrayList<String> accelData) {
        mTextAccelX.setText(accelData.get(0));
        mTextAccelY.setText(accelData.get(1));
        mTextAccelZ.setText(accelData.get(2));

    }

    /*public static void addMessage(String data) {
        mMessageAdapter.addMessage(new Message(data));
    }*/

    public static void updateTextView(final String str) {
        mTextView.setText(str);
    }

    private class MessageAdapter extends BaseAdapter {
        private static final int MAX_MESSAGES_TO_DISPLAY = 20;
        private List<Message> mMessages;

        public MessageAdapter() {
            mMessages = Collections.synchronizedList(new ArrayList<Message>());
        }

        void addMessage(final Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMessages.size() == MAX_MESSAGES_TO_DISPLAY) {
                        mMessages.remove(0);
                        mMessages.add(msg);
                    } else {
                        mMessages.add(msg);
                    }
                    notifyDataSetChanged();
                    //mMessageListView.setSelection(getCount() - 1);
                    //Log.e("gear", msg.data);
                   /* if (msg.data.equals("gyro"))
                    {
                        gyroTurn = true;
                        index = 0;
                    }
                    else if (msg.data.equals("accelero"))
                    {
                        gyroTurn = false;
                        index = 0;
                    }

                    if (gyroTurn)
                    {
                      if (index == 0)
                      {
                          index++;
                      }
                      else if (index == 1)
                      {
                          mTextGyroX.setText(msg.data);
                          index++;
                      }
                      else if (index == 2)
                      {
                          mTextGyroY.setText(msg.data);
                          index++;
                      }
                      else if (index == 3)
                      {
                          mTextGyroZ.setText(msg.data);
                          index++;
                      }
                    }
                    else
                    {
                        if (index == 0)
                        {
                            index++;
                        }
                        else if (index == 1)
                        {
                            mTextAccelX.setText(msg.data);
                            index++;
                        }
                        else if (index == 2)
                        {
                            mTextAccelY.setText(msg.data);
                            index++;
                        }
                        else if (index == 3)
                        {
                            mTextAccelZ.setText(msg.data);
                            index++;
                        }
                    }*/

                }
            });
        }

        void clear() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.clear();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View messageRecordView = null;
            if (inflator != null) {
                messageRecordView = inflator.inflate(R.layout.message, null);
                TextView tvData = (TextView) messageRecordView.findViewById(R.id.tvData);
                Message message = (Message) getItem(position);
                tvData.setText(message.data);
            }
            return messageRecordView;
        }
    }

    private static final class Message {
        String data;

        public Message(String data) {
            super();
            this.data = data;
        }
    }
}
