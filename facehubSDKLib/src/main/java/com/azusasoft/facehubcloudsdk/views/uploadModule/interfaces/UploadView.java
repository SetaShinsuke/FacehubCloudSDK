package com.azusasoft.facehubcloudsdk.views.uploadModule.interfaces;

/**
 * Created by SETA_WORK on 2016/9/6.
 */
public interface UploadView {
    void onUploadStart();
    void onPicSelected();
    void onUploadFinish(String resultType);
}
