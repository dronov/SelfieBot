package roman10.tutorial.tcpcommclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class TcpClient extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MyTask mt = new MyTask();
		mt.execute();
        finish();
    }
    private byte inMsg[] = new byte[5];
	private byte outMsg[] = new byte[5];
	private Socket mS = new Socket();

    private static final int TCP_SERVER_PORT = 1553;
	private void runTcpClient() {
    	try {
            for(byte i=0; i<3;i++) {
                //send output msg
                outMsg[0] = i;
                mS.getOutputStream().write(outMsg);
                Log.i("TcpClient", "sent: " + outMsg[0]);

                //accept server response
                final int read = mS.getInputStream().read(inMsg);
                Log.i("TcpClient", "received: " + inMsg[0]);
            }
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
	//replace runTcpClient() at onCreate with this method if you want to run tcp client as a service
	private void runTcpClientAsService() {
		Intent lIntent = new Intent(this.getApplicationContext(), TcpClientService.class);
        this.startService(lIntent);
	}

    class MyTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
            InetSocketAddress adr = new InetSocketAddress("localhost",TCP_SERVER_PORT);
            try {
                mS.connect(adr);
            } catch (IOException e) {
                e.printStackTrace();
            }

		}

		@Override
		protected Void doInBackground(Void... voids) {
            if(mS.isConnected()) {
//                Toast.makeText(getApplicationContext(),
//                        "Successful connection", Toast.LENGTH_SHORT).show();
                Log.i("TcpClient", "Successful connection");
                runTcpClient();
            }
            else
            {
                Log.i("TcpClient", "Can't connect to server");
//                Toast.makeText(getApplicationContext(),
//                        "Can't connect to server", Toast.LENGTH_SHORT).show();
            }
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
            //close connection
            try {
                mS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
		}
	}
}