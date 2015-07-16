package com.endurancerobots.headcontrolclient;

import android.util.Log;
import android.widget.Toast;

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
    String strId = "G987654321\r";
    private byte inputBuf[] = new byte[50];
    public static final int TCP_PROXY_SERVER_PORT = 4445;
    public static final String PROXY_IP = "46.38.49.133";
    /**
     * Connection with proxy client
     * @param proxyIp - ip-address of server
     * @param proxyServerPort - destination port
     * @return true if connection is successful
     */
    public boolean runTcpProxyClient(String proxyIp, int proxyServerPort) {
        String s="";
        if (runTcpClient(proxyIp, proxyServerPort)) {
//            while (!s.contains(CONNECT))
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.getOutputStream()));
                    /// Send id-string
                    out.write(strId);
                    out.flush();
                    Log.i(TAG, "Send id-string '" + strId + "'");
                    /// Receive answer
                    BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
                    this.getInputStream().read(inputBuf);
                    Log.d(TAG, "Receive answer: " + Arrays.toString(inputBuf));
                    ///Analize string
                    s = new String(inputBuf, "UTF-8");
                    Log.d(TAG, "Convert answer: " + s);
                    if (s.contains(CONNECT)) {
                        Log.i(TAG, CONNECT);
                        return true;
                    } else if (s.contains(WAIT)) {
                        Log.i(TAG, WAIT);
                    } else if (s.contains(ERROR)) {
                        Log.e(TAG, ERROR);
                        return false;
                    } else {
                        Log.d(TAG, "Got only: " + Arrays.toString(inputBuf));
                        return false;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
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
        try {
           connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (isConnected()) {
            Log.i(TAG, "Successful connection to " + host + ":" + port);
            return true;
        } else {
            Log.w(TAG, "Can't connect to server" + host + ":"+port);
            return false;
        }
    }

//    @Override
//    public synchronized void close() throws IOException {
//        this.getOutputStream().write((byte) 99);
//        super.close();
//    }
}
