package com.iubgdfy.live.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iubgdfy.common.CommonAppConfig;
import com.iubgdfy.common.Constants;
import com.iubgdfy.common.HtmlConfig;
import com.iubgdfy.common.activity.AbsActivity;
import com.iubgdfy.common.bean.ConfigBean;
import com.iubgdfy.common.bean.LiveGiftBean;
import com.iubgdfy.live.bean.TurntableGiftBean;
import com.iubgdfy.common.bean.UserBean;
import com.iubgdfy.common.dialog.AbsDialogFragment;
import com.iubgdfy.common.event.CoinChangeEvent;
import com.iubgdfy.common.event.FollowEvent;
import com.iubgdfy.common.http.HttpCallback;
import com.iubgdfy.common.interfaces.KeyBoardHeightChangeListener;
import com.iubgdfy.common.mob.MobCallback;
import com.iubgdfy.common.mob.MobShareUtil;
import com.iubgdfy.common.mob.ShareData;
import com.iubgdfy.common.utils.KeyBoardHeightUtil2;
import com.iubgdfy.common.utils.L;
import com.iubgdfy.common.utils.ProcessImageUtil;
import com.iubgdfy.common.utils.ToastUtil;
import com.iubgdfy.common.utils.WordUtil;
import com.iubgdfy.im.event.ImUnReadCountEvent;
import com.iubgdfy.im.utils.ImMessageUtil;
import com.iubgdfy.live.R;
import com.iubgdfy.live.bean.LiveBean;
import com.iubgdfy.live.bean.LiveBuyGuardMsgBean;
import com.iubgdfy.live.bean.LiveChatBean;
import com.iubgdfy.live.bean.LiveDanMuBean;
import com.iubgdfy.live.bean.LiveEnterRoomBean;
import com.iubgdfy.live.bean.LiveGiftPrizePoolWinBean;
import com.iubgdfy.live.bean.LiveGuardInfo;
import com.iubgdfy.live.bean.LiveLuckGiftWinBean;
import com.iubgdfy.live.bean.LiveReceiveGiftBean;
import com.iubgdfy.live.bean.LiveUserGiftBean;
import com.iubgdfy.live.dialog.GiftPrizePoolFragment;
import com.iubgdfy.live.dialog.LiveChatListDialogFragment;
import com.iubgdfy.live.dialog.LiveChatRoomDialogFragment;
import com.iubgdfy.live.dialog.LiveGuardBuyDialogFragment;
import com.iubgdfy.live.dialog.LiveGuardDialogFragment;
import com.iubgdfy.live.dialog.LiveInputDialogFragment;
import com.iubgdfy.live.dialog.LiveRedPackListDialogFragment;
import com.iubgdfy.live.dialog.LiveRedPackSendDialogFragment;
import com.iubgdfy.live.dialog.LiveShareDialogFragment;
import com.iubgdfy.live.dialog.LuckPanDialogFragment;
import com.iubgdfy.live.dialog.LuckPanRecordDialogFragment;
import com.iubgdfy.live.dialog.LuckPanTipDialogFragment;
import com.iubgdfy.live.dialog.LuckPanWinDialogFragment;
import com.iubgdfy.live.http.LiveHttpConsts;
import com.iubgdfy.live.http.LiveHttpUtil;
import com.iubgdfy.live.presenter.LiveLinkMicAnchorPresenter;
import com.iubgdfy.live.presenter.LiveLinkMicPkPresenter;
import com.iubgdfy.live.presenter.LiveLinkMicPresenter;
import com.iubgdfy.live.socket.SocketChatUtil;
import com.iubgdfy.live.socket.SocketClient;
import com.iubgdfy.live.socket.SocketMessageListener;
import com.iubgdfy.live.views.AbsLiveViewHolder;
import com.iubgdfy.live.views.LiveAddImpressViewHolder;
import com.iubgdfy.live.views.LiveAdminListViewHolder;
import com.iubgdfy.live.views.LiveContributeViewHolder;
import com.iubgdfy.live.views.LiveEndViewHolder;
import com.iubgdfy.live.views.LiveRoomViewHolder;
import com.iubgdfy.live.views.LiveWebViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.List;

/**
 * Created by cxf on 2018/10/7.
 */

public abstract class LiveActivity extends AbsActivity implements SocketMessageListener, LiveShareDialogFragment.ActionListener, KeyBoardHeightChangeListener, AbsDialogFragment.LifeCycleListener {

    protected ViewGroup mContainer;
    protected ViewGroup mPageContainer;
    protected LiveRoomViewHolder mLiveRoomViewHolder;
    protected AbsLiveViewHolder mLiveBottomViewHolder;
    protected LiveAddImpressViewHolder mLiveAddImpressViewHolder;
    protected LiveContributeViewHolder mLiveContributeViewHolder;
    protected LiveWebViewHolder mLiveLuckGiftTipViewHolder;
    protected LiveAdminListViewHolder mLiveAdminListViewHolder;
    protected LiveEndViewHolder mLiveEndViewHolder;
    protected LiveLinkMicPresenter mLiveLinkMicPresenter;//???????????????????????????
    protected LiveLinkMicAnchorPresenter mLiveLinkMicAnchorPresenter;//???????????????????????????
    protected LiveLinkMicPkPresenter mLiveLinkMicPkPresenter;//???????????????PK??????
    protected SocketClient mSocketClient;
    protected LiveBean mLiveBean;
    protected int mLiveSDK;//sdk??????  0??????  1??????
    protected String mTxAppId;//??????sdkAppId
    protected boolean mIsAnchor;//???????????????
    protected int mSocketUserType;//socket????????????  30 ????????????  40 ?????????  50 ??????  60??????
    protected String mStream;
    protected String mLiveUid;
    protected String mDanmuPrice;//????????????
    protected String mCoinName;//????????????
    protected int mLiveType;//??????????????????  ?????? ?????? ?????? ?????????
    protected int mLiveTypeVal;//????????????,??????????????????????????????
    protected KeyBoardHeightUtil2 mKeyBoardHeightUtil;
    protected int mChatLevel;//??????????????????
    protected int mDanMuLevel;//??????????????????
    private MobShareUtil mMobShareUtil;
    private ProcessImageUtil mImageUtil;
    private boolean mFirstConnectSocket;//??????????????????????????????socket
    private boolean mGamePlaying;//??????????????????
    private boolean mChatRoomOpened;//????????????????????????????????????
    private LiveChatRoomDialogFragment mLiveChatRoomDialogFragment;//??????????????????
    protected LiveGuardInfo mLiveGuardInfo;
    private HashSet<DialogFragment> mDialogFragmentSet;

    @Override
    protected void main() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mCoinName = CommonAppConfig.getInstance().getCoinName();
        mIsAnchor = this instanceof LiveAnchorActivity;
        mPageContainer = (ViewGroup) findViewById(R.id.page_container);
        EventBus.getDefault().register(this);
        mImageUtil = new ProcessImageUtil(this);
        mDialogFragmentSet = new HashSet<>();
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    public ViewGroup getPageContainer() {
        return mPageContainer;
    }

    public ProcessImageUtil getProcessImageUtil() {
        return mImageUtil;
    }

    @Override
    public void onDialogFragmentShow(AbsDialogFragment dialogFragment) {
        if (mDialogFragmentSet != null && dialogFragment != null) {
            mDialogFragmentSet.add(dialogFragment);
        }
    }

    @Override
    public void onDialogFragmentHide(AbsDialogFragment dialogFragment) {
        if (mDialogFragmentSet != null && dialogFragment != null) {
            mDialogFragmentSet.remove(dialogFragment);
        }
    }

    private void hideDialogs() {
        if (mDialogFragmentSet != null) {
            for (DialogFragment dialogFragment : mDialogFragmentSet) {
                if (dialogFragment != null) {
                    dialogFragment.dismissAllowingStateLoss();
                }
            }
        }
    }


    /**
     * ????????????socket?????????
     */
    @Override
    public void onConnect(boolean successConn) {
        if (successConn) {
            if (!mFirstConnectSocket) {
                mFirstConnectSocket = true;
                if (mLiveType == Constants.LIVE_TYPE_PAY || mLiveType == Constants.LIVE_TYPE_TIME) {
                    SocketChatUtil.sendUpdateVotesMessage(mSocketClient, mLiveTypeVal, 1);
                }
                SocketChatUtil.getFakeFans(mSocketClient);
            }
        }
    }

    /**
     * ?????????socket??????
     */
    @Override
    public void onDisConnect() {

    }

    /**
     * ??????????????????
     */
    @Override
    public void onChat(LiveChatBean bean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.insertChat(bean);
        }
        if (bean.getType() == LiveChatBean.LIGHT) {
            onLight();
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void onLight() {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.playLightAnim();
        }
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void onEnterRoom(LiveEnterRoomBean bean) {
        if (mLiveRoomViewHolder != null) {
            LiveUserGiftBean u = bean.getUserBean();
            mLiveRoomViewHolder.insertUser(u);
            mLiveRoomViewHolder.insertChat(bean.getLiveChatBean());
            mLiveRoomViewHolder.onEnterRoom(bean);
        }
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public void onLeaveRoom(UserBean bean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.removeUser(bean.getId());
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAudienceLeaveRoom(bean);
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void onSendGift(LiveReceiveGiftBean bean) {
        if (mLiveRoomViewHolder != null) {
            // mLiveRoomViewHolder.insertChat(bean.getLiveChatBean());
            mLiveRoomViewHolder.showGiftMessage(bean);
        }
    }

    /**
     * pk ??????????????????
     *
     * @param leftGift  ??????????????????
     * @param rightGift ??????????????????
     */
    @Override
    public void onSendGiftPk(long leftGift, long rightGift) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onPkProgressChanged(leftGift, rightGift);
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void onSendDanMu(LiveDanMuBean bean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.showDanmu(bean);
        }
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public void onLiveEnd() {
        hideDialogs();
    }

    /**
     * ?????????  ??????????????????
     */
    @Override
    public void onAnchorInvalid() {
        hideDialogs();
    }

    /**
     * ?????????????????????
     */
    @Override
    public void onSuperCloseLive() {
        hideDialogs();
    }

    /**
     * ??????
     */
    @Override
    public void onKick(String touid) {

    }

    /**
     * ??????
     */
    @Override
    public void onShutUp(String touid, String content) {

    }

    /**
     * ????????????????????????
     */
    @Override
    public void onSetAdmin(String toUid, int isAdmin) {
        if (!TextUtils.isEmpty(toUid) && toUid.equals(CommonAppConfig.getInstance().getUid())) {
            mSocketUserType = isAdmin == 1 ? Constants.SOCKET_USER_TYPE_ADMIN : Constants.SOCKET_USER_TYPE_NORMAL;
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     */
    @Override
    public void onChangeTimeCharge(int typeVal) {

    }

    /**
     * ??????????????????????????????????????????
     */
    @Override
    public void onUpdateVotes(String uid, String deltaVal, int first) {
        if (!CommonAppConfig.getInstance().getUid().equals(uid) || first != 1) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.updateVotes(deltaVal);
            }
        }
    }

    /**
     * ???????????????
     */
    @Override
    public void addFakeFans(List<LiveUserGiftBean> list) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.insertUser(list);
        }
    }

    /**
     * ?????????  ????????????????????????
     */
    @Override
    public void onBuyGuard(LiveBuyGuardMsgBean bean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.onGuardInfoChanged(bean);
            LiveChatBean chatBean = new LiveChatBean();
            chatBean.setContent(bean.getUserName() + WordUtil.getString(R.string.guard_buy_msg));
            chatBean.setType(LiveChatBean.SYSTEM);
            mLiveRoomViewHolder.insertChat(chatBean);
        }
    }

    /**
     * ????????? ??????????????????
     */
    @Override
    public void onRedPack(LiveChatBean liveChatBean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setRedPackBtnVisible(true);
            mLiveRoomViewHolder.insertChat(liveChatBean);
        }
    }

    /**
     * ?????????????????????  ?????????????????????????????????
     */
    @Override
    public void onAudienceApplyLinkMic(UserBean u) {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAudienceApplyLinkMic(u);
        }
    }

    /**
     * ?????????????????????  ?????????????????????????????????socket
     */
    @Override
    public void onAnchorAcceptLinkMic() {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAnchorAcceptLinkMic();
        }
    }

    /**
     * ?????????????????????  ?????????????????????????????????socket
     */
    @Override
    public void onAnchorRefuseLinkMic() {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAnchorRefuseLinkMic();
        }
    }

    /**
     * ?????????????????????  ???????????????????????????????????????
     */
    @Override
    public void onAudienceSendLinkMicUrl(String uid, String uname, String playUrl) {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAudienceSendLinkMicUrl(uid, uname, playUrl);
        }

    }

    /**
     * ?????????????????????  ???????????????????????????
     */
    @Override
    public void onAnchorCloseLinkMic(String touid, String uname) {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAnchorCloseLinkMic(touid, uname);
        }
    }

    /**
     * ?????????????????????  ????????????????????????
     */
    @Override
    public void onAudienceCloseLinkMic(String uid, String uname) {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAudienceCloseLinkMic(uid, uname);
        }
    }

    /**
     * ?????????????????????  ?????????????????????
     */
    @Override
    public void onAnchorNotResponse() {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAnchorNotResponse();
        }
    }

    /**
     * ?????????????????????  ???????????????
     */
    @Override
    public void onAnchorBusy() {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAnchorBusy();
        }
    }

    /**
     * ?????????????????????  ?????????????????????????????????????????????????????????
     */
    @Override
    public void onLinkMicAnchorApply(UserBean u, String stream) {
        //??????????????????????????????
    }

    /**
     * ?????????????????????  ???????????????????????????????????????????????????
     *
     * @param playUrl ???????????????????????????
     * @param pkUid   ???????????????uid
     */
    @Override
    public void onLinkMicAnchorPlayUrl(String pkUid, String playUrl) {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorPlayUrl(pkUid, playUrl);
        }
        if (this instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) this).onLinkMicTxAnchor(true);
        }
    }

    /**
     * ?????????????????????  ?????????????????????
     */
    @Override
    public void onLinkMicAnchorClose() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorClose();
        }
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkClose();
        }
        if (this instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) this).onLinkMicTxAnchor(false);
        }
    }

    /**
     * ?????????????????????  ?????????????????????????????????
     */
    @Override
    public void onLinkMicAnchorRefuse() {
        //??????????????????????????????
    }

    /**
     * ?????????????????????  ??????????????????????????????
     */
    @Override
    public void onLinkMicAnchorNotResponse() {
        //??????????????????????????????
    }

    /**
     * ?????????????????????  ????????????????????????
     */
    @Override
    public void onlinkMicPlayGaming() {
        //??????????????????????????????
    }

    /**
     * ?????????????????????  ??????????????????????????????
     */
    @Override
    public void onLinkMicAnchorBusy() {
        //??????????????????????????????
    }

    /**
     * ???????????????PK  ????????????????????????????????????PK???????????????
     *
     * @param u      ?????????????????????
     * @param stream ???????????????stream
     */
    @Override
    public void onLinkMicPkApply(UserBean u, String stream) {
        //??????????????????????????????
    }

    /**
     * ???????????????PK ???????????????PK???????????????
     */
    @Override
    public void onLinkMicPkStart(String pkUid) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkStart(pkUid);
        }
    }

    /**
     * ???????????????PK  ???????????????????????????pk?????????
     */
    @Override
    public void onLinkMicPkClose() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkClose();
        }
    }

    /**
     * ???????????????PK  ??????????????????pk?????????
     */
    @Override
    public void onLinkMicPkRefuse() {
        //??????????????????????????????
    }

    /**
     * ???????????????PK   ??????????????????????????????
     */
    @Override
    public void onLinkMicPkBusy() {
        //??????????????????????????????
    }

    /**
     * ???????????????PK   ??????????????????????????????
     */
    @Override
    public void onLinkMicPkNotResponse() {
        //??????????????????????????????
    }

    /**
     * ???????????????PK   ???????????????PK???????????????
     */
    @Override
    public void onLinkMicPkEnd(String winUid) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkEnd(winUid);
        }
    }


    /**
     * ???????????????????????????
     */
    @Override
    public void onAudienceLinkMicExitRoom(String touid) {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.onAudienceLinkMicExitRoom(touid);
        }
    }


    /**
     * ??????????????????
     */
    @Override
    public void onLuckGiftWin(LiveLuckGiftWinBean bean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.onLuckGiftWin(bean);
        }
    }

    /**
     * ????????????
     */
    @Override
    public void onPrizePoolWin(LiveGiftPrizePoolWinBean bean) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.onPrizePoolWin(bean);
        }
    }


    /**
     * ????????????
     */
    @Override
    public void onPrizePoolUp(String level) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.onPrizePoolUp(level);
        }
    }


    @Override
    public void onGameZjh(JSONObject obj) {

    }

    @Override
    public void onGameHd(JSONObject obj) {

    }

    @Override
    public void onGameZp(JSONObject obj) {

    }

    @Override
    public void onGameNz(JSONObject obj) {

    }

    @Override
    public void onGameEbb(JSONObject obj) {

    }

    /**
     * ?????????????????????
     */
    public void openChatWindow() {
        if (mKeyBoardHeightUtil == null) {
            mKeyBoardHeightUtil = new KeyBoardHeightUtil2(mContext, super.findViewById(android.R.id.content), this);
            mKeyBoardHeightUtil.start();
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.chatScrollToBottom();
        }
        LiveInputDialogFragment fragment = new LiveInputDialogFragment();
        fragment.setLifeCycleListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_DANMU_PRICE, mDanmuPrice);
        bundle.putString(Constants.COIN_NAME, mCoinName);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveInputDialogFragment");
    }

    /**
     * ????????????????????????
     */
    public void openChatListWindow() {
        LiveChatListDialogFragment fragment = new LiveChatListDialogFragment();
        fragment.setLifeCycleListener(this);
        if (!mIsAnchor) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.LIVE_UID, mLiveUid);
            fragment.setArguments(bundle);
        }
        fragment.show(getSupportFragmentManager(), "LiveChatListDialogFragment");
    }

    public String getLiveUid() {
        return mLiveUid;
    }

    /**
     * ????????????????????????
     */
    public void openChatRoomWindow(UserBean userBean, boolean following) {
        if (mKeyBoardHeightUtil == null) {
            mKeyBoardHeightUtil = new KeyBoardHeightUtil2(mContext, super.findViewById(android.R.id.content), this);
            mKeyBoardHeightUtil.start();
        }
        LiveChatRoomDialogFragment fragment = new LiveChatRoomDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setImageUtil(mImageUtil);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.USER_BEAN, userBean);
        bundle.putBoolean(Constants.FOLLOW, following);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveChatRoomDialogFragment");
    }

    /**
     * ??? ?????? ??????
     */
    public void sendDanmuMessage(String content) {
        if (!mIsAnchor) {
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null && u.getLevel() < mDanMuLevel) {
                ToastUtil.show(String.format(WordUtil.getString(R.string.live_level_danmu_limit), mDanMuLevel));
                return;
            }
        }
        LiveHttpUtil.sendDanmu(content, mLiveUid, mStream, mDanmuCallback);
    }

    private HttpCallback mDanmuCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0 && info.length > 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                UserBean u = CommonAppConfig.getInstance().getUserBean();
                if (u != null) {
                    u.setLevel(obj.getIntValue("level"));
                    String coin = obj.getString("coin");
                    u.setCoin(coin);
                    onCoinChanged(coin);
                }
                SocketChatUtil.sendDanmuMessage(mSocketClient, obj.getString("barragetoken"));
            } else {
                ToastUtil.show(msg);
            }
        }
    };


    /**
     * ??? ?????? ??????
     */
    public void sendChatMessage(String content) {
        if (!mIsAnchor) {
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null && u.getLevel() < mChatLevel) {
                ToastUtil.show(String.format(WordUtil.getString(R.string.live_level_chat_limit), mChatLevel));
                return;
            }
        }
        int guardType = mLiveGuardInfo != null ? mLiveGuardInfo.getMyGuardType() : Constants.GUARD_TYPE_NONE;
        SocketChatUtil.sendChatMessage(mSocketClient, content, mIsAnchor, mSocketUserType, guardType);
    }

    /**
     * ??? ?????? ??????
     */
    public void sendSystemMessage(String content) {
        SocketChatUtil.sendSystemMessage(mSocketClient, content);
    }

    /**
     * ??? ????????? ??????
     */
    public void sendGiftMessage(LiveGiftBean giftBean, String giftToken) {
        SocketChatUtil.sendGiftMessage(mSocketClient, giftBean.getType(), giftToken, mLiveUid);
    }

    /**
     * ????????????????????????
     */
    public void kickUser(String toUid, String toName) {
        SocketChatUtil.sendKickMessage(mSocketClient, toUid, toName);
    }

    /**
     * ??????
     */
    public void setShutUp(String toUid, String toName, int type) {
        SocketChatUtil.sendShutUpMessage(mSocketClient, toUid, toName, type);
    }

    /**
     * ??????????????????????????????
     */
    public void sendSetAdminMessage(int action, String toUid, String toName) {
        SocketChatUtil.sendSetAdminMessage(mSocketClient, action, toUid, toName);
    }


    /**
     * ?????????????????????
     */
    public void superCloseRoom() {
        SocketChatUtil.superCloseRoom(mSocketClient);
    }

    /**
     * ?????????????????????
     */
    public void sendUpdateVotesMessage(int deltaVal) {
        SocketChatUtil.sendUpdateVotesMessage(mSocketClient, deltaVal);
    }


    /**
     * ??????????????????????????????
     */
    public void sendBuyGuardMessage(String votes, int guardNum, int guardType) {
        SocketChatUtil.sendBuyGuardMessage(mSocketClient, votes, guardNum, guardType);
    }

    /**
     * ???????????????????????????
     */
    public void sendRedPackMessage() {
        SocketChatUtil.sendRedPackMessage(mSocketClient);
    }


    /**
     * ????????????????????????
     */
    public void openAddImpressWindow(String toUid) {
        if (mLiveAddImpressViewHolder == null) {
            mLiveAddImpressViewHolder = new LiveAddImpressViewHolder(mContext, mPageContainer);
            mLiveAddImpressViewHolder.subscribeActivityLifeCycle();
        }
        mLiveAddImpressViewHolder.setToUid(toUid);
        mLiveAddImpressViewHolder.addToParent();
        mLiveAddImpressViewHolder.show();
    }

    /**
     * ????????????????????????
     */
    public void openContributeWindow() {
        if (mLiveContributeViewHolder == null) {
            mLiveContributeViewHolder = new LiveContributeViewHolder(mContext, mPageContainer, mLiveUid);
            mLiveContributeViewHolder.subscribeActivityLifeCycle();
            mLiveContributeViewHolder.addToParent();
        }
        mLiveContributeViewHolder.show();
        if (CommonAppConfig.LIVE_ROOM_SCROLL && !mIsAnchor) {
            ((LiveAudienceActivity) this).setScrollFrozen(true);
        }
    }






    /**
     * ????????????????????????
     */
    public void openAdminListWindow() {
        if (mLiveAdminListViewHolder == null) {
            mLiveAdminListViewHolder = new LiveAdminListViewHolder(mContext, mPageContainer, mLiveUid);
            mLiveAdminListViewHolder.subscribeActivityLifeCycle();
            mLiveAdminListViewHolder.addToParent();
        }
        mLiveAdminListViewHolder.show();
    }

    /**
     * ??????????????????
     */
    protected boolean canBackPressed() {
        if (mLiveContributeViewHolder != null && mLiveContributeViewHolder.isShowed()) {
            mLiveContributeViewHolder.hide();
            return false;
        }
        if (mLiveAddImpressViewHolder != null && mLiveAddImpressViewHolder.isShowed()) {
            mLiveAddImpressViewHolder.hide();
            return false;
        }
        if (mLiveAdminListViewHolder != null && mLiveAdminListViewHolder.isShowed()) {
            mLiveAdminListViewHolder.hide();
            return false;
        }
        if (mLiveLuckGiftTipViewHolder != null && mLiveLuckGiftTipViewHolder.isShowed()) {
            mLiveLuckGiftTipViewHolder.hide();
            return false;
        }
        return true;
    }

    /**
     * ??????????????????
     */
    public void openShareWindow() {
        LiveShareDialogFragment fragment = new LiveShareDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setActionListener(this);
        fragment.show(getSupportFragmentManager(), "LiveShareDialogFragment");
    }

    /**
     * ????????????????????????
     */
    @Override
    public void onItemClick(String type) {
        if (Constants.LINK.equals(type)) {
            copyLink();
        } else {
            shareLive(type, null);
        }
    }

    /**
     * ?????????????????????
     */
    private void copyLink() {
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean == null) {
            return;
        }
        String link = configBean.getLiveWxShareUrl() + mLiveUid;
        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", link);
        cm.setPrimaryClip(clipData);
        ToastUtil.show(WordUtil.getString(R.string.copy_success));
    }


    /**
     * ???????????????
     */
    public void shareLive(String type, MobCallback callback) {
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean == null) {
            return;
        }
        if (mMobShareUtil == null) {
            mMobShareUtil = new MobShareUtil();
        }
        ShareData data = new ShareData();
        data.setTitle(configBean.getLiveShareTitle());
        data.setDes(mLiveBean.getUserNiceName() + configBean.getLiveShareDes());
        data.setImgUrl(mLiveBean.getAvatarThumb());
        String webUrl = configBean.getDownloadApkUrl();
        if (Constants.MOB_WX.equals(type) || Constants.MOB_WX_PYQ.equals(type)) {
            webUrl = configBean.getLiveWxShareUrl() + mLiveUid;
        }
        data.setWebUrl(webUrl);
        mMobShareUtil.execute(type, data, callback);
    }

    /**
     * ????????????????????????
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (!TextUtils.isEmpty(mLiveUid) && mLiveUid.equals(e.getToUid())) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.setAttention(e.getIsAttention());
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        String unReadCount = e.getUnReadCount();
        if (!TextUtils.isEmpty(unReadCount) && mLiveBottomViewHolder != null) {
            mLiveBottomViewHolder.setUnReadCount(unReadCount);
        }
    }

    /**
     * ??????????????????????????????
     */
    protected String getImUnReadCount() {
        return ImMessageUtil.getInstance().getAllUnReadMsgCount();
    }

    /**
     * ??????????????????????????????
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCoinChangeEvent(CoinChangeEvent e) {
        onCoinChanged(e.getCoin());
        if (e.isChargeSuccess() && this instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) this).onChargeSuccess();
        }
    }

    public void onCoinChanged(String coin) {

    }

    /**
     * ??????????????????
     */
    public void openGuardListWindow() {
        LiveGuardDialogFragment fragment = new LiveGuardDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setLiveGuardInfo(mLiveGuardInfo);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putBoolean(Constants.ANCHOR, mIsAnchor);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveGuardDialogFragment");
    }

    /**
     * ???????????????????????????
     */
    public void openBuyGuardWindow() {
        if (TextUtils.isEmpty(mLiveUid) || TextUtils.isEmpty(mStream) || mLiveGuardInfo == null) {
            return;
        }
        LiveGuardBuyDialogFragment fragment = new LiveGuardBuyDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setLiveGuardInfo(mLiveGuardInfo);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.COIN_NAME, mCoinName);
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putString(Constants.STREAM, mStream);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveGuardBuyDialogFragment");
    }

    /**
     * ????????????????????????
     */
    public void openRedPackSendWindow() {
        LiveRedPackSendDialogFragment fragment = new LiveRedPackSendDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setStream(mStream);
        //fragment.setCoinName(mCoinName);
        fragment.show(getSupportFragmentManager(), "LiveRedPackSendDialogFragment");
    }

    /**
     * ???????????????????????????
     */
    public void openRedPackListWindow() {
        LiveRedPackListDialogFragment fragment = new LiveRedPackListDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setStream(mStream);
        fragment.setCoinName(mCoinName);
        fragment.show(getSupportFragmentManager(), "LiveRedPackListDialogFragment");
    }


    /**
     * ??????????????????
     */
    public void openPrizePoolWindow() {
        GiftPrizePoolFragment fragment = new GiftPrizePoolFragment();
        fragment.setLifeCycleListener(this);
        fragment.setLiveUid(mLiveUid);
        fragment.setStream(mStream);
        fragment.show(getSupportFragmentManager(), "GiftPrizePoolFragment");
    }

    /**
     * ????????????????????????
     */
    public void openLuckGiftTip() {
        if (mLiveLuckGiftTipViewHolder == null) {
            mLiveLuckGiftTipViewHolder = new LiveWebViewHolder(mContext, mPageContainer, HtmlConfig.LUCK_GIFT_TIP);
            mLiveLuckGiftTipViewHolder.subscribeActivityLifeCycle();
            mLiveLuckGiftTipViewHolder.addToParent();
        }
        mLiveLuckGiftTipViewHolder.show();
        if (CommonAppConfig.LIVE_ROOM_SCROLL && !mIsAnchor) {
            ((LiveAudienceActivity) this).setScrollFrozen(true);
        }
    }


    /**
     * ??????????????????
     */
    public void openLuckPanWindow() {
        LuckPanDialogFragment fragment = new LuckPanDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.show(getSupportFragmentManager(), "LuckPanDialogFragment");
    }


    /**
     * ??????????????????
     * @param winResultGiftBeanList
     */
    public void openLuckPanWinWindow(List<TurntableGiftBean> winResultGiftBeanList) {
        if(winResultGiftBeanList==null||winResultGiftBeanList.size()==0)
            return;

        LuckPanWinDialogFragment fragment = new LuckPanWinDialogFragment();
        fragment.setTurntableResultGiftBeans(winResultGiftBeanList);
        fragment.setLifeCycleListener(this);
        fragment.show(getSupportFragmentManager(), "LuckPanWinDialogFragment");
    }

    /**
     * ???????????? ????????????
     */
    public void openLuckPanTipWindow() {
        LuckPanTipDialogFragment fragment = new LuckPanTipDialogFragment();
        fragment.setLifeCycleListener(this);

        fragment.show(getSupportFragmentManager(), "LuckPanTipDialogFragment");
    }

    /**
     * ????????????????????????
     */
    public void openLuckPanRecordWindow() {
        LuckPanRecordDialogFragment fragment = new LuckPanRecordDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.show(getSupportFragmentManager(), "LuckPanRecordDialogFragment");
    }


    /**
     * ?????????????????????
     */
    @Override
    public void onKeyBoardHeightChanged(int visibleHeight, int keyboardHeight) {
        if (mChatRoomOpened) {//????????????????????????????????????
            if (mLiveChatRoomDialogFragment != null) {
                mLiveChatRoomDialogFragment.scrollToBottom();
            }
        } else {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.onKeyBoardChanged(visibleHeight, keyboardHeight);
            }
        }
    }

    @Override
    public boolean isSoftInputShowed() {
        if (mKeyBoardHeightUtil != null) {
            return mKeyBoardHeightUtil.isSoftInputShowed();
        }
        return false;
    }

    public void setChatRoomOpened(LiveChatRoomDialogFragment chatRoomDialogFragment, boolean chatRoomOpened) {
        mChatRoomOpened = chatRoomOpened;
        mLiveChatRoomDialogFragment = chatRoomDialogFragment;
    }

    /**
     * ??????????????????
     */
    public boolean isGamePlaying() {
        return mGamePlaying;
    }

    public void setGamePlaying(boolean gamePlaying) {
        mGamePlaying = gamePlaying;
    }

    /**
     * ??????????????????
     */
    public boolean isLinkMic() {
        return mLiveLinkMicPresenter != null && mLiveLinkMicPresenter.isLinkMic();
    }

    /**
     * ????????????????????????
     */
    public boolean isLinkMicAnchor() {
        return mLiveLinkMicAnchorPresenter != null && mLiveLinkMicAnchorPresenter.isLinkMic();
    }


    @Override
    protected void onPause() {
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.pause();
        }
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.resume();
        }
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.resume();
        }
    }

    protected void release() {
        EventBus.getDefault().unregister(this);
        LiveHttpUtil.cancel(LiveHttpConsts.SEND_DANMU);
        if (mKeyBoardHeightUtil != null) {
            mKeyBoardHeightUtil.release();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.release();
        }
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.release();
        }
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.release();
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.release();
        }
        if (mLiveAddImpressViewHolder != null) {
            mLiveAddImpressViewHolder.release();
        }
        if (mLiveContributeViewHolder != null) {
            mLiveContributeViewHolder.release();
        }
        if (mLiveLuckGiftTipViewHolder != null) {
            mLiveLuckGiftTipViewHolder.release();
        }
        if (mMobShareUtil != null) {
            mMobShareUtil.release();
        }
        if (mImageUtil != null) {
            mImageUtil.release();
        }

        mKeyBoardHeightUtil = null;
        mLiveLinkMicPresenter = null;
        mLiveLinkMicAnchorPresenter = null;
        mLiveLinkMicPkPresenter = null;
        mLiveRoomViewHolder = null;
        mLiveAddImpressViewHolder = null;
        mLiveContributeViewHolder = null;
        mLiveLuckGiftTipViewHolder = null;
        mMobShareUtil = null;
        mImageUtil = null;
        L.e("LiveActivity--------release------>");
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    public String getStream() {
        return mStream;
    }

    public String getTxAppId() {
        return mTxAppId;
    }
}
