package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.loopj.android.http.RequestParams;

/**
 * Created by SETA on 2016/3/8.
 */
public class User {
    private final String USER = "user";
    private final String USER_ID = "user_id";
    private final String TOKEN = "auth_token";
    private String userId="";
    private String token="";
    private Context context;

    public User(Context context){
        this.context = context;
    }

    //设置用户token
    public void setToken(String token){
        this.token = token;
    }

    //设置当前用户id & token
    public void setUserId(String userId , String token){
        this.userId = userId;
        setToken( token );
        //TODO:存储User到 SharedPreference
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString( USER_ID , userId );
        editor.putString( TOKEN , token);
        editor.apply();
    }

    public String getUserId(){
        return this.userId;
    }

    public RequestParams getParams(){
        RequestParams params = new RequestParams();
        params.put("user_id" , this.userId);
        params.put("auth_token" , this.token);
        params.put("app_id" , FacehubApi.appId);
        return params;
    }


}
