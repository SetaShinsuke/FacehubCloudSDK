package com.azusasoft.facehubcloudsdk.outOfSdk;

import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 */
public class JsonHttpHandlerExample {
    private ResultHandlerInterface resultHandlerInterface;

    public JsonHttpHandlerExample(){
        Object o = new JsonHttpResponseHandler(){

            //region 事例代码
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                resultHandlerInterface.onResponse( response );
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
            //endregion


        };
    }
}
