package com.example.guest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private String ip = "192.168.0.21";
    private int maxBufferSize = 1024;

    private Handler mHandler = new Handler();
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (LinearLayout) findViewById(R.id.layout);

        ClientThread client = new ClientThread();
        client.start();
    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = new Socket(ip, port);
                    InputThread inputThread = new InputThread(socket);
                    inputThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class InputThread extends Thread {

        private Socket socket;

        public InputThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                byte[] recvBuffer = new byte[maxBufferSize];
                InputStream is = socket.getInputStream();
                int num = is.read(recvBuffer);

                if (num > 0) {
                    Log.d(TAG, "Trigger Event Occured in Server");
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(recvBuffer);
                    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

                    String getString = dataInputStream.readUTF();
                    int getSize = dataInputStream.readInt();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            EditText(getString, getSize);
                        }
                    });
                } else{
                    Log.d(TAG,"Trigger Event NOT occured");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void EditText(String txt, int txt_size) {
        EditText view1 = new EditText(this);
        view1.setText(txt);
        view1.setTextSize(txt_size);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        view1.setLayoutParams(lp);
        container.addView(view1);
    }
}
