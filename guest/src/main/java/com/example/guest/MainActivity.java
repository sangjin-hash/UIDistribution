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
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private int port = 5672;
    private int maxBufferSize = 1024;

    private Handler mHandler = new Handler();
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (LinearLayout)findViewById(R.id.layout);

        SocketConnect client = new SocketConnect();
        client.start();
    }

    class SocketConnect extends Thread{
        @Override
        public void run() {
            try{
                ServerSocket serverSocket = new ServerSocket(port);

                while(true){
                    Socket socket = serverSocket.accept();
                    Log.d(TAG, "run: accept!");

                    byte[] recvBuffer = new byte[maxBufferSize];
                    InputStream is = socket.getInputStream();
                    int num = is.read(recvBuffer);

                    if(num > 0){
                        Log.d(TAG, "Trigger Event Occured in Server");
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(recvBuffer);
                        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

                        String getString = dataInputStream.readUTF();
                        int getSize = dataInputStream.readInt();

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                    textView(getString, getSize);
                                }
                        });
                        socket.close();
                        Log.d(TAG,"Socket closed");
                    }
                    else{
                        Log.d(TAG, "Not Trigger Event Occured");
                        socket.close();
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
