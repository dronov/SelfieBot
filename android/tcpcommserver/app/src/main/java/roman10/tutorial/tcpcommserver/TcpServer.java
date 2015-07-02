package roman10.tutorial.tcpcommserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
        finish();
    }
    private TextView textDisplay;
    private static final int TCP_SERVER_PORT = 21111;
    MyTask mt;
    ServerSocket ss = null;
    private void runTcpServer() {

    	try {
			ss = new ServerSocket(TCP_SERVER_PORT);
			//accept connections
			Socket s = ss.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			//receive a message
			String incomingMsg = in.readLine() + System.getProperty("line.separator");
			Log.i("TcpServer", "received: " + incomingMsg);
			//send a message
			String outgoingMsg = "goodbye from port " + TCP_SERVER_PORT + System.getProperty("line.separator");
			out.write(outgoingMsg);
			out.flush();
			Log.i("TcpServer", "sent: " + outgoingMsg);
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
	class MyTask extends AsyncTask<Void,Void,Void>{
		@Override
		protected Void doInBackground(Void... voids) {
            while(true){
			    runTcpServer();
            }
//			return null;
		}
	}
}