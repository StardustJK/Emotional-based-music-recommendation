package com.itheima.musicproject.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.itheima.musicproject.R;

public class ScanLocalMusicActivity extends BaseTitleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_local_music);
    }

    @Override
    protected void initViews() {
        super.initViews();
        enableBackMenu();
    }
}
