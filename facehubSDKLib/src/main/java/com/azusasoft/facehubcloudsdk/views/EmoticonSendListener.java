package com.azusasoft.facehubcloudsdk.views;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;

/**
 * Created by SETA on 2016/3/16.
 * 表情键盘点击发送的接口
 * {@link #onSend(Emoticon)}回调中包含一个{@link Emoticon}对象
 *              其中{@link Emoticon#isLocal()}用来区分是否本地表情;
 */
public interface EmoticonSendListener{
    public void onSend(Emoticon emoticon);
}