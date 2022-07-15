package com.iubgdfy.common.upload;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.iubgdfy.common.CommonAppConfig;
import com.iubgdfy.common.http.CommonHttpUtil;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.utils.L;
import com.iubgdfy.common.utils.StringUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * Created by cxf on 2019/4/16.
 * 七牛上传文件
 */

public class UploadQnImpl implements UploadStrategy {

    private static final String TAG = "UploadQnImpl";
    private Context mContext;
    private List<UploadBean> mList;
    private int mIndex;
    private boolean mNeedCompress;
    private UploadCallback mUploadCallback;
    private HttpCallback mGetUploadTokenCallback;
    private String mToken;
    private UploadManager mUploadManager;
    private UpCompletionHandler mCompletionHandler;//上传回调
    private Luban.Builder mLubanBuilder;

    public UploadQnImpl(Context context) {
        mContext = context;
        mCompletionHandler = new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if(mList==null||mList.size()==mIndex){
                    if (mUploadCallback != null) {
                        mUploadCallback.onFinish(mList, false);
                    }
                    return;
                }
                UploadBean uploadBean = mList.get(mIndex);
                uploadBean.setSuccess(true);
                if (mNeedCompress) {
                    //上传完成后把 压缩后的图片 删掉
                    File compressedFile = uploadBean.getOriginFile();
                    if (compressedFile != null && compressedFile.exists()) {
                        compressedFile.delete();
                    }
                }
                mIndex++;
                if (mIndex < mList.size()) {
                    uploadNext();
                } else {
                    if (mUploadCallback != null) {
                        mUploadCallback.onFinish(mList, true);
                    }
                }
            }
        };
    }

    @Override
    public void upload(List<UploadBean> list, boolean needCompress, UploadCallback callback) {
        if (callback == null) {
            return;
        }
        if (list == null || list.size() == 0) {
            callback.onFinish(list, false);
            return;
        }
        boolean hasFile = false;
        for (UploadBean bean : list) {
            if (bean.getOriginFile() != null) {
                hasFile = true;
                break;
            }
        }
        if (!hasFile) {
            callback.onFinish(list, true);
            return;
        }
        mList = list;
        mNeedCompress = needCompress;
        mUploadCallback = callback;
        mIndex = 0;

        if (mGetUploadTokenCallback == null) {
            mGetUploadTokenCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        mToken = JSON.parseObject(info[0]).getString("token");
                        L.e(TAG, "-------上传的token------>" + mToken);
                        uploadNext();
                    }
                }
            };
        }
        CommonHttpUtil.getUploadQiNiuToken(mGetUploadTokenCallback);
    }

    @Override
    public void cancelUpload() {

    }

    private void uploadNext() {
        UploadBean bean = null;
        while (mIndex < mList.size() && (bean = mList.get(mIndex)).getOriginFile() == null) {
            mIndex++;
        }
        if (mIndex >= mList.size() || bean == null) {
            if (mUploadCallback != null) {
                mUploadCallback.onFinish(mList, true);
            }
            return;
        }
        bean.setRemoteFileName(StringUtil.generateFileName());
        if (mNeedCompress) {
            if (mLubanBuilder == null) {
                mLubanBuilder = Luban.with(mContext)
                        .ignoreBy(8)//8k以下不压缩
                        .setTargetDir(CommonAppConfig.CAMERA_IMAGE_PATH)
                        .setRenameListener(new OnRenameListener() {
                            @Override
                            public String rename(String filePath) {
                                return mList.get(mIndex).getRemoteFileName();
                            }
                        }).setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                            }
                            @Override
                            public void onSuccess(File file) {
                                UploadBean uploadBean = mList.get(mIndex);
                                uploadBean.setOriginFile(file);
                                upload(uploadBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                upload(mList.get(mIndex));
                            }
                        });
            }
            mLubanBuilder.load(bean.getOriginFile()).launch();
        } else {
            upload(bean);
        }
    }


    private void upload(UploadBean bean) {
        if (bean != null && !TextUtils.isEmpty(mToken) && mCompletionHandler != null) {
            if (mUploadManager == null) {
                mUploadManager = new UploadManager();
            }
            mUploadManager.put(bean.getOriginFile(), bean.getRemoteFileName(), mToken, mCompletionHandler, null);
        } else {
            if (mUploadCallback != null) {
                mUploadCallback.onFinish(mList, false);
            }
        }
    }

}
