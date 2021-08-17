package com.example.guest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private String ip = "192.168.0.21";
    private int port = 5672;
    Socket socket;

    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "run: before connect");
                    socket = new Socket(ip,port);
                    Log.d(TAG, "run: socket Connect");
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    str = (String)ois.readObject();
                    Log.d(TAG, "run: " + str);
                    socket.close();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: Socket no connect");

                }

            }
        }).start();
    }

}

