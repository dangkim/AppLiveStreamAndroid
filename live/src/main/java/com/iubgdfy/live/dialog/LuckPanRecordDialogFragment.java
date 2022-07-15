package com.iubgdfy.live.dialog;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.iubgdfy.common.adapter.RefreshAdapter;
import com.iubgdfy.common.custom.CommonRefreshView;
import com.iubgdfy.common.dialog.AbsDialogFragment;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.utils.DpUtil;
import com.iubgdfy.common.utils.JsonUtil;
import com.iubgdfy.live.R;
import com.iubgdfy.live.activity.LiveActivity;
import com.iubgdfy.live.adapter.LuckPanRecordAdapter;
import com.iubgdfy.live.bean.LuckPanBean;
import com.iubgdfy.live.http.LiveHttpConsts;
import com.iubgdfy.live.http.LiveHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2019/8/27.
 * 中奖记录
 */

public class LuckPanRecordDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private LuckPanRecordAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_luck_pan_record;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(300);
        params.height = DpUtil.dp2px(320);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LuckPanBean>() {
            @Override
            public RefreshAdapter<LuckPanBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LuckPanRecordAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getTurnRecord(p,callback);
            }
            @Override
            public List<LuckPanBean> processData(String[] info) {
               return JsonUtil.getJsonToList(Arrays.toString(info),LuckPanBean.class);
            }
            @Override
            public void onRefreshSuccess(List<LuckPanBean> list, int listCount) {

            }
            @Override
            public void onRefreshFailure() {

            }
            @Override
            public void onLoadMoreSuccess(List<LuckPanBean> loadItemList, int loadItemCount) {

            }
            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveHttpUtil.cancel(LiveHttpConsts.GET_WIN);
    }

    private String getLiveUid() {
        if(mContext!=null&&mContext instanceof LiveActivity){
            return ((LiveActivity)mContext).getLiveUid();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_confirm) {
            dismiss();
        }
    }
}
