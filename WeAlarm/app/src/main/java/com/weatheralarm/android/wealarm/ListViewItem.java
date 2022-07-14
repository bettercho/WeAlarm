package com.weatheralarm.android.wealarm;

import android.graphics.drawable.Drawable;

/**
 * Created by yeonjin.cho on 2017-04-17.
 */
public class ListViewItem {

    int mHour = 0, mMinute = 0, mImage = -1, mKey =0;
    boolean mDay[];

    public int getSelectedImage()
    {
        return mImage;
    }

    public int getHour()
    {
        return mHour;
    }

    public int getMinute()
    {
        return mMinute;
    }

    public boolean[] getDay() { return mDay; }

    public void setSelectedImage(int image)
    {
        mImage = image;
    }

    public void setHour(int hour)
    {
        mHour = hour;
    }

    public void setMinute(int minute)
    {
        mMinute = minute;
    }

    public void setDay(boolean[] day)
    {
        mDay = day;
    }
    public void setKey(int key)
    {
        mKey = key;
    }
    public int getKey()
    {
        return mKey;
    }
}

