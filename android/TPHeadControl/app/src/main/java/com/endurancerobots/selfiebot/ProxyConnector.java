package com.endurancerobots.selfiebot;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by ilya on 09.07.15.
 */
public class ProxyConnector extends Thread {
    public static final String PROXY_IP = "46.38.49.133";
    public static final int TCP_PROXY_SERVER_PORT = 4445;


    private static final String TAG = "ProxyConnector";
    public static final String ERROR = "\r\nERROR\r\n";
    public static final String WAIT = "\r\nWAIT\r\n";
    public static final String CONNECT = "\r\nCONNECT\r\n";
    public static final String NOT_RESPONSED = "NOT_RESP";
    public static final String NOT_CONNECTED = "NOT_CONNECTED";
    public static final int CONNECTED_SOCKET = 111;

    private String mHeadId="";
    Handler mInHandler;
    Handler mOutHandler;
    TcpDataTransferThread transferThread;

    private byte mInputBuf[] = new byte[50];
    private String mStrId=null;
    private Socket socket = new Socket();
    Boolean isConnected = false;

    public ProxyConnector(Handler outHandler){
        mOutHandler=outHandler;
        mInHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case TcpDataTransferThread.MESSAGE_READ:
                        String s = new String((byte[])msg.obj,0,msg.arg1);
                        if (s.contains(CONNECT)) {
                            Log.i(TAG, CONNECT);
                            isConnected=true;

                            Socket sock = getSocket();
                            if(mOutHandler!=null){
                                mOutHandler.obtainMessage(CONNECTED_SOCKET,sock)
                                        .sendToTarget();
                            }
                            transferThread.cancel();
                        } else if (s.contains(WAIT)) {
                            Log.v(TAG, WAIT);
                        } else if (s.contains(ERROR)) {
                            Log.e(TAG, ERROR);
                        } else {
                            Log.i(TAG, "Got only: " + Arrays.toString((byte[])msg.obj));
                        }
                }
            }
        };
    }
    public void startAsServer(String headId){
        mStrId = "S" + headId + "\r";
        this.start();
    }
    public void startAsClient(String headId){
        mStrId = "G" + headId + "\r";
        this.start();
    }

    @Override
    public synchronized void start() {
        if(mStrId!=null)
            super.start();
        else Log.i(TAG,"idStringIs null!");
    }

    @Override
    public void run() {
        super.run();
        connect(PROXY_IP, TCP_PROXY_SERVER_PORT);
    }

    public Socket getSocket() {
        if(isConnected) return socket;
        else return null;
    }

    /**
     * @param host - server ip-address
     * @param port - server port
     * @return true - with successful connection, else return "false"
     */
    public void connect(String host, int port) {
        if (socket.isConnected()) {
            Log.w(TAG, "Already connected to " + host + ":" + port);
        }
        else {
            try {
//                TODO сделать подключение в отдельном потоке (v6)
                socket.connect(new InetSocketAddress(host, port));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socket.isConnected()) {
                Log.i(TAG, "Successful connection to " + host + ":" + port);
                transferThread = new TcpDataTransferThread(socket);
                transferThread.setOutHandler(mInHandler);
                transferThread.start();
                try {
                    transferThread.write(mStrId.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "Can't connect to server" + host + ":" + port);
            }
        }
    }

}
