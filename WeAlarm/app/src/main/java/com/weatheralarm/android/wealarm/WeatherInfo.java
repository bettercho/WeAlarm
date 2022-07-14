package com.weatheralarm.android.wealarm;

/**
 * Created by yeonjin.cho on 2017-04-19.
 */
public class WeatherInfo {
    public float temp = -1; // 기온
    public float windSpeed = -1; // 풍속
    public int sky = -1; // 1: 맑음, 2: 구름조금, 3: 구름많음, 4: 흐림
    public int rainState = -1; // 0: 없음, 1: 비, 2: 비/눈, 3: 눈/비, 4:눈
    public int rainProb = -1; // 강수 확률

}
