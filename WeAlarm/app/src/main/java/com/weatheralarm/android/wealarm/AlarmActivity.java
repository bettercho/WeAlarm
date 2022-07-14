package com.weatheralarm.android.wealarm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;

/**
 * Created by yeonjin.cho on 2017-04-20.
 */
public class AlarmActivity extends Activity {

    private static String TAG = "[YJ]AlarmActivity";
    public static String URL_PATH = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=4121063100";
    private final static int COMPLETE = 1;
    private final static int ERROR = 0;

    WeatherThread mWeatherThread = null;
    WeatherInfo mWeatherInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_alarm);

        mWeatherThread = new WeatherThread();
        mWeatherThread.start();

    }

    private final Handler mAlarmHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case COMPLETE:
                    Log.d(TAG, "complete");
                    mWeatherInfo = (WeatherInfo) msg.obj;
                    Log.d(TAG, "print weather info : " + mWeatherInfo.temp + " " + mWeatherInfo.windSpeed + " " + mWeatherInfo.sky + " " +
                        mWeatherInfo.rainState + " " + mWeatherInfo.rainProb );

                    MusicPlayAsyncTask task = new MusicPlayAsyncTask(AlarmActivity.this);
                    task.execute(mWeatherInfo);

                    break;
                case ERROR:
                    break;
            }
        }
    };

    // 날씨 정보를 기상청으로 부터 가지고 오는 Thread
    private class WeatherThread extends Thread{

        public void run() {
            Log.d(TAG, "run");
            WeatherInfo weatherInfo = weatherParser(URL_PATH);
            Message msg = mAlarmHandler.obtainMessage(COMPLETE, weatherInfo);
            mAlarmHandler.sendMessage(msg);
        }

        private WeatherInfo weatherParser(String path){
            WeatherInfo weInfo = new WeatherInfo();

            try{
                URL url = new URL(path);

                XmlPullParserFactory xppf;
                xppf = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = xppf.newPullParser();
                xpp.setInput(url.openStream(), null);

                int et = xpp.getEventType();

                // 기상청에서 제공하는 RSS XML 을 파싱하여 내가 사용하고자 하는 정보를 WeatherInfo 에 저장한다.
                while(et != XmlPullParser.END_DOCUMENT){
                    String tag = xpp.getName();
                    if(tag != null){
                        // 기온
                        if(tag.equals("temp")){
                            weInfo.temp = Float.parseFloat(xpp.nextText());
                            Log.d(TAG, "temp : " + weInfo.temp);
                        }
                        else if(tag.equals("ws")){
                            weInfo.windSpeed = Float.parseFloat(xpp.nextText());
                            Log.d(TAG, "windSpeed : " + weInfo.windSpeed);
                        }
                        else if(tag.equals("sky")){
                            weInfo.sky = Integer.parseInt(xpp.nextText());
                            Log.d(TAG, "sky : " + weInfo.sky);
                        }
                        else if(tag.equals("pty")){
                            weInfo.rainState =  Integer.parseInt(xpp.nextText());
                            Log.d(TAG, "rainState : " + weInfo.rainState);
                        }
                        else if(tag.equals("pop")){
                            weInfo.rainProb =  Integer.parseInt(xpp.nextText());
                            Log.d(TAG, "rainProb : " + weInfo.rainProb);
                        }
                    }
                    if(weInfo.temp != -1 && weInfo.windSpeed != -1 && weInfo.sky != -1 &&
                            weInfo.rainState != -1 && weInfo.rainProb != -1)
                        break;
                    et = xpp.next();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
            return weInfo;
        }
    }
}
