package com.endurancerobots.tpheadcontrol;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by ilya on 09.07.15.
 */
public class TcpProxyClient extends Socket {
    private static final String TAG = "TcpProxyClient";
    String ERROR = "\r\nERROR\r\n";
    String WAIT = "\r\nWAIT\r\n";
    String CONNECT = "\r\nCONNECT\r\n";
    String strId = "987654321";
    private byte inputBuf[] = new byte[50];
    public static final int TCP_PROXY_SERVER_PORT = 4445;
    public static final String PROXY_IP = "46.38.49.133";
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
        strId=headId;
        if (runTcpClient(proxyIp, proxyServerPort)) {
                try {
                    String s="";
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.getOutputStream()));
                    /// Send id-string
                    out.write(strId);
                    out.flush();
                    Log.v(TAG, "Send id-string '" + strId + "'");
                    /// Receive answer
                    this.getInputStream().read(inputBuf);
                    Log.v(TAG, "Receive answer: " + Arrays.toString(inputBuf));
                    ///Analize string
                    s = new String(inputBuf, "UTF-8");
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
                        Log.i(TAG, "Got only: " + Arrays.toString(inputBuf));
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
//                TODO сделать подключение в отдельном потоке
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
