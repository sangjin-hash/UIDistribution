package com.example.host;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MyService extends Service {

    private static final String TAG = "[SOCKET] Service";
    private static boolean isClick = false;
    private static String str;
    private static int text_size;
    private static String UI_flag;

    private int port = 5672;
    private ArrayList<Socket> uiList;
    private ArrayList<String> idList;

    public IServiceInterface.Stub mBinder = new IServiceInterface.Stub() {

        @Override
        public void isClick() throws RemoteException {
            isClick = true;
        }

        public void setStringText(String text) throws RemoteException {
            str = text;
        }

        public void setSizeOfText(int size) throws RemoteException {
            text_size = size;
        }

        public void setFlag(String flag) throws RemoteException {
            UI_flag = flag;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ServerThread server = new ServerThread(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return mBinder;
    }

    class ServerThread extends Thread {
        private ServerSocket server;
        private boolean search_result = false;
        private int i = 0;

        public ServerThread(int port) throws IOException {
            server = new ServerSocket(port);
            uiList = new ArrayList<Socket>();
            idList = new ArrayList<String>();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (isClick) {
                        for (int j = 0; j < idList.size(); j++) {
                            if (UI_flag.compareTo(idList.get(j)) == 0) {
                                search_result = true;
                                break;
                            } else {
                                search_result = false;
                            }
                        }

                        if (idList.size() == 0) search_result = false;

                        if (!search_result) {
                            idList.add(UI_flag);
                            Socket socket = server.accept();
                            uiList.add(socket);

                            OutputThread outputThread = new OutputThread(uiList.get(i));
                            outputThread.start();
                            outputThread.join();
                            i++;
                        } else {
                            Log.d(TAG, "이미 Distribution된 UI입니다.");
                            isClick = false;
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class OutputThread extends Thread {
        private Socket socket;

        public OutputThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                byte[] dtoByteArray = null;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                OutputStream os = socket.getOutputStream();
                dataOutputStream.writeUTF(str);
                dataOutputStream.writeInt(text_size);
                dataOutputStream.flush();

                dtoByteArray = byteArrayOutputStream.toByteArray();
                os.write(dtoByteArray);
                isClick = false;
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Data IO exception");
            }
        }
    }
}
