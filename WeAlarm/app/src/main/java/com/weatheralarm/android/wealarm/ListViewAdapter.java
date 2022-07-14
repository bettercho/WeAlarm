package com.weatheralarm.android.wealarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

/**
 * Created by yeonjin.cho on 2017-04-17.
 */
public class ListViewAdapter extends BaseAdapter {
    private static String TAG = "[YJ]ListViewAdapter";

    public ArrayList<ListViewItem> mListViewItemList = new ArrayList<ListViewItem>() ;
    public Context mContext = null;

    public ListViewAdapter(Context ct)    {
        mContext = ct;
    }
    @Override
    public int getCount() {
        return mListViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mListViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        Log.d(TAG, "getView");
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }
        // ListView 한 item 들에 대한 값들을 가져옴.
        ImageView iv_clock = (ImageView) convertView.findViewById(R.id.iv_clock) ;
        TextView tv_hour = (TextView) convertView.findViewById(R.id.tv_hour) ;
        TextView tv_minute = (TextView) convertView.findViewById(R.id.tv_minute);
        TextView tv_day = (TextView) convertView.findViewById(R.id.tv_day);

        // Listview list 중 하나의 item 만 가져옴
        ListViewItem listViewItem = mListViewItemList.get(position);

        // 가져온 item 에서 설정된 값들을 가져와 UI에 업데이트 함
        if(listViewItem != null)
        {
            iv_clock.setImageResource(listViewItem.getSelectedImage());
            tv_hour.setText(""+listViewItem.getHour());
            tv_minute.setText(""+listViewItem.getMinute());

            tv_day.setText("");
            for(int i= 0; i < listViewItem.getDay().length; i++)
            {
                if(listViewItem.getDay()[i])
                {
                    switch(i) {
                        case 0:
                            tv_day.setText("MON ");
                            break;
                        case 1:
                            tv_day.setText(tv_day.getText() + "TUE ");
                            break;
                        case 2:
                            tv_day.setText(tv_day.getText() + "WED ");
                            break;
                        case 3:
                            tv_day.setText(tv_day.getText() + "THU ");
                            break;
                        case 4:
                            tv_day.setText(tv_day.getText() + "FRI ");
                            break;
                        case 5:
                            tv_day.setText(tv_day.getText() + "SAT ");
                            break;
                        case 6:
                            tv_day.setText(tv_day.getText() + "SUN ");
                            break;
                    }
                }
            }
        }
        return convertView;
    }

    public void addItem(int image, int key, int hour, int minute, boolean day[]) {
        Log.d(TAG, "addItem key " + key + " hour " + hour +", minute " + minute + ", day " + day);
        ListViewItem item = new ListViewItem();

        item.setSelectedImage(image);
        item.setHour(hour);
        item.setMinute(minute);
        item.setDay(day);
        item.setKey(key);

        mListViewItemList.add(item);
        notifyDataSetChanged();
    }

    public void deleteItem(int position)
    {
        Log.d(TAG, "delete item " + position);
        mListViewItemList.remove(position);
        notifyDataSetChanged();
    }
}
