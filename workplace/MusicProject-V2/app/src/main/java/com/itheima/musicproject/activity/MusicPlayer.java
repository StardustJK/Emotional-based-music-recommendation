package com.itheima.musicproject.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.itheima.musicproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class MusicPlayer extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player3);
        overridePendingTransition(0, 0);
        Intent intent=getIntent();
        emotion=intent.getStringExtra("emotion");
        bindviews();


    }

    private Button btn_play,btn_again;
    private String emotion;
    private MediaPlayer mPlayer;
    private String songurl;

    private void bindviews(){
        btn_again=findViewById(R.id.btn_again);
        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MusicPlayer.this,Facedetect.class));
            }
        });

        btn_play=findViewById(R.id.btn_play);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("hccc","play start");
                        playMusic();

                    }
                }).start();

            }
        });
    }



    public void playMusic(){

        String url=getSongList(emotion);
        Log.d("hccc","songlist url"+url);
        mPlayer=new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();


}

    public String getSongList(final String str){
        HttpURLConnection connection = null;

        int offset = (int) (Math.random() * 10);
        StringBuilder host = new StringBuilder();
        try {
            host.append(" https://api.itooi.cn/music/netease/hotSongList?key=579621905&cat=")
                    .append(URLEncoder.encode(str, "utf-8")).append("&limit=1&offset=").append(offset);
            Log.d("hccc", "获取分类的歌单"+String.valueOf(host));
            if (Thread.interrupted())
                throw new InterruptedException();

            URL url = new URL(String.valueOf(host));
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            if (Thread.interrupted())
                throw new InterruptedException();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String jonString = reader.readLine();
            Log.d("hccc", "获取分类的歌单返回"+jonString);

            reader.close();
            JSONObject jsonObject = new JSONObject(jonString);
            JSONArray jsdata = jsonObject.getJSONArray("data");
            JSONObject jslist = jsdata.getJSONObject(0);
            String id = jslist.getString("id");
            Log.d("hccc", "歌单id"+id);
            return getSongId(id);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return null;


    }

    public String getSongId(final String listId){
        HttpURLConnection connection = null;

        StringBuilder host = new StringBuilder();
        try {
            host.append("https://api.itooi.cn/music/netease/songList?key=579621905&id=")
                    .append(URLEncoder.encode(listId, "utf-8")).append("&limit=1&offset=0");
            Log.d("hccc", "获取歌单"+String.valueOf(host));
            if (Thread.interrupted())
                throw new InterruptedException();

            URL url = new URL(String.valueOf(host));
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            if (Thread.interrupted())
                throw new InterruptedException();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String jonString = reader.readLine();
            Log.d("hccc", "返回的歌单"+jonString);

            reader.close();


            JSONObject jsonObject=new JSONObject(jonString);
            JSONObject jsdata=jsonObject.getJSONObject("data");
            int seed=jsdata.getInt("songListCount");
            int offset = (int) (Math.random() * seed);
            JSONArray jsSongs=jsdata.getJSONArray("songs");
            JSONObject jssong=jsSongs.getJSONObject(offset);
            int songid=jssong.getInt("id");
            String songurl=jssong.getString("url");
            Log.d("hccc", "歌曲播放地址"+songurl);
            return songurl;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}

