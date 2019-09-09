package com.itheima.musicproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itheima.musicproject.R;
import com.itheima.musicproject.util.Consts;

import org.apache.commons.lang3.StringUtils;

/**
 * @author glsite.com
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class FeedFragment extends BaseCommonFragment {
    public static FeedFragment newInstance(String userId) {

        Bundle args = new Bundle();
        if (StringUtils.isNotBlank(userId)) {
            args.putString(Consts.ID,userId);
        }

        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static FeedFragment newInstance() {
        return newInstance(null);
    }


    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_detail_music,null);
    }
}
