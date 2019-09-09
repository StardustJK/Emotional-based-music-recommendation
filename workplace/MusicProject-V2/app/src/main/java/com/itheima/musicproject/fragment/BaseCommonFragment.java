package com.itheima.musicproject.fragment;


import com.itheima.musicproject.util.OrmUtil;
import com.itheima.musicproject.util.SharedPreferencesUtil;

/**
 * Created by smile on 02/03/2018.
 */

public abstract class BaseCommonFragment extends BaseFragment {
    protected SharedPreferencesUtil sp;
    protected OrmUtil orm;

    @Override
    protected void initViews() {
        super.initViews();

        sp = SharedPreferencesUtil.getInstance(getActivity().getApplicationContext());
        orm = OrmUtil.getInstance(getActivity().getApplicationContext());
    }


}
