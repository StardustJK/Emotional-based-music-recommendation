package com.itheima.musicproject.activity;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.itheima.musicproject.R;



import java.io.File;
import java.util.ArrayList;

public class LocalMusicActivity extends AppCompatActivity {
    private ListView mLv;



    public static final String MP3DIR = Environment.getExternalStorageDirectory() + "/Download/";
    private ArrayList<String> mMp3List;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        mLv = findViewById(R.id.lv);
        initPlayList();
    }



    /**
     * 初始化播放列表
     */
    private void initPlayList() {
        File file = new File(MP3DIR);
        File[] files = file.listFiles();
        mMp3List = new ArrayList<>();
        for (File f  : files) {
            if (f.getName().endsWith(".mp3")) {
                mMp3List.add(f.getAbsolutePath());
                System.out.println(f.getAbsolutePath());
            }
        }
        mLv.setAdapter(new MusicListAdapter());
    }

    private class MusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMp3List.size();
        }

        @Override
        public Object getItem(int position) {
            return mMp3List.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(LocalMusicActivity.this, R.layout.item_music, null);

            TextView tv_name = (TextView) view.findViewById(R.id.tv_item_name);
            final String path = mMp3List.get(position);
            tv_name.setText(path.substring(path.lastIndexOf("/") + 1));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent intent = new Intent(LocalMusicActivity.this,MusicPlayerActivity.class);
                    intent.putExtra("path",path);
                    startActivity(intent);
                }
            });
            return view;
        }
    }

}
