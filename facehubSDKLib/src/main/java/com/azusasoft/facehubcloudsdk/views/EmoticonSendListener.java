package com.azusasoft.facehubcloudsdk.views;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;

public interface EmoticonSendListener{
    public void onSend(Emoticon emoticon);
}