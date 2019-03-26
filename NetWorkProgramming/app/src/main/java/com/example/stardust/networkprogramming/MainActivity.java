package com.example.stardust.networkprogramming;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mPlayer=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSongList("治愈");
    }

    private void getSongList(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                int offset = (int) (Math.random() * 10);
                StringBuilder host = new StringBuilder();
                try {
                    host.append("https://api.bzqll.com/music/netease/hotSongList?key=579621905&cat=")
                            .append(URLEncoder.encode(str, "utf-8")).append("&limit=1&offset=").append(offset);
                    Log.d("hccc", String.valueOf(host));
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
                    Log.d("hcc", jonString);

                    reader.close();
                    JSONObject jsonObject = new JSONObject(jonString);
                    JSONArray jsdata = jsonObject.getJSONArray("data");
                    Log.d("hccc", String.valueOf(jsdata));
                    JSONObject jslist = jsdata.getJSONObject(0);
                    Log.d("hccc", String.valueOf(jslist));
                    String id = jslist.getString("id");
                    Log.d("hccc", id);
                    getSongId(id);


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


            }
        }).start();

    }

    public void getSongId(final String listId) {
        HttpURLConnection connection = null;

        StringBuilder host = new StringBuilder();
        try {
            host.append("https://api.bzqll.com/music/netease/songList?key=579621905&id=")
                    .append(URLEncoder.encode(listId, "utf-8")).append("&limit=1&offset=0");
            Log.d("hcccx", String.valueOf(host));
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
            Log.d("hcccx", jonString);

            reader.close();


            JSONObject jsonObject=new JSONObject(jonString);
            JSONObject jsdata=jsonObject.getJSONObject("data");
            int seed=jsdata.getInt("songListCount");
            int offset = (int) (Math.random() * seed);
            JSONArray jsSongs=jsdata.getJSONArray("songs");
            JSONObject jssong=jsSongs.getJSONObject(offset);
            int songid=jssong.getInt("id");
            String songurl=jssong.getString("url");
            Log.d("hccc", songurl);
            playSong(songurl);

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

    }
    public void playSong(String url){
        mPlayer=new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(url);
        } catch (IOException e) {
            Log.d("hcc","setdata");
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
            Log.d("hcc","pd");
        } catch (IOException e) {
            Log.d("hcc","prepare");
            e.printStackTrace();
        }
        mPlayer.start();
    }
}
