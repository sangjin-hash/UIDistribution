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
import android.widget.TextView;

import com.example.host.IServiceInterface;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "[SOCKET] Server";

    IServiceInterface myService;

    private TextView text1;
    private Button btn_change;

    private final int flag_text = 1;
    private final int flag_btn = 2;

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

        text1 = (TextView)findViewById(R.id.text01);
        btn_change = (Button)findViewById(R.id.btn_change); // Modify button name

        text1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //AIDL의 메소드를 호출해서 text1의 String text값과 int textSize값 Host로 보냄.
                String text = text1.getText().toString();
                int text_size = pxToDp((int) text1.getTextSize());
                try {
                    //이거 아닌거같음.
                    myService.getFlag(flag_text);
                    myService.getStringText(text);
                    myService.getSizeOfText(text_size);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.d(TAG,"onLongclick에서 RemoteException 발생");
                }
                return true;
            }
        });


        btn_change.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
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

            case R.id.btn_Service:
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

    public int pxToDp(int px)
    {
        return (int)(px/ Resources.getSystem().getDisplayMetrics().density);
    }
}