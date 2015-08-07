package com.endurancerobots.tpheadcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ilya on 17.07.15.
 */
public class BtConnectThread extends Thread {
    public static final int BLUETOOTH_SOCKET_OPEN = 112;
    public static final int BLUETOOTH_SOCKET_CLOSE = 211;

    private static final String TAG = "BtConnectThread";
    private final BluetoothSocket mmSocket;
    private Handler mOutHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isRunning=true;

    public BtConnectThread(BluetoothDevice device,
                           BluetoothAdapter adapter,
                           Handler handler) throws NullPointerException{
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final

        BluetoothSocket tmp = null;
        mBluetoothAdapter = adapter;
        mOutHandler = handler;
        try {
            Method m = device.getClass().getMethod("createRfcommSocket",new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(device,1);
        } catch (SecurityException e) {
            Log.e("BLUETOOTH", e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        mmSocket = tmp;
        if(mmSocket==null) {
            NullPointerException e = new NullPointerException("Device was not connected");
            Log.e(TAG, "mmSocket is nul");
            throw e;
        }

    }

    public void run() {
        Log.d(TAG, "thread started");

        mBluetoothAdapter.cancelDiscovery();
        Log.i(TAG, "cancelDiscovery");
        try {
            mmSocket.connect();
            mOutHandler.obtainMessage(BLUETOOTH_SOCKET_OPEN,mmSocket).sendToTarget();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "\ntrying to close the socket");
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
                mOutHandler.obtainMessage(BLUETOOTH_SOCKET_CLOSE).sendToTarget();
            } catch (IOException closeException) {
                Log.e(TAG,closeException.getMessage());
            } catch (NullPointerException ne){
                Log.e(TAG,"BT Socket is null: "+ne.getMessage());
            }
            return;
        }

    }

    public BtDataTransferThread getDataTransferThread() throws NullPointerException {
        BtDataTransferThread mBtDataTransferThread = new BtDataTransferThread(mmSocket);
        mBtDataTransferThread.start();
        return mBtDataTransferThread;
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        isRunning=false;
        Log.d(TAG,"thread canceled");
    }
}
