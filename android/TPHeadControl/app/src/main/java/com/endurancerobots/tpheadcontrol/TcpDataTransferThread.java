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

    private static final String TAG = "TcpDataTransferThread";
    public static final int MESSAGE_READ = 10;
    public static final int CONNECTION_INFO = 20;
    public static final int CLOSE_CONNECTION = 113;

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final TcpProxyClient mmSocket;
    private Handler mHandler;
    private boolean mSendInLoop=false;
    private byte[] mBytes;

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
        Log.d(TAG,"thread started");
        byte[] buffer = new byte[128];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                Log.v(TAG, "read " + bytes + " bytes:" + Arrays.toString(buffer));
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
                if(buffer[0]==CLOSE_CONNECTION){
                    mHandler.obtainMessage(CLOSE_CONNECTION, -1, -1, buffer)
                            .sendToTarget();
                }

            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
                mHandler.obtainMessage(CONNECTION_INFO, -1, -1, "Connection lost")
                        .sendToTarget();
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
            Log.d(TAG, "thread canceled");
        } catch (Exception e) {Log.e(TAG, e.getMessage());}
    }
}
