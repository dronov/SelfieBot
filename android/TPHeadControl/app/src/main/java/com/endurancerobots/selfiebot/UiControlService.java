package com.endurancerobots.selfiebot;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;

import android.media.FaceDetector;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class UiControlService extends Service {

    public static final String EXTRA_HEAD_ID
            = "com.endurancerobots.selfiebot.extra.HEAD_ID";
    public static final String ACTION_START_CONTROLS
            = "com.endurancerobots.selfiebot.extra.START_CONTROLS";
    public static final String EXTRA_PENDING_INTENT
            = "com.endurancerobots.selfiebot.extra.PENDING_INTENT";

    private static Handler sHandler;

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager.LayoutParams mPausedLayoutParams;
    private WindowManager mWinMgr;
    private View mMyView;
    private ProxyConnector mConnector;
    private byte mOutMsg[] = new byte[5];
    private float mXDelta;

    private float mYDelta;
    private String mHeadId = "987654321";

    private TcpDataTransferThread tcpDataTransferThread;

    private static final String TAG = "UiControlService";
    private boolean waitForIp=true;
    private PendingIntent pendingIntent;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStart");
        if (intent != null) {
            String action = intent.getAction();
            if ((ACTION_START_CONTROLS).equals(action)) {

                mHeadId = intent.getStringExtra(EXTRA_HEAD_ID);
                pendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);

                Log.i(TAG, "mHeadId:" + mHeadId + " pendingIntent:" + pendingIntent.toString());

                sHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case TcpDataTransferThread.MESSAGE_READ:
                                Log.i(TAG, "sDataHandler got message: " + msg.arg1 + " "
                                        + msg.arg2 + " " + Arrays.toString(((byte[]) msg.obj)));
                                setInfo(ComandDecoder.decode((byte[]) msg.obj));
                                break;
                            case ProxyConnector.CONNECTED_CLIENT_SOCKET:
                                mConnector.cancel();
                                Log.d(TAG, "CONNECT");
                                setupLayout();
                                setupClicks();
                                setInfo(getString(R.string.successful_connection));
                                tcpDataTransferThread = new TcpDataTransferThread((Socket) msg.obj);
                                tcpDataTransferThread.setOutDataHandler(sHandler);
                                tcpDataTransferThread.setName("Client");
                                tcpDataTransferThread.start();

                                try {pendingIntent.send(MainActivity.CLIENT_CONNECTED);}
                                catch (PendingIntent.CanceledException e) {e.printStackTrace();}

                                break;
                            case TcpDataTransferThread.CLOSE_CONNECTION:
                                stopSelf();
                                break;
                            default:
                                Log.w(TAG, "sDataHandler got Unknown message " + msg.arg1 + " " + msg.arg2 + " " + msg.obj);
                        }
                    }
                };

                Log.d(TAG, "onCreate");

                Log.i(TAG, "UI Control got Head Id:" + mHeadId);

                try {
                    pendingIntent.send(MainActivity.CLIENT_START_CONNECTION);
                    Log.i(TAG,"send pending intent");
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                mConnector = new ProxyConnector(sHandler); // Пытаемся подключиться
                mConnector.startAsClient(mHeadId);

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateTransportSocket(Socket socket) {
        TcpDataTransferThread newTrans = new TcpDataTransferThread(socket);
        newTrans.setOutDataHandler(sHandler);
        newTrans.start();
        tcpDataTransferThread = newTrans;
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
        ViewGroup viewGroup = new ViewGroup(getApplicationContext()) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {

            }
        };
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
//                saveBitmap(takeScreenShot());
            }
        });
    }
    private int imageWidth, imageHeight;
    private int numberOfFace = 5;
    private FaceDetector myFaceDetect;
    private FaceDetector.Face[] myFace;
    float myEyesDistance;
    int numberOfFaceDetected;
    private Bitmap mBitmap;
    private String path;
    private int framerate=20;

    private Bitmap takeScreenShot() {
        Log.v(TAG, "take Screenshot");
        View rootView = mMyView;
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        return rootView.getDrawingCache();
    }

    private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
        Log.v(TAG,"converting bitmap");
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }

    private FaceDetector.Face[] findFaces(Bitmap bitmap) {
        Log.v(TAG,"finding faces");
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();
        myFace = new FaceDetector.Face[numberOfFace];
        myFaceDetect = new FaceDetector(imageWidth, imageHeight,
                numberOfFace);
        numberOfFaceDetected = myFaceDetect.findFaces(bitmap, myFace);
        Log.i("findFaces", "Number Of Face Detected = " + numberOfFaceDetected);
        return myFace;
    }
    private void saveBitmap(Bitmap bitmap) {
        path = Environment.getExternalStorageDirectory() + "/screenshot.png";
        File imagePath = new File(path);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
//            Toast.makeText(getApplicationContext(), "Saved to " + path, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }

    private void setInfo(String info) {
        Log.i(TAG, info);
        if(mMyView!=null) {
            TextView tvCmdDebug = (TextView) mMyView.findViewById(R.id.cmdDebug);
            tvCmdDebug.setText(info);
        }
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
        mOutMsg[0] = 'q';
        mOutMsg[1] = 'q';
        mOutMsg[2] = 'q';
        mOutMsg[3] = 'q';
        mOutMsg[4] = 'q';
        writeCmd(mOutMsg);

        Toast.makeText(getApplicationContext(),
                getString(R.string.connection_closed),
                Toast.LENGTH_LONG).show();

        if (mMyView != null){
            if (mWinMgr != null){
                mWinMgr.removeView(mMyView);
                mMyView = null;
            }
        }
        Log.i(TAG, "Connection closed");
        if(mConnector !=null) mConnector.cancel();
        if(tcpDataTransferThread!=null)tcpDataTransferThread.cancel();
        super.onDestroy();
    }
}

