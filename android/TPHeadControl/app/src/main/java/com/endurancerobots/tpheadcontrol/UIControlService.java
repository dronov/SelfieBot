package com.endurancerobots.tpheadcontrol;


import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.PixelFormat;

import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;

public class UIControlService extends Service {
    private WindowManager.LayoutParams layoutParams;
    private WindowManager.LayoutParams pausedLayoutParams;
    private WindowManager _winMgr;
    private View myView;
    private Button bUp, bDown, bLeft, bRight;
    private Button bClose;
    private Button bPause;
    private LayoutInflater layoutInflater;
    private final int controlHeight = 450; // in dp
    private final int controlWidth = 450; // in dp
    private String ip = "localhost";
    private int port = 4445;
    private TcpProxyClient mS;
    private byte outMsg[] = new byte[5];

    private float _xDelta;
    private float _yDelta;

    private static final String ACTION_START_CONTROLS = "com.endurancerobots.tpheadcontrol.action.START_CONTROLS";

    private static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    private static final String TAG = "UIControlService";
    private String headId = "987654321";
    public boolean connected=false;
    private SocketThread sth;

    //
//    public static void startUIControls(Context context, String headId_) {
//        Log.v(TAG, "startUIControls");
//        Intent intent = new Intent(context, UIControlService.class);
//        intent.setAction(ACTION_START_CONTROLS);
//        intent.putExtra(EXTRA_HEAD_ID, headId_);
//        context.startService(intent);
//    }
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
//        headId = intent.getStringExtra(EXTRA_HEAD_ID);
    }
    public UIControlService() {

    }

    class SocketThread extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            return mS.connectAsClient(headId);
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);
            Log.d(TAG, "onPostExecute");
            Log.d(TAG, "connected="+connected);
            if (!connected) {
                Log.e(TAG, "Client was not connected!");
                Toast.makeText(getApplicationContext(), "Client was not connected!", Toast.LENGTH_LONG).show();
                stopSelf();
            } else {
                Log.d(TAG, "CONNECT");
                Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_LONG).show();
                setupLayout();
                setupClicks();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        Log.i(TAG, "UI Control got Head Id:" + headId);

        mS = new TcpProxyClient(); // Пытаемся подключиться
        sth = new SocketThread();

        sth.execute();
    }

    private void setupLayout() {
        /*******Setup layout**************************/
        final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams = new WindowManager.LayoutParams(
                controlWidth,
                controlHeight,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                LayoutParamFlags,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.NO_GRAVITY;
        layoutParams.verticalMargin = (float) 0.5;
        layoutParams.horizontalMargin = (float) 0.5;

        pausedLayoutParams = new WindowManager.LayoutParams();
        pausedLayoutParams.copyFrom(layoutParams);
        pausedLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        pausedLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;

        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        myView = layoutInflater.inflate(R.layout.keys, null);

        _winMgr = (WindowManager) getSystemService(WINDOW_SERVICE);
        _winMgr.addView(myView, layoutParams);
    }

    private void setupClicks() {

        /*******Setup clicks**************************/
        bUp = (Button) myView.findViewById(R.id.bUp);
        bDown = (Button) myView.findViewById(R.id.bDown);
        bLeft = (Button) myView.findViewById(R.id.bLeft);
        bRight = (Button) myView.findViewById(R.id.bRight);
        bClose = (Button) myView.findViewById(R.id.bClose);
        bPause = (Button) myView.findViewById(R.id.bPause);
        Log.d(TAG, "layoutInflater");

        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float k = (float) 2.5;
                final float X = event.getRawX()/layoutParams.width/k;
                final float Y = event.getRawY()/layoutParams.height/k;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        _xDelta = X - (layoutParams.horizontalMargin);
                        _yDelta = Y - (layoutParams.verticalMargin);
                        Log.d(TAG,"ACTION_DOWN X="+X+" Y="+Y+" _xDelta="+_xDelta+" _yDelta="+_yDelta+
                                " lP("+layoutParams.horizontalMargin+", "+layoutParams.verticalMargin+")");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG,"ACTION_UP X="+X+" Y="+Y+" _xDelta="+_xDelta+" _yDelta="+_yDelta+
                                " lP("+layoutParams.horizontalMargin+", "+layoutParams.verticalMargin+")");
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.horizontalMargin = (X - _xDelta);
                        layoutParams.verticalMargin = (Y - _yDelta);
                        break;
                }
                _winMgr.updateViewLayout(myView, layoutParams);
                return true;
            }
        });
        myView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i(TAG, "Longpress detected");
                return false;
            }
        });

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
                quit();
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
                    _winMgr.updateViewLayout(myView, pausedLayoutParams);
                } else {
                    fl.setVisibility(View.VISIBLE);
                    _winMgr.updateViewLayout(myView, layoutParams);
                }
            }
        });
    }

    private void setInfo(String info) {
        Log.i(TAG, info);
        TextView tvCmdDebug = (TextView) myView.findViewById(R.id.cmdDebug);
        tvCmdDebug.setText(info);
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
        if (myView != null){
            if (_winMgr != null){
                _winMgr.removeView(myView);
                myView = null;
            }
        }
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
        Toast.makeText(getApplicationContext(),
                getString(R.string.connection_closed),
                Toast.LENGTH_LONG).show();
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
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

