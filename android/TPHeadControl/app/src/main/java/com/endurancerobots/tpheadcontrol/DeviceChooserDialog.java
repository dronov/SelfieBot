package com.endurancerobots.tpheadcontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
//import android.support.v4.*;
/**
 * Created by ilya on 16.07.15.
 */
public class DeviceChooserDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] bluetoothMacAddr = {"00:12:05:04:80:21", "98:D3:31:90:42:C9"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.chooseDevice))
                .setItems(bluetoothMacAddr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).deviceChoosed(bluetoothMacAddr[which]);
                        Toast.makeText(getActivity(),
                                "Выбрано: " + bluetoothMacAddr[which],
                                Toast.LENGTH_SHORT).show();
                    }
                });

        return builder.create();
    }
}
