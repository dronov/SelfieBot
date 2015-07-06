package com.endurancerobots.headcontrolserver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;

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
	private TextView serverIp;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textDisplay = (TextView) this.findViewById(R.id.text1);
        textDisplay.setText("Server waiting for connections");

		serverIp = (TextView)findViewById(R.id.ipAddr);
		serverIp.setText("IP:" + getLocalIpAddress());

        mt = new MyTask();
        mt.execute();
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

	private TextView textDisplay;
    private static final int TCP_SERVER_PORT = 1553;
    MyTask mt;
    ServerSocket ss = null;
	Socket s = null;
	byte inMsg[] = new byte[5];
	byte outMsg[] = new byte[5];


	public void buttonBackgroundOnClick(View view) {
		finish();
	}

	class MyTask extends AsyncTask<Void, byte[], Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				ss = new ServerSocket(TCP_SERVER_PORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("TcpServer", "Server started.\nHost: " +
					ss.getLocalSocketAddress().toString());
			Toast.makeText(getApplicationContext(),
					"Server started.\nHost: "+
					ss.getLocalSocketAddress().toString(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				s = ss.accept();
				runTcpServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(byte[]... values) {
			super.onProgressUpdate(values);
			textDisplay.setText(values[0].toString());
			switch (values[0][0]) {
				case 119:
					textDisplay.setText("UP");
					Log.i("TcpServer.Control", "UP");
					break;
				case 97:
					textDisplay.setText("LEFT");
					Log.i("TcpServer.Control", "LEFT");
					break;
				case 115:
					textDisplay.setText("DOWN");
					Log.i("TcpServer.Control", "DOWN");
					break;
				case 100:
					textDisplay.setText("RIGHT");
					Log.i("TcpServer.Control", "RIGHT");
					break;
			}
			Toast.makeText(getApplicationContext(),
					"Command:"+values[0][0],
					Toast.LENGTH_SHORT).show();
			Amarino.sendDataToArduino(getApplicationContext(), getMac(), 'A', values[0]);
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			try {
				s.close();
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("TcpServer", "Server closed");
			Toast.makeText(getApplicationContext(),
					"Server closed on port: " + TCP_SERVER_PORT, Toast.LENGTH_SHORT).show();
		}
		private void runTcpServer() {
			try {
				while (true){
					s.getInputStream().read(inMsg);
					Log.i("TcpServer", "received: " + inMsg[0]);
//				outMsg[0] = 's';
//				s.getOutputStream().write(outMsg);
//				Log.i("TcpServer", "sent: " + outMsg[0]);
//				if(outMsg[0] == -1) break;
					publishProgress(inMsg);

				}
			} catch (InterruptedIOException e) {
				//if timeout occurs
				e.printStackTrace();
			} catch (SocketException se){
				se.printStackTrace();
				Log.i("TcpServer", "client cuts wire!!! ");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getMac() {
		EditText editText = (EditText)findViewById(R.id.editArduinoMac);
		return editText.getText().toString();
	}
}