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
	public String proxyIp="46.38.49.133";
	public int proxyServerPort=4445;
	//proxy protocol
	public String CONNECT="\r\nCONNECT\r\n";
	public String WAIT="\r\nWAIT\r\n";
	public String ERROR="\r\nERROR\r\n";


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

	private TextView textDisplay;
    private static final int TCP_SERVER_PORT = 4445;
    MyTask mt;
    ServerSocket ss = null;
	public Socket serv = new Socket();
	byte inMsg[] = new byte[5];
	byte outMsg[] = new byte[5];


	public void buttonBackgroundOnClick(View view) {
		finish();
	}

	public void connectViaProxy(View view) {
		mt = new MyTask(false);
		mt.execute();
	}

	public void createLocalServer(View view) {
		serverIp = (TextView)findViewById(R.id.ipAddr);
		serverIp.setText("IP:" + getLocalIpAddress());

		mt = new MyTask(true);
		mt.execute();
	}

	class MyTask extends AsyncTask<Void, byte[], Void> {
		private boolean isLocal=true;

		public MyTask(boolean local){
			isLocal=local;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(isLocal)
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
		protected Void doInBackground(Void... Voids) {
			if(isLocal) {
				try {
					serv = ss.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
				runTcpServer();
			}
			else
				if(connectToOperator())
					runTcpServer();
			return null;
		}

		private boolean connectToOperator() {
			String s="";
			try {
				serv.connect(new InetSocketAddress(proxyIp, proxyServerPort));
			}catch (IOException e){
				e.printStackTrace();
			}
			if (serv.isConnected()) {
				while (!s.contains(CONNECT))
					try {
						String strId = "S123456789\r";
						/// Send id-string
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(serv.getOutputStream()));
						out.write(strId);
						out.flush();
						Log.i("TcpClient.proxy", "Send id-string '" + strId + "'");
						/// Receive answer
						BufferedReader in = new BufferedReader(new InputStreamReader(serv.getInputStream()));
						byte[] inputBuf=new byte[50];
						serv.getInputStream().read(inputBuf);
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
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return false;
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
				serv.close();
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
					serv.getInputStream().read(inMsg);
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