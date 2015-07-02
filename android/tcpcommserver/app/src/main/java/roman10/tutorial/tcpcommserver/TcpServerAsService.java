package roman10.tutorial.tcpcommserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerAsService extends Service {
    public TcpServerAsService() {
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
//        return null;
//    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TcpServerService", "running...");
        runTcpServer();
        this.stopSelf();
    }


    Socket s=null;
    private BufferedReader in = null;
    private BufferedWriter out = null;
    public static final int TCP_SERVER_PORT = 21111;

    private void runTcpServer() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(TCP_SERVER_PORT);
            while (true) {
                //accept connections
                s = ss.accept();
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                Toast.makeText(getApplicationContext(),
                        "Connect to " + s.getLocalAddress().toString(), Toast.LENGTH_SHORT).show();

                //receive a message
                String incomingMsg = in.readLine();
                Log.i("TcpServerService", "received: " + incomingMsg);
                Toast.makeText(getApplicationContext(),
                        "received: " + incomingMsg, Toast.LENGTH_SHORT).show();

                //send a message
                String outgoingMsg =
                        "goodbye from port " + TCP_SERVER_PORT + System.getProperty("line.separator");
                out.write(outgoingMsg);
                out.flush();
                Log.i("TcpServerService", "sent: " + outgoingMsg);
                Toast.makeText(getApplicationContext(),
                        "sent: " + outgoingMsg, Toast.LENGTH_SHORT).show();
                s.close();
                Log.i("TcpServerService", "closed" + outgoingMsg);
                Toast.makeText(getApplicationContext(),
                        "Server closed", Toast.LENGTH_SHORT).show();
            }

            }catch(InterruptedIOException e){
                //if timeout occurs
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
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
