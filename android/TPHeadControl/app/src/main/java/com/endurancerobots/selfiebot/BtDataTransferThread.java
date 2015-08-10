package com.endurancerobots.selfiebot;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by ilya on 17.07.15.
 */
public class BtDataTransferThread extends Thread {

    private static final String TAG = "BtDataTransferThread";
    public static final int MESSAGE_READ = 1;
    public static final int CONNECTION_INFO = 2;
    public static final int READING_FAILED = 3;
    private static final int MESSAGE_WRITE = 4;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mInHandler;
    private Handler mOutHandler = null; // TODO: Соеденить напрямую с потоком TCP (Учесть Broadcast-сообщения). Убрать после 07.08.2015
    boolean mSendInLoop =false;
    private byte[] mBytes=null;
    private int counter=0;
    private boolean isRunning=true;

//    private android.os.Handler mOutHandler;


    public BtDataTransferThread(BluetoothSocket socket) throws NullPointerException {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (Exception e) {
            Log.e(TAG,"Exeption: "+e.getMessage());
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        mInHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case TcpDataTransferThread.MESSAGE_READ:
                        try {
                            write((byte[])msg.obj);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Log.i(TAG,"UNKNOWN_MESSAGE");
                        break;
                }
            }
        };
    }

    public void run() {
        Log.d(TAG,"thread started");
        final int packLen=5;
        int packCurrentLen=0;
        byte[] pack=new byte[packLen];
        byte[] buffer = new byte[1];  // buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs

                    // TODO: 05.08.15 сделать обратную связь
            while (isRunning) {
//                try {
//                    bytes = mmInStream.read(buffer);
//                    System.arraycopy(buffer, 0, pack, packCurrentLen, bytes);
//                     TODO: Исправить до 01.09.2015. bytes не могут быть больше 1
//                    packCurrentLen += bytes;
//                    if (packCurrentLen == packLen) {
//                        String s = new String(pack);
//                        Log.v(TAG, "read "+" packet: '" +s+ "' (" + Arrays.toString(pack) + ")");
//                        if(mOutHandler!=null) {
//                            mOutHandler.obtainMessage(MESSAGE_READ,
//                                    pack.length, -1, pack).sendToTarget();
//                        }
//                        packCurrentLen = 0;
//                    } else {
//                        Log.v(TAG, "read " + bytes + " bytes: '" + Arrays.toString(buffer) + "'");
//                    }
//                } catch (NullPointerException e){
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    Log.e(TAG, "Failed with reading: " + e.getMessage());
//                    break;
//                    mOutHandler.obtainMessage(READING_FAILED, e.getMessage().length(), -1,
//                            e.getMessage()).sendToTarget();
//                } catch (ArrayIndexOutOfBoundsException e){
//                    Log.e(TAG, e.getMessage());
//                }
//                emulateResponse(pack);
            }
    }

    private void emulateResponse(byte[] pack) {
        if(mOutHandler!=null) {
            pack = "12345".getBytes(); // TODO: 05.08.15 убрать эмуляцию
            mOutHandler.obtainMessage(MESSAGE_READ, pack.length, -1, pack).sendToTarget();
        }
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) throws IOException {
            mmOutStream.write(bytes);
            Log.v(TAG, "write "+bytes.length+" bytes:"+Arrays.toString(bytes));
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        isRunning=false;
        try {
            mmSocket.close();
        } catch (IOException e) {Log.e(TAG, e.getMessage());}
        Log.d(TAG,"thread canceled");
    }

    public Handler getInHandler() {
        return mInHandler;
    }

//    public void setOutHandler(Handler mOutHandler) {
//        this.mOutHandler = mOutHandler;
//    }
}


