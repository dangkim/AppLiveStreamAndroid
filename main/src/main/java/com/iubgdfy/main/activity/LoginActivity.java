package com.iubgdfy.main.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.iubgdfy.common.CommonAppConfig;
import com.iubgdfy.common.CommonAppContext;
import com.iubgdfy.common.Constants;
import com.iubgdfy.common.HtmlConfig;
import com.iubgdfy.common.activity.AbsActivity;
import com.iubgdfy.common.activity.WebViewActivity;
import com.iubgdfy.common.bean.ConfigBean;
import com.iubgdfy.common.bean.UserBean;
import com.iubgdfy.common.http.CommonHttpConsts;
import com.iubgdfy.common.http.CommonHttpUtil;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.interfaces.CommonCallback;
import com.iubgdfy.common.interfaces.OnItemClickListener;
import com.iubgdfy.common.mob.LoginData;
import com.iubgdfy.common.mob.MobBean;
import com.iubgdfy.common.mob.MobCallback;
import com.iubgdfy.common.mob.MobLoginUtil;
import com.iubgdfy.common.utils.DialogUitl;
import com.iubgdfy.common.utils.ToastUtil;
import com.iubgdfy.common.utils.ValidatePhoneUtil;
import com.iubgdfy.common.utils.WordUtil;
import com.iubgdfy.main.R;
import com.iubgdfy.main.adapter.LoginTypeAdapter;
import com.iubgdfy.main.event.RegSuccessEvent;
import com.iubgdfy.main.http.MainHttpConsts;
import com.iubgdfy.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * Created by cxf on 2018/9/17.
 */

public class LoginActivity extends AbsActivity implements OnItemClickListener<MobBean> {

    private View mRoot;
    private ImageView mBg;
    private ObjectAnimator mAnimator;
    private EditText mEditPhone;
    private EditText mEditPwd;
    private View mBtnLogin;
    private RecyclerView mRecyclerView;
    private MobLoginUtil mLoginUtil;
    private boolean mFirstLogin;//是否是第一次登录
    private boolean mShowInvite;//显示邀请码弹窗
    private String mLoginType = Constants.MOB_PHONE;//登录方式

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void main() {
        mRoot = findViewById(R.id.root);
        mBg = findViewById(R.id.bg);
        mBg.post(new Runnable() {
            @Override
            public void run() {
                if (mBg != null && mRoot != null) {
                    int bgHeight = mBg.getHeight();
                    int rootHeight = mRoot.getHeight();
                    int dy = bgHeight - rootHeight;
                    if (dy > 0) {
                        mAnimator = ObjectAnimator.ofFloat(mBg, "translationY", 0, -dy);
                        mAnimator.setInterpolator(new LinearInterpolator());
                        mAnimator.setDuration(4000);
                        mAnimator.setRepeatCount(-1);
                        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
                        mAnimator.start();
                    }
                }
            }
        });
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditPwd = (EditText) findViewById(R.id.edit_pwd);
        mBtnLogin = findViewById(R.id.btn_login);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = mEditPhone.getText().toString();
                String pwd = mEditPwd.getText().toString();
                mBtnLogin.setEnabled(!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(pwd));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mEditPhone.addTextChangedListener(textWatcher);
        mEditPwd.addTextChangedListener(textWatcher);
        boolean otherLoginType = false;
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            List<MobBean> list = MobBean.getLoginTypeList(configBean.getLoginType());
            if (list != null && list.size() > 0) {
                mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                LoginTypeAdapter adapter = new LoginTypeAdapter(mContext, list);
                adapter.setOnItemClickListener(this);
                mRecyclerView.setAdapter(adapter);
                mLoginUtil = new MobLoginUtil();
                otherLoginType = true;
            }
        }
        if (!otherLoginType) {
            findViewById(R.id.other_login_tip).setVisibility(View.INVISIBLE);
        }
        EventBus.getDefault().register(this);
    }


    public static void forward() {
        Intent intent = new Intent(CommonAppContext.sInstance, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        CommonAppContext.sInstance.startActivity(intent);
    }


    public void loginClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_login) {
            login();

        } else if (i == R.id.btn_register) {
            register();

        } else if (i == R.id.btn_forget_pwd) {
            forgetPwd();

        } else if (i == R.id.btn_tip) {
            forwardTip();
        }
    }

    //注册
    private void register() {
        startActivity(new Intent(mContext, RegisterActivity.class));
    }

    //忘记密码
    private void forgetPwd() {
        startActivity(new Intent(mContext, FindPwdActivity.class));
    }


    //手机号密码登录
    private void login() {
        String phoneNum = mEditPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNum)) {
            mEditPhone.setError(WordUtil.getString(R.string.login_input_phone));
            mEditPhone.requestFocus();
            return;
        }
        if (!ValidatePhoneUtil.validateMobileNumber(phoneNum)) {
            mEditPhone.setError(WordUtil.getString(R.string.login_phone_error));
            mEditPhone.requestFocus();
            return;
        }
        String pwd = mEditPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            mEditPwd.setError(WordUtil.getString(R.string.login_input_pwd));
            mEditPwd.requestFocus();
            return;
        }
        mLoginType = Constants.MOB_PHONE;
        MainHttpUtil.login(phoneNum, pwd, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                onLoginSuccess(code, msg, info);
            }
        });
    }

    //登录即代表同意服务和隐私条款
    private void forwardTip() {
        WebViewActivity.forward(mContext, HtmlConfig.LOGIN_PRIVCAY);
    }

    //登录成功！
    private void onLoginSuccess(int code, String msg, String[] info) {
        if (code == 0 && info.length > 0) {
            JSONObject obj = JSON.parseObject(info[0]);
            String uid = obj.getString("id");
            String token = obj.getString("token");
            mFirstLogin = obj.getIntValue("isreg") == 1;
            mShowInvite = obj.getIntValue("isagent") == 1;
            CommonAppConfig.getInstance().setLoginInfo(uid, token, true);
            getBaseUserInfo();
            //友盟统计登录
            MobclickAgent.onProfileSignIn(mLoginType, uid);
        } else {
            ToastUtil.show(msg);
        }
    }

    /**
     * 获取用户信息
     */
    private void getBaseUserInfo() {
        MainHttpUtil.getBaseInfo(new CommonCallback<UserBean>() {
            @Override
            public void callback(UserBean bean) {
                if (mFirstLogin) {
                    RecommendActivity.forward(mContext, mShowInvite);
                } else {
                    MainActivity.forward(mContext, mShowInvite);
                }
                finish();
            }
        });
    }

    /**
     * 三方登录
     */
    private void loginBuyThird(LoginData data) {
        mLoginType = data.getType();
        MainHttpUtil.loginByThird(data.getOpenID(), data.getNickName(), data.getAvatar(), data.getType(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                onLoginSuccess(code, msg, info);
            }
        });
    }

    @Override
    public void onItemClick(MobBean bean, int position) {
        if (mLoginUtil == null) {
            return;
        }
        final Dialog dialog = DialogUitl.loginAuthDialog(mContext);
        dialog.show();
        mLoginUtil.execute(bean.getType(), new MobCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data != null) {
                    loginBuyThird((LoginData) data);
                }
            }

            @Override
            public void onError() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFinish() {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegSuccessEvent(RegSuccessEvent e) {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = null;
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(MainHttpConsts.LOGIN);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_QQ_LOGIN_UNION_ID);
        MainHttpUtil.cancel(MainHttpConsts.LOGIN_BY_THIRD);
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
        if (mLoginUtil != null) {
            mLoginUtil.release();
        }
        super.onDestroy();
    }
}
