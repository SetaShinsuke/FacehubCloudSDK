package com.azusasoft.facehubcloudsdk.api.utils;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;

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

    private boolean isOffLineMode(){
        return FacehubApi.getApi().isOfflineMode();
    }

    public void setCheckHost(boolean checkHost){
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
        if(!isOffLineMode()) {
            client.put(context, url, entity, contentType, responseHandler);
            return;
        }
        if(!isUrlAvalable(url)){
            responseHandler.onFailure(0,null,new Throwable("Url error!"),new JSONObject());
            return;
        }
//        if(url.startsWith(host+))
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

    private boolean isUrlAvalable(String url){
        if(!checkHost){
            return true;
        }
        return !(url == null || !url.startsWith(host));
    }


    /**
     * ================================= PUT =================================
     */
    private void reorderUserLists() {

    }


//    private String entity2String(HttpEntity entity) {
//        String inputLine = "";
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new InputStreamReader(entity.getContent()));
//            int i = 0;
//            while ((inputLine = br.readLine()) != null) {
//                LogX.fastLog("input line-" + i + " : " + inputLine);
//            }
//            br.close();
//        } catch (IOException e){
//            LogX.e("Entity to String error : " + e);
//            e.printStackTrace();
//        }
//        LogX.fastLog("Input line RESULT : " + inputLine);
//        return inputLine;
//    }
}
