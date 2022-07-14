package com.weatheralarm.android.wealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by yeonjin.cho on 2017-04-19.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static String TAG = "[YJ]AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Toast.makeText(context, "Alarm Received!", Toast.LENGTH_LONG).show();

        Intent it = new Intent(context, AlarmActivity.class);
        context.startActivity(it);
    }
}
