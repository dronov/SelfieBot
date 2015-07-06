package com.endurancerobots.headcontrolclient;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TcpClient extends Activity {
    public static final String FILE_NAME = "filename";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent intent = getIntent();
    }

    /// TODO: Сделать акитвити настроек
    public void connectClick(View view) {
        EditText editHeadIp = (EditText)findViewById(R.id.editHeadIp);
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        if(runTcpClient(editHeadIp.getText().toString(), TCP_SERVER_PORT))
        {
            view.setVisibility(View.INVISIBLE);
            editHeadIp.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }else{
            view.setVisibility(View.VISIBLE);
            editHeadIp.setVisibility(View.VISIBLE);
        }
    }
    private byte inMsg[] = new byte[5];
	private byte outMsg[] = new byte[5];
	private Socket mS = new Socket();

    private static final int TCP_SERVER_PORT = 1553;

	private boolean runTcpClient(String host, int port) {
//        for (int i=0; i<3 && (!mS.isConnected());i++) { // try to connect N times
            try {
                mS.connect(new InetSocketAddress(host, port));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
//        }
        if(mS.isConnected()) {
                Toast.makeText(getApplicationContext(),
                        "Successful connection", Toast.LENGTH_SHORT).show();
            Log.i("TcpClient", "Successful connection");
            return true;
        } else {
            Log.i("TcpClient", "Can't connect to server");
                Toast.makeText(getApplicationContext(),
                        "Can't connect to server", Toast.LENGTH_SHORT).show();
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
        stopService(new Intent(this,ControlOpenService.class));
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
        Log.i("TcpClient", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("TcpClient", "onStop");
//        startService(new Intent(this, ControlOpenService.class));
    }

    public void buttonLeftOnClick(View view)  {
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

    public void buttonRightOnClick(View view)  {

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

    public void buttonUpOnClick(View view) {
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

    public void buttonDownOnClick(View view)  {
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


    public void logoOnClick(View view) {
        if(view.getVisibility() == View.VISIBLE)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }
}