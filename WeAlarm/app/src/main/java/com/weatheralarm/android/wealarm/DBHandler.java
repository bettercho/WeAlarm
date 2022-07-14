package com.weatheralarm.android.wealarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by 조연진 on 2017-05-28.
 */
public class DBHandler {
    private static String TAG = "[YJ]DBHandler";

    MySQLiteOpenHelper helper;
    SQLiteDatabase db;

    public DBHandler(Context ct) {
        Log.d(TAG, "DBHandler");
        helper = new MySQLiteOpenHelper(ct, "alarm.sqlite", null, 1);
    }

    public static DBHandler open(Context ct) {
        Log.d(TAG,"open");
        return new DBHandler(ct);
    }

    public void close(){
        helper.close();
    }

    public void insert(int key, int hour, int min, boolean[] repeats, boolean enable) {
        Log.d(TAG, "insert "+ key);
        byte[] repeatValue = new byte[repeats.length];
        db = helper.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put("key", key);
        value.put("hour", hour);
        value.put("minute", min);
        for(int i=0; i<repeats.length; i++){
            switch(i){
                case 0:
                    if(repeats[0])
                        value.put("mon", 1);
                    break;
                case 1:
                    if(repeats[1])
                        value.put("tue", 1);
                    break;
                case 2:
                    if(repeats[2])
                        value.put("wed", 1);
                    break;
                case 3:
                    if(repeats[3])
                        value.put("thurs", 1);
                    break;
                case 4:
                    if(repeats[4])
                        value.put("fri", 1);
                    break;
                case 5:
                    if(repeats[5])
                        value.put("sat", 1);
                    break;
                case 6:
                    if(repeats[6])
                        value.put("sun", 1);
                    break;
            }
        }
        value.put("enable", enable);

        db.insert("alarm", null, value);
    }

    public void delete(int key)
    {
        Log.d(TAG, "delete " + key);
        db = helper.getWritableDatabase();
        int rest = db.delete("alarm", "key=?", new String[]{key+""});
        Log.d(TAG, "result " + rest);
    }

    public Cursor select()
    {
        Log.d(TAG, "select");
        db = helper.getReadableDatabase();

        Cursor c = db.query("alarm", null, null, null, null, null, null);
        Log.d(TAG, "cursor count "+ c.getCount());
        return c;
    }

    public void deleteAll()
    {
        Log.d(TAG, "deleteAll");
        db.delete("alarm", null, null);
    }

}
