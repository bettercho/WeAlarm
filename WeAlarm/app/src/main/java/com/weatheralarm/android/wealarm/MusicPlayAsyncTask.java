package com.weatheralarm.android.wealarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by yeonjin.cho on 2017-04-21.
 */
public class MusicPlayAsyncTask extends AsyncTask<WeatherInfo, Integer, Void> {

    private static String TAG = "[YJ]MusicPlayAsyncTask";
    private static final int TIMEOUT_US = 1000;

    // 비나 눈이 오는날 : Blues, R&B, Ballad
    // 보통날 : Jazz, Rap, Hip-hop
    // 맑은날 : Dance, Disco, Folk
    private String rainDayGenre[] = {"Blues", "R&B", "Ballad", "Soul"};
    private String normalDayGenre[] = {"Jazz", "Rap", "Hip-hop", "Country", "Pop"};
    private String sunnyDayGenre[] = {"Dance", "Disco", "Folk", "Metal", "Techno", "Rock", "Death Metal"};
    private String weather = "";

    WeatherInfo mInfo = null;
    AlarmActivity mActivity = null;

    private MediaCodec mMediaCodec = null;
    private MediaExtractor mExtractor = null;
    private AudioTrack mAudioTrack  = null;
    private MediaFormat mFormat = null;

    private int mSampleRate = 0;
    private int mChannel = 0;

    Thread mPlayThread = null;

    public MusicPlayAsyncTask(AlarmActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(TAG, "onPostExecute");
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle("오늘 날씨는 " + weather );
        dialog.setMessage("알람을 끄시겠습니까?").setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface
                                                dialog, int which) {
                        mPlayThread.interrupt();

                        mAudioTrack.stop();
                        mAudioTrack.release();
                        mAudioTrack = null;

                        mMediaCodec.stop();
                        mMediaCodec.release();
                        mMediaCodec = null;

                        mExtractor.release();
                        mExtractor = null;

                        dialog.dismiss();
                        mActivity.finish();
                    }
                });
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(WeatherInfo... weatherInfos) {
        mInfo = weatherInfos[0];
        Log.d(TAG, "doInBackground info " + mInfo.temp);
        // 강수상태/강수확률 을 토대로 장르를 추출
        String todayGenre[] = getGenre();

        // 기온/풍속 을 토대로 재생 속도 추출
        float rate = getSpeedRate();
        String path = selectMusic(todayGenre);
        startMediaCodec(path, rate);

        return null;
    }

    private void startMediaCodec(final String path, final float rate) {
        Log.d(TAG, "startMediaCodec path : " + path + ", rate is " + rate);

        mPlayThread = new Thread(new Runnable() {
            int inputBufferIndex = 0;
            int outputBufferIndex = 0;

            @Override
            public void run() {
                mExtractor = new MediaExtractor();

                try {
                    mExtractor.setDataSource(path);
                }catch (IOException e) {
                    e.printStackTrace();
                }

                int audioIndex = -1;

                int trackCount = mExtractor.getTrackCount();
                for (int i = 0; i < trackCount; i++) {
                    MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
                    String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("audio/")) {
                        audioIndex = i;
                        break;
                    }
                }
                mExtractor.selectTrack(audioIndex);
                mFormat = mExtractor.getTrackFormat(audioIndex);

                try {
                    mMediaCodec = MediaCodec.createDecoderByType(mFormat.getString(MediaFormat.KEY_MIME));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mSampleRate = mFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                mChannel = mFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

                mMediaCodec.configure(mFormat, null, null, 0);
                mMediaCodec.start();

                int bufferSize = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize*2, AudioTrack.MODE_STREAM);

                if(mAudioTrack == null || mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioTrack initiaize error!!");
                    return ;
                }
                mAudioTrack.play();

                while(!mPlayThread.isInterrupted()) {
                    inputBufferIndex = mMediaCodec.dequeueInputBuffer(100);
                    if(inputBufferIndex >= 0) {
                        ByteBuffer buffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                        if(buffer == null)
                            Log.e(TAG, "buffer is null");

                        int readsize = mExtractor.readSampleData(buffer, 0);
                        if(readsize < 0) {
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        }
                        else {
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, readsize, mExtractor.getSampleTime(), 0);
                            mExtractor.advance();
                        }
                    }
                    else
                        Log.e(TAG, "inputbuffer index is invalid");

                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    outputBufferIndex = mMediaCodec.dequeueOutputBuffer(info, 100);

                    switch(outputBufferIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.d(TAG, "output format changed");
                            MediaFormat format = mMediaCodec.getOutputFormat();
                            Log.d(TAG, "new sample rate : " + format.getInteger(MediaFormat.KEY_SAMPLE_RATE) + ", new channel : " + format.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                            mAudioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                            //mAudioTrack.setPlaybackRate((int)(format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 1.2));
                            PlaybackParams params = new PlaybackParams();
                            params.setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT);
                            params.setSpeed(rate);
                            params.setPitch(1.0f);
                            mAudioTrack.setPlaybackParams(params);
                            //mAudioTrack.setPlaybackRate(88200);
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.d(TAG, "output try again later");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            final byte[] chunck = new byte[info.size];
                            mMediaCodec.getOutputBuffer(outputBufferIndex).get(chunck);
                            mMediaCodec.getOutputBuffer(outputBufferIndex).clear();
                            if(chunck.length >= 4 )
                                Log.d(TAG, "chunk length " +chunck.length + ", chunck sample... " + chunck[0] + " " + chunck[1] + " "  + chunck[2] + " "  + chunck[3]);
                            else
                                Log.d(TAG, "chunk length " + chunck.length);
                            //byte[] result = changeSpeedRate(chunck, 0);
                            mAudioTrack.write(chunck, 0, chunck.length);
                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);

                            break;
                    }
                }
            }
        });

        mPlayThread.start();
    }

    private byte[] changeSpeedRate(byte[] samples, float ratio)
    {
        byte[] result = new byte[samples.length];
        int index = 0;
       for (int i = 0; i < samples.length / 2 ; i+=4 )
        {
            result[index] = (byte)((samples[i] +samples[i+2])*0.5);
            result[index+1] = (byte) ((samples[i+1] + samples[i+3]) *0.5);
            //result[index] = samples[i];
            //result[index+1] = samples[i+1];
            index+=2 ;
        }
        return result;
    }

    private String selectMusic(String genre[]){
        String path = mActivity.getFilesDir().toString() + "/WeAlarm";
        ArrayList<Integer> genre_id = new ArrayList<Integer>();

        File file = new File(path);
        if(!file.exists()) {
            boolean ret = file.mkdirs();
            if(ret)
                Log.d(TAG, "WeAlarm dir create");
            else
                Log.e(TAG, "dir create error");
        }

        // audio_genres 에서 genre name 에 맞는 genre id 를 알아냄
        Cursor cursor_id = (Cursor) mActivity.getContentResolver().query( MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null, null, null, null);
        cursor_id.moveToFirst();
        while(cursor_id.moveToNext()){
            String str_genre = cursor_id.getString(cursor_id.getColumnIndex(MediaStore.Audio.Genres.NAME));
            Log.d(TAG,"genre : " + str_genre + ", length is " + genre.length);

            for(int i =0; i < genre.length; i++ ) {
                if (str_genre.contains(genre[i])) {
                    int _id = cursor_id.getInt(cursor_id.getColumnIndex(MediaStore.Audio.Genres._ID));
                    if(!genre_id.contains(_id)) {
                        genre_id.add(_id);
                        Log.d(TAG, "genre_id is " + _id);
                    }
                }
            }
        }

        // 전체 오디오 컨텐츠에 대한 table을 가져옴
        Cursor cursor_media = (Cursor) mActivity.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        ArrayList<String> play_audio_path = new ArrayList<String>();

        for(int i = 0; i < genre_id.size(); i++){
            Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre_id.get(i));
            String columns[] = {
                    MediaStore.Audio.Genres.Members.AUDIO_ID,
                    MediaStore.Audio.Genres.Members.GENRE_ID
            };

            Cursor cursor_audio =  (Cursor) mActivity.getContentResolver().query(uri, columns, null, null, null);
            cursor_audio.moveToFirst();
            do{
                cursor_media.moveToFirst();
                do{
                    String _id = cursor_media.getString(cursor_media.getColumnIndex(MediaStore.Audio.Media._ID));

                    if( cursor_audio.getString(0).equals(_id) ) {
                        Log.d(TAG, "this is the play list~~~ ");
                        Log.d(TAG, "" + cursor_media.getString(cursor_media.getColumnIndex((MediaStore.Audio.Media.TITLE))));
                        String audio_path = cursor_media.getString(cursor_media.getColumnIndex((MediaStore.Audio.Media.DATA)));
                        Log.d(TAG, "" + audio_path);

                        if(!play_audio_path.contains(audio_path))
                            play_audio_path.add(audio_path);
                    }
                }while(cursor_media.moveToNext());
            }while(cursor_audio.moveToNext());
        }
        // play_audio_path 가 이제 실제 play 할 path 가 된다!!

        Random random = new Random();
        int number = random.nextInt(play_audio_path.size()-1);
        Log.d(TAG, "path size is : " + play_audio_path.size() + ", number is " + number);
        return play_audio_path.get(number);
    }

    private String[] getGenre(){
        String genre[] = {};
        // rainState -> 0: 없음, 1: 비, 2: 비/눈, 3: 눈/비, 4:눈
        // sky -> 1: 맑음, 2: 구름조금, 3: 구름많음, 4: 흐림

        if(MainActivity.MODE > 0){
            if(MainActivity.MODE == 1){
                weather="맑습니다.";
                return sunnyDayGenre;
            }
            else if (MainActivity.MODE ==  2){
                weather = "구름이 많아요.";
                return normalDayGenre;
            }
            else if(MainActivity.MODE == 3){
                weather = "비가 옵니다.";
                return rainDayGenre;
            }
        }
        // 지금 비가 오거나, 비가 올 예정
        if(mInfo.rainState > 0 || mInfo.rainProb >= 60) {
            genre = rainDayGenre;
            weather = "비가 옵니다.";
        }
        // 지금 비는 안오지만, 구름이 많아 흐림
        else if(mInfo.sky > 2) {
            genre = normalDayGenre;
            weather = "구름이 많아요.";
        }
        // 맑음
        else{
            genre = sunnyDayGenre;
            weather = "맑습니다.";
        }
        return genre;
    }

    private float getSpeedRate(){
        // 체감온도 = 13.12 + 0.6215*T - 11.37*V^0.16 + 0.3965*V^0.16*T  (V: 풍속 km/h, T: 온도)
        // 체감온도는 기온 10도 이하, 풍속 4.8km/h (1.3m/s) 이상에서만 산출됨.
        // -45 미만 : 위험
        // -45~-25 : 경고
        // -25~-10 : 주의
        // -10 이상 : 관심

        // 풍속 4m/s 이하 : 약한바람
        // 풍속 9m/s 이하 : 약간 강한바람
        // 풍속 14m/s 이하 : 강한 바람
        // 풍속 14m/s 이상 : 매우 강한바람

        // 풍속 14m/s 이상: 강풍주의보
        // 풍속 21m/s 이상: 강풍경보

        if(MainActivity.RATE > 0) {
            return MainActivity.RATE;
        }

        float rate = 1.0f;

        if( mInfo.windSpeed <= 4) {
            Log.d(TAG, "normal case");
        }
        else if ( mInfo.windSpeed <= 9) {
            Log.d(TAG, "under 9m/s");
            rate = 1.2f;

        }
        else if ( mInfo.windSpeed <= 14) {
            Log.d(TAG, "under 14m/s");
            rate = 1.5f;
        }
        else {
            Log.d(TAG, "dangerous~~ ");
            rate = 2.0f;
        }

        /*if ( mInfo.windSpeed >= 1.3) {
            Log.d(TAG, "apply real temperature");

            double realTemp = 13.12 + (0.6215*mInfo.temp) - 11.37*Math.pow(mInfo.windSpeed*3.6, 0.16) + 0.3965*Math.pow(mInfo.windSpeed*3.6, 0.16)*mInfo.temp;
            Log.d(TAG, "real temperature is " + realTemp);

            // 현재 기온보다 체감온도가 +-3도 범위에 있으면 normal case
            if((double)mInfo.temp+3 >= realTemp && (double)mInfo.temp -3 <= realTemp ){
                Log.d(TAG, "normal case");
                rate = 1.0f;
            }
            else if((double)mInfo.temp - 3 > realTemp){
                Log.d(TAG, "cold case");
                rate = 1.5f;
            }
            else {
                Log.e(TAG, "abnormal case");
            }
        }*/
        return rate;
    }
}
