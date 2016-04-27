package com.azusasoft.facehubcloudsdk.api.utils;

import android.util.Log;

import com.loopj.android.http.RequestParams;

/**
 * Created by SETA on 2016/3/8.
 * Custom Logging class.
 */
public class LogX {
    public static int logLevel = Log.VERBOSE;
    public static final String TAG_LOGX = "facehub_cloud";
    public static final String EMO_LOGX = "emoticon";
    public static final String LIST_LOGX = "user_list";
    public static final String TOUCH_LOGX = "touch";

    public static void fastLog( String s){
        Log.v(TAG_LOGX, "" + s );
    }

    public static void fastLog(String content,Object... args){
        try {
            fastLog(String.format(content,args));
        }catch (Exception e){
            fastLog(content);
        }
    }

    //TODO:Log分级
    public static void e( String s){
        e(TAG_LOGX, s);
    }
    public static void e( String tag , String s){
        if(logLevel<=Log.ERROR) {
            Log.e(tag , s);
        }
    }

    public static void d( String s){
        d(TAG_LOGX, s);
    }
    public static void d( String tag , String s){
        if(logLevel<=Log.DEBUG) {
            Log.d(tag, s);
        }
    }

    public static void i( String s){
        d(TAG_LOGX, s );
    }
    public static void i( String tag , String s){
        if(logLevel<=Log.INFO) {
            Log.i(tag, s);
        }
    }

    public static void w( String s){
        d(TAG_LOGX, s);
    }
    public static void w( String tag , String s){
        if(logLevel<=Log.WARN) {
            Log.w(tag, s);
        }
    }

    public static void dumpReq(String url , RequestParams params){
        fastLog("url : " + url + "?" + params);
    }
}
