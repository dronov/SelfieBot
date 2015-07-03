package com.endurancerobots.headcontrolserver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TcpServer extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textDisplay = (TextView) this.findViewById(R.id.text1);
        textDisplay.setText("Server waiting for connections");
        mt = new MyTask();
        mt.execute();
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
			Log.i("TcpServer", "Server started");
			Toast.makeText(getApplicationContext(),
					"Server started on port: " + TCP_SERVER_PORT, Toast.LENGTH_SHORT).show();
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
			textDisplay.setText(values.toString());
			switch (values.toString().charAt(0)){
				case 'w':
					textDisplay.setText("UP");
					break;
				case 'a':
					textDisplay.setText("LEFT");
					break;
				case 's':
					textDisplay.setText("DOWN");
					break;
				case 'd':
					textDisplay.setText("RIGHT");
					break;
			}
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
}