package com.robotia.approbotia;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    final UUID ARDUINO_PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //need this
    final UUID DEVICE_PORT_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    Handler mHandler;
    BluetoothSocket bluetoothSocket = null;
    private ConnectedThread mConnectedThread;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == handlerState){
                    String readMessage = (String) msg.obj;
                    Toast.makeText(BluetoothDevicesCustom.this, readMessage, Toast.LENGTH_SHORT).show();
                }
            }
        };
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
        }
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
            bluetoothSocket = device.createRfcommSocketToServiceRecord(DEVICE_PORT_UUID);
        } catch (IOException e) {
            return false;
        }
        try {
            bluetoothSocket.connect();
            Toast.makeText(instance, "Conexión socket correcta", Toast.LENGTH_SHORT).show();

            mConnectedThread = new ConnectedThread(bluetoothSocket);
            mConnectedThread.start();
            return true;
        } catch (IOException e) {
            try {
                Toast.makeText(instance, "No hay conexión", Toast.LENGTH_SHORT).show();
                bluetoothSocket.close();
                return false;
            } catch (IOException e2) {
                Toast.makeText(instance, "Error durante el cierre de conexión!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

    }

    void setHandler(Handler handler) {
        mHandler = handler;
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
                    mHandler.obtainMessage(handlerState, bytes, -1, readMessage)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
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