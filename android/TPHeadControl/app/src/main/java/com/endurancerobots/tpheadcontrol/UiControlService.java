package com.endurancerobots.tpheadcontrol;


import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
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

public class UiControlService extends Service {

    public static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    public static final String ACTION_START_CONTROLS = "com.endurancerobots.tpheadcontrol.extra.START_CONTROLS";


    private static Handler sHandler;

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager.LayoutParams mPausedLayoutParams;
    private WindowManager mWinMgr;
    private View mMyView;
    private TcpProxyClient mS;
    private byte mOutMsg[] = new byte[5];
    private float mXDelta;

    private float mYDelta;
    private String mHeadId = "987654321";

    private TcpDataTransferThread tcpDataTransferThread;

    private static final String TAG = "UiControlService";
    private SocketThread socketThread;

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG,"onStart");
        if(intent!=null) {
            String action = intent.getAction();
            if((ACTION_START_CONTROLS).equals(action))
            mHeadId = intent.getStringExtra(EXTRA_HEAD_ID);
            Log.i(TAG,"mHeadId :"+mHeadId);
        }
        Log.d(TAG, "Service Created");
        sHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case TcpDataTransferThread.MESSAGE_READ:
                        Log.i(TAG, "sHandler got message: " + msg.arg1 + " "
                                + msg.arg2 + " " + Arrays.toString(((byte[]) msg.obj)));
                        setInfo(ComandDecoder.decode((byte[]) msg.obj));
                        break;
                    default:
                        Log.w(TAG,"sHandler got Unknown message "+msg.arg1+" "+msg.arg2+" "+msg.obj);
                }
            }
        };
        Log.d(TAG, "onCreate");

        Log.i(TAG, "UI Control got Head Id:" + mHeadId);

        mS = new TcpProxyClient(mHeadId); // Пытаемся подключиться
        socketThread = new SocketThread();
        socketThread.execute();
    }

    class SocketThread extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            return TcpProxyClient.CONNECT.equals(mS.connectAsClient()); // TODO: 05.08.15 исправить сравнение
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);
            Log.d(TAG, "onPostExecute");
            Log.d(TAG, "connected="+connected);
            if (!connected) {
                Log.e(TAG, "Client was not connected!");
                Toast.makeText(getApplicationContext(),
                        getString(R.string.unsuccessful_connection), Toast.LENGTH_LONG).show();
                stopSelf();
            } else {
                Log.d(TAG, "CONNECT");
                Toast.makeText(getApplicationContext(),
                        getString(R.string.successful_connection), Toast.LENGTH_LONG).show();
                tcpDataTransferThread = new TcpDataTransferThread(mS);
                tcpDataTransferThread.setOutHandler(sHandler);
                tcpDataTransferThread.start();
                setupLayout();
                setupClicks();
            }
        }
    }

    private void setupLayout() {
        /*******Setup layout**************************/
        final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        final int controlHeight = 450;
        final int controlWidth = 450;
        mLayoutParams = new WindowManager.LayoutParams(
                controlWidth,
                controlHeight,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                LayoutParamFlags,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.NO_GRAVITY;
        mLayoutParams.verticalMargin = (float) 0.5;
        mLayoutParams.horizontalMargin = (float) 0.5;

        mPausedLayoutParams = new WindowManager.LayoutParams();
        mPausedLayoutParams.copyFrom(mLayoutParams);
        mPausedLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mPausedLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mMyView = layoutInflater.inflate(R.layout.keys, null);

        mWinMgr = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWinMgr.addView(mMyView, mLayoutParams);
    }

    private void setupClicks() {

        /*******Setup clicks**************************/
        Button bUp = (Button) mMyView.findViewById(R.id.bUp);
        Button bDown = (Button) mMyView.findViewById(R.id.bDown);
        Button bLeft = (Button) mMyView.findViewById(R.id.bLeft);
        Button bRight = (Button) mMyView.findViewById(R.id.bRight);
        Button bClose = (Button) mMyView.findViewById(R.id.bClose);
        Button bPause = (Button) mMyView.findViewById(R.id.bPause);
        Log.v(TAG, "layoutInflater");

        mMyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float k = (float) 2.5;
                final float X = event.getRawX() / mLayoutParams.width / k;
                final float Y = event.getRawY() / mLayoutParams.height / k;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mXDelta = X - (mLayoutParams.horizontalMargin);
                        mYDelta = Y - (mLayoutParams.verticalMargin);
                        Log.d(TAG, "ACTION_DOWN X=" + X + " Y=" + Y + " mXDelta=" + mXDelta + " mYDelta=" + mYDelta +
                                " lP(" + mLayoutParams.horizontalMargin + ", " + mLayoutParams.verticalMargin + ")");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP X=" + X + " Y=" + Y + " mXDelta=" + mXDelta + " mYDelta=" + mYDelta +
                                " lP(" + mLayoutParams.horizontalMargin + ", " + mLayoutParams.verticalMargin + ")");
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.v(TAG, "ACTION_POINTER_DOWN");
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.v(TAG, "ACTION_POINTER_UP");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "ACTION_MOVE");
                        mLayoutParams.horizontalMargin = (X - mXDelta);
                        mLayoutParams.verticalMargin = (Y - mYDelta);
                        break;
                }
                mWinMgr.updateViewLayout(mMyView, mLayoutParams);
                return true;
            }
        });

        bUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setInfo("Up");
                turnUp();
            }
        });
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        v.setVisibility(View.INVISIBLE);
                        switch (v.getId()){
                            case R.id.bUp: turnUp(); break;
                            case R.id.bDown: turnDown(); break;
                            case R.id.bLeft: turnLeft(); break;
                            case R.id.bRight: turnRight(); break;
                            default:
                                break;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setVisibility(View.VISIBLE);
                        switch (v.getId()){
                            case R.id.bUp: turnUp(); break;
                            case R.id.bDown: turnDown(); break;
                            case R.id.bLeft: turnLeft(); break;
                            case R.id.bRight: turnRight(); break;
                            default:
                                break;
                        }
                        break;
                }
                return true;
            }
        };
        bUp.setOnTouchListener(touchListener);
        bDown.setOnTouchListener(touchListener);
        bLeft.setOnTouchListener(touchListener);
        bRight.setOnTouchListener(touchListener);
        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setInfo("Closing...");
                quit();
                stopSelf();
            }
        });
        bPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, getString(R.string.pausing));
                FrameLayout fl = (FrameLayout) mMyView.findViewById(R.id.controlButtons);
                if (View.VISIBLE == fl.getVisibility()) {
                    fl.setVisibility(View.GONE);
                    mWinMgr.updateViewLayout(mMyView, mPausedLayoutParams);
                } else {
                    fl.setVisibility(View.VISIBLE);
                    mWinMgr.updateViewLayout(mMyView, mLayoutParams);
                }
            }
        });
    }

    private void setInfo(String info) {
        Log.i(TAG, info);
        TextView tvCmdDebug = (TextView) mMyView.findViewById(R.id.cmdDebug);
        tvCmdDebug.setText(info);
    }

    public void turnLeft()  {
        Arrays.fill(mOutMsg, (byte) 97);
        writeCmd(mOutMsg);
    }

    public void turnRight()  {
        Arrays.fill(mOutMsg, (byte) 100);
        writeCmd(mOutMsg);
    }

    public void turnUp() {
        Arrays.fill(mOutMsg, (byte) 119);
        writeCmd(mOutMsg);
    }

    public void echo(){
        Arrays.fill(mOutMsg, (byte) 13);
        writeCmd(mOutMsg);
    }
    public void turnDown()  {
        Arrays.fill(mOutMsg, (byte) 115);
        writeCmd(mOutMsg);
    }
    public void quit()  {
        if (mMyView != null){
            if (mWinMgr != null){
                mWinMgr.removeView(mMyView);
                mMyView = null;
            }
        }
        mOutMsg[0] = 'q';
        mOutMsg[1] = 'q';
        mOutMsg[2] = 'q';
        mOutMsg[3] = 'q';
        mOutMsg[4] = 'q';
        writeCmd(mOutMsg);
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
        try {
            tcpDataTransferThread.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "sent: " + Arrays.toString(cmd));
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        super.onDestroy();
    }
}

