package com.endurancerobots.selfiebot;

import android.app.Activity;
//import android.app.FragmentManager;
import android.app.PendingIntent;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    public static final int CHOOSE_BLUETOOTH_DEVICE = 0;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int CLIENT_CONNECTED_CODE = 22;
    public static final int CLIENT_START_CONNECTION = 33;
    public static final int CLIENT_CONNECTED = 4;
    public static final int SERVER_CONNECTED_CODE = 44;
    public static final int SERVER_START_CONNECTION = 441;
    public static final int SERVER_CONNECTED = 442;
    public static final String EXTRA_SERVER_PINTENT
            = "com.endurancerobots.selfiebot.extra.EXTRA_SERVER_PINTENT";

    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;
    private BroadcastReceiver mReceiver;
    private String mMacAddr ="";
    private Intent mUiControlServiceIntent;
    private boolean mBluetoothEnabled=true;
    private Intent mServoControlIntent=null;
    private int activityState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }else
        if (id == R.id.exit) {
            if(mUiControlServiceIntent !=null) stopService(mUiControlServiceIntent);
            if(mServoControlIntent!=null) stopService(mServoControlIntent);
            finish();
            return true;
        }else if(id == R.id.about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void makeServerOnClick(View view) {
        setBluetoothOn();
    }


    public void connect2RobotOnClick(View view) {
        Log.i(TAG, "connect2RobotOnClick pressed");

        PendingIntent pendingIntent;
        mUiControlServiceIntent = new Intent(this, UiControlService.class);
        pendingIntent = createPendingResult(CLIENT_CONNECTED_CODE,new Intent(),0);
        mUiControlServiceIntent
                .setAction(UiControlService.ACTION_START_CONTROLS)
                .putExtra(UiControlService.EXTRA_HEAD_ID, getHeadId())
                .putExtra(UiControlService.EXTRA_PENDING_INTENT,pendingIntent);
        startService(mUiControlServiceIntent);
    }

    private String getMac(){
        return mMacAddr;
    }

    public String getHeadId() {
        EditText editText = (EditText) findViewById(R.id.headId);
        return  editText.getText().toString();
    }
    private void setBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            Log.d(TAG,"Device support bluetooth");
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth was not Enabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else {
                Log.d(TAG,"Bluetooth was Enabled");
                gettingBoundedDevices();
            }
        }else
        {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "get Result:" + "requestCode" + requestCode + "resultCode" + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, getString(R.string.bluetooth_enabled));
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.bluetooth_enabled), Toast.LENGTH_SHORT).show();
                    gettingBoundedDevices();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i(TAG, getString(R.string.bluetooth_not_enabled));
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.bluetooth_not_enabled), Toast.LENGTH_SHORT).show();
                }
                break;
            case CHOOSE_BLUETOOTH_DEVICE:
                switch (resultCode) {
                    case RESULT_OK:
                        deviceChoosed(data.getStringExtra(DeviceChoosing.BLUETOOTH_MAC));
                        break;
                    case RESULT_CANCELED:
                        Log.i(TAG, "Bluetooth device choosing canceled");
                        break;
                }
                break;
            case CLIENT_CONNECTED_CODE:
                Log.i(TAG,"CLIENT_CONNECTED_CODE");
                switch (resultCode) {
                    case CLIENT_START_CONNECTION:
                        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
                        pb.setVisibility(View.VISIBLE);
                        Button makeServerButton = (Button) findViewById(R.id.makeServer);
                        Button connect2RobotButton = (Button) findViewById(R.id.connect2Robot);
                        makeServerButton.setClickable(false);
                        makeServerButton.setTextColor(Color.GRAY);

                        connect2RobotButton.setClickable(false);
                        connect2RobotButton.setTextColor(Color.GRAY);

                        activityState = CLIENT_START_CONNECTION;
                        Log.i(TAG, "CLIENT_START_CONNECTION");
                        break;
                    case CLIENT_CONNECTED:
                        activityState = CLIENT_CONNECTED;
                        pb = (ProgressBar) findViewById(R.id.progressBar);
                        pb.setVisibility(View.INVISIBLE);
                        makeServerButton = (Button) findViewById(R.id.makeServer);
                        makeServerButton.setClickable(true);
                        makeServerButton.setTextColor(Color.BLACK);

                        finish();
                        Log.i(TAG, "CLIENT_CONNECTED");
                        break;
                }
                break;
            case SERVER_CONNECTED_CODE:
                Log.i(TAG,"CLIENT_CONNECTED_CODE");
                switch (resultCode) {
                    case SERVER_START_CONNECTION:

                        Button makeServerButton = (Button) findViewById(R.id.makeServer);
                        Button connect2RobotButton = (Button) findViewById(R.id.connect2Robot);
                        makeServerButton.setClickable(false);
                        makeServerButton.setTextColor(Color.GRAY);


                        Log.i(TAG, "SERVER_START_CONNECTION");
                        break;
                    case SERVER_CONNECTED:

                        finish();
                        Log.i(TAG, "SERVER_CONNECTED");
                        break;
                }
                break;
        }
    }

    private void gettingBoundedDevices() {
        Log.i(TAG, "gettingBoundedDevices");
//        FragmentManager manager = getSupportFragmentManager();
//        DeviceChooserDialog deviceChooser = new DeviceChooserDialog();
//        deviceChooser.show(manager, "Device choosing");
        Intent deviceChooseIntent = new Intent(getApplicationContext(),DeviceChoosing.class);
        startActivityForResult(deviceChooseIntent, CHOOSE_BLUETOOTH_DEVICE);
        /** see deviceChoosed */
    }
    public void deviceChoosed(String mac) {
        Toast.makeText(getApplicationContext(), getString(R.string.choosed) +mac,
                Toast.LENGTH_SHORT).show();
        mMacAddr =mac;
        PendingIntent pendingIntent;
        mUiControlServiceIntent = new Intent(this, UiControlService.class);
        pendingIntent = createPendingResult(SERVER_CONNECTED_CODE,new Intent(),0);
        mServoControlIntent = new Intent(getApplicationContext(), ServoControlService.class);
        mServoControlIntent
                .setAction(ServoControlService.START_SERVO_CONTROL)
                .putExtra(ServoControlService.EXTRA_HEAD_ID, getHeadId())
                .putExtra(ServoControlService.EXTRA_MAC, getMac())
                .putExtra(EXTRA_SERVER_PINTENT,pendingIntent);
        startService(mServoControlIntent);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if(activityState==CLIENT_START_CONNECTION){
            Log.i(TAG, "activityState==CLIENT_START_CONNECTION");
//            stopService(mUiControlServiceIntent);
        }
        super.onDestroy();
    }

    public void startFaceTracking(View view) {
        startActivity(new Intent(getApplicationContext(),FaceTrackingActivity.class));
    }
}
