package com.endurancerobots.tpheadcontrol;

import android.app.IntentService;
//import android.app.Notification;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.Arrays;
import at.abraxas.amarino.Amarino;

public class ServoControlService extends IntentService {
    private static final String START_SERVO_CONTROL = "com.endurancerobots.tpheadcontrol.action.START_SERVO_CONTROL";

    private static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    private static final String EXTRA_MAC = "com.endurancerobots.tpheadcontrol.extra.MAC";
    private static final String TAG = "ServoControlService";
    private String headId = "123456789";
    private String macAddr;
    private TcpProxyClient serv;
    private byte[] inMsg=new byte[5];
    private Handler handler;
    private int NOTIFY_ID=101;
    private Context context;
    private DataTransferThread mDataTransferThread;
    private Handler mHandler;
    private ConnectThread connectThread;

    public static void startServoControl(Context context, String headId_, String mac_) {
        Log.v(TAG, "startServoControl");
        Intent intent = new Intent(context, ServoControlService.class);
        intent.setAction(START_SERVO_CONTROL);
        intent.putExtra(EXTRA_HEAD_ID, headId_);
        intent.putExtra(EXTRA_MAC, mac_);
        context.startService(intent);
    }

    public ServoControlService() {
        super("UIControlService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (START_SERVO_CONTROL.equals(action)) {
                final String headId_ = intent.getStringExtra(EXTRA_HEAD_ID);
                final String mac_ = intent.getStringExtra(EXTRA_MAC);
                handleActionStartServoControl(headId_,mac_);
            }
        }
    }

    private void handleActionStartServoControl(String headId_, String mac) {
        Log.v(TAG, "handleActionStartServoControl");
        Log.i(TAG, "headId_=" + headId_);

        Log.i(TAG,headId_+" "+mac);
        headId=headId_;
        macAddr = mac;
        serv = new TcpProxyClient();
        String answ="";
        if(bluetoothConnect()) {
            while (!answ.contains("CLOSE")) {
                if (serv.connectAsServer(headId)) {
                    publishProgress(getString(R.string.successful_connect));
                    answ = runTcpServer();
                }
            }
        }
        try {
            if(serv != null)
                serv.close();
        } catch (IOException e) {
            Log.e(TAG, "error while closing server: "+e.getMessage());
        }
        Log.i(TAG, "Server closed");
        publishProgress(getString(R.string.server_closed));
        stopSelf();
    }

    private boolean bluetoothConnect() {
//        Amarino.connect(getApplicationContext(),macAddr);
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetooth.getRemoteDevice(getMac());
        connectThread = new ConnectThread(device,bluetooth, mHandler);
        connectThread.start();

        mDataTransferThread = connectThread.getDataTransferThread();
        if(mDataTransferThread==null){
            publishProgress(getString(R.string.holder_is_not_connected));
            return false;
        }else{
            publishProgress(getString(R.string.holder_is_connected));
            return true;
        }

    }

    private String runTcpServer() {
        Log.v(TAG, "runTcpServer");
        try {
            while (true){
                int read = serv.getInputStream().read(inMsg);
                Log.d(TAG, "received msg: " + Arrays.toString(inMsg) +"read: "+read);
                write(inMsg);
                switch (inMsg[0]) {
                    case 119:
                        publishProgress("Command: UP (" + inMsg[0] + ")");
                        break;
                    case 97:
                        publishProgress("Command: LEFT (" + inMsg[0] + ")");
                        break;
                    case 115:
                        publishProgress("Command: DOWN (" + inMsg[0] + ")");
                        break;
                    case 100:
                        publishProgress("Command: RIGHT (" + inMsg[0] + ")");
                        break;
                    case 113:
                        publishProgress("Command: CLOSE CONNECTION (" + inMsg[0] + ")");
                        return "CLOSE";
                    default:
                        publishProgress("Unknown command: ("+inMsg[0]+")");
                }

                //TODO: Передавать байтовые массивы "не вскрывая"
            }
        } catch (InterruptedIOException e) {
            //if timeout occurs
            Log.e(TAG, "timeout occurs " + e.getMessage() + "!!!");
        } catch (SocketException se){
            if(se.getMessage().contains("ECONNRESET")){
                Log.e(TAG, "client cuts wire!!! " + se.getMessage() + "!!!");
                publishProgress(getString(R.string.connection_cutted));
            } else{
                Log.e(TAG, "problem with socket"+se.getMessage());}
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage() + "!!!");
            Toast.makeText(getApplicationContext(),"problem with socket",Toast.LENGTH_SHORT).show();
        }
        return "PROBLEM";
    }

    private void write(byte[] bytes) {
        mDataTransferThread.write(bytes);
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
        notificationManager.notify(NOTIFY_ID, notification);
    }



    public String getMac() {
        return macAddr;
    }

    @Override
    public void onDestroy() {
        Amarino.disconnect(getApplicationContext(),getMac());
        super.onDestroy();
    }



}
