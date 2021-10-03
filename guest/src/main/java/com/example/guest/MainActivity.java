package com.example.guest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[SOCKET] Guest";

    private int port = 5672;
    private String ip = "192.168.0.21";
    private int maxBufferSize = 1024;

    private Handler mHandler = new Handler();
    private Handler updateHandler = new Handler();
    private Handler workerHandler;

    private LinearLayout container;
    private ArrayList<EditText> UI_List = new ArrayList<EditText>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (LinearLayout) findViewById(R.id.layout);

        ClientThread client = new ClientThread();
        client.start();

        WorkerThread worker = new WorkerThread();
        worker.start();
    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = new Socket(ip, port);
                    Message msg = Message.obtain();
                    msg.obj = socket;
                    workerHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class WorkerThread extends Thread{
        private Socket socket;

        public WorkerThread(){ }

        public void run(){
            Looper.prepare();

            workerHandler = new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    socket = (Socket)msg.obj;

                    try {
                        byte[] recvBuffer = new byte[maxBufferSize];
                        InputStream is = socket.getInputStream();
                        int num = is.read(recvBuffer);

                        if (num > 0) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(recvBuffer);
                            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

                            int k = dataInputStream.readInt();
                            Boolean isUpdate = dataInputStream.readBoolean();
                            String getString = dataInputStream.readUTF();

                            if (isUpdate) {
                                Log.d(TAG,"Update is occured");
                                updateHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        UI_List.get(k).setText(getString);
                                    }
                                });
                            } else {
                                Log.d(TAG, "Update is NOT occured");
                                int getSize = dataInputStream.readInt();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        EditText(getString, getSize);
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "Trigger Event NOT occured");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
        }
    }

    public void EditText(String txt, int txt_size) {
        EditText view1 = new EditText(this);
        UI_List.add(view1);
        view1.setText(txt);
        view1.setTextSize(txt_size);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        view1.setLayoutParams(lp);
        container.addView(view1);
    }
}
