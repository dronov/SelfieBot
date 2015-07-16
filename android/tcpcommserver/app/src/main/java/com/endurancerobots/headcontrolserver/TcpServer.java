package com.endurancerobots.headcontrolserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import at.abraxas.amarino.Amarino;


public class TcpServer extends Activity {
	private static final String TAG = "TcpServer";
	private TextView serverIp;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textDisplay = (TextView) this.findViewById(R.id.text1);
        textDisplay.setText("Server waiting for connections");
    }

	public String getLocalIpAddress()
	{
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception ex) {
			Log.e("IP Address", ex.toString());
		}
		return null;
	}
	private String getWifiIp(){
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
	}

	public TextView textDisplay;


	public void buttonBackgroundOnClick(View view) {
		finish();
	}

	public void connectViaProxy(View view) {
		Log.i(TAG, "connectViaProxy");
		/**
		 * Start server
		 */
		TcpProxyService.startActionFoo(getApplicationContext(), "987654321", getMacAddr());
	}

	public void createLocalServer(View view) {
		serverIp = (TextView)findViewById(R.id.ipAddr);
		serverIp.setText("Head IP: " + getLocalIpAddress());
		Log.i(TAG, "createLocalServer");
	}
	private String getMacAddr() {
		EditText editText = (EditText)findViewById(R.id.editArduinoMac);
		return editText.getText().toString();
	}

	public void exit(View view) {
		stopService(new Intent(getApplicationContext(), TcpProxyService.class));
		finish();
	}
}