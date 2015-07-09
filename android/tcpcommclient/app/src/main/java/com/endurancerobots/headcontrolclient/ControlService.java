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
        Log.d(TAG,"layoutParams");

        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        Log.d(TAG,"layoutInflater");

        _winMgr = (WindowManager)getSystemService(WINDOW_SERVICE);

        myView = layoutInflater.inflate(R.layout.keys,null);
        Log.d(TAG,"layoutInflater");

        bUp = (Button) myView.findViewById(R.id.bUp);
        bDown = (Button) myView.findViewById(R.id.bDown);
        bLeft = (Button) myView.findViewById(R.id.bLeft);
        bRight = (Button) myView.findViewById(R.id.bRight);
        Button bClose = (Button) myView.findViewById(R.id.bClose);
        Button bPause = (Button) myView.findViewById(R.id.bPause);
        Log.d(TAG, "layoutInflater");

        bUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "Up");
                return false;
            }
        });
        bDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "Down");
                return false;
            }
        });
        bLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "Left");
                return false;
            }
        });
        bRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "Right");
                return false;
            }
        });
        bClose.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "Closing...");
                stopSelf();
                return false;
            }
        });
        bPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Pausing...");
                FrameLayout fl = (FrameLayout) myView.findViewById(R.id.controlButtons);
                if(View.VISIBLE == fl.getVisibility()){
                    fl.setVisibility(View.GONE);
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                }else
                {
                    fl.setVisibility(View.VISIBLE);
                    layoutParams.height = controlHeight;
                    layoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
                }
                _winMgr.updateViewLayout(myView,layoutParams);
            }
        });

        Log.d(TAG, "setOnTouchListener");

        _winMgr.addView(myView, layoutParams);
        Log.d(TAG, "_winMgr.addView");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public void onDestroy(){
        Log.d(TAG, "onDestroy *** ENTER ***");
        super.onDestroy();
        if (myView != null){
            if (_winMgr != null){
                _winMgr.removeView(myView);
                myView = null;
            }
        }
        Log.d(TAG, "onDestroy *** LEAVE ***");
    }
}
