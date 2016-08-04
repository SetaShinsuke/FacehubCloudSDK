package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException.ErrorType.mock_http_error;

/**
 * Created by SETA_WORK on 2016/8/2.
 */
public class MockClient {
    AsyncHttpClient client = new AsyncHttpClient();
    String host = "";
    private boolean checkHost = true;

    public MockClient(String host) {
        this.host = host;
    }

    private boolean isOffLineMode() {
        return FacehubApi.getApi().isOfflineMode();
    }

    public void setCheckHost(boolean checkHost) {
        this.checkHost = checkHost;
    }

    /**
     * Get
     */
    public RequestHandle get(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        return client.get(null, url, params, responseHandler);
    }

    /**
     * Get主要是下载
     */
    public RequestHandle get(String url, ResponseHandlerInterface responseHandler) {
        return client.get(url, responseHandler);
    }

    /**
     * Put
     */
    public void put(Context context, String url, HttpEntity entity, String contentType, JsonHttpResponseHandler responseHandler) {
        if (!isOffLineMode()) {
            client.put(context, url, entity, contentType, responseHandler);
            return;
        }
        //判断url是否有误
        if (!isUrlAvailable(url)) {
            onFail(new FacehubSDKException(mock_http_error, "Url error!"), responseHandler);
            return;
        }
        String[] urlParts = url.split("/");
        int length = urlParts.length;
        if (url.startsWith(host + "/api/v1/users/")
                && url.endsWith("/lists")) {
            reorderUserLists(entity,responseHandler);
            return;
        }
        if(url.startsWith(host + "/api/v1/users/")
                && urlParts[length-2].equals("lists")){
            editList(urlParts[length-1],entity,responseHandler);
            client.put(context,url,entity,contentType,responseHandler);
            return;
        }
    }

    /**
     * Post
     */
    public RequestHandle post(Context context, String url, HttpEntity entity, String contentType, ResponseHandlerInterface responseHandler) {
        return client.post(context, url, entity, contentType, responseHandler);
    }

    /**
     * Delete
     */
    public void delete(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.delete(url, params, responseHandler);
    }

    private boolean isUrlAvailable(String url) {
        if (!checkHost) {
            return true;
        }
        return !(url == null || !url.startsWith(host));
    }


    /**
     * ================================= PUT =================================
     */
    //列表排序
    private void reorderUserLists(HttpEntity entity,JsonHttpResponseHandler responseHandler) {
        try {
            JSONObject queryJson = new JSONObject(EntityUtils.toString(entity));
            JSONArray listsJsonArray = queryJson.getJSONArray("contents");

            JSONObject resultJson = new JSONObject();
            JSONObject userJsonObject  = new JSONObject();
            resultJson.put("user",userJsonObject);
            JSONArray resultJsonArray = new JSONArray();
            userJsonObject.put("contents",resultJsonArray);
            for(int i=0;i<listsJsonArray.length();i++){
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("id",listsJsonArray.get(i));
                resultJsonArray.put(jsonObject1);
            }
            onSuccess(resultJson,responseHandler);
        } catch (Exception e) {
            FacehubSDKException exception = new FacehubSDKException(mock_http_error, "Mock http error : " + e);
            onFail(exception, responseHandler);
        }
    }
    //列表删除/重命名
    //表情添加/替换/删除
    private void editList(String listId,HttpEntity entity,JsonHttpResponseHandler responseHandler){
        try {
            UserList userList = FacehubApi.getApi().getUser().getUserListById(listId);
            JSONObject queryJson = new JSONObject(EntityUtils.toString(entity));
            JSONArray queryEmoticonsJsonArray = queryJson.getJSONArray("contents");
            String action = queryJson.getString("action");
            JSONObject resultJson = new JSONObject();
            switch (action){
                case "add": //添加表情
//                    resultJson.put("list",userList.toJson());
                    break;
            }
        } catch (Exception e) {
            LogX.e("e : " + e);
        }
    }



    private void onSuccess(JSONObject resultJson, JsonHttpResponseHandler responseHandler){
        responseHandler.onSuccess(200,null,resultJson);
    }

    private void onFail(Throwable throwable, JsonHttpResponseHandler responseHandler) {
        responseHandler.onFailure(0, null, throwable, new JSONObject());
    }
}
