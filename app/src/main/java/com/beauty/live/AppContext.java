package com.iubgdfy.phonelive;

import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.mob.MobSDK;
//import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.TXLiveBase;
import com.iubgdfy.common.CommonAppConfig;
import com.iubgdfy.common.CommonAppContext;
import com.iubgdfy.common.utils.L;
import com.iubgdfy.im.utils.ImMessageUtil;
import com.iubgdfy.im.utils.ImPushUtil;

import cn.tillusory.sdk.TiSDK;


/**
 * Created by cxf on 2017/8/3.
 */

public class AppContext extends CommonAppContext {

    public static AppContext sInstance;
    private boolean mBeautyInited;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        //腾讯云鉴权url
        //String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/fe049f839bb0f01f283878ecfb9bfcf0/TXUgcSDK.licence";
        //腾讯云鉴权key
        //String ugcKey = "d6931021642b853067e5d3d0937f1c7c";
        //腾讯云鉴权url
        String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/d2cf1c9c36f085621d406a3d78938c30/TXUgcSDK.licence";
        //腾讯云鉴权key
        String ugcKey = "e2419e867df3d70128da2869facdbacf";
        TXLiveBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);
        L.setDeBug(BuildConfig.DEBUG);
        //初始化腾讯bugly
        CrashReport.initCrashReport(this);
        CrashReport.setAppVersion(this, CommonAppConfig.getInstance().getVersion());
        //初始化ShareSdk
        MobSDK.init(this);
        //初始化极光推送
        ImPushUtil.getInstance().init(this);
        //初始化极光IM
        ImMessageUtil.getInstance().init();

        //初始化 ARouter
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);

//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this);
//        }
    }

    /**
     * 初始化萌颜
     */
    public void initBeautySdk(String beautyKey) {
        if(!TextUtils.isEmpty(beautyKey)){
            if (!mBeautyInited) {
                mBeautyInited = true;
                TiSDK.init(beautyKey, this);
                CommonAppConfig.getInstance().setTiBeautyEnable(true);
                L.e("萌颜初始化------->");
            }
        }else{
            CommonAppConfig.getInstance().setTiBeautyEnable(false);
        }

    }

}
