package com.azusasoft.facehubcloudsdk.views.uploacModule.presenters;

import android.content.Context;
import android.content.Intent;

/**
 * Created by SETA_WORK on 2016/9/6.
 */
public interface UploadEmoPresenter {
    void startUpload();
    boolean handleUploadIntent(Context context , int requestCode , int resultCode , Intent intent);
    void finishUpload(String resultType);
}
