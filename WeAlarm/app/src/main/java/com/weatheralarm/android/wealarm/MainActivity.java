package com.weatheralarm.android.wealarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

// 해야될것
// 1. 알람 enable/disable
// 2. 10초마다 반복
// 3. 알람 울릴 때 화면 켜지게
// 4. 배속재생
// 5. 앱 닫아도 리스트 유지하도록
// 6.

public class MainActivity extends AppCompatActivity {

    private static String TAG = "[YJ]MainActivity";
    private int mYear, mMonth, mDay, mHour, mMinute;
    private boolean mRepeat[] = {false, false, false, false, false, false, false};
    private Toolbar mToolbar = null;
    private ListView mAlarmList = null;
    private ListViewAdapter mAdapter = null;
    private DBHandler mHandler = null;
    private int mDeletePosition = 0;
    private RadioGroup mRG = null;
    private RadioGroup mRG2 = null;
    public static int MODE = 0;
    public static float RATE = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.tb);
        mRG = (RadioGroup) findViewById(R.id.rg);
        mRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.rb_user:
                        MODE = 0;
                        break;
                    case R.id.rb_sunny:
                        MODE = 1;
                        break;
                    case R.id.rb_cloud:
                        MODE = 2;
                        break;
                    case R.id.rb_rain:
                        MODE = 3;
                        break;
                }
            }
        });

        mRG2 = (RadioGroup)findViewById(R.id.rg2);
        mRG2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.rb_user2:
                        RATE = 0f;
                        break;
                    case R.id.rb_4:
                        RATE = 1.0f;
                        break;
                    case R.id.rb_9:
                        RATE = 1.2f;
                        break;
                    case R.id.rb_14:
                        RATE = 1.5f;
                        break;
                    case R.id.rb_15:
                        RATE = 2.0f;
                }
            }
        });
        // 오늘 날짜 tool bar 에 update
        updateToolBar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        mAdapter = new ListViewAdapter(this);
        mAlarmList = (ListView) findViewById(R.id.lv_alarm);

        mHandler = DBHandler.open(this);
        if(mHandler == null)
            Log.e(TAG, "Handler is null");

        // 테스트용 알람 등록
        boolean tmp[] = {true, true, true, true, true, true, true};

        Log.d(TAG, "hour : " + Calendar.getInstance().get(Calendar.HOUR)+ ", minute :" + Calendar.getInstance().get(Calendar.MINUTE) + 1);

        Cursor c = mHandler.select();
        c.moveToFirst();
        while(c.moveToNext()){
            Log.d(TAG, "add item");
            setAlarm(c.getInt(c.getColumnIndex("key")), c.getInt(c.getColumnIndex("hour")), c.getInt(c.getColumnIndex("minute")), tmp);
        }

        if(Calendar.getInstance().get(Calendar.AM_PM) == 1 ){
            setAlarm(-1, Calendar.getInstance().get(Calendar.HOUR)+12, Calendar.getInstance().get(Calendar.MINUTE) + 1, tmp);
        }
        else{
            setAlarm(-1, Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE) + 1, tmp);
        }

        mAlarmList.setAdapter(mAdapter);
        mAlarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "item clicked~~~ " + position + "  id " + id);
                mDeletePosition = position;

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("삭제");
                dialog.setMessage("삭제하시겠습니까?").setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteData(mDeletePosition);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("취소", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                });
                dialog.show();
            }
        });
    }

    private void deleteData(int position)     {
        Log.d(TAG, "deleteData " + position);
        int key;
        key = ((ListViewItem) mAdapter.getItem(position)).getKey();

        Log.d(TAG, "count is " + mAdapter.getCount());
        Log.d(TAG, "key is " + key);

        cancleAlarm(key); // 알람 등록해놓은것 해제하고
        mAdapter.deleteItem(position); // 리스트에서 지우고
        mHandler.delete(key); // db에서 지우고
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item_setting) {
            DatePickerDialog dialog = new DatePickerDialog(this, listener, mYear, mMonth, mDay);
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            updateToolBar(year, monthOfYear, dayOfMonth);
        }
    };

    private void updateToolBar(int year, int month, int day) {
        Toast.makeText(this, "year " + year + ", month " + month + ", day " + day, Toast.LENGTH_SHORT).show();
        mYear = year;
        mMonth = month;
        mDay = day;
        mToolbar.setTitle("Today is " + mMonth + "." + mDay + "." + mYear);
        setSupportActionBar(mToolbar);
    }

    public void addSchedule(View view) {
        Log.d(TAG, "addSchedule");
        Intent intent = new Intent(this, AlarmSettingActivity.class);
        startActivityForResult(intent, 0);
    }
    public void deleteSchedule(View view){
        Log.d(TAG, "deleteSchedule");
        mHandler.deleteAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == 0) {
            mHour = data.getIntExtra("hour", 0);
            mMinute = data.getIntExtra("minute", 0);
            mRepeat = data.getBooleanArrayExtra("day");
            setAlarm(-1, mHour, mMinute, mRepeat);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
            super.onStop();
    }

    private void setAlarm(int key, int hour, int minute, boolean repeat[])
    {
        PendingIntent alarmIntent;
        AlarmManager alarmMng = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);

        long intervalTime = 24 * 60 * 60 * 1000;// 24시간
        long triggerTime = 0;
        if (key > 0 )
            triggerTime = key;
        else
            triggerTime = setTriggerTime(hour, minute, repeat);
        Log.d(TAG, "trigger time is " + triggerTime);

        alarmIntent = PendingIntent.getBroadcast(this, (int)triggerTime, intent, 0);
        //alarmMng.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalTime, alarmIntent);
        alarmMng.setExact(AlarmManager.RTC_WAKEUP, triggerTime, alarmIntent);

        mAdapter.addItem(R.mipmap.clock_selected,(int)triggerTime, hour, minute, repeat);
        mHandler.insert((int)triggerTime, hour, minute, repeat, true);
    }

    public void cancleAlarm(int key)
    {
        AlarmManager alarmMng =  (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, key, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMng.cancel(alarmIntent);
        alarmIntent.cancel();
    }

    private long setTriggerTime(int hour, int minute, boolean repeat[])
    {
        // current Time
        long atime = System.currentTimeMillis();
        // timepicker
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, hour);
        curTime.set(Calendar.MINUTE, minute);
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);

        long btime = curTime.getTimeInMillis();
        long triggerTime = btime;
        if (atime > btime)
            triggerTime += 1000 * 60 * 60 * 24;

        return triggerTime;
    }
}
