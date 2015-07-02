package com.endurancerobots.headcontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import at.abraxas.amarino.Amarino;

public class MainActivity extends Activity {

    private String MAC="00:12:05:04:80:21";
//    private String MAC="98:D3:31:90:42:C9";
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**Using wasd-keys to control Head
     * 'A' - is identifier for device*/

    public void buttonLeftOnCklick(View view) {
        Amarino.sendDataToArduino(getApplicationContext(), MAC, 'A', 'a');
    }

    public void buttonRightOnCklick(View view) {
        Amarino.sendDataToArduino(getApplicationContext(), MAC, 'A', 'd');
    }

    public void buttonUpOnCklick(View view) {
        Amarino.sendDataToArduino(getApplicationContext(), MAC, 'A', 'w');
    }

    public void buttonDownOnCklick(View view) {
        Amarino.sendDataToArduino(getApplicationContext(), MAC, 'A', 's');
    }
}
