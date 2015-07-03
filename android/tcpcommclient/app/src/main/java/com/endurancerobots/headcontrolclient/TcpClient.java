package com.endurancerobots.headcontrolclient;


import java.io.IOException;
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
import android.widget.TextView;
import android.widget.Toast;

public class TcpClient extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    public void connectClick(View view) {
        if(mS.isConnected())
        {
            try {
                mS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
        }else
        {
            runTcpClient();
        }
    }
    private byte inMsg[] = new byte[5];
	private byte outMsg[] = new byte[5];
	private Socket mS = new Socket();

    private static final int TCP_SERVER_PORT = 1553;
	private void runTcpClient() {
        EditText editHeadIp = (EditText)findViewById(R.id.editHeadIp);
        try {
            mS.connect(new InetSocketAddress(editHeadIp.getText().toString(),TCP_SERVER_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mS.isConnected()) {
                Toast.makeText(getApplicationContext(),
                        "Successful connection", Toast.LENGTH_SHORT).show();
            Log.i("TcpClient", "Successful connection");
        }
        else
        {
            Log.i("TcpClient", "Can't connect to server");
                Toast.makeText(getApplicationContext(),
                        "Can't connect to server", Toast.LENGTH_SHORT).show();
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


}