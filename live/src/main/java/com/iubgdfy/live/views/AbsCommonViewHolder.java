package com.iubgdfy.live.views;

import android.content.Context;
import android.view.ViewGroup;

import com.iubgdfy.common.views.AbsViewHolder;

/**
 * Created by cxf on 2018/10/26.
 */

public abstract class AbsCommonViewHolder extends AbsViewHolder {

    protected boolean mFirstLoadData = true;
    private boolean mShowed;

    public AbsCommonViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public void loadData() {
    }

    protected boolean isFirstLoadData() {
        if (mFirstLoadData) {
            mFirstLoadData = false;
            return true;
        }
        return false;
    }


    public void setShowed(boolean showed) {
        mShowed = showed;
    }

    public boolean isShowed() {
        return mShowed;
    }
}
