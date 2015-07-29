package com.endurancerobots.tpheadcontrol;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Timestamp;
import java.sql.Time;
import java.util.Arrays;

/**
 * Created by ilya on 17.07.15.
 */
public class BtDataTransferThread extends Thread {

    private static final String TAG = "BtDataTransferThread";

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;
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
        byte[] buffer = new byte[1];  // buffer store for the stream
        int bytes; // bytes returned from read()
//        Packet packet = new Packet();

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                Log.v(TAG, "read " + 1 + " bytes:" + Arrays.toString(buffer));
//                packet.appendByte(buffer[0]);
//                if(packet.isReady()){
//                    Log.i(TAG, "read Packet with " + packet.getPackLength() + " bytes:" + Arrays.toString(packet.getBytes()));
//                }
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(ServoControlService.BLUETOOTH_MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();

            } catch (IOException e) {
                Log.e(TAG,"Failed with reading:"+e.getMessage());
                break;
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
        } catch (Exception e) {Log.e(TAG, e.getMessage());}
    }
}


