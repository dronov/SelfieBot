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

public class ServoControlService extends IntentService {
    private static final String START_SERVO_CONTROL = "com.endurancerobots.tpheadcontrol.action.START_SERVO_CONTROL";
    public static final int BLUETOOTH_MESSAGE_READ = 1;
    private static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    private static final String EXTRA_MAC = "com.endurancerobots.tpheadcontrol.extra.MAC";
    private static final String TAG = "ServoControlService";
    private String headId = "123456789";
    private String macAddr;
    private TcpProxyClient serv;
    private byte[] inMsg=new byte[5];
    private int NOTIFY_ID=101;
    private Context context;
    private BtDataTransferThread mBtDataTransferThread;
    private static Handler mHandler;
    private BtConnectThread btConnectThread;
    private OutputStream mOutStream;
    private boolean mBtTransferIsEnabled=true;

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
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case BLUETOOTH_MESSAGE_READ:
                        Log.d(TAG,"got bluetoth message "+Arrays.toString((byte[])msg.obj)
                                +"with length: "+msg.arg1);
                        break;
                    default:
                        Log.w(TAG,"Unknown message "+Arrays.toString((byte[])msg.obj)
                                +"with length: "+msg.arg1);
                }
            }
        };
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
        boolean connected=true;
        if(mBtTransferIsEnabled)
            connected = bluetoothConnect();
        if(connected) {
            publishProgress(getString(R.string.holder_is_connected));
            Log.i(TAG, "Bluetooth device connected");
            while (!answ.contains("CLOSE")) {
                if (serv.connectAsServer(headId)) {
                    try {
                        mOutStream = serv.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    publishProgress(getString(R.string.successful_connect));
                    answ = runTcpServer();
                }
            }
            try {
                serv.close();
                Log.i(TAG, "Server closed");
            } catch (IOException e) {
                Log.e(TAG, "error while closing server: " + e.getMessage());
            }catch (NullPointerException e){
                Log.e(TAG, "Null pointer: " + e.getMessage());
            }
            publishProgress(getString(R.string.server_closed));
        }else {
            publishProgress(getString(R.string.holder_is_not_connected));
        }

        stopSelf();
    }

    private boolean bluetoothConnect() {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetooth.getRemoteDevice(getMac());

        try {
            btConnectThread = new BtConnectThread(device, bluetooth, mHandler);
            btConnectThread.start();

            mBtDataTransferThread = btConnectThread.getDataTransferThread();
            btConnectThread.cancel();

            mBtDataTransferThread.write(new String("ping").getBytes());
            return true;
        }catch (NullPointerException e){
            e.printStackTrace();
        } catch (IOException e) {
            mBtDataTransferThread.cancel();
        }
        return false;
    }

    private String runTcpServer() {
        Log.v(TAG, "runTcpServer");
        try {
            InputStream inStream = serv.getInputStream();
            while (true){
                int read = inStream.read(inMsg);
                Log.d(TAG, "received msg: " + Arrays.toString(inMsg) + "read: " + read);
                try{
                    writeToBluetooth(inMsg);
                    switch (inMsg[0]) {
                        case 119:
                            publishProgress("Command: UP (" + inMsg[0] + ")");
                            writeToOperator(new byte[]{inMsg[0]});
                            break;
                        case 97:
                            publishProgress("Command: LEFT (" + inMsg[0] + ")");
                            writeToOperator(new byte[]{inMsg[0]});
                            break;
                        case 115:
                            publishProgress("Command: DOWN (" + inMsg[0] + ")");
                            writeToOperator(new byte[]{inMsg[0]});
                            break;
                        case 100:
                            publishProgress("Command: RIGHT (" + inMsg[0] + ")");
                            writeToOperator(new byte[]{inMsg[0]});
                            break;
                        case 113:
                            publishProgress("Command: CLOSE CONNECTION (" + inMsg[0] + ")");
                            writeToOperator(new byte[]{inMsg[0]});
                            return "CLOSE";
                        default:
                            publishProgress("Unknown command: (" + inMsg[0] + ")");
                            writeToOperator(new String("Unknown command: ("+ inMsg[0]+")").getBytes());
                    }
                }catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    publishProgress(getString(R.string.bluetooth_source_is_unreachable));
                    writeToOperator(getString(R.string.bluetooth_source_is_unreachable).getBytes());
                }
                catch (NullPointerException e){
                    Log.w(TAG,e.getMessage());
                }
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
    private void writeToOperator(byte[] bytes){
        try {
            if(mOutStream!=null) {
                mOutStream.write(bytes);
                Log.d(TAG, "writeToOperator: " + Arrays.toString(bytes));
            }else {
                Log.i(TAG, "writeToOperator failed: stream is null");
            }
        } catch (IOException e) {
            Log.v(TAG, "error when write echo message");
        }
    }

    private void writeToBluetooth(byte[] bytes)throws IOException{
            if(mBtTransferIsEnabled)
                mBtDataTransferThread.write(bytes);
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
        super.onDestroy();
    }



}
