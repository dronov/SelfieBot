package com.endurancerobots.selfiebot;

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

    public static final int MESSAGE_READ = 10;
    public static final int CONNECTION_INFO = 20;
    public static final int CLOSE_CONNECTION = 113;
    public static final int MESSAGE_WRITE = 30;
    private final InputStream mmInStream;

    private final OutputStream mmOutStream;
    private final Socket mmSocket;
    private Handler mOutHandler=null;
    Handler mInHandler;   // TODO: 05.08.15 сделать обратную связь
    private boolean mSendInLoop=false;
    private byte[] mBytes;
    private byte counter=0;
    private boolean isRunning=true;
    static int numOfThreads = 0;
    private String TAG = "TcpDataTransferThread";

    public TcpDataTransferThread(Socket socket) throws NullPointerException {
        numOfThreads++;
        TAG = "TcpDataTransferThread"+numOfThreads;

        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

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
        // TODO: 05.08.15 сделать обратную связь
//        mInHandler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                switch (msg.what){
//                    case BtDataTransferThread.MESSAGE_READ:
//                        try {
//                            write((byte[])msg.obj);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    default:
//                        Log.i(TAG,"UNKNOWN_MESSAGE");
//                        break;
//                }
//            }
//        };
    }

    public void run() {
        TAG+=getName();
        Log.d(TAG, "thread started");
        byte[] buffer = new byte[128];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (isRunning) {
            try {
                bytes = mmInStream.read(buffer);
                Log.v(TAG, "read " + bytes + " bytes:" + new String(buffer,0,bytes));

                if(mOutHandler!=null) {
                    byte[] msg = new byte[bytes];
                    System.arraycopy(buffer,0,msg,0,bytes);
                    mOutHandler.obtainMessage(MESSAGE_READ, bytes, -1, msg).sendToTarget();
                    if(buffer[0]==CLOSE_CONNECTION){
                        mOutHandler.obtainMessage(CLOSE_CONNECTION, -1, -1, buffer)
                                .sendToTarget();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
                if(mOutHandler!=null) {
                    mOutHandler.obtainMessage(CONNECTION_INFO, -1, -1, "Connection lost")
                            .sendToTarget();
                }
            } catch (StringIndexOutOfBoundsException e){
                Log.e(TAG,e.getMessage());
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
        isRunning=false;
        Log.i(TAG,"canceled");
    }

    public void setOutHandler(Handler mOutHandler) {
        this.mOutHandler = mOutHandler;
    }

    public Socket getMmSocket() {
        return mmSocket;
    }

// TODO: 05.08.15 сделать обратную связь
//    public Handler getInHandler() {
//        return mInHandler;
//    }
}
