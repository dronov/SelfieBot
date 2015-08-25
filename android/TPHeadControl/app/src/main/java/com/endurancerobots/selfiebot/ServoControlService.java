package com.endurancerobots.selfiebot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ServoControlService extends Service {

    static final String START_SERVO_CONTROL = "com.endurancerobots.selfiebot.action.START_SERVO_CONTROL";
    static final String EXTRA_HEAD_ID = "com.endurancerobots.selfiebot.extra.HEAD_ID";
    static final String EXTRA_MAC = "com.endurancerobots.selfiebot.extra.MAC";
    static final String TAG = "ServoControlService";


    TcpDataTransferThread mTcpTransport;
    String mMacAddr;
    ProxyConnector mConnector;
    byte[] mInMsg =new byte[5];
    BtDataTransferThread mBtTransport;
    OutputStream mOutStream = null;
    boolean mBtTransferIsEnabled=true;
    byte mMsgCounter =1;
    static Handler sDataHandler, sControlHandler;
    String mHeadId;
    private P2PConnector p2pConnector;
    private PendingIntent pendingIntent;
    private boolean mAutoConnection=true;
    private static int sInstacesCount =0;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.w(TAG,"Service got action: "+action);
            if(intent.getAction().equals(MainActivity.MAIN_ACTIVITY_START)){
                sendBroadcast(new Intent(MainActivity.SERVO_CONTROL_IS_ALIFE));
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");

        IntentFilter filter = new IntentFilter(MainActivity.MAIN_ACTIVITY_START);
        registerReceiver(receiver, filter);

        sDataHandler = new Handler(){
            String TAG = ServoControlService.TAG+"hm";
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ProxyConnector.CONNECTED_SERVER_SOCKET:
                        publishProgress(getString(R.string.successful_connection));
                        mConnector.cancel();

                        mTcpTransport = new TcpDataTransferThread((Socket)msg.obj);
                        mTcpTransport.setName("Server");
                        mTcpTransport.setFeedEnabled(true);
                        mTcpTransport.setOutControlHandler(sControlHandler);
                        if(mBtTransport !=null) {
                            mTcpTransport.setOutDataHandler(mBtTransport.getInHandler());
                        }
                        mTcpTransport.start();
                        try {
                            pendingIntent.send(MainActivity.SERVER_CONNECTED);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        break;
                    case BtConnectThread.BLUETOOTH_SOCKET_OPEN:
                        publishProgress(getString(R.string.holder_is_connected));
                        mBtTransport = new BtDataTransferThread((BluetoothSocket)msg.obj);
                        mBtTransport.setName("Bluetooth client");
                        mBtTransport.start();
                        if(mTcpTransport !=null) {
                            mTcpTransport.setOutDataHandler(mBtTransport.getInHandler());
                        }
                        break;
                    case BtConnectThread.BLUETOOTH_SOCKET_CLOSE:
                        publishProgress(getString(R.string.holder_is_not_connected));
                        break;
                    default:
                        Log.w(TAG,"Unknown message "+Arrays.toString((byte[])msg.obj)
                                +"with length: "+msg.arg1);
                }
            }
        };

        sControlHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG, "handleMessage: "+msg.what+" "+msg.arg1);
                switch (msg.what) {
                    case TcpDataTransferThread.CLOSE_CONNECTION:
                        switch (msg.arg1){
                            case TcpDataTransferThread.BY_ERROR:
                                publishProgress(getString(R.string.connection_closed_by_error));
                                if(mAutoConnection){
                                    publishProgress(getString(R.string.wait_for_another_connection));
                                    startTcpThread(mHeadId, mMacAddr);
                                }
                                break;
                            case TcpDataTransferThread.BY_CLIENT:
                                publishProgress(getString(R.string.connection_closed_by_client));
                                if(mAutoConnection){
                                    publishProgress(getString(R.string.wait_for_another_connection));
                                    startTcpThread(mHeadId, mMacAddr);
                                }
                                break;
                        }
                        break;
                    default:
                        Log.w(TAG,"Unknown message");
                        break;
                }
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
        if (intent != null) {
            final String action = intent.getAction();
            if (START_SERVO_CONTROL.equals(action)) {
                if(sInstacesCount <1) {
                    sInstacesCount++;
                    mHeadId = intent.getStringExtra(EXTRA_HEAD_ID);
                    mMacAddr = intent.getStringExtra(EXTRA_MAC);
                    pendingIntent = intent.getParcelableExtra(MainActivity.EXTRA_SERVER_PINTENT);
                    startTcpThread(mHeadId, mMacAddr);
                }else {
                    Log.w(TAG,"Should be only one server");
                }
            }
        }
    }

    private void startTcpThread(String headId, String mac){
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetooth.getRemoteDevice(mac);
        BtConnectThread mBtConnectThread = new BtConnectThread(device, bluetooth, sDataHandler);
        mBtConnectThread.start();

        mConnector = new ProxyConnector(sDataHandler);
        mConnector.startAsServer(headId);
        try {pendingIntent.send(MainActivity.SERVER_START_CONNECTION);}
        catch (PendingIntent.CanceledException e) {e.printStackTrace();}
    }

    private void writeToOperator(byte[] bytes){
        try {
            mTcpTransport.write(bytes);
            Log.v(TAG, "writeToOperator: " + Arrays.toString(bytes));
        } catch (IOException e) {
            publishProgress("e3"+getString(R.string.cant_write_to_operator));
            stopSelf();
        }
    }

    private void writeToBluetooth(byte[] bytes)throws IOException, NullPointerException {
//            if(mBtTransferIsEnabled) {
        mBtTransport.write(bytes);
        mMsgCounter++;
        //            }
    }

    private void publishProgress(String s) {
        Log.i(TAG, s);

        /** Make notification */
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(s)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(s); // Текст уведомления

        Notification notification = builder.build();
        final int NOTIFY_ID = 101;
        notificationManager.notify(NOTIFY_ID, notification);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service atempting to destroy");
        try {
            mBtTransport.cancel();
            publishProgress(getString(R.string.bt_thread_closed));
            mTcpTransport.cancel();
            publishProgress(getString(R.string.server_closed));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        sendBroadcast(new Intent(MainActivity.SERVO_CONTROL_STOPPED));
        unregisterReceiver(receiver);
        Log.d(TAG, "Service destroyed");
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG,"stopService");
        return super.stopService(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
