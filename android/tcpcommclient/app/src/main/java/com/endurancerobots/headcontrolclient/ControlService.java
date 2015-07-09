package com.endurancerobots.headcontrolclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;

public class ControlService extends Service {
    private static final String TAG = "ControlService";
    private WindowManager.LayoutParams layoutParams;
    private WindowManager _winMgr;
    private View myView;
    private Button bUp,bDown,bLeft,bRight;
    private LayoutInflater layoutInflater;
    private final int controlHeight = 300; // in dp
    private String ip="localhost";
    private int port=4445;
    private TcpProxyClient mS;
    private byte outMsg[] = new byte[5];

    public ControlService() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent serverParamIntent, int startId){
        Log.d(TAG, "onStart *** ENTER ***");
        super.onStart(serverParamIntent, startId);
        Log.d(TAG, "onStart *** LEAVE ***");
//        ip = serverParamIntent.getStringExtra("com.endurancerobots.headcontrolclient.serverIp");
//        port = serverParamIntent.getIntExtra("com.endurancerobots.headcontrolclient.serverPort",4445);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate ENTER");
        super.onCreate();

        mS = new TcpProxyClient();
        boolean connected = mS.runTcpProxyClient(TcpProxyClient.PROXY_IP, TcpProxyClient.TCP_PROXY_SERVER_PORT);
        if(!connected){
            Log.i(TAG, "runTcpProxyClient was not connected!");
            Toast.makeText(getApplicationContext(),"Client was not connected!",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(getApplicationContext(),TcpClient.class));
            stopSelf();
        }else {
            Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_LONG).show();

            final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
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
            Button bClose = (Button) myView.findViewById(R.id.bClose);
            Button bPause = (Button) myView.findViewById(R.id.bPause);
            Log.d(TAG, "layoutInflater");

            bUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Up");
                    turnUp();
                }
            });
            bDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Down");
                    turnDown();
                }
            });
            bLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Left");
                    turnLeft();
                }
            });
            bRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Right");
                    turnRight();
                }
            });
            bClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Closing...");
                    stopSelf();
                }
            });
            bPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Pausing...");
                    FrameLayout fl = (FrameLayout) myView.findViewById(R.id.controlButtons);
                    if (View.VISIBLE == fl.getVisibility()) {
                        fl.setVisibility(View.GONE);
                        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    } else {
                        fl.setVisibility(View.VISIBLE);
                        layoutParams.height = controlHeight;
                        layoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
                    }
                    _winMgr.updateViewLayout(myView, layoutParams);
                }
            });

            Log.d(TAG, "setOnTouchListener");

            _winMgr.addView(myView, layoutParams);
            Log.d(TAG, "_winMgr.addView");
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void onDestroy(){
        Log.d(TAG, "onDestroy *** ENTER ***");
        quit();
        if (myView != null){
            if (_winMgr != null){
                _winMgr.removeView(myView);
                myView = null;
            }
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy *** LEAVE ***");
    }
    public void turnLeft()  {
        outMsg[0] = 'a';
        outMsg[1] = 'a';
        outMsg[2] = 'a';
        outMsg[3] = 'a';
        outMsg[4] = 'a';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }

    public void turnRight()  {
        outMsg[0] = 'd';
        outMsg[1] = 'd';
        outMsg[2] = 'd';
        outMsg[3] = 'd';
        outMsg[4] = 'd';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }

    public void turnUp() {
        outMsg[0] = 'w';
        outMsg[1] = 'w';
        outMsg[2] = 'w';
        outMsg[3] = 'w';
        outMsg[4] = 'w';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg);
    }


    public void turnDown()  {
        outMsg[0] = 's';
        outMsg[1] = 's';
        outMsg[2] = 's';
        outMsg[3] = 's';
        outMsg[4] = 's';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }
    public void quit()  {
        outMsg[0] = 'q';
        outMsg[1] = 'q';
        outMsg[2] = 'q';
        outMsg[3] = 'q';
        outMsg[4] = 'q';
        try {
            mS.getOutputStream().write(outMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),"Connection closed",Toast.LENGTH_LONG).show();
        Log.i("TcpClient", "sent: " + outMsg.toString());
    }
}
