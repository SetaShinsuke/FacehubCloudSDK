package com.azusasoft.facehubcloudsdk.views.uploacModule.presenters;

import android.content.Intent;
import android.os.Bundle;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.uploacModule.interfaces.UploadView;

/**
 * Created by SETA_WORK on 2016/9/6.
 */
public class UploadEmoPresenterImpl implements UploadEmoPresenter {
    private UploadView uploadView;

    public UploadEmoPresenterImpl(UploadView uploadView){
        this.uploadView = uploadView;
    }

    @Override
    public void startUpload() {
        //更改视图
        uploadView.onUploadStart();
        //开始上传的逻辑(此处放在activity里进行了)
    }

    @Override
    public boolean handleUploadIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null || action == null) {
            return false;
        }
        if (!Intent.ACTION_SEND.equals(action)) {
            LogX.i(getClass().getName() + " 不是 ACTION_SEND 且不是 ACTION_SEND_MULTIPLE.");
            return false;
        }

        //调用上传接口
        String emoticonId = "";
        String userListId = "";
        FacehubApi.getApi().uploadEmoticon(emoticonId, userListId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                finishUpload(true);
            }

            @Override
            public void onError(Exception e) {
                finishUpload(false);
            }
        });
        return true;
    }

    @Override
    public void finishUpload(boolean isSuccess) {
        uploadView.onUploadFinish(isSuccess);
    }
}
