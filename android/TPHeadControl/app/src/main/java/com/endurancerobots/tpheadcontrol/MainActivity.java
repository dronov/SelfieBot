package com.endurancerobots.tpheadcontrol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import at.abraxas.amarino.Amarino;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private String headId;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;
    private BroadcastReceiver mReceiver;
    private String macAddr="";

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
            finish();
            return true;
        }else if(id == R.id.about){
            startActivity(new Intent(getApplicationContext(),AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void makeServerOnClick(View view) {
        FragmentManager manager = getFragmentManager();
        DeviceChooserDialog deviceChooser  =new DeviceChooserDialog();
        deviceChooser.show(manager,"Device choosing");

        ServoControlService.startServoControl(getApplicationContext(), getHeadId(), getMac());
    }

    private static final String ACTION_START_CONTROLS = "com.endurancerobots.tpheadcontrol.action.START_CONTROLS";

    private static final String EXTRA_HEAD_ID = "com.endurancerobots.tpheadcontrol.extra.HEAD_ID";
    public void connect2RobotOnClick(View view) {
        Log.d(TAG, "connect2RobotOnClick pressed");
//        UIControlService.startUIControls(getApplicationContext(), getHeadId());
        Log.v(TAG, "startUIControls");
        Intent intent = new Intent(getApplicationContext(), UIControlService.class);
        intent.setAction(ACTION_START_CONTROLS);
//        intent.putExtra(EXTRA_HEAD_ID, getHeadId());
        getApplicationContext().startService(intent);
    }

    private String getMac(){
        return macAddr;
    }

    public String getHeadId() {
        EditText editText = (EditText) findViewById(R.id.headId);
        return  editText.getText().toString();
    }


    public void deviceChoosed(String mac) {
        macAddr=mac;
    }
}
