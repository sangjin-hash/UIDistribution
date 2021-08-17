package com.example.uidistribution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.example.host.IServiceInterface;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "[SOCKET] Server";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_Bind:
                Intent intent = new Intent("com.example.host.MY_SERVICE");
                intent.setPackage("com.example.host");
                //getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                Log.d(TAG, "onCreate: Bind Service");

                break;
            case R.id.button:
                try {
                    Log.d(TAG, "onCreate: Start Service from Server?");
                    myService.serviceThreadStart();
                    Log.d(TAG, "onCreate: Service Start");
                } catch (RemoteException e) {
                    Log.d(TAG, "onCreate: Service Error");
                    e.printStackTrace();
                }
                break;
        }
    }
}