package com.endurancerobots.selfiebot;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * Created by ilya on 12.08.15.
 */
public class P2PConnector extends Thread {
    public static final int SERVER_PORT = 6336;
    private static final String TAG = "P2PConnector";
    public static final int UPDATE_SOCKET = 1333;
    public static final int DO_NOT_UPDATE_SOCKET = 3111;
    public static final CharSequence IP_TAG = "IP\n\r";
    ServerSocket mServerSock;
    Handler mOutHandler;
    TcpDataTransferThread mTransferThread;


    public P2PConnector(Handler handler, TcpDataTransferThread transferThread){
        mOutHandler=handler;
        mTransferThread=transferThread;
        try {
            mServerSock = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            sendServerAddr(getLocalIpAddress(), SERVER_PORT);
            mServerSock.setSoTimeout(3000);
            updateTransportSocket(mServerSock.accept());
        } catch (IOException e) {
            Log.w(TAG, "Unable get accept from client. Send proxy socket.");
            mOutHandler.obtainMessage(DO_NOT_UPDATE_SOCKET).sendToTarget();
        }
        super.run();
    }

    private void sendServerAddr(String ip, int port) {
        Log.d(TAG, "sendServerAddr:" + ip + ":" + port);
        try {
            mTransferThread.write((IP_TAG+ip+"\n\r").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTransportSocket(Socket socket4transport){
        Log.d(TAG, "updateTransportSocket:" + socket4transport.toString());
        mOutHandler.obtainMessage(UPDATE_SOCKET, -1, 0, socket4transport).sendToTarget();
    }
    private String getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        Log.i(TAG, "My IP:"+inetAddress.getHostAddress());
                        return inetAddress.getHostAddress();
                    }else
                    {
                        Log.i(TAG, "My IP:"+inetAddress.getHostAddress());
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    static public String parseAddress(String inMsg) {
        Log.i(TAG, "Parsed address'"+inMsg.split("\n\r")[1]+"'");
        return inMsg.split("\n\r")[1];

    }

    public void cancel() {
        mTransferThread.cancel();
        interrupt();
    }
}
