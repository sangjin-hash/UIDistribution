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

    private int port = 5672;
    private Handler ServerHandler;
    private Handler OutputHandler;

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
        ServerHandlerThread serverHandlerThread = new ServerHandlerThread();
        serverHandlerThread.start();

        OutputHandlerThread outputHandlerThread = new OutputHandlerThread();
        outputHandlerThread.start();
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
        private ArrayList<Socket> uiList;
        private int i = 0;

        public ServerThread(int port) throws IOException {
            server = new ServerSocket(port);
            uiList = new ArrayList<Socket>();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (isClick) { // 발생할 수 있는 문제점 -> 같은 UI를 여러번 눌렀을 때 중복으로 생성되지 않게 막아야함.
                        Socket socket = server.accept();
                        uiList.add(socket);

                        OutputThread outputThread = new OutputThread(uiList.get(i));
                        outputThread.start();

                        Message m = Message.obtain();
                        m.what = i;
                        Log.d(TAG, "Message 객체를 만들어 what에 i를 넣어 보내기 -> i =" + m.what);
                        OutputHandler.sendMessage(m);
                        i++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ServerHandlerThread extends Thread {
        public ServerHandlerThread() {
            ServerHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    Log.d(TAG, msg.what + "번째 UI에 해당하는 Socket 생성 완료");
                }
            };
        }

        @Override
        public void run() {
            Looper.prepare();
            Looper.loop();
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

    class OutputHandlerThread extends Thread {
        public OutputHandlerThread() {
            OutputHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    int i = msg.what;
                    Log.d(TAG, "ServerHandler에게서 msg를 받음 i = " + i);
                    Message m2 = Message.obtain();
                    m2.what = i;
                    ServerHandler.sendMessage(m2);
                }
            };
        }

        @Override
        public void run() {
            Looper.prepare();
            Looper.loop();
        }
    }
}
