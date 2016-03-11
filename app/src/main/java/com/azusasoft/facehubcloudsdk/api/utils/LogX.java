package com.azusasoft.facehubcloudsdk.api.utils;

import android.util.Log;

import com.loopj.android.http.RequestParams;

import java.util.logging.Level;

/**
 * Created by SETA on 2016/3/8.
 */
public class LogX {
    public static int logLevel = Log.VERBOSE;
    public static final String LOGX_TAG = "facehub_cloud";

    public static void fastLog( String s){
        Log.v( LOGX_TAG, "" + s );
    }

    //TODO:Log分级
    public static void e( String s){
        if(logLevel>=Log.ERROR) {
            Log.e(LOGX_TAG, s);
        }
    }

    public static void dumpReq(String url , RequestParams params){
        fastLog("url : " + url + "?" + params);
    }
}