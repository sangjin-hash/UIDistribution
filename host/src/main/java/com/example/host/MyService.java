package com.example.host;

import android.app.Service;
import android.content.Intent;
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
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class MyService extends Service {

    private static final String TAG = "[SOCKET] Service";
    private static boolean isClick = false;
    private static boolean isUpdate = false;
    private static String str;
    private static int text_size;
    private static String UI_flag;

    private int port = 5672;
    private ArrayList<UI_Information> UIList = new ArrayList<>();

    private Handler workerHandler;

    private final int distribute_flag = 1;
    private final int update_flag = 2;

    public IServiceInterface.Stub mBinder = new IServiceInterface.Stub() {

        @Override
        public void isClick() throws RemoteException {
            isClick = true;
        }

        public void isUpdate() throws RemoteException {
            isUpdate = true;
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

            WorkerThread worker = new WorkerThread();
            worker.start();
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

    class UI_Information {
        private String UI_ID;
        private String text;
        private boolean is_update;
        private boolean is_distribute;

        public UI_Information(String UI_ID, String text, boolean is_update, boolean is_distribute) {
            this.UI_ID = UI_ID;
            this.text = text;
            this.is_update = is_update;
            this.is_distribute = is_distribute;
        }

        public void setUI_ID(String UI_ID) {
            this.UI_ID = UI_ID;
        }

        public void set_text(String text) {
            this.text = text;
        }

        public void set_update(boolean is_update) {
            this.is_update = is_update;
        }

        public void set_distribute(boolean is_distribute) {
            this.is_distribute = is_distribute;
        }

        public String getUI_ID() {
            return UI_ID;
        }

        public String get_text() {
            return text;
        }

        public boolean get_update() {
            return is_update;
        }

        public boolean get_distribute() {
            return is_distribute;
        }
    }

    class ServerThread extends Thread {
        private ServerSocket server;
        private boolean search_result = false;

        public ServerThread(int port) throws IOException {
            server = new ServerSocket(port);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (isClick) {
                        search_result = isSearch();
                        if (!search_result) {
                            UI_Information UI = new UI_Information(UI_flag, str, false, false);
                            UIList.add(UI);
                            Socket socket = server.accept();
                            Log.d(TAG, "Socket Connected");

                            int k;
                            for (k = 0; k < UIList.size(); k++) {
                                if (UI_flag.compareTo(UIList.get(k).getUI_ID()) == 0) {
                                    break;
                                }
                            }

                            Message msg = Message.obtain();
                            msg.what = distribute_flag;
                            msg.arg1 = k;
                            msg.obj = socket;
                            workerHandler.sendMessage(msg);
                        } else {
                            Log.d(TAG, "Already existed UI");
                            isClick = false;
                        }
                    } else if (isUpdate) { //update 이전에 모든 UI가 이미 Distribute 되어 있고, 그 위에서 update할 때
                        search_result = isSearch();

                        if (search_result) {
                            int k;
                            for (k = 0; k < UIList.size(); k++) {
                                if (UI_flag.compareTo(UIList.get(k).getUI_ID()) == 0) break;
                            }
                            UIList.get(k).set_update(true);
                            UIList.get(k).set_text(str);

                            Socket socket = server.accept();

                            Message msg = Message.obtain();
                            msg.what = update_flag;
                            msg.arg1 = k;
                            msg.obj = socket;
                            workerHandler.sendMessage(msg);
                        } else {
                            Log.d(TAG, "It is not distributed so return initial state");
                            isUpdate = false;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isSearch() {
        boolean result = false;
        for (int j = 0; j < UIList.size(); j++) {
            if (UI_flag.compareTo(UIList.get(j).getUI_ID()) == 0) {
                result = true;
                break;
            } else {
                result = false;
            }
        }
        if (UIList.size() == 0) result = false;

        return result;
    }


    class WorkerThread extends Thread {

        private Socket socket;
        private int flag;
        private int k;

        public WorkerThread() {
        }

        @Override
        public void run() {
            Looper.prepare();

            workerHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    flag = msg.what;
                    k = msg.arg1;
                    socket = (Socket) msg.obj;

                    switch (flag) {
                        case 1: //distriubte
                            try {
                                byte[] dtoByteArray = null;
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                                OutputStream os = socket.getOutputStream();

                                dataOutputStream.writeInt(k);
                                dataOutputStream.writeBoolean(UIList.get(k).get_update());
                                dataOutputStream.writeUTF(str);
                                dataOutputStream.writeInt(text_size);
                                dataOutputStream.flush();

                                dtoByteArray = byteArrayOutputStream.toByteArray();
                                os.write(dtoByteArray);
                                UIList.get(k).set_distribute(true);
                                isClick = false;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case 2: //update
                            if (UIList.get(k).get_distribute()) { //distribute되었을 때 ui의 str값만 보낸다.
                                try {
                                    UIList.get(k).set_update(true);
                                    byte[] dtoByteArray = null;
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                                    OutputStream os = socket.getOutputStream();

                                    dataOutputStream.writeInt(k);
                                    dataOutputStream.writeBoolean(UIList.get(k).get_update());
                                    dataOutputStream.writeUTF(UIList.get(k).get_text());
                                    dataOutputStream.flush();

                                    dtoByteArray = byteArrayOutputStream.toByteArray();
                                    os.write(dtoByteArray);

                                    UIList.get(k).set_update(false);
                                    isUpdate = false;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {//distribute되지 않았을 때, 그냥 초기 상태로 되돌림. 즉, 다시 ServerThread가 돌게 하고, 사용자가 정보를 받는 입장에서는
                                //변경된 str값이 default값인줄 알고 있는거임.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                isUpdate = false;
                            }
                            break;
                        default:
                            Log.d(TAG, "Flag value is default");
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
}

    /*class OutputThread extends Thread {
        private Socket socket;
        private int k;

        public OutputThread(Socket socket) {
            this.socket = socket;
            outputHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    k = msg.arg1;
                }
            };
        }

        @Override
        public void run() {
            try {
                byte[] dtoByteArray = null;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                OutputStream os = socket.getOutputStream();

                dataOutputStream.writeInt(k);
                dataOutputStream.writeBoolean(UIList.get(k).get_update());
                dataOutputStream.writeUTF(str);
                dataOutputStream.writeInt(text_size);
                dataOutputStream.flush();

                dtoByteArray = byteArrayOutputStream.toByteArray();
                os.write(dtoByteArray);
                UIList.get(k).set_distribute(true);
                isClick = false;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Data IO exception");
            }
        }
    }

    class UpdateThread extends Thread {
        private Socket socket;
        private int k;

        public UpdateThread(Socket socket) {
            this.socket = socket;

            updateHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    k = msg.arg1;
                }
            };
        }

        @Override
        public void run() {

            if (UIList.get(k).get_distribute()) { //distribute되었을 때 ui의 str값만 보낸다.
                try {
                    UIList.get(k).set_update(true);
                    byte[] dtoByteArray = null;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                    OutputStream os = socket.getOutputStream();

                    dataOutputStream.writeInt(k);
                    dataOutputStream.writeBoolean(UIList.get(k).get_update());
                    dataOutputStream.writeUTF(UIList.get(k).get_text());
                    dataOutputStream.flush();

                    dtoByteArray = byteArrayOutputStream.toByteArray();
                    os.write(dtoByteArray);

                    UIList.get(k).set_update(false);
                    isUpdate = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {//distribute되지 않았을 때, 그냥 초기 상태로 되돌림. 즉, 다시 ServerThread가 돌게 하고, 사용자가 정보를 받는 입장에서는
                //변경된 str값이 default값인줄 알고 있는거임.
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isUpdate = false;
            }
        }
    }*/
