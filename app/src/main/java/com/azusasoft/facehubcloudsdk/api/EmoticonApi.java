package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.EmoticonDAO;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.HOST;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 */
public class EmoticonApi {
    private User user;
    private AsyncHttpClient client;

    public EmoticonApi(User user,AsyncHttpClient client){
        this.user = user;
        this.client = client;
    }

    /**
     * 通过表情唯一标识向服务器请求表情资源
     *
     * @param emoticonId             表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    public void getEmoticonById(String emoticonId,final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        String url = HOST + "/api/v1/emoticons/" + emoticonId;
        LogX.dumpReq( url , params );
        client.get(url , params , new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("emoticon");
                    Emoticon emoticon = new Emoticon();
                    emoticon.emoticonFactoryByJson( jsonObject , true );
                    resultHandlerInterface.onResponse( emoticon );
                } catch (JSONException e) {
                    resultHandlerInterface.onError( e );
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail( statusCode , throwable , responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail( statusCode , throwable , errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail( statusCode , throwable , errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode , Throwable throwable , Object addition){
                resultHandlerInterface.onError( parseHttpError( statusCode , throwable , addition) );
            }
        });
    }

    /**
     * 检查本地是否已收藏该表情
     *
     * @param emoticonId 表情唯一标识
     * @return 是否已收藏
     */
    public boolean isEmoticonCollected(String emoticonId) {
        //TODO:检查本地是否已收藏
        return EmoticonDAO.isCollected( emoticonId );
    }
}
