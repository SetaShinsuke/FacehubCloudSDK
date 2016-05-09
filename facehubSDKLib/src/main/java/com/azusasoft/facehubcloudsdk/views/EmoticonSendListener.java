package com.azusasoft.facehubcloudsdk.views;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;

/**
 * Created by SETA on 2016/3/16.
 * 表情键盘点击发送的接口
 * {@link #onSend(Emoticon)}回调中包含一个{@link Emoticon}对象;
 */
public interface EmoticonSendListener{
    public void onSend(Emoticon emoticon);
}