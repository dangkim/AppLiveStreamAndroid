package com.iubgdfy.live.dialog;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.iubgdfy.common.Constants;
import com.iubgdfy.common.adapter.RefreshAdapter;
import com.iubgdfy.common.bean.GoodsBean;
import com.iubgdfy.common.custom.CommonRefreshView;
import com.iubgdfy.common.dialog.AbsDialogFragment;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.interfaces.OnItemClickListener;
import com.iubgdfy.common.utils.DpUtil;
import com.iubgdfy.common.utils.JsonUtil;
import com.iubgdfy.common.utils.RouteUtil;
import com.iubgdfy.common.utils.WordUtil;
import com.iubgdfy.live.R;
import com.iubgdfy.live.adapter.LiveGoodsAdapter;
import com.iubgdfy.live.http.LiveHttpConsts;
import com.iubgdfy.live.http.LiveHttpUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2019/8/29.
 */

public class LiveGoodsDialogFragment extends AbsDialogFragment implements OnItemClickListener<GoodsBean> {

    private CommonRefreshView mRefreshView;
    private LiveGoodsAdapter mAdapter;
    private TextView mTitle;
    private String mLiveUid;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_goods;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DpUtil.dp2px(320);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLiveUid = bundle.getString(Constants.LIVE_UID);
        }
        mTitle = (TextView) findViewById(R.id.title);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_goods);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GoodsBean>() {
            @Override
            public RefreshAdapter<GoodsBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LiveGoodsAdapter(mContext);
                    mAdapter.setOnItemClickListener(LiveGoodsDialogFragment.this);
                }
                return mAdapter;
            }
            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getSale(p,mLiveUid,callback);
            }
            @Override
            public List<GoodsBean> processData(String[] info) {
                if(info!=null&&info.length>0){
                    String jsonStr=info[0];
                    String count=JsonUtil.getString(jsonStr,"nums");
                    mTitle.setText(WordUtil.getString(R.string.goods_tip_17)+count);
                    return JsonUtil.getJsonToList(JsonUtil.getString(jsonStr,"list"),GoodsBean.class);
                }else{
                    return new ArrayList<>();
                }
            }
            @Override
            public void onRefreshSuccess(List<GoodsBean> list, int listCount) {
            }
            @Override
            public void onRefreshFailure() {

            }
            @Override
            public void onLoadMoreSuccess(List<GoodsBean> loadItemList, int loadItemCount) {
            }
            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();

    }
    @Override
    public void onDestroy() {
        mContext = null;
        LiveHttpUtil.cancel(LiveHttpConsts.GET_SALE);
        super.onDestroy();

    }

    @Override
    public void onItemClick(GoodsBean bean, int position) {
        RouteUtil.forwardGoods(mContext,bean,null);


    }
}
