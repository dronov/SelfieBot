package com.endurancerobots.tpheadcontrol;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
    /// TODO: наследоваться от класса Service (v5)

    static final String START_SERVO_CONTROL = "com.endurancerobots.tpheadcontrol.action.START_SERVO_CONTROL";
    static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    static final String EXTRA_MAC = "com.endurancerobots.tpheadcontrol.extra.MAC";
    static final String TAG = "ServoControlService";

    TcpDataTransferThread tcpDataTransferThread;
    String mMacAddr;
    TcpProxyClient mServ;
    byte[] mInMsg =new byte[5];
    BtDataTransferThread mBtDataTransferThread;
    OutputStream mOutStream = null;
    boolean mBtTransferIsEnabled=true;
    byte mMsgCounter =1;
    static Handler sHandler;
    String mHeadId;

    public static Intent startServoControl(Context context, String headId_, String mac_) {
        /// TODO: наследоваться от класса Service: скопировать это в MainActivity (v5)
        Log.v(TAG, "startServoControl");
        Intent intent = new Intent(context, ServoControlService.class);
        intent.setAction(START_SERVO_CONTROL);
        intent.putExtra(EXTRA_HEAD_ID, headId_);
        intent.putExtra(EXTRA_MAC, mac_);
        context.startService(intent);
        return intent;
    }

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
                        try {
                            writeToBluetooth((byte[])msg.obj);
                            writeToOperator(((byte[])msg.obj)); // Обратная связь
                            publishProgress(ComandDecoder.decode((byte[]) msg.obj));

                        } catch (IOException e ) {
                            e.printStackTrace();
                            if(isTcpUnreachable) {
                                writeToOperator(getString(R.string.bluetooth_source_is_unreachable).getBytes());
                                publishProgress("e1"+getString(R.string.bluetooth_source_is_unreachable));
                                isTcpUnreachable =false;
                            }
                            stopSelf();
                        } catch (NullPointerException e){
                            e.printStackTrace();
                            publishProgress("e2" + getString(R.string.bluetooth_source_is_unreachable_not_connected));
                            stopSelf();
                        }
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

    class ConnectionThread extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            return mServ.connectAsServer();
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);
            Log.v(TAG, "Connected: "+String.valueOf(connected));
            if(connected){
                Log.v(TAG, "Server is connected");
                Toast.makeText(getApplicationContext(),
                        getString(R.string.successful_connection), Toast.LENGTH_LONG).show();
                tcpDataTransferThread = new TcpDataTransferThread(mServ,sHandler);
                tcpDataTransferThread.start();
            }else {
                Log.v(TAG, "Server is not connected");
                Toast.makeText(getApplicationContext(),
                        getString(R.string.unsuccessful_connection), Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }
    }
    private void startTcpThread(String headId, String mac){
        // TODO: запускать сервер в отдельном потоке наследованномот TcpDataTransferThread (in v5)
        boolean connected=true;
        connected = bluetoothConnect(mac);
        if(connected) {
            publishProgress(getString(R.string.holder_is_connected));
            mServ = new TcpProxyClient(headId);
            ConnectionThread socketThread = new ConnectionThread();
            socketThread.execute();
        }else {
            publishProgress(getString(R.string.holder_is_not_connected));
        }
    }

    private boolean bluetoothConnect(String mac) {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetooth.getRemoteDevice(mac);

        try {
            BtConnectThread mBtConnectThread = new BtConnectThread(device, bluetooth, sHandler);
            mBtConnectThread.start();

            mBtDataTransferThread = mBtConnectThread.getDataTransferThread();
            mBtConnectThread.cancel();

//            writeToBluetooth(new String("ping").getBytes());
            return true;
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }

    private void writeToOperator(byte[] bytes){
        try {
            tcpDataTransferThread.write(bytes);
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
            tcpDataTransferThread.cancel();
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

    private String runTcpServer() {
        Log.v(TAG, "runTcpServer");
        try {
            InputStream inStream = mServ.getInputStream();
            while (true){
                int read = inStream.read(mInMsg);
                Log.d(TAG, "received msg: " + Arrays.toString(mInMsg) + "read: " + read);
                try{
                    writeToBluetooth(mInMsg);
                    switch (mInMsg[0]) {
                        case 119:
                            publishProgress("Command: UP (" + mInMsg[0] + ")");
                            writeToOperator(new byte[]{mInMsg[0]});
                            break;
                        case 97:
                            publishProgress("Command: LEFT (" + mInMsg[0] + ")");
                            writeToOperator(new byte[]{mInMsg[0]});
                            break;
                        case 115:
                            publishProgress("Command: DOWN (" + mInMsg[0] + ")");
                            writeToOperator(new byte[]{mInMsg[0]});
                            break;
                        case 100:
                            publishProgress("Command: RIGHT (" + mInMsg[0] + ")");
                            writeToOperator(new byte[]{mInMsg[0]});
                            break;
                        case 113:
                            publishProgress("Command: CLOSE CONNECTION (" + mInMsg[0] + ")");
                            writeToOperator(new byte[]{mInMsg[0]});
                            return "CLOSE";
                        default:
                            publishProgress("Unknown command: (" + mInMsg[0] + ")");
                            writeToOperator(new String("Unknown command: ("+ mInMsg[0]+")").getBytes());
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
        return "CLOSE";
    }
}
