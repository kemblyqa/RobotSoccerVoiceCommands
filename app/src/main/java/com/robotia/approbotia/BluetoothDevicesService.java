package com.robotia.approbotia;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class BluetoothDevicesService extends Application {
    private static BluetoothDevicesService instance;
    static BluetoothDevicesService getInstance() {
        return instance;
    }

    //flags
    static final int REQUEST_BLUETOOTH = 1;
    static final String ARDUINO_MAC_ADDRESS = "00:00";
    static final String DEVICE_MAC_ADDRESS = "E0:99:71:DB:9F:C9";
    static final String DEVICE_QUIROS_MAC_ADDRESS = "A4:93:3F:61:71:65";
    static final String HEADPHONES_MAC_ADDRESS = "22:22:22:67:0E:00";
    private static final String TAG = "MY_APP_DEBUG_TAG";

    //need this
    // final UUID HEADSET_PORT_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    static final UUID DEVICE_PORT_UUID = UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB");
    static final UUID DEVICE_PORT_UUID_1 = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
    static final UUID DEVICE_PORT_UUID_2 = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb");
    static final UUID DEVICE_PORT_UUID_3 = UUID.fromString("00001112-0000-1000-8000-00805f9b34fb");
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Bluetooth
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothSocket bluetoothSocket = null;
    ConnectedThread mConnectedThread;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public boolean sendToDevice(String word){
        //send word through bt to device
//        String charComm;
//        switch (word){
//            case "green":{
//                charComm = "1";
//                break;
//            }
//            case "yellow":{
//                charComm = "2";
//                break;
//            }
//            case "blue":{
//                charComm = "3";
//                break;
//            }
//            case "orange":{
//                charComm = "4";
//                break;
//            }
//            case "red":{
//                charComm = "0";
//                break;
//            }
//            default:{
//                return false;
//            }
//        }
        mConnectedThread.write(word);
        return true;
    }

    public boolean createConnection(BluetoothDevice device, UUID uuid){
        ConnectThread connectThread = new ConnectThread(device, uuid);
        connectThread.start();
        return true;
    }

    private void connected(BluetoothSocket mmSocket) {
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
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
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device, UUID MY_UUID) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e("","trying fallback...");
                try {
                    mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException e) { }
            }
            connected(mmSocket);
        }
        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        byte[] mmBuffer; // mmBuffer store for the stream

        //creation of the connect thread
        ConnectedThread(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(mmBuffer);         //read bytes from input buffer
                    String readMessage = new String(mmBuffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    MainActivity.handler.obtainMessage(MainActivity.HANDLER_STATE, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream

                MainActivity.handler.obtainMessage(MainActivity.HANDLER_STATE, -1, -1, msgBuffer).sendToTarget();
                // Toast.makeText(BluetoothDevicesService.this, input, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                //if you cannot write, close the application
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        MainActivity.handler.obtainMessage(MainActivity.HANDLER_STATE);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                MainActivity.handler.sendMessage(writeErrorMsg);
                // Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
