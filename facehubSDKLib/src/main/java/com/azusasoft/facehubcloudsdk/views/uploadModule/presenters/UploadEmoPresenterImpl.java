package com.azusasoft.facehubcloudsdk.views.uploadModule.presenters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.azusasoft.facehubcloudsdk.activities.BaseActivity;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.uploadModule.interfaces.UploadView;

import static com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException.ErrorType.upload_error;
import static com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException.ErrorType.upload_oversize;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOAD_FAIL;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOAD_OVERSIZE;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOAD_SUCCESS;

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
    public boolean handleUploadIntent(Context context ,int requestCode , int resultCode , Intent data) {
        String picturePath = null;
        if (requestCode == Constants.RESULT_LOAD_IMAGE
                && resultCode == BaseActivity.RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = context.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if(cursor == null){
                return false;
            }
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        }
        if(picturePath == null){ //未选择图片:取消
            return true;
        }


        //选好图片，开始Loading
        uploadView.onPicSelected();

        //TODO:检查图片尺寸与大小

        //调用上传接口
        String userListId = FacehubApi.getApi().getUser().getDefaultFavorList().getId();
        LogX.fastLog("上传图片 : " + picturePath);
        FacehubApi.getApi().uploadEmoticon(picturePath, userListId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                finishUpload(UPLOAD_SUCCESS);
            }

            @Override
            public void onError(Exception e) {
                if(e instanceof FacehubSDKException){
                    FacehubSDKException uploadException = (FacehubSDKException)e;
                    if(uploadException.getErrorType() == upload_error){
                        finishUpload(UPLOAD_FAIL);
                    }else if(uploadException.getErrorType() == upload_oversize){
                        finishUpload(UPLOAD_OVERSIZE);
                    }else {
                        finishUpload(UPLOAD_FAIL);
                    }
                }else {
                    finishUpload(UPLOAD_FAIL);
                }
            }
        });
        return true;
    }

    @Override
    public void finishUpload(String resultType) {
        uploadView.onUploadFinish(resultType);
    }
}
