package com.example.stardust.musicplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_play;
    private MediaPlayer mPlayer=null;
    private boolean isRelease=true;//判断MediaPlayer是否释放的标识

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    private void bindViews(){
        btn_play=(Button)findViewById(R.id.btn_play);
        btn_play.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(isRelease){
            mPlayer=new MediaPlayer();
            String url="https://api.bzqll.com/music/netease/url?key=579621905&id=29850685&br=999000";
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
}
