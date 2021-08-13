package com.example.guest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private String ip = "192.168.0.21";
    private int port = 5672;
    private Socket socket;

    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    socket = new Socket(ip,port);
                    Log.d("Connect", "Socket 연결 완료");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Connect", "Connect 실패");
                }

                while(true){
                    try {
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        str = (String)ois.readObject();
                        Log.d("통신 결과", "받은 문자열 = "+str);
                        socket.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        Log.d("통신 결과", "통신에 실패하였습니다");
                    }
                }
            }
        });
    }
}