package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by SETA on 2016/3/8.
 */
public class User {
    private final String USER = "facehub_sdk_user";
    private final String UPDATE_AT = "facehub_sdk_updated_at";
    private final String USER_ID = "facehub_sdk_user_id";
    private final String TOKEN = "facehub_sdk_auth_token";
    private String userId="";
    private String token="";
    private Context context;



    private String updated_at="";
    private boolean modified;
    private ArrayList<UserList> userLists=new ArrayList<>();

    public User(Context context){
        this.context = context;
        this.modified=false;
    }

    //设置用户token
    public void setToken(String token){
        this.token = token;
    }

    //设置当前用户id & token
    public void setUserId(String userId , String token){

    }

    public boolean restore(){
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        if( !preferences.contains(USER_ID)){
            return false;
        }
        this.userId = preferences.getString(USER_ID,"");
        this.token = preferences.getString(TOKEN,"");
        this.updated_at = preferences.getString(UPDATE_AT,"");
        return true;
    }

    public void logout(){
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove( USER_ID );
        editor.remove( TOKEN );
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
    public JSONObject getParamsJson(){
        JSONObject params = new JSONObject();
        try {
            params.put("user_id" , this.userId);
            params.put("auth_token" , this.token);
            params.put("app_id" , FacehubApi.appId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return params;
    }

    public void setUpdated_at(String updated_at) {
        modified= ! this.updated_at.equals(updated_at);
        this.updated_at = updated_at;
    }
    public String getUpdated_at() {
        return updated_at;
    }

    public boolean isModified() {
        return modified;
    }

    public void setUserInfo(String userId, String token, String updated_at) {
        this.userId = userId;
        setToken( token );
        setUpdated_at(updated_at);
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString( USER_ID , userId );
        editor.putString( TOKEN , token);
        editor.putString(UPDATE_AT,updated_at);
        editor.apply();
    }

    public void setUserLists(ArrayList<UserList> userLists) {
        this.userLists = userLists;
    }
}
