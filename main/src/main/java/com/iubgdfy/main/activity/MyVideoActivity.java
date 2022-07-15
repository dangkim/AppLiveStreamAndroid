package com.iubgdfy.main.activity;

import android.view.ViewGroup;

import com.iubgdfy.common.CommonAppConfig;
import com.iubgdfy.common.activity.AbsActivity;
import com.iubgdfy.common.utils.WordUtil;
import com.iubgdfy.main.R;
import com.iubgdfy.main.views.VideoHomeViewHolder;

/**
 * Created by cxf on 2018/12/14.
 */

public class MyVideoActivity extends AbsActivity {

    private VideoHomeViewHolder mVideoHomeViewHolder;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_video;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.video_my_video));
        mVideoHomeViewHolder = new VideoHomeViewHolder(mContext, (ViewGroup) findViewById(R.id.container), CommonAppConfig.getInstance().getUid());
        mVideoHomeViewHolder.addToParent();
        mVideoHomeViewHolder.subscribeActivityLifeCycle();
        mVideoHomeViewHolder.loadData();
    }

    private void release(){
        if(mVideoHomeViewHolder!=null){
            mVideoHomeViewHolder.release();
        }
        mVideoHomeViewHolder=null;
    }

    @Override
    public void onBackPressed() {
        release();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }
}
