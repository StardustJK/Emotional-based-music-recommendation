package com.itheima.musicproject.domain.event;


import com.itheima.musicproject.domain.Song;

/**
 * Created by smile on 2018/6/8.
 */

public class OnStartRecordEvent {
    private final Song song;

    public OnStartRecordEvent(Song song) {
        this.song=song;
    }

    public Song getSong() {
        return song;
    }
}
