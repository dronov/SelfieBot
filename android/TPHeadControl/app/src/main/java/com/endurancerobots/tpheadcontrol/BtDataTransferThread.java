package com.endurancerobots.tpheadcontrol;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.util.TimeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by ilya on 17.07.15.
 */
public class BtDataTransferThread extends Thread {

    private static final String TAG = "BtDataTransferThread";
    public static final int MESSAGE_READ = 1;
    public static final int CONNECTION_INFO = 2;
    public static final int READING_FAILED = 3;
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;
    boolean mSendInLoop =false;
    private byte[] mBytes=null;


//    private android.os.Handler mHandler;


    public BtDataTransferThread(BluetoothSocket socket, Handler handler) throws NullPointerException {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = handler;

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
    }

    public void run() {
        Log.d(TAG,"thread started");
        final int packLen=5;
        int packCurrentLen=0;
        byte[] pack=new byte[packLen];
        byte[] buffer = new byte[1];  // buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                    System.arraycopy(buffer, 0, pack, packCurrentLen, bytes); // TODO: Исправить до 01.09.2015. bytes не могут быть больше 1
                    packCurrentLen += bytes;
                    if (packCurrentLen == packLen) {
                        String s = new String(pack);
                        Log.v(TAG, "read " + " packet: '" + s + "' (" + Arrays.toString(pack) + ")");
                        // Send the obtained bytes to the UI activity
                        mHandler.obtainMessage(MESSAGE_READ, pack.length, -1,pack).sendToTarget();
                        packCurrentLen = 0;
                    } else {
                        Log.v(TAG, "read " + bytes + " bytes: '" + Arrays.toString(buffer) + "'");
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "Failed with reading: " + e.getMessage());
//                    break;
                    mHandler.obtainMessage(READING_FAILED, e.getMessage().length(), -1,
                            e.getMessage()).sendToTarget();
                } catch (ArrayIndexOutOfBoundsException e){
                    Log.e(TAG, e.getMessage());
                }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) throws IOException {
            mmOutStream.write(bytes);
            Log.v(TAG, "write "+bytes.length+" bytes:"+Arrays.toString(bytes));
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {Log.e(TAG, e.getMessage());}
        Log.d(TAG,"thread canceled");
    }
}


