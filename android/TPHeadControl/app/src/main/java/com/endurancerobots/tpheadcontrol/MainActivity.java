package com.endurancerobots.tpheadcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private String headId;

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
        if (id == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void makeServerOnClick(View view) {
        ServoControlService.startServoControl(getApplicationContext(),getHeadId(),getMac());
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
        return "98:D3:31:90:42:C9";
    }
    public String getHeadId() {
        EditText editText = (EditText) findViewById(R.id.headId);
        return  editText.getText().toString();
    }
}
