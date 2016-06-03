package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.HOST;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * Emoticon操作接口
 */
public class EmoticonApi {
    private AsyncHttpClient client;

     EmoticonApi( AsyncHttpClient client) {
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
                } catch (JSONException e) {
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
