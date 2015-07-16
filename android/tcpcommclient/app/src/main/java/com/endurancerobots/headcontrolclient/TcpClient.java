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
    private TcpProxyClient mS;

    private static final int TCP_SERVER_PORT = 4445;

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
    public void connectProxyClick(View view) {
        hideUI();
    }


    private void hideUI() {
        /** Make UI transparent to see the Skype, Linphone etc...*/
        finish();
        startService(_serviceIntent);
    }

	//replace runTcpClient() at onCreate with this method if you want to run tcp client as a service
	private void runTcpClientAsService() {
		Intent lIntent = new Intent(this.getApplicationContext(), TcpClientService.class);
        this.startService(lIntent);
	}

    @Override
    protected void onStart() {
        super.onStart();
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