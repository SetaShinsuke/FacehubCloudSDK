package com.azusasoft.facehubcloudsdk.views.uploacModule.presenters;

import android.content.Intent;

/**
 * Created by SETA_WORK on 2016/9/6.
 */
public interface UploadEmoPresenter {
    void startUpload();
    boolean handleUploadIntent(Intent intent);
    void finishUpload(boolean isSuccess);
}
