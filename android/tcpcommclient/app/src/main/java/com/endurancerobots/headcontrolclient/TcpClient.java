package com.endurancerobots.headcontrolclient;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.ByteArrayBuffer;

public class TcpClient extends Activity {
    public static final String FILE_NAME = "filename"; // TODO: delete this string
    private byte inMsg[] = new byte[5];

    private byte outMsg[] = new byte[5];
    private Socket mS;

    private static final int TCP_SERVER_PORT = 4445;
    private static final int TCP_PROXY_SERVER_PORT = 4445;
    private static final String PROXY_IP = "46.38.49.133";
    private static final String COMP_IP = "192.168.1.117";
    private Context _context;
    private Intent _serviceIntent;
    private String ip="localhost";
    private int port=4445;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        _context = this.getBaseContext();
        _serviceIntent = new Intent(_context, com.endurancerobots.headcontrolclient.ControlService.class);
    }

    /// TODO: Сделать акитвити настроек
    /**
     * Connection to server
     * @param view
     */
    public void connectClick(View view) {
        EditText editHeadIp = (EditText)findViewById(R.id.editHeadIp);
        mS = new Socket();
        boolean connected = runTcpClient(editHeadIp.getText().toString(), TCP_SERVER_PORT);
        Log.i("TcpClient","P2PConnect");
        if(connected){
            hideUI();
            Toast.makeText(getApplicationContext(),
                    getString(R.string.connectionp2p), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.connectionp2pnot), Toast.LENGTH_SHORT).show();
        }
    }
    public void connectProxyClick(View view) {
        mS = new Socket();
        boolean connected = runTcpProxyClient(PROXY_IP, TCP_PROXY_SERVER_PORT);
        Log.i("TcpClient","ProxyConnect");
        if(connected){
            hideUI();
            Toast.makeText(getApplicationContext(),
                    getString(R.string.connectionproxy), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.connectionproxynot), Toast.LENGTH_SHORT).show();
        }
    }
    String ERROR = "\r\nERROR\r\n";
    String WAIT = "\r\nWAIT\r\n";
    String CONNECT = "\r\nCONNECT\r\n";
    String strId = "G123456789\r";
    private byte inputBuf[] = new byte[50];

    /**
     * Connection with proxy client
     * @param proxyIp - ip-address of server
     * @param proxyServerPort - destination port
     * @return true if connection is successful
     */
    private boolean runTcpProxyClient(String proxyIp, int proxyServerPort) {
        String s="";
            if (runTcpClient(proxyIp, proxyServerPort)) {
                while (!s.contains(CONNECT))
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(mS.getOutputStream()));
                    /// Send id-string
                    out.write(strId);
                    out.flush();
                    Log.i("TcpClient.proxy", "Send id-string '" + strId + "'");
                    /// Receive answer
                    BufferedReader in = new BufferedReader(new InputStreamReader(mS.getInputStream()));
                    mS.getInputStream().read(inputBuf);
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
                        continue;
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

    private void hideUI() {
        /** Make UI transparent to see the Skype, Linphone etc...*/
//        RelativeLayout connectionUILayout = (RelativeLayout) findViewById(R.id.connectionUI);
//        connectionUILayout.setVisibility(View.INVISIBLE);
        finish();
        startService(_serviceIntent);
    }
    /**
     * @param host - server ip-address
     * @param port - server port
     * @return true - with successful connection, else return "false"
     */
    private boolean runTcpClient(String host, int port) {
        try {
            mS.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (mS.isConnected()) {
            Log.i("TcpClient", "Successful connection to " + host + ":"+port);
            return true;
        } else {
            Log.i("TcpClient", "Can't connect to server" + host + ":"+port);
            return false;
        }
    }

	//replace runTcpClient() at onCreate with this method if you want to run tcp client as a service
	private void runTcpClientAsService() {
		Intent lIntent = new Intent(this.getApplicationContext(), TcpClientService.class);
        this.startService(lIntent);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //close connection
        try {
            mS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        stopService(new Intent(this, MyService.class));
        Log.i("TcpClient", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("TcpClient", "onStop");
    }
    public void turnLeft(View view)  {
        outMsg[0] = 'a';
        outMsg[1] = 'a';
        outMsg[2] = 'a';
        outMsg[3] = 'a';
        outMsg[4] = 'a';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }

    public void turnRight(View view)  {
        outMsg[0] = 'd';
        outMsg[1] = 'd';
        outMsg[2] = 'd';
        outMsg[3] = 'd';
        outMsg[4] = 'd';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }

    public void turnUp(View view) {
        outMsg[0] = 'w';
        outMsg[1] = 'w';
        outMsg[2] = 'w';
        outMsg[3] = 'w';
        outMsg[4] = 'w';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg);
    }


    public void turnDown(View view)  {
        outMsg[0] = 's';
        outMsg[1] = 's';
        outMsg[2] = 's';
        outMsg[3] = 's';
        outMsg[4] = 's';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }
}