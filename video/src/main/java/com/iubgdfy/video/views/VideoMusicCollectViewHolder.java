package com.iubgdfy.video.views;

import android.content.Context;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.iubgdfy.common.adapter.RefreshAdapter;
import com.iubgdfy.common.custom.CommonRefreshView;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.video.R;
import com.iubgdfy.video.adapter.MusicAdapter;
import com.iubgdfy.video.bean.MusicBean;
import com.iubgdfy.video.http.VideoHttpUtil;
import com.iubgdfy.video.interfaces.VideoMusicActionListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/12/7.
 * 视频收藏音乐
 */

public class VideoMusicCollectViewHolder extends VideoMusicChildViewHolder {

    public VideoMusicCollectViewHolder(Context context, ViewGroup parentView, VideoMusicActionListener actionListener) {
        super(context, parentView, actionListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_music_collect;
    }

    @Override
    public void init() {
        super.init();
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_music_collect);

        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<MusicBean>() {
            @Override
            public RefreshAdapter<MusicBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MusicAdapter(mContext);
                    mAdapter.setActionListener(mActionListener);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                VideoHttpUtil.getMusicCollectList(p, callback);
            }

            @Override
            public List<MusicBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), MusicBean.class);
            }

            @Override
            public void onRefreshSuccess(List<MusicBean> list, int listCount) {
                if(mActionListener!=null){
                    mActionListener.onStopMusic();
                }
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<MusicBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });

    }


}
