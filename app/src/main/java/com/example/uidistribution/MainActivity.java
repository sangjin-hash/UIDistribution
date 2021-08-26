package com.example.uidistribution;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.host.IServiceInterface;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "[SOCKET] Server";

    IServiceInterface myService;

    private EditText text1;
    private EditText text2;
    private EditText text3;

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

        Intent intent = new Intent("com.example.host.MY_SERVICE");
        intent.setPackage("com.example.host");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onCreate: Bind Service");

        text1 = (EditText) findViewById(R.id.text1);
        text2 = (EditText) findViewById(R.id.text2);
        text3 = (EditText) findViewById(R.id.text3);

        text1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String text = text1.getText().toString();
                int text_size = pxToDp((int) text1.getTextSize());
                try {
                    Log.d(TAG,"Test 1 에서 Trigger 발생");
                    myService.isClick();
                    myService.setStringText(text);
                    myService.setSizeOfText(text_size);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.d(TAG,"RemoteException");
                }
                return true;
            }
        });

        text2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String text = text2.getText().toString();
                int text_size = pxToDp((int) text2.getTextSize());
                try {
                    Log.d(TAG,"Test 2 에서 Trigger 발생");
                    myService.isClick();
                    myService.setStringText(text);
                    myService.setSizeOfText(text_size);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.d(TAG,"RemoteException");
                }
                return true;
            }
        });

        text3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String text = text3.getText().toString();
                int text_size = pxToDp((int) text3.getTextSize());
                try {
                    Log.d(TAG,"Test 3 에서 Trigger 발생");
                    myService.isClick();
                    myService.setStringText(text);
                    myService.setSizeOfText(text_size);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.d(TAG,"RemoteException");
                }
                return true;
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public int pxToDp(int px)
    {
        return (int)(px/ Resources.getSystem().getDisplayMetrics().density);
    }
}