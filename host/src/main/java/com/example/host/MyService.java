package com.example.host;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyService extends Service {
    
    private static final String TAG = "[SOCKET] Service";

    IServiceInterface myService;

    final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: Service Connected?");
            myService = IServiceInterface.Stub.asInterface(service);
            Log.d(TAG, "onServiceConnected: Service Connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    public Binder mBinder = new IServiceInterface.Stub() {
        @Override
        public void serviceThreadStart() throws RemoteException {
            Log.d(TAG, "serviceThreadStart: ");
            ServerThread serverThread = new ServerThread();
            serverThread.start();
        }

        @Override
        public String getStringText(String text) throws RemoteException {
            return text;
        }

        @Override
        public int getSizeOfText(int size) throws RemoteException {
            return size;
        }

        @Override
        public int getFlag(int flag) throws RemoteException {
            return flag;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
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
                while (true){
                    Socket socket = serverSocket.accept();
                    Log.d(TAG, "run: accept!");

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
