package com.robotia.approbotia;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Button findArduinoDevice, findHeadsetDevice, bluetoothSwitch;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static int REQUEST_BLUETOOTH = 1;
    public final String ARDUINO_MAC_ADDRESS = "00:00";
    public ArrayAdapter<String> arrayAdapter;
    public ArrayList<BluetoothDevice> bluetoothDevices;
    public BluetoothSocket arduinoBluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findArduinoDevice = findViewById(R.id.firstDeviceBtn);
        findHeadsetDevice = findViewById(R.id.secondDeviceBtn);
        bluetoothSwitch = findViewById(R.id.toggleBluetoothBtn);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothDevices = new ArrayList<>();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
        } else {
            bluetoothSwitch.setText(getString(R.string.turnOffBluetoothTitle));
            Toast.makeText(this, "Discovering mode....", Toast.LENGTH_SHORT).show();
        }
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if(action != null){
//                switch (action) {
//                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
//                        //discovery starts, we can show progress dialog or perform other tasks
//                        break;
//                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
//                        // bluetoothAdapter.cancelDiscovery();
//                        break;
//                    case BluetoothDevice.ACTION_FOUND:
//                        // Discovery has found a device. Get the BluetoothDevice
//                        // object and its info from the Intent.
//                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                        if (addNewDeviceIfExists(device)) arrayAdapter.add(device.getName());
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//    };

//    public boolean addNewDeviceIfExists(BluetoothDevice newDevice){
//        for(BluetoothDevice device : bluetoothDevices){
//            if(device.getAddress().equals(newDevice.getAddress()))
//                return false;
//        }
//        bluetoothDevices.add(newDevice);
//        return true;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                // bluetoothAdapter.startDiscovery();
                bluetoothSwitch.setText(getString(R.string.turnOffBluetoothTitle));
                Toast.makeText(this, "Discovering mode....", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                System.exit(0);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void selectDeviceAlertDialog(String title){
        new AlertDialog.Builder(MainActivity.this)
            .setTitle(title)
            .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Holis "+which, Toast.LENGTH_SHORT).show();
                    //connect socket to handle the device

                }
            })
            .show();
    }

    public void connectBtSocket(BluetoothDevice device) throws IOException {
        if(device.getAddress().equals(ARDUINO_MAC_ADDRESS)){
//            arduinoBluetoothSocket = device.createRfcommSocketToServiceRecord(PORT_UUID);
//            arduinoBluetoothSocket.connect();
        }
    }

    public void pairDevice(View view){
//        if(!bluetoothAdapter.isDiscovering()) bluetoothAdapter.startDiscovery();
        arrayAdapter.clear();
        arrayAdapter.notifyDataSetChanged();
        bluetoothDevices.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices){
            bluetoothDevices.add(bt);
            arrayAdapter.add(bt.getUuids().toString());
        }
        selectDeviceAlertDialog("Request pair");
    }

    public void requestForBluetoothService(View view){
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            bluetoothSwitch.setText(getString(R.string.turnOnBluetoothTitle));
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
        }
    }
}
