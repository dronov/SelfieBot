package com.endurancerobots.tpheadcontrol;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Arrays;

public class ServoControlService extends Service {

    static final String START_SERVO_CONTROL = "com.endurancerobots.tpheadcontrol.action.START_SERVO_CONTROL";
    static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    static final String EXTRA_MAC = "com.endurancerobots.tpheadcontrol.extra.MAC";
    static final String TAG = "ServoControlService";

    TcpDataTransferThread mTcpDataTransferThread;
    String mMacAddr;
    TcpProxyClient mServ;
    byte[] mInMsg =new byte[5];
    BtDataTransferThread mBtDataTransferThread;
    OutputStream mOutStream = null;
    boolean mBtTransferIsEnabled=true;
    byte mMsgCounter =1;
    static Handler sHandler;
    String mHeadId;

    public ServoControlService() {
        sHandler = new Handler(){
            public boolean isTcpUnreachable =true;
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case BtDataTransferThread.MESSAGE_READ:
                        Log.d(TAG, "Got bluetooth message " + Arrays.toString((byte[]) msg.obj)
                                + "with length: " + msg.arg1);
                        break;
                    case BtDataTransferThread.CONNECTION_INFO:
                        Log.v(TAG, "Got INFO message from device" + (String) msg.obj);
                        break;
                    case BtDataTransferThread.READING_FAILED:
                        publishProgress("e4"+getString(R.string.bluetooth_source_is_unreachable));
                        stopSelf();
                        break;
                    case TcpDataTransferThread.MESSAGE_READ:
                        Log.d(TAG,"Got TCP message "+Arrays.toString((byte[])msg.obj)
                                +"with length: "+msg.arg1);
//                        try {
////                            writeToBluetooth((byte[])msg.obj);
////                            writeToOperator(((byte[])msg.obj)); // Обратная связь
//                            publishProgress(ComandDecoder.decode((byte[]) msg.obj));
//
//                        } catch (IOException e ) {
//                            e.printStackTrace();
//                            if(isTcpUnreachable) {
//                                writeToOperator(getString(R.string.bluetooth_source_is_unreachable).getBytes());
//                                publishProgress("e1"+getString(R.string.bluetooth_source_is_unreachable));
//                                isTcpUnreachable =false;
//                            }
//                            stopSelf();
//                        } catch (NullPointerException e){
//                            e.printStackTrace();
//                            publishProgress("e2" + getString(R.string.bluetooth_source_is_unreachable_not_connected));
//                            stopSelf();
//                        }
                        break;
                    case TcpDataTransferThread.CLOSE_CONNECTION:
                        Log.i(TAG, "TCP connection info: " + (String)msg.obj);
                        stopSelf();
                        break;
                    case TcpDataTransferThread.CONNECTION_INFO:
                        Log.v(TAG, "TCP connection info: " + (String)msg.obj);
                        break;
                    default:
                        Log.w(TAG,"Unknown message "+Arrays.toString((byte[])msg.obj)
                                +"with length: "+msg.arg1);
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
                mHeadId = intent.getStringExtra(EXTRA_HEAD_ID);
                mMacAddr = intent.getStringExtra(EXTRA_MAC);
                startTcpThread(mHeadId, mMacAddr);
            }
        }
    }

    class ConnectionThread extends Thread {
        private String stateLast="";

        public void run() {
            Log.d(TAG, "doInBackground");
            String status="";
            do {
                status = mServ.connectAsServer();
                publishProgress(status);
            }while (!status.contains(TcpProxyClient.CONNECT));
            Log.v(TAG, "Server is connected");
            publishProgress(getString(R.string.successful_connection));
            mTcpDataTransferThread = new TcpDataTransferThread(mServ);
            mTcpDataTransferThread.start();
            // Связываем потоки напрямую
//                mBtDataTransferThread.setOutHandler(mTcpDataTransferThread.getInHandler());
            if(mBtDataTransferThread!=null)
                mTcpDataTransferThread.setOutHandler(mBtDataTransferThread.getInHandler());
            cancel();
        }

        public void cancel() {

        }

//        @Override
//        protected void onPostExecute(Boolean connected) {
//            super.onPostExecute(connected);
//            Log.v(TAG, "Connected: "+String.valueOf(connected));
//            if(connected){
//                Log.v(TAG, "Server is connected");
//                Toast.makeText(getApplicationContext(),
//                        getString(R.string.successful_connection), Toast.LENGTH_LONG).show();
//                mTcpDataTransferThread = new TcpDataTransferThread(mServ);
//                mTcpDataTransferThread.start();
//                // Связываем потоки напрямую
////                mBtDataTransferThread.setOutHandler(mTcpDataTransferThread.getInHandler());
//                if(mBtDataTransferThread!=null)
//                    mTcpDataTransferThread.setOutHandler(mBtDataTransferThread.getInHandler());
//            }else {
//                Log.v(TAG, "Server is not connected");
//                Toast.makeText(getApplicationContext(),
//                        getString(R.string.unsuccessful_connection), Toast.LENGTH_LONG).show();
//                stopSelf();
//            }
//        }
    }
    private void startTcpThread(String headId, String mac){
        // TODO: 05.08.15 сделать обмен данными между узлами независимым
        boolean connected=true;
        connected = bluetoothConnect(mac);
        if(connected) {
            publishProgress(getString(R.string.holder_is_connected));
        }else {
            publishProgress(getString(R.string.holder_is_not_connected));
        }
        mServ = new TcpProxyClient(headId);
        ConnectionThread socketThread = new ConnectionThread();
        socketThread.start();
    }

    private boolean bluetoothConnect(String mac) {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetooth.getRemoteDevice(mac);

        try {
            BtConnectThread mBtConnectThread = new BtConnectThread(device, bluetooth);
            mBtConnectThread.start();

            mBtDataTransferThread = mBtConnectThread.getDataTransferThread();
//            mBtDataTransferThread.setOutHandler(sHandler);
            mBtConnectThread.cancel();
            return true;
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }

    private void writeToOperator(byte[] bytes){
        try {
            mTcpDataTransferThread.write(bytes);
            Log.v(TAG, "writeToOperator: " + Arrays.toString(bytes));
        } catch (IOException e) {
            publishProgress("e3"+getString(R.string.cant_write_to_operator));
            stopSelf();
        }
    }

    private void writeToBluetooth(byte[] bytes)throws IOException, NullPointerException {
//            if(mBtTransferIsEnabled) {
        mBtDataTransferThread.write(bytes);
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
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service atempting to destroy");
        try {
            mBtDataTransferThread.cancel();
            publishProgress(getString(R.string.bt_thread_closed));
            mTcpDataTransferThread.cancel();
            publishProgress(getString(R.string.server_closed));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
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
