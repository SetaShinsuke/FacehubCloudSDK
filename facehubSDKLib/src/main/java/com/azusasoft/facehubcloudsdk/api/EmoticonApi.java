package com.azusasoft.facehubcloudsdk.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.models.MockClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.HOST;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * Emoticon操作接口
 */
public class EmoticonApi {
//    private AsyncHttpClient client;
    private MockClient client;

     EmoticonApi(MockClient client) {
        this.client = client;
    }

    /**
     * 通过表情唯一标识向服务器请求表情资源;
     *
     * @param emoticonId             表情包唯一标识;
     * @param resultHandlerInterface 结果回调,返回一个 {@link Emoticon} 对象;
     */
    void getEmoticonById(User user , final String emoticonId, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/emoticons/" + emoticonId;
        LogX.dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("emoticon");
                    Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoticonId);
                    emoticon.updateField(jsonObject);
                    resultHandlerInterface.onResponse(emoticon);
                } catch (Exception e) {
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    public void uploadEmoticon(final User user , final String filePath , final String userListId
                                , final ResultHandlerInterface resultHandlerInterface){

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if(width > 600 || height > 600){
            FacehubSDKException uploadException = new FacehubSDKException(FacehubSDKException.ErrorType.upload_oversize
            ,"图片长/宽超过了600px");
            resultHandlerInterface.onError(uploadException);
            return;
        }
        int byteCount = bitmap.getRowBytes() * height;
        if(byteCount > 2*1000*1000){
            FacehubSDKException uploadException = new FacehubSDKException(FacehubSDKException.ErrorType.upload_oversize
                    ,"图片大小超过2M");
            resultHandlerInterface.onError(uploadException);
            return;
        }

        getUploadToken(user, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                final String token = (String) response;
                LogX.i("获取上传token成功.");
                LogX.v("token : " + token);
                File myFile = new File(filePath);
                RequestParams params = new RequestParams();
                try {
                    params.put("file", myFile);
                    params.add("token", token);
                    params.add("x:auth_token",user.getToken());
                    params.add("x:user_id", user.getUserId());
                    params.add("x:app_id",FacehubApi.appId);
                    params.add("x:list_id", userListId);
                    client.post(FacehubApi.UPLOADHOST, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                LogX.i("上传完成! response : " + response);
                                JSONObject emoticonJson = response.getJSONObject("emoticon");
                                String emoticonId = emoticonJson.getString("id");
                                final Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoticonId);
                                emoticon.updateField(emoticonJson);
                                FacehubApi.getApi().getEmoticonContainer().updateEmoticons2DB(emoticon);
                                ArrayList<Emoticon> emoticons = user.getDefaultFavorList().getEmoticons();
                                if(emoticons.contains(emoticon)){
                                    emoticons.remove(emoticon);
                                }
                                emoticons.add(emoticon);
                                user.updateLists();

                                emoticon.download2File(true, new ResultHandlerInterface() {
                                    @Override
                                    public void onResponse(Object response) {
                                        EmoticonCollectEvent event = new EmoticonCollectEvent();
                                        EventBus.getDefault().post(event);
                                        resultHandlerInterface.onResponse(emoticon);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        EmoticonCollectEvent event = new EmoticonCollectEvent();
                                        EventBus.getDefault().post(event);
                                        resultHandlerInterface.onResponse(emoticon);
                                    }
                                });
                            } catch (Exception e) {
                                resultHandlerInterface.onError(e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                            onFail(statusCode, throwable, responseString);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            onFail(statusCode, throwable, errorResponse);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            onFail(statusCode, throwable, errorResponse);
                        }

                        //打印错误信息
                        private void onFail(int statusCode, Throwable throwable, Object addition) {
                            resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
                        }
                    });
                }catch (Exception e){
                    FacehubSDKException exception = new FacehubSDKException(FacehubSDKException.ErrorType.upload_error
                            ,"上传:token获取成功,但 上传表情出错 : " + e);
                    resultHandlerInterface.onError(exception);
                }
            }

            @Override
            public void onError(Exception e) {
                FacehubSDKException exception = new FacehubSDKException(FacehubSDKException.ErrorType.upload_error
                    ,"token获取出错 : " + e);
                resultHandlerInterface.onError(exception);
            }
        });
    }

    private void getUploadToken(User user , final ResultHandlerInterface resultHandlerInterface){
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/emoticons/upload-token";
        LogX.dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String token = response.getString("token");
                    resultHandlerInterface.onResponse(token);
                } catch (Exception e) {
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    /**
     * 检查本地是否已收藏该表情;
     *
     * @param emoticonId 表情唯一标识;
     * @return 是否已收藏;
     */
    boolean isEmoticonCollected(String emoticonId) {
        //TODO:检查本地是否已收藏:比较耗时?
        CodeTimer codeTimer = new CodeTimer();
        codeTimer.start("检查是否收藏 findAll");
        ArrayList<UserList> allLists = FacehubApi.getApi().getUser().getUserLists();
        codeTimer.end("检查是否收藏 findAll");
//        for(UserList userList:allLists){ //所有个人列表
//            ArrayList<Emoticon> emoticons = userList.getEmoticons();
//            for(Emoticon emoticon:emoticons){ //列表内所有表情
//                if(emoticon.getId().equals(emoticonId)){
//                    return true;
//                }
//            }
//        }


        codeTimer.start("遍历emoticons");
        if (allLists.size() > 0) {
            ArrayList<Emoticon> emoticons = allLists.get(0).getEmoticons();
            for (int i = 0; i < emoticons.size(); i++) { //列表内所有表情
                if (emoticons.get(i).getId().equals(emoticonId)) {
                    return true;
                }
            }
        }
        codeTimer.end("遍历emoticons");
        return false;
    }
}
