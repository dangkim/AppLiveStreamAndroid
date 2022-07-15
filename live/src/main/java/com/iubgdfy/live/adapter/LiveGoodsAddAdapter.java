package com.iubgdfy.live.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.iubgdfy.common.Constants;
import com.iubgdfy.common.adapter.RefreshAdapter;
import com.iubgdfy.common.bean.GoodsBean;
import com.iubgdfy.common.custom.MyRadioButton;
import com.iubgdfy.common.glide.ImgLoader;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.utils.ToastUtil;
import com.iubgdfy.common.utils.WordUtil;
import com.iubgdfy.live.R;
import com.iubgdfy.live.http.LiveHttpUtil;

import java.util.List;

/**
 * Created by cxf on 2019/8/29.
 */

public class LiveGoodsAddAdapter extends RefreshAdapter<GoodsBean> {

    private View.OnClickListener mAddClickListener;
    private String mAddString;
    private String mAddedString;

    private OnItemChildClickListner onItemChildClickListner;

    public LiveGoodsAddAdapter(Context context) {
        super(context);
        mAddClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) v.getTag();
                GoodsBean bean = mList.get(position);
                setIsSale(v,bean,position);
            }
        };
        mAddString = WordUtil.getString(R.string.add);
        mAddedString = WordUtil.getString(R.string.added);
    }


    private void setIsSale(final View v,final GoodsBean bean,final int position) {
        v.setEnabled(false);
        final int isSale=bean.getIssale()==1?0:1;
        LiveHttpUtil.shopSetSale(bean.getId(),isSale, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                ToastUtil.show(msg);
                if(code==0){
                    bean.setIssale(isSale);
                    notifyItemChanged(position, Constants.PAYLOAD);
                }
            }
            @Override
            public void onFinish() {
                super.onFinish();
                v.setEnabled(true);
            }
        });
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_live_goods_add, parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {

    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mTitle;
        TextView mPrice;
        TextView mPriceOrigin;
        MyRadioButton mBtnAdd;

        public Vh(View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mTitle = itemView.findViewById(R.id.title);
            mPrice = itemView.findViewById(R.id.price);
            mPriceOrigin = itemView.findViewById(R.id.price_origin);
            mBtnAdd = itemView.findViewById(R.id.btn_add);
            mBtnAdd.setOnClickListener(mAddClickListener);
        }

        void setData(GoodsBean bean, int position, Object payload) {
            if (payload == null) {
                mBtnAdd.setTag(position);
                ImgLoader.display(mContext,bean.getThumb(),mThumb);
                mPrice.setText(bean.getHaveUnitPrice());
                mPriceOrigin.setText(bean.getHaveUnitmOriginPrice());
                mPriceOrigin.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG);
                mTitle.setText(bean.getName());
            }

            if (bean.getIssale()==1) {
                mBtnAdd.setText(mAddedString);
                mBtnAdd.doChecked(true);

            } else {
                mBtnAdd.setText(mAddString);
                mBtnAdd.doChecked(false);

            }
        }
    }
}
