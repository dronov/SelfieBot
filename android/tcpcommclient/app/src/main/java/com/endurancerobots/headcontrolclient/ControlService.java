package com.endurancerobots.headcontrolclient;

import android.app.Service;
import android.content.Intent;

import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;

public class ControlService extends Service {
    private static final String TAG = "ControlService";
    private WindowManager.LayoutParams layoutParams;
    private WindowManager _winMgr;
    private View myView;
    private Button bUp,bDown,bLeft,bRight;
    private Button bClose;
    private Button bPause;
    private LayoutInflater layoutInflater;
    private final int controlHeight = 300; // in dp
    private final int controlWidth = 300; // in dp
    private String ip="localhost";
    private int port=4445;
    private TcpProxyClient mS;
    private byte outMsg[] = new byte[5];

    public ControlService() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent serverParamIntent, int startId){
        Log.d(TAG, "ControlService started");
        super.onStart(serverParamIntent, startId);
//        ip = serverParamIntent.getStringExtra("com.endurancerobots.headcontrolclient.serverIp");
//        port = serverParamIntent.getIntExtra("com.endurancerobots.headcontrolclient.serverPort",4445);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate ENTER");
        super.onCreate();

        mS = new TcpProxyClient(); // Пытаемся подключиться
        boolean connected = mS.runTcpProxyClient(TcpProxyClient.PROXY_IP, TcpProxyClient.TCP_PROXY_SERVER_PORT);
        if(!connected){
            /************************************************************************/

            Log.e(TAG, "runTcpProxyClient was not connected!");
            Toast.makeText(getApplicationContext(),"Client was not connected!",Toast.LENGTH_LONG).show();
            stopSelf();
        }
        else {
            /************************************************************************/
            Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_LONG).show();

            final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.FILL_PARENT,
                    controlHeight,
                    WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                    LayoutParamFlags,
                    PixelFormat.TRANSLUCENT);
            Log.d(TAG, "layoutParams");

            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            Log.d(TAG, "layoutInflater");

            _winMgr = (WindowManager) getSystemService(WINDOW_SERVICE);

            myView = layoutInflater.inflate(R.layout.keys, null);
            Log.d(TAG, "layoutInflater");

            bUp = (Button) myView.findViewById(R.id.bUp);
            bDown = (Button) myView.findViewById(R.id.bDown);
            bLeft = (Button) myView.findViewById(R.id.bLeft);
            bRight = (Button) myView.findViewById(R.id.bRight);
            bClose = (Button) myView.findViewById(R.id.bClose);
            bPause = (Button) myView.findViewById(R.id.bPause);
            Log.d(TAG, "layoutInflater");

            bUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setInfo("Up");
                    turnUp();
                }
            });
            bDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setInfo("Down");
                    turnDown();
                }
            });
            bLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setInfo("Left");
                    turnLeft();
                }
            });
            bRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setInfo("Right");
                    turnRight();
                }
            });
            bClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setInfo("Closing...");
                    stopSelf();
                }
            });
            bPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, getString(R.string.pausing));
                    FrameLayout fl = (FrameLayout) myView.findViewById(R.id.controlButtons);
                    if (View.VISIBLE == fl.getVisibility()) {
                        fl.setVisibility(View.GONE);
                        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    } else {
                        fl.setVisibility(View.VISIBLE);
                        layoutParams.height = controlHeight;
                        layoutParams.width = controlWidth;
                    }
                    _winMgr.updateViewLayout(myView, layoutParams);
                }
            });
            Log.d(TAG, "setOnTouchListener");

            _winMgr.addView(myView, layoutParams);
            Log.d(TAG, "_winMgr.addView");
        }
    }

    private void setInfo(String info) {
        Log.i(TAG,info);
        TextView tvCmdDebug = (TextView)myView.findViewById(R.id.cmdDebug);
        tvCmdDebug.setText(info);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void onDestroy(){
        quit();
        if (myView != null){
            if (_winMgr != null){
                _winMgr.removeView(myView);
                myView = null;
            }
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
    public void turnLeft()  {
        outMsg[0] = 'a';
        outMsg[1] = 'a';
        outMsg[2] = 'a';
        outMsg[3] = 'a';
        outMsg[4] = 'a';
        writeCmd(outMsg);
    }

    public void turnRight()  {
        outMsg[0] = 'd';
        outMsg[1] = 'd';
        outMsg[2] = 'd';
        outMsg[3] = 'd';
        outMsg[4] = 'd';
        writeCmd(outMsg);
    }

    public void turnUp() {
        outMsg[0] = 'w';
        outMsg[1] = 'w';
        outMsg[2] = 'w';
        outMsg[3] = 'w';
        outMsg[4] = 'w';
        writeCmd(outMsg);
    }


    public void turnDown()  {
        outMsg[0] = 's';
        outMsg[1] = 's';
        outMsg[2] = 's';
        outMsg[3] = 's';
        outMsg[4] = 's';
        writeCmd(outMsg);
    }
    public void quit()  {
        outMsg[0] = 'q';
        outMsg[1] = 'q';
        outMsg[2] = 'q';
        outMsg[3] = 'q';
        outMsg[4] = 'q';
        writeCmd(outMsg);
        try {
            mS.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem with closing socket" + e.getMessage());
        }
        Log.i(TAG, "Connection closed");
        Toast.makeText(getApplicationContext(),"Connection closed",Toast.LENGTH_LONG).show();
    }

    private void writeCmd(byte[] cmd) {
        try{
            mS.getOutputStream().write(cmd);
        } catch (SocketException e){
            Log.e(TAG, "Can't write cmd. Problem with socket: " + e.getMessage());
            stopSelf();
        } catch (IOException e) {
            Log.e(TAG, "Can't write cmd." + e.getMessage());
            stopSelf();
        }
        Log.d(TAG, "sent: " + Arrays.toString(cmd));
    }
}
