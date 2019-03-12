package com.example.stardust.networkprogramming;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("hccc","started");
        getJSON();
        Log.d("hccc","done");
    }
    private void getJSON(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String host="https://api.bzqll.com/music/netease/url?key=579621905&id=526307800&br=999000";
//                String host="https://songsearch.kugou.com/song_search_v2?keyword=%E5%86%8D%E9%A3%9E%E8%A1%8C&page=1&pagesize=30";

                HttpURLConnection connection=null;
                try{
                    if(Thread.interrupted())
                        throw new InterruptedException();

                    URL url=new URL(host);
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setReadTimeout(15000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
//TODO :连接出错了

                    connection.connect();
                    Log.d("hccc","connected");
                    if(Thread.interrupted())
                        throw new InterruptedException();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                    String jonString=reader.readLine();
//                    JsonReader jsonReader=reader.readLine();
                    Log.d("hcc",jonString);

                    reader.close();



                } catch (MalformedURLException e) {
                    Log.d("hccc","malfor");
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    Log.d("hccc","proto");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("hccc","ioex");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.d("hccc","inter");
                    e.printStackTrace();
                } finally {
                    if(connection!=null)
                        connection.disconnect();
                }
            }
        }).start();
    }
}
