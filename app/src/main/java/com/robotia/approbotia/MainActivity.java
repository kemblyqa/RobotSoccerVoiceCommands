package com.robotia.approbotia;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button bluetoothSwitch, startTalking;
    private TextView mVoiceInputTv, currentConnectedDevice;
    private BluetoothDevicesCustom bluetoothDevicesCustom = BluetoothDevicesCustom.getInstance();
    public ArrayAdapter<String> arrayAdapter;
    public ArrayList<BluetoothDevice> bluetoothDevices;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSwitch = findViewById(R.id.toggleBluetoothBtn);
        startTalking = findViewById(R.id.startBtn);
        mVoiceInputTv = findViewById(R.id.inputCommand);
        currentConnectedDevice = findViewById(R.id.currentConnectedDevice);

        bluetoothAdapter = bluetoothDevicesCustom.getBluetoothAdapter();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothDevices = new ArrayList<>();
        makeBluetoothConnection();

        startTalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Send your command!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {}
    }

    private void makeBluetoothConnection(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothDevicesCustom.REQUEST_BLUETOOTH);
        } else {
            bluetoothSwitch.setText(getString(R.string.turnOffBluetoothTitle));
            Toast.makeText(this, "Bluetooth connected....", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String resultText = result.get(0);
                    mVoiceInputTv.setText(resultText);
                    sendVoiceCommand(resultText);
                }
                break;
            }
            case BluetoothDevicesCustom.REQUEST_BLUETOOTH:{
                if(resultCode == Activity.RESULT_OK){
                    bluetoothSwitch.setText(getString(R.string.turnOffBluetoothTitle));
                    Toast.makeText(this, "Bluetooth connected....", Toast.LENGTH_SHORT).show();
                }
                else if (resultCode == Activity.RESULT_CANCELED) {
                    finish();
                    System.exit(0);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendVoiceCommand(String comm){
        String[] stringWords = comm.split(" ");
        for(String word : stringWords){
            if(!bluetoothDevicesCustom.sendToDevice(word)){
                Toast.makeText(bluetoothDevicesCustom, "Word "+word+" not sent", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void selectDeviceAlertDialog(){
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("Request pair")
            .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connectBtSocket(bluetoothDevices.get(which));
                }
            })
            .show();
    }

    public void connectBtSocket(BluetoothDevice device){
        if (device.getAddress().equals(BluetoothDevicesCustom.HEADPHONES_MAC_ADDRESS)){
            if(bluetoothDevicesCustom.createConnection(device)){
                currentConnectedDevice.setText(device.getName());
                //TODO: connected bluetooth device image
            }
        } else {
            Toast.makeText(bluetoothDevicesCustom, "Wrong device!", Toast.LENGTH_SHORT).show();
        }
    }

    public void requestForBluetoothService(View view){
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            bluetoothSwitch.setText(getString(R.string.turnOnBluetoothTitle));
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothDevicesCustom.REQUEST_BLUETOOTH);
        }
    }

    public void createNewBluetoothConnection(View view){
        bluetoothDevicesCustom.closeConnection();
        currentConnectedDevice.setText("");
        arrayAdapter.clear();
        arrayAdapter.notifyDataSetChanged();
        bluetoothDevices.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices){
            bluetoothDevices.add(bt);
            arrayAdapter.add(bt.getName() + "\n" + bt.getAddress());
        }
        selectDeviceAlertDialog();
    }
}
