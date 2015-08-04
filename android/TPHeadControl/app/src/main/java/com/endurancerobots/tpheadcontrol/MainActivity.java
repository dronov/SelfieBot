package com.endurancerobots.tpheadcontrol;

import android.app.Activity;
//import android.app.FragmentManager;
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
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    public static final int CHOOSE_BLUETOOTH_DEVICE = 0;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;
    private BroadcastReceiver mReceiver;
    private String mMacAddr ="";
    private Intent mUiControlServiceIntent;
    private boolean mBluetoothEnabled=true;
    private Intent mServoControlIntent=null;

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

    private void disableBluetooth() {
        mBluetoothEnabled=false;
    }

    private void enableBluetooth() {
        mBluetoothEnabled=true;
    }

    public void makeServerOnClick(View view) {
        setBluetoothOn();
    }


    public void connect2RobotOnClick(View view) {
        Log.d(TAG, "connect2RobotOnClick pressed");
//        UiControlService.startUIControls(getApplicationContext(), getHeadId());
        Log.v(TAG, "startUIControls");
        mUiControlServiceIntent = new Intent(getApplicationContext(), UiControlService.class);
        mUiControlServiceIntent.setAction(UiControlService.ACTION_START_CONTROLS);
        mUiControlServiceIntent.putExtra(UiControlService.EXTRA_HEAD_ID, getHeadId());
        getApplicationContext().startService(mUiControlServiceIntent);
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
        Log.d(TAG,"get Result:");
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode== Activity.RESULT_OK){
                Log.i(TAG,getString(R.string.bluetooth_enabled));
                Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_enabled),Toast.LENGTH_SHORT).show();
                gettingBoundedDevices();
            }else if(resultCode== Activity.RESULT_CANCELED){
                Log.i(TAG,getString(R.string.bluetooth_not_enabled));
                Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_not_enabled),Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == CHOOSE_BLUETOOTH_DEVICE){
            switch (resultCode){
                case RESULT_OK:
                    deviceChoosed(data.getStringExtra(DeviceChoosing.BLUETOOTH_MAC));
                    break;
                case RESULT_CANCELED:
                    Log.i(TAG,"Bluetooth device choosing canceled");
                    break;
            }
        }
    }

    private void gettingBoundedDevices() {
        Log.i(TAG, "gettingBoundedDevices");
        FragmentManager manager = getSupportFragmentManager();
        DeviceChooserDialog deviceChooser = new DeviceChooserDialog();
        deviceChooser.show(manager, "Device choosing");
//        Intent deviceChooseIntent = new Intent(getApplicationContext(),DeviceChoosing.class);
//        startActivityForResult(deviceChooseIntent,CHOOSE_BLUETOOTH_DEVICE);
        /** see deviceChoosed */
    }
    public void deviceChoosed(String mac) {
        mMacAddr =mac;
//        mServoControlIntent = ServoControlService.startServoControl(getApplicationContext(), getHeadId(), getMac());
        mServoControlIntent = new Intent(getApplicationContext(), ServoControlService.class);
        mServoControlIntent.setAction(ServoControlService.START_SERVO_CONTROL);
        mServoControlIntent.putExtra(ServoControlService.EXTRA_HEAD_ID, getHeadId());
        mServoControlIntent.putExtra(ServoControlService.EXTRA_MAC, getMac());
        getApplicationContext().startService(mServoControlIntent);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
    }
}
