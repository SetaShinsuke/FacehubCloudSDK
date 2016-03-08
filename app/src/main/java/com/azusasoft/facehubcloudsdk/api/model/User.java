package com.azusasoft.facehubcloudsdk.api.model;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.loopj.android.http.RequestParams;

/**
 * Created by SETA on 2016/3/8.
 */
public class User {
    private String userId="";
    private String token="";

    //设置用户token
    public void setToken(String token){
        this.token = token;
    }

    //设置当前用户id & token
    public void setUserId(String userId , String token){
        this.userId = userId;
        setToken( token );
        //TODO:存储User到 SharedPreference
    }

    public RequestParams getParams(){
        RequestParams params = new RequestParams();
        params.put("user_id" , this.userId);
        params.put("auth_token" , this.token);
        params.put("app_id" , FacehubApi.appId);
        return params;
    }


}
