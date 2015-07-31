package com.endurancerobots.tpheadcontrol;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by ilya on 09.07.15.
 */
public class TcpProxyClient extends Socket {
    public static final String PROXY_IP = "46.38.49.133";
    public static final int TCP_PROXY_SERVER_PORT = 4445;

    private static final String TAG = "TcpProxyClient";
    private static final String ERROR = "\r\nERROR\r\n";
    private static final String WAIT = "\r\nWAIT\r\n";
    private static final String CONNECT = "\r\nCONNECT\r\n";

    private byte mInputBuf[] = new byte[50];

    public boolean connectAsClient(String headId){
        Log.v(TAG,"connectAsClient");
        return connectToProxyServer(PROXY_IP,TCP_PROXY_SERVER_PORT,"G"+headId+"\r");
    }
    public boolean connectAsServer(String headId){
        Log.v(TAG,"connectAsServer");
        return connectToProxyServer(PROXY_IP,TCP_PROXY_SERVER_PORT,"S"+headId+"\r");
    }
    /**
     * Connection with proxy client
     * @param proxyIp - ip-address of server
     * @param proxyServerPort - destination port
     * @return true if connection is successful
     */
    private boolean connectToProxyServer(String proxyIp, int proxyServerPort, String headId) {
        String mStrId = headId;
        if (runTcpClient(proxyIp, proxyServerPort)) {
                try {
                    String s="";
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.getOutputStream()));
                    /// Send id-string
                    out.write(mStrId);
                    out.flush();
                    Log.v(TAG, "Send id-string '" + mStrId + "'");
                    /// Receive answer
                    this.getInputStream().read(mInputBuf);
                    Log.v(TAG, "Receive answer: " + Arrays.toString(mInputBuf));
                    ///Analize string
                    s = new String(mInputBuf, "UTF-8");
                    Log.v(TAG, "Convert answer: " + s);
                    if (s.contains(CONNECT)) {
                        Log.i(TAG, CONNECT);
                        return true;
                    } else if (s.contains(WAIT)) {
                        Log.v(TAG, WAIT);
                    } else if (s.contains(ERROR)) {
                        Log.e(TAG, ERROR);
                        return false;
                    } else {
                        Log.i(TAG, "Got only: " + Arrays.toString(mInputBuf));
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }
    /**
     * @param host - server ip-address
     * @param port - server port
     * @return true - with successful connection, else return "false"
     */
    public boolean runTcpClient(String host, int port) {
        if (isConnected()) {
            Log.v(TAG, "Already connected to " + host + ":" + port);
            return true;
        }
        else {
            try {
//                TODO сделать подключение в отдельном потоке (v6)
                connect(new InetSocketAddress(host, port));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (isConnected()) {
                Log.i(TAG, "Successful connection to " + host + ":" + port);
                return true;
            } else {
                Log.w(TAG, "Can't connect to server" + host + ":" + port);
                return false;
            }
        }
    }

}
