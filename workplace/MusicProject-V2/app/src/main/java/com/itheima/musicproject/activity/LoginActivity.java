package com.itheima.musicproject.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.itheima.musicproject.AppContext;
import com.itheima.musicproject.R;
import com.itheima.musicproject.adapter.GuideAdapter;
import com.itheima.musicproject.domain.event.LoginSuccessEvent;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.OnClick;

public class LoginActivity extends BaseCommonActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    protected void initDatas() {
        super.initDatas();
        EventBus.getDefault().register(this);

    }
    @OnClick(R.id.bt_login)
    public void bt_login() {
        startActivity(LoginPhoneActivity.class);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loginSuccessEvent(LoginSuccessEvent event) {
        //连接融云服务器
//        ((AppContext)getApplication()).imConnect();
        finish();
    }


    @OnClick(R.id.bt_register)
    public void bt_register() {
        startActivity(RegisterActivity.class);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
