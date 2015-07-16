package com.endurancerobots.tpheadcontrol;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.Arrays;
import at.abraxas.amarino.Amarino;

public class ServoControlService extends IntentService {
    private static final String START_SERVO_CONTROL = "com.endurancerobots.tpheadcontrol.action.START_SERVO_CONTROL";

    private static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    private static final String EXTRA_MAC = "com.endurancerobots.tpheadcontrol.extra.MAC";
    private static final String TAG = "ServoControlService";
    private String headId = "123456789";
    private String macAddr;
    private TcpProxyClient serv;
    private byte[] inMsg=new byte[5];

    public static void startServoControl(Context context, String headId_, String mac_) {
        Log.v(TAG, "startServoControl");
        Intent intent = new Intent(context, ServoControlService.class);
        intent.setAction(START_SERVO_CONTROL);
        intent.putExtra(EXTRA_HEAD_ID, headId_);
        intent.putExtra(EXTRA_MAC, mac_);
        context.startService(intent);
    }

    public ServoControlService() {
        super("UIControlService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (START_SERVO_CONTROL.equals(action)) {
                final String headId_ = intent.getStringExtra(EXTRA_HEAD_ID);
                final String mac_ = intent.getStringExtra(EXTRA_MAC);
                handleActionStartServoControl(headId_,mac_);
            }
        }
    }

    private void handleActionStartServoControl(String headId_, String mac) {
        Log.v(TAG, "handleActionStartServoControl");
        Log.i(TAG, "headId_=" + headId_);

        Log.i(TAG,headId_+" "+mac);
        headId=headId_;
        macAddr = mac;
        serv = new TcpProxyClient();
        String answ="";
        while(!answ.contains("CLOSE"))
            if(serv.connectAsServer(headId))
                answ=runTcpServer();
        try {
            if(serv != null)
                serv.close();
        } catch (IOException e) {
            Log.e(TAG, "error while closing server: "+e.getMessage());
        }
        Log.i(TAG, "Server closed");
    }

    private String runTcpServer() {
        Log.v(TAG, "runTcpServer");
        Log.d(TAG, "runTcpServer");
        try {
            while (true){
                int read = serv.getInputStream().read(inMsg);
                Log.d(TAG, "received msg: " + Arrays.toString(inMsg) +"read: "+read);

                switch (inMsg[0]) {
                    case 119:
                        Amarino.sendDataToArduino(getApplicationContext(), getMac(), 'A', 'w');
                        publishProgress("Command: UP (" + inMsg[0] + ")");
                        break;
                    case 97:
                        Amarino.sendDataToArduino(getApplicationContext(), getMac(), 'A', 'a');
                        publishProgress("Command: LEFT (" + inMsg[0] + ")");
                        break;
                    case 115:
                        Amarino.sendDataToArduino(getApplicationContext(), getMac(), 'A', 's');
                        publishProgress("Command: DOWN (" + inMsg[0] + ")");
                        break;
                    case 100:
                        Amarino.sendDataToArduino(getApplicationContext(), getMac(), 'A', 'd');
                        publishProgress("Command: RIGHT (" + inMsg[0] + ")");
                        break;
                    case 113:
                        Amarino.disconnect(getApplicationContext(), getMac());
                        publishProgress("Command: CLOSE CONNECTION (" + inMsg[0] + ")");
                        return "CLOSE";
                    default:
                        publishProgress("Unknown command: ("+inMsg[0]+")");
                }

                //TODO: Передавать байтовые массивы "не вскрывая"
            }
        } catch (InterruptedIOException e) {
            //if timeout occurs
            Log.e(TAG, "timeout occurs " + e.getMessage() + "!!!");
        } catch (SocketException se){
            if(se.getMessage().contains("ECONNRESET")){
                Log.e(TAG, "client cuts wire!!! " + se.getMessage() + "!!!");
            } else{
                Log.e(TAG, "problem woth socket"+se.getMessage());}
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage() + "!!!");
        }
        return "PROBLEM";
    }

    private void publishProgress(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
        Log.i(TAG,s);
    }

    public String getMac() {
        return macAddr;
    }
}
