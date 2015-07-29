package com.endurancerobots.tpheadcontrol;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by ilya on 23.07.15.
 */
public class TcpDataTransferThread extends Thread {
    private static final String TAG = "BtDataTransferThread";

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final TcpProxyClient mmSocket;
    private boolean succesfullSent; //Успешная доставка
    private Handler mHandler;

    public TcpDataTransferThread(TcpProxyClient socket, Handler handler) throws NullPointerException {
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
            Log.e(TAG, "Exeption: " + e.getMessage());
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[128];  // buffer store for the stream
        int bytes; // bytes returned from read()
//        Packet packet = new Packet();

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                Log.d(TAG, "read " + bytes + " bytes:" + Arrays.toString(buffer));
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(UIControlService.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();

            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) throws IOException {
        mmOutStream.write(bytes);
        Log.d(TAG, "write "+bytes.length+" bytes:"+Arrays.toString(bytes));
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (Exception e) {Log.e(TAG, e.getMessage());}
    }
}
