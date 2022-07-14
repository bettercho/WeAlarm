package com.weatheralarm.android.wealarm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Created by yeonjin.cho on 2017-04-18.
 */
public class AlarmSettingActivity extends AppCompatActivity{

    private static String TAG = "AlarmSettingActivity";
    private static int MON = 0;
    private static int TUE = 1;
    private static int WED = 2;
    private static int THU = 3;
    private static int FRI = 4;
    private static int SAT = 5;
    private static int SUN = 6;

    private boolean mSelectedDay[] = {false, false, false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_alarmsetting);

    }

    public void cancelClickListener(View view)
    {
        finish();
    }

    public void saveClickListener(View view)
    {
        boolean isSelected = false;

        Intent intent = new Intent();
        TimePicker tp = (TimePicker)findViewById(R.id.tp);
        intent.putExtra("hour", tp.getHour());
        intent.putExtra("minute", tp.getMinute());
        intent.putExtra("day", mSelectedDay);

        for(int i=0; i<mSelectedDay.length; i++ ){
            if(mSelectedDay[i] == true) {
                isSelected = true;
                break;
            }
        }
        if(isSelected != true) {
            Toast.makeText(this, "요일을 선택하세요" , Toast.LENGTH_SHORT).show();
            return;
        }

        setResult(0, intent);
        finish();
    }

    public void dayClickListener(View view)
    {
        int buttonId = view.getId();
        Button bt = (Button) view.findViewById(buttonId);

        switch(buttonId){
            case R.id.bt_mon:
                if(mSelectedDay[MON]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[MON] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[MON] = true;
                }
                break;
            case R.id.bt_tue:
                if(mSelectedDay[TUE]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[TUE] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[TUE] = true;
                }
                break;
            case R.id.bt_wed:
                if(mSelectedDay[WED]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[WED] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[WED] = true;
                }
                break;
            case R.id.bt_thu:
                if(mSelectedDay[THU]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[THU] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[THU] = true;
                }
                break;
            case R.id.bt_fri:
                if(mSelectedDay[FRI]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[FRI] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[FRI] = true;
                }
                break;
            case R.id.bt_sat:
                if(mSelectedDay[SAT]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[SAT] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[SAT] = true;
                }
                break;
            case R.id.bt_sun:
                if(mSelectedDay[SUN]){
                    bt.setTextColor(Color.WHITE);
                    mSelectedDay[SUN] = false;
                }
                else{
                    bt.setTextColor(Color.RED);
                    mSelectedDay[SUN] = true;
                }
                break;
            default:
                Log.e(TAG, "this is unexpected error");
                break;
        }

    }

}
