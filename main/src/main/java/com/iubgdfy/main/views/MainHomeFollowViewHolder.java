package com.iubgdfy.main.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iubgdfy.common.CommonAppConfig;
import com.iubgdfy.common.Constants;
import com.iubgdfy.common.adapter.RefreshAdapter;
import com.iubgdfy.common.custom.CommonRefreshView;
import com.iubgdfy.common.custom.ItemDecoration;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.interfaces.OnItemClickListener;
import com.iubgdfy.live.bean.LiveBean;
import com.iubgdfy.live.utils.LiveStorge;
import com.iubgdfy.main.R;
import com.iubgdfy.main.adapter.MainHomeFollowAdapter;
import com.iubgdfy.main.http.MainHttpConsts;
import com.iubgdfy.main.http.MainHttpUtil;

import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 首页 关注
 */

public class MainHomeFollowViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<LiveBean> {

    private CommonRefreshView mRefreshView;
    private MainHomeFollowAdapter mAdapter;


    public MainHomeFollowViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_follow;
    }

    @Override
    public void init() {
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_live_follow);
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 5, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveBean>() {
            @Override
            public RefreshAdapter<LiveBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MainHomeFollowAdapter(mContext);
                    mAdapter.setOnItemClickListener(MainHomeFollowViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getFollow(p, callback);
            }

            @Override
            public List<LiveBean> processData(String[] info) {
                if (info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    return JSON.parseArray(obj.getString("list"), LiveBean.class);
                }
                return null;
            }

            @Override
            public void onRefreshSuccess(List<LiveBean> adapterItemList, int allItemCount) {
                if(CommonAppConfig.LIVE_ROOM_SCROLL){
                    LiveStorge.getInstance().put(Constants.LIVE_FOLLOW, adapterItemList);
                }
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onItemClick(LiveBean bean, int position) {
        watchLive(bean, Constants.LIVE_FOLLOW, position);
    }


    @Override
    public void release() {
        MainHttpUtil.cancel(MainHttpConsts.GET_FOLLOW);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }
}
