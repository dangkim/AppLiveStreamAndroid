package com.iubgdfy.im.event;


import com.iubgdfy.im.bean.ImUserBean;

/**
 * Created by cxf on 2018/7/20.
 * IM漫游消息 事件
 */

public class ImRoamMsgEvent {

    private ImUserBean mBean;

    public ImRoamMsgEvent(ImUserBean bean){
        mBean=bean;
    }

}
