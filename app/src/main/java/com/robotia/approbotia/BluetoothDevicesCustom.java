package com.robotia.approbotia;

import android.app.Application;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

public class BluetoothDevicesCustom extends Application {
    private static BluetoothDevicesCustom instance;

    static BluetoothDevicesCustom getInstance() {
        return instance;
    }

    final int handlerState = 0;             //used to identify handler message
    static final int REQUEST_BLUETOOTH = 1;
    static final String ARDUINO_MAC_ADDRESS = "00:00";
    static final String DEVICE_MAC_ADDRESS = "00:00";
    static final String HEADPHONES_MAC_ADDRESS = "22:22:22:67:0E:00";
    final UUID ARDUINO_PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //need this
    final UUID HEADSET_PORT_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    final UUID DEVICE_PORT_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Handler mHandler;
    public BluetoothSocket bluetoothSocket = null;
    public ConnectedThread mConnectedThread;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public boolean sendToDevice(String word){
        //send word through bt to device
        int integerComm;
        switch (word){
            case "green":{
                integerComm = 1;
                break;
            }
            case "yellow":{
                integerComm = 2;
                break;
            }
            case "blue":{
                integerComm = 3;
                break;
            }
            case "orange":{
                integerComm = 4;
                break;
            }
            case "red":{
                integerComm = 0;
                break;
            }
            default:{
                return false;
            }
        }
        mConnectedThread.write(integerComm);
        return true;
    }

    BluetoothAdapter getBluetoothAdapter(){
        return bluetoothAdapter;
    }

    public void closeConnection(){
        if(bluetoothSocket != null && bluetoothSocket.isConnected()){
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Toast.makeText(instance, "Closing error!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    boolean createConnection(BluetoothDevice device) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(HEADSET_PORT_UUID);
        } catch (IOException e) {
            return false;
        }
        try {
            bluetoothSocket.connect();
            Toast.makeText(instance, "Connection successful", Toast.LENGTH_SHORT).show();

            mConnectedThread = new ConnectedThread(bluetoothSocket);
            mConnectedThread.start();
            mConnectedThread.write(0);
            return true;
        } catch (IOException e) {
            try {
                Toast.makeText(instance, "No connection", Toast.LENGTH_SHORT).show();
                bluetoothSocket.close();
                return false;
            } catch (IOException e2) {
                Toast.makeText(instance, "Closing connection error!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
//                    mHandler.obtainMessage(handlerState, bytes, -1, readMessage)
//                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(int input) {
            byte[] msgBuffer = BigInteger.valueOf(input).toByteArray();           //converts entered String into bytes
            //final int i = new BigInteger(bytes).intValue();
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection failed", Toast.LENGTH_LONG).show();
                //finish();
            }
        }
    }


}
