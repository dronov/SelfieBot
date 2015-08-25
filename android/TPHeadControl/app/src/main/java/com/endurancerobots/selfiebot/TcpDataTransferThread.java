package com.endurancerobots.selfiebot;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by ilya on 23.07.15.
 */
public class TcpDataTransferThread extends Thread {

    public static final int MESSAGE_READ = 10;
    public static final int MESSAGE_READ_ANS = 20;

    public static final int CONNECTION_INFO = 20;
    public static final int CLOSE_CONNECTION = 113;
    public static final int MESSAGE_WRITE = 30;
    public static final int BY_CLIENT = 1007;
    public static final int BY_ERROR = 999;

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Socket mmSocket;

    private Handler mData = null;
    private Handler mControl = null;
    Handler mInHandler;   // TODO: 05.08.15 сделать обратную связь
    static int numOfThreads = 0;
    private String TAG = "TcpDataTransferThread";
    public static final String ECHO_TAG = "echo:";
    private boolean feedEnabled=false;

    public TcpDataTransferThread(Socket socket) throws NullPointerException {
        numOfThreads++;
        TAG = "TcpTransfer_"+numOfThreads;

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
//                    case BtDataTransferThread.MESSAGE_RECIEVED:
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
//        Looper.prepare();

    }

    public void run() {
        TAG+="_"+getName();
        Log.d(TAG, "thread started");
        byte[] buffer = new byte[128];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (!isInterrupted()) {
            try {
                bytes = mmInStream.read(buffer);
                Log.v(TAG, "read " + bytes + " bytes:" + new String(buffer, 0, bytes));
                if(buffer[0]==113){
                    if (mControl != null) {
                        mControl.obtainMessage(CLOSE_CONNECTION, BY_CLIENT,0).sendToTarget();
                        Log.i(TAG, "Client wants to close connection. I'll send msg to service");
                    }else Log.w(TAG,"Oops, mControl handler is null");
                    interrupt();
                }
                sendDataToService(MESSAGE_READ, buffer, bytes);
//                if(new String(buffer).contains(ECHO_TAG)){
////                    sendDataToService(MESSAGE_READ_ANS, buffer, bytes);
//                }else
//                {
//                    sendEchoMsg(buffer);
//                }
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
                if (mControl != null) {
                    mControl.obtainMessage(CLOSE_CONNECTION,BY_ERROR,0).sendToTarget();
                }
                interrupt();
            } catch (StringIndexOutOfBoundsException e){
                Log.w(TAG,"String index error:"+ e.getMessage());
                if (mControl != null) {
                    mControl.obtainMessage(CLOSE_CONNECTION,BY_ERROR,0).sendToTarget();
                }
                interrupt();
            }
        }
        try {
            mmInStream.close();
            mmOutStream.close();
            Log.i(TAG,"Streams closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEchoMsg(byte[] buffer) throws IOException {
        write((ECHO_TAG + new String(buffer)).getBytes());
    }

    private void sendDataToService(int messageType, byte[] buffer, int bytes) {
        if(mData != null) {
            byte[] msg = new byte[bytes];
            System.arraycopy(buffer,0,msg,0,bytes);
            mData.obtainMessage(MESSAGE_READ, bytes, -1, msg).sendToTarget();
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] buf) throws IOException {
        if(!isInterrupted()) {
            mmOutStream.write(buf);
            Log.v(TAG, "write " + buf.length + " bytes:" + new String(buf));
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        Log.i(TAG, "interrupt()");
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        interrupt();
    }

    public void setOutControlHandler(Handler mOutControlHandler) {
        this.mControl = mOutControlHandler;
    }

    public void setOutDataHandler(Handler mOutHandler) {
        this.mData = mOutHandler;
    }

    public Socket getMmSocket() {
        return mmSocket;
    }

    public void setFeedEnabled(boolean feedEnabled) {
        this.feedEnabled = feedEnabled;
    }

    // TODO: 05.08.15 сделать обратную связь
    public Handler getInHandler() {
        return mInHandler;
    }
}
