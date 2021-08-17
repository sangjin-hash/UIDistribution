package com.example.guest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private String ip = "192.168.0.21";
    private int port = 5672;
    Socket socket;

    private Button btn_connect, btn_close;
    private boolean isClose = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = (Button)findViewById(R.id.btn_connect);
        btn_close = (Button)findViewById(R.id.btn_close);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new SocketConnect()).start();
            }
        });
    }

    class SocketConnect extends Thread{
        @Override
        public void run() {
            while(true){
                Log.d(TAG, "run: before connect");
                try {
                    socket = new Socket(ip,port);
                    Log.d(TAG, "run: socket Connect");


                } catch (IOException e) {
                    e.printStackTrace();
                }

                btn_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            socket.close();
                            Log.d(TAG, "run: socket closed");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isClose = true;
                    }
                });
                if(isClose)
                    break;
            }
        }
    }
}
