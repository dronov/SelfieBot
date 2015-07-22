package com.endurancerobots.tpheadcontrol;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by ilya on 17.07.15.
 */
public class DataTransferThread extends Thread {

    private static final String TAG = "DataTransferThread";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;
//    private android.os.Handler mHandler;

    public DataTransferThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1];  // buffer store for the stream
        int bytes; // bytes returned from read()
//        Packet packet = new Packet();


        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                Log.i(TAG, "read " + 1 + " bytes:" + Arrays.toString(buffer));
//                packet.appendByte(buffer[0]);
//                if(packet.isReady()){
//                    Log.i(TAG, "read Packet with " + packet.getPackLength() + " bytes:" + Arrays.toString(packet.getBytes()));
//                }
                // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget();

            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
            Log.i(TAG, "write "+bytes.length+" bytes:"+Arrays.toString(bytes));
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { Log.e(TAG,e.getMessage());}
    }
}


