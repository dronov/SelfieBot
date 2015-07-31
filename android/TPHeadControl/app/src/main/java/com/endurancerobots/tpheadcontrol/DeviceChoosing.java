package com.endurancerobots.tpheadcontrol;

import com.endurancerobots.tpheadcontrol.util.SystemUiHider;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class DeviceChoosing extends Activity {


    private static final String TAG = "DeviceChoosing";
    public static final String REQUEST_BLUETOOTH_DEVICE_CHOOSED = "com.endurancerobots.tpheadcontrol.REQUEST_BLUETOOTH_DEVICE_CHOOSED";
    public static final String BLUETOOTH_MAC = "com.endurancerobots.tpheadcontrol.BLUETOOTH_MAC";

    private ListAdapter mBluetoothList;
    private ArrayList<String> mDeviceArray = new ArrayList<String>();
    private ArrayAdapter<String> mStringAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_choosing);
        Log.d(TAG, "onCreate");
        ListView listView = (ListView) findViewById(R.id.bluetooth_devices);

        mStringAdapter = new ArrayAdapter<String>(this,
                R.layout.bluetooth_device_item,
                mDeviceArray);
        listView.setAdapter(mStringAdapter);
        getDevices();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                Log.i(TAG, mStringAdapter.getItem(position));
                Intent answerIntent = new Intent();
                answerIntent.putExtra(BLUETOOTH_MAC, mStringAdapter.getItem(position));
                setResult(RESULT_OK, answerIntent);
                finish();
            }
        });

    }

    private void getDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDeviceArray.add(device.getAddress());
                mStringAdapter.notifyDataSetChanged();
                Log.i(TAG, device.getName() + " " + device.getAddress());
            }
        }
    }

    public void onBackClick(View view) {
        Log.d(TAG,"onBackClick");
        setResult(RESULT_CANCELED);
        finish();
    }
}
