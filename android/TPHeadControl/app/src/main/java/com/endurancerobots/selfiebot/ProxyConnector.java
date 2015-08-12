package com.endurancerobots.selfiebot;

import android.os.AsyncTask;
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
    public static final int CONNECTED_CLIENT_SOCKET = 111;
    public static final int CONNECTED_SERVER_SOCKET = 222;

    private String mHeadId="";
    Handler mInHandler;
    Handler mOutHandler;


    private byte mInputBuf[] = new byte[50];
    private String mStrId=null;

    Boolean isConnected = false;
    private P2PConnector p2pConnector;
    private TcpDataTransferThread transferThread;
    private Socket socket = new Socket();

    public ProxyConnector(Handler outHandler) {
        mOutHandler = outHandler;
        mInHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case TcpDataTransferThread.MESSAGE_READ:
                        String s = new String((byte[]) msg.obj, 0, msg.arg1);
                        if (s.contains(CONNECT)) {
                            Log.i(TAG, CONNECT);
                            isConnected = true;
                            if(mStrId.contains("S"))startP2PConnection(); // TODO: 12.08.15 подключение только для сервера
                        } else if (s.contains(WAIT)) {
                            Log.v(TAG, WAIT);
                        } else if (s.contains(ERROR)) {
                            Log.e(TAG, ERROR);
                        } else if (s.contains(P2PConnector.IP_TAG)) {
                            String addr = P2PConnector.parseAddress(s);
                            if (addr != null) {
                                transferThread.cancel();
                                ConnectorTask p2pConnector = new ConnectorTask();
                                p2pConnector.execute(addr);
                            }
                        } else Log.i(TAG, "Got only: " + Arrays.toString((byte[]) msg.obj));
                        break;
                    case P2PConnector.UPDATE_SOCKET:
                        Log.i(TAG, "UPDATE_SOCKET");
                        transferThread.cancel();
                        if (mOutHandler != null) {
                            mOutHandler.obtainMessage(CONNECTED_SERVER_SOCKET, 0,0, msg.obj)
                                    .sendToTarget();
                            Log.i(TAG, "obtainMessage with new Socket");
                        }else Log.w(TAG, "mOutHandler is null");
                    case P2PConnector.DO_NOT_UPDATE_SOCKET:
                        Log.i(TAG, "DO_NOT_UPDATE_SOCKET");
                        transferThread.cancel();
                        if (mOutHandler != null) {
                            mOutHandler.obtainMessage(CONNECTED_SERVER_SOCKET, 0,0,getSocket())
                                    .sendToTarget();
                            Log.i(TAG, "obtainMessage with OLD Socket");
                        }else Log.w(TAG, "mOutHandler is null");

                        break;
                }
            }
        };
    }
    class ConnectorTask extends AsyncTask<String,Void,Socket>{
        @Override
        protected Socket doInBackground(String... address) {
            Socket sock = null;
            try {
                sock = new Socket(address[0], P2PConnector.SERVER_PORT);
                return sock;
            } catch (IOException e) {
                Log.w(TAG,"Couldn't connect directly. Try to connect via proxy");
                return getSocket();
            }
        }
        @Override
        protected void onPostExecute(Socket socket) {
            super.onPostExecute(socket);
            if (socket != null && mOutHandler != null) {
                mOutHandler.obtainMessage(CONNECTED_CLIENT_SOCKET, socket)
                        .sendToTarget();
                Log.i(TAG, "Transport Socket updated");
            } else Log.i(TAG, "Unable to update Transport Socket");
        }
    }
    private void startP2PConnection() {
        p2pConnector = new P2PConnector(mInHandler,transferThread);
        p2pConnector.start();
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
