package com.robotia.approbotia;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //UI elements
    private TextView mVoiceInputTv, currentConnectedDevice, prueba;
    Button bluetoothSwitch, startTalking;

    //Handler
    static Handler handler;
    static final int HANDLER_STATE = 0;             //used to identify handler message

    //Bluetooth
    private BluetoothDevicesService bluetoothDevicesService = BluetoothDevicesService.getInstance();
    public ArrayAdapter<String> arrayAdapter;
    public ArrayList<BluetoothDevice> bluetoothDevices;
    private BluetoothAdapter bluetoothAdapter;

    //speech
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSwitch = findViewById(R.id.toggleBluetoothBtn);
        startTalking = findViewById(R.id.startBtn);
        mVoiceInputTv = findViewById(R.id.inputCommand);
        currentConnectedDevice = findViewById(R.id.currentConnectedDevice);
        prueba = findViewById(R.id.prueba);

        bluetoothAdapter = bluetoothDevicesService.getBluetoothAdapter();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothDevices = new ArrayList<>();
        makeBluetoothConnection();

        startTalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == HANDLER_STATE){
                    String readMessage = new String((byte[]) msg.obj) ;// msg.arg1 = bytes from connect thread
                    prueba.setText(readMessage);
                }
                super.handleMessage(msg);
            }
        };
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
            startActivityForResult(enableBtIntent, BluetoothDevicesService.REQUEST_BLUETOOTH);
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
            case BluetoothDevicesService.REQUEST_BLUETOOTH:{
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
            if(!bluetoothDevicesService.sendToDevice(word)){
                Toast.makeText(bluetoothDevicesService, "Word "+word+" not sent", Toast.LENGTH_SHORT).show();
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
        if (device.getAddress().equals(BluetoothDevicesService.DEVICE_QUIROS_MAC_ADDRESS)){
//            try {
//                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//                Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
//                ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
//
//                if(uuids != null) {
//                    for (ParcelUuid uuid : uuids) {
//                        Log.d("my", "UUID: " + uuid.getUuid().toString());
//                    }
//                }else{
//                    Log.d("my", "Uuids not found, be sure to enable Bluetooth!");
//                }
//
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }

            if(bluetoothDevicesService.createConnection(device, BluetoothDevicesService.DEVICE_PORT_UUID_3)){
                currentConnectedDevice.setText(device.getName());
                //TODO: connected bluetooth device image
                //TODO: verify with boolean (always true)
            }
        } else {
            Toast.makeText(bluetoothDevicesService, "Wrong device!", Toast.LENGTH_SHORT).show();
        }
    }

    public void requestForBluetoothService(View view){
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            bluetoothSwitch.setText(getString(R.string.turnOnBluetoothTitle));
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothDevicesService.REQUEST_BLUETOOTH);
        }
    }

    public void createNewBluetoothConnection(View view){
        bluetoothDevicesService.closeConnection();
        currentConnectedDevice.setText("Some device");
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
