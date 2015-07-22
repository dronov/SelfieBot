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
public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private DataTransferThread mDataTransferThread;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final

        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = adapter;
        mHandler = handler;
        try {
            Method m = device.getClass().getMethod("createRfcommSocket",new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(device,1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
        Log.i(TAG, "mmSocket = tmp;");
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();
        Log.i(TAG, "cancelDiscovery");
        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            Log.e(TAG,connectException.getMessage()+"\ntrying to close the socket");
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG,closeException.getMessage());
            } catch (NullPointerException e){
                Log.e(TAG,"BT Socket is null: "+e.getMessage());
            }
            return;
        }

        Log.i(TAG,"manageConnectedSocket");
    }

    public DataTransferThread getDataTransferThread() {
        mDataTransferThread = new DataTransferThread(mmSocket, mHandler);
        mDataTransferThread.start();
        cancel();
        return mDataTransferThread;
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
    }

}
