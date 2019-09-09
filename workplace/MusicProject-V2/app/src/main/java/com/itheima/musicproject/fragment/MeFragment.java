package com.itheima.musicproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.itheima.musicproject.R;
import com.itheima.musicproject.activity.LocalMusicActivity;
import com.itheima.musicproject.adapter.MeAdapter;
import com.itheima.musicproject.api.Api;
import com.itheima.musicproject.domain.List;
import com.itheima.musicproject.domain.MeUI;
import com.itheima.musicproject.domain.response.ListResponse;
import com.itheima.musicproject.fragment.BaseCommonFragment;
import com.itheima.musicproject.reactivex.HttpListener;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MeFragment extends BaseCommonFragment implements View.OnClickListener {

    private ExpandableListView elv;
    private MeAdapter adapter;
    private LinearLayout ll_local_music;
    private LinearLayout ll_download;

    public static MeFragment newInstance() {

        Bundle args = new Bundle();
        MeFragment fragment = new MeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews() {
        super.initViews();
        elv=findViewById(R.id.elv);

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.header_me, elv, false);
        elv.addHeaderView(headerView);

        ll_local_music=findViewById(R.id.ll_local_music);
        ll_download=findViewById(R.id.ll_download);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        adapter = new MeAdapter(getActivity());
        elv.setAdapter(adapter);

        fetchData();
    }

    private void fetchData() {
        final ArrayList<MeUI> d = new ArrayList<>();

        Observable<ListResponse<List>> list = Api.getInstance().listsMyCreate();
        list.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpListener<ListResponse<List>>(getMainActivity()) {
                    //                    @Override
                    //                    public void onSucceeded(final ListResponse<com.ixuea.android.courses.music.domain.List> data) {
                    //                        super.onSucceeded(data);
                    //                        d.add(new MeUI("我创建的歌单",data.getData()));
                    //
                    //                        Api.getInstance().listsMyCollection().subscribeOn(Schedulers.io())
                    //                                .observeOn(AndroidSchedulers.mainThread())
                    //                                .subscribe(new HttpListener<ListResponse<List>>(getMainActivity()) {
                    //                                    @Override
                    //                                    public void onSucceeded(final ListResponse<com.ixuea.android.courses.music.domain.List> data) {
                    //                                        super.onSucceeded(data);
                    //                                        d.add(new MeUI("我收藏的歌单",data.getData()));
                    //                                        adapter.setData(d);
                    //                                    }
                    //                                });
                    //                    }
                });
    }


    @Override
    protected void initListener() {
        super.initListener();
        ll_local_music.setOnClickListener(this);
        ll_download.setOnClickListener(this);
    }


    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me,null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_local_music:
                                startActivity(LocalMusicActivity.class);
                break;
            case R.id.ll_download:

                break;
        }
    }
}

