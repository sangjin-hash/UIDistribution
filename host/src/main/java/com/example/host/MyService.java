package com.example.host;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyService extends Service {
    
    private static final String TAG = "[SOCKET] Service";

    IServiceInterface myService;

    public Binder mBinder = new IServiceInterface.Stub() {

        private boolean isClick;
        private String text;
        private int size;

        @Override
        public void isClick() throws RemoteException {
            isClick = true;
        }

        public boolean getClick() throws RemoteException{
            return isClick;
        }

        public void setStringText(String text) throws RemoteException{
            this.text = text;
        }

        @Override
        public String getStringText() throws RemoteException {
            return text;
        }

        public void setSizeOfText(int size) throws RemoteException{
            this.size = size;
        }

        @Override
        public int getSizeOfText() throws RemoteException {
            return size;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        ServerThread server = new ServerThread();
        server.start();
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

    class ServerThread extends Thread{
        @Override
        public void run() {
            Log.d(TAG, "run: accept?");
            int port = 5672;

            try{
                ServerSocket serverSocket = new ServerSocket(port);
                Log.d(TAG, "서버가 실행됨");
                while (true){
                    Socket socket = serverSocket.accept();
                    Log.d(TAG, "run: accept!");

                    while(true){
                            Log.d(TAG,"while문 안으로 들어옴");
                            boolean isClick = myService.getClick();
                            Log.d(TAG, "isClick = "+isClick); //문제점 2 myService 자체가 안됌.
                            if(isClick){
                                String text = myService.getStringText();
                                Log.d(TAG, "myService.getStringText() = "+text);
                                int size = myService.getSizeOfText();
                                Log.d(TAG, "myService.getSizeOfText() = "+size);
                                byte[] dtoByteArray = null;

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                                OutputStream os = socket.getOutputStream();

                                try {
                                    dataOutputStream.writeUTF(text);
                                    dataOutputStream.writeInt(size);
                                    dataOutputStream.flush();

                                    dtoByteArray = byteArrayOutputStream.toByteArray();
                                    os.write(dtoByteArray);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "dataOutputStream IO exception");
                                }
                            }
                        }
                    }
            } catch (UnknownHostException e){
                Log.d(TAG, "run: unknown host exception");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "run: IO exception");
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
