package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by SETA on 2016/3/10.
 */
public class RetryReq {
    public static final String RETRY = "retry_req";
    public static final String REMOVE_EMO = "remove_emo";
    public static final String REMOVE_LIST = "remove_list";

    private String type = REMOVE_EMO;
    private String params;
    private Context context;

    public RetryReq(Context context){
        this.context = context;
    }

//    private void save(String type,String params){
//        SharedPreferences sharedPreferences = context.getSharedPreferences( RETRY , Context.MODE_PRIVATE );
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.put
//        editor.apply();
//    }

}
