package com.itheima.musicproject.domain.event;


import com.itheima.musicproject.domain.Song;

/**
 * Created by smile on 2018/6/8.
 */

public class OnStopRecordEvent {
    private final Song song;

    public OnStopRecordEvent(Song song) {
        this.song=song;
    }

    public Song getSong() {
        return song;
    }
}
