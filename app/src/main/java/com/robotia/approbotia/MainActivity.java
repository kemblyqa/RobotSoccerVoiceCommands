package com.robotia.approbotia;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    //UI elements
    private TextView mVoiceInputTv;
    Button startTalking;
    //speech
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    //socket
    Socket socket;
    private static final String BASE_URI = "http://172.24.29.56:3000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTalking = findViewById(R.id.startBtn);
        mVoiceInputTv = findViewById(R.id.inputCommand);

        startTalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
        try {
            socket = IO.socket(BASE_URI);
            socket.connect();
            socket.emit("join","TEST");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on("test_response", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(MainActivity.this,data,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.on("comm", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(MainActivity.this,data,Toast.LENGTH_SHORT).show();
                    }
                });
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String resultText = result.get(0);
                    sendVoiceCommand(resultText);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void sendVoiceCommand(String comm){
        String[] stringWords = comm.split(" ");
        for (String word : stringWords) {
            if (!sendToDevice(word)) {
                Toast.makeText(this, "Word " + word + " not sent", Toast.LENGTH_SHORT).show();
            } else mVoiceInputTv.setText(word);

        }
    }
    public boolean sendToDevice(String word) {
        //send word through bt to device
        char charComm;
        switch (word.toLowerCase()) {
            //u:adelante
            case "green": {
                charComm = 'u';
                break;
            }
            //r:derecha
            case "yellow": {
                charComm = 'r';
                break;
            }
            //d:atras
            case "blue": {
                charComm = 'd';
                break;
            }
            //l:izquierda
            case "orange": {
                charComm = 'l';
                break;
            }
            //b:freno
            case "white": {
                charComm = 'b';
                break;
            }
            //s:lento
            case "slow": {
                charComm = 's';
                break;
            }
            //m:medio
            case "medium": {
                charComm = 'm';
                break;
            }
            //f:rápido
            case "fast": {
                charComm = 'f';
                break;
            }
            default: {
                return false;
            }
        }
        //TODO: create a service to send the word to local server trough sockets!!!
        socket.emit("record",charComm);
        return true;
    }
}
