package com.endurancerobots.selfiebot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by ilya on 16.07.15.
 */
public class DeviceChooserDialog extends DialogFragment {


    final String[] bluetoothMacAddr = {"00:12:05:04:80:21", "98:D3:31:90:42:C9"};
    private BluetoothAdapter mBluetoothAdapter;
    private String TAG="DeviceChooserDialog";
    private ArrayList<String> mDeviceArray=new ArrayList<>();
    private boolean isDevicesFound=false;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



//        // Register the BroadcastReceiver
        final String[] devicesList = getBluetoothDevicesList();
//        LinearLayout layout = new LinearLayout(getActivity());
//        layout.setOrientation(LinearLayout.VERTICAL);
//        for (final String mac : devicesList)
//        {
//            TextView tv = new TextView(getActivity());
//            tv.setText(mac);
//            tv.setPadding(10, 10, 10, 10);
//            tv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                ((MainActivity) getActivity()).deviceChoosed(mac);
//                Toast.makeText(getActivity(),
//                        getString(R.string.choosed) + mac,
//                        Toast.LENGTH_SHORT).show();
//                }
//            });
//            layout.addView(tv);
//        }
//
//        final ScrollView scrollView = new ScrollView(getActivity());
//        scrollView.addView(layout);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.chooseDevice))
                .setItems(devicesList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).deviceChoosed(devicesList[which]);
                        Toast.makeText(getActivity(),
                                getString(R.string.choosed) + devicesList[which],
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Create a BroadcastReceiver for ACTION_FOUND
//        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                // When discovery finds a device
//                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    // Get the BluetoothDevice object from the Intent
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    // Add the name and address to an array adapter to show in a ListView
//                    mDeviceArray.add(device.getName());
//                    Log.i(TAG, "Found: " + device.getName() + " " + device.getAddress());
//                }
//            }
//        };
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


        return builder.create();
    }

    private String[] getBluetoothDevicesList() {
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        mBluetoothAdapter.startDiscovery();
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        // If there are paired devices
//        if (pairedDevices.size() > 0) {
//            // Loop through paired devices
//            for (BluetoothDevice device : pairedDevices) {
//                // Add the name and address to an array adapter to show in a ListView
//                mDeviceArray.add(device.getAddress());
//                Log.i(TAG,device.getName() +" "+ device.getAddress());
//            }
//        }

//        return mDeviceArray.toArray(new String[mDeviceArray.size()]);
        return bluetoothMacAddr;
    }

}
