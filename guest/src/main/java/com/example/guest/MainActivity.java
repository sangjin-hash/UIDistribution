package com.example.guest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private String ip = "192.168.0.21";
    private int port = 5672;
    Socket socket;
    private int maxBufferSize = 1024;

    private Handler mHandler = new Handler();
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SocketConnect client = new SocketConnect();
        Log.d(TAG,"Client의 Socket 만들기");
        client.start();
        Log.d(TAG, "Client Socket Thread 시작");
    }

    class SocketConnect extends Thread{
        @Override
        public void run() {
            try{
                Log.d(TAG, "run: before connect");
                socket = new Socket(ip,port);
                Log.d(TAG, "run: socket Connect");

                while(true){
                    if(socket.isConnected()){
                        Log.d(TAG, "Server와 연결 완료");

                        //문제점 1 연결은 됐으나 트리거 이전에 빈 소켓이 전달됌
                        byte[] recvBuffer = new byte[maxBufferSize];
                        InputStream is = socket.getInputStream();
                        is.read(recvBuffer);
                        Log.d(TAG, "서버에서 trigger 발생");
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(recvBuffer);
                        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

                        String getString = dataInputStream.readUTF();
                        Log.d(TAG,"받은 문자열 = " + getString);
                        int getSize = dataInputStream.readInt();
                        Log.d(TAG, "받은 size = " + getSize);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                    textView(getString, getSize);
                                }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: Connection failed");
            }
        }
    }

    public void textView(String txt, int txt_size){
        TextView view1 = new TextView(this);
        view1.setText(txt);
        view1.setTextSize(txt_size);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        view1.setLayoutParams(lp);
        container.addView(view1);
    }

}
