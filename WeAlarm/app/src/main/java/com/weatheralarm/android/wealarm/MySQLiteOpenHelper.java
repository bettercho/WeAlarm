package com.weatheralarm.android.wealarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by 조연진 on 2017-05-28.
 */
public class MySQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper{
    private static String TAG = "[YJ]MySQLiteOpenHelper";

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d(TAG, "MySQLiteOpenHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        String sql = "create table alarm (_id integer primary key autoincrement, key integer, hour integer, minute integer," +
                " mon integer, tue integer, wed integer, thurs integer, fri integer, sat integer, sun integer, enable integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql="drop table if exists alarm";
        db.execSQL(sql);

        onCreate(db);
    }
}
