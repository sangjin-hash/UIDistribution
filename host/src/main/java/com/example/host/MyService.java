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
    private static boolean isClick = false;
    private static String str;
    private static int text_size;

    public IServiceInterface.Stub mBinder = new IServiceInterface.Stub() {

        @Override
        public void isClick() throws RemoteException {
            isClick = true;
        }

        public void setStringText(String text) throws RemoteException{
            str = text;
        }

        public void setSizeOfText(int size) throws RemoteException{
            text_size = size;
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
            String ip = "192.168.0.28";

            try{
                while(true){
                    if(isClick){
                        Log.d(TAG,"Trigger Event Occured");

                        Log.d(TAG, "run: before connect");
                        Socket socket = new Socket(ip,port);
                        Log.d(TAG, "run: socket Connect");

                        byte[] dtoByteArray = null;
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                        OutputStream os = socket.getOutputStream();

                        try {
                            dataOutputStream.writeUTF(str);
                            dataOutputStream.writeInt(text_size);
                            dataOutputStream.flush();

                            dtoByteArray = byteArrayOutputStream.toByteArray();
                            os.write(dtoByteArray);
                            isClick = false;
                            socket.close();
                            Log.d(TAG, "Socket closed");
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Data IO exception");
                        }
                    }else{
                        Log.d(TAG,"Not Trigger Event Occured");
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "run: IO exception");
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
