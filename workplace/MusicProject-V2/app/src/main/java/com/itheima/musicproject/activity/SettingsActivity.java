package com.itheima.musicproject.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.itheima.musicproject.R;
import com.itheima.musicproject.domain.event.LogoutSuccessEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;

public class SettingsActivity extends BaseTitleActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
    @Override
    protected void initViews() {
        super.initViews();
        enableBackMenu();
    }
    @OnClick(R.id.bt_logout)
    public void bt_logout() {
        sp.logout();
        //发布退出登陆的信息，因为首页要根据登陆状态显示
        EventBus.getDefault().post(new LogoutSuccessEvent());
    }
}
