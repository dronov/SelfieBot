package roman10.tutorial.tcpcommserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TcpServer extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textDisplay = (TextView) this.findViewById(R.id.text1);

        textDisplay.setText("Server waiting for connection");
		//ss.setSoTimeout(10000);
		//accept connections
        runTcpServerAsService();
		finish();
    }
    private TextView textDisplay;
	private void runTcpServerAsService() {
		Intent lIntent = new Intent(this.getApplicationContext(), TcpServerAsService.class);
		this.startService(lIntent);
	}
    private void runTcpServer() {
    	ServerSocket ss = null;
    	try {
			ss = new ServerSocket(TcpServerAsService.TCP_SERVER_PORT);
			//ss.setSoTimeout(10000);
			//accept connections
			Socket s = ss.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			//receive a message
			String incomingMsg = in.readLine();
			Log.i("TcpServer", "received: " + incomingMsg);
			textDisplay.append("received: " + incomingMsg);
			//send a message
			String outgoingMsg =
					"goodbye from port " +
							TcpServerAsService.TCP_SERVER_PORT +
							System.getProperty("line.separator");
			out.write(outgoingMsg);
			out.flush();
			Log.i("TcpServer", "sent: " + outgoingMsg);
			textDisplay.append("sent: " + outgoingMsg);
			//SystemClock.sleep(5000);
			s.close();
		} catch (InterruptedIOException e) {
			//if timeout occurs
			e.printStackTrace();
    	} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}