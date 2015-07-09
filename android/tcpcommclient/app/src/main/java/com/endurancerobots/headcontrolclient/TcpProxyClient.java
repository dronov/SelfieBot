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

/**
 * Created by ilya on 09.07.15.
 */
public class TcpProxyClient extends Socket {
    String ERROR = "\r\nERROR\r\n";
    String WAIT = "\r\nWAIT\r\n";
    String CONNECT = "\r\nCONNECT\r\n";
    String strId = "G123456789\r";
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
                    Log.i("TcpClient.proxy", "Send id-string '" + strId + "'");
                    /// Receive answer
                    BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStream()));
                    this.getInputStream().read(inputBuf);
                    Log.i("TcpClient.proxy", "Receive answer: " + inputBuf);
                    ///Analize string
                    s = new String(inputBuf, "UTF-8");
                    Log.i("TcpClient.proxy", "Convert answer: " + s);
                    if (s.contains(CONNECT)) {
                        Log.i("TcpClient.proxy", CONNECT);
                        return true;
                    } else if (s.contains(WAIT)) {
                        Log.i("TcpClient.proxy", WAIT);
//                        wait(500);
                        return false; //
                    } else if (s.contains(ERROR)) {
                        Log.e("TcpClient.proxy", ERROR);
                        return false;
                    } else {
                        Log.i("TcpClient.proxy", "Got only: " + inputBuf.toString());
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
            Log.i("TcpClient", "Successful connection to " + host + ":" + port);
            return true;
        } else {
            Log.i("TcpClient", "Can't connect to server" + host + ":"+port);
            return false;
        }
    }
}
