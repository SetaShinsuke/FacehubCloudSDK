package com.azusasoft.facehubcloudsdk.api.utils;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.RequestParams;

/**
 * Created by SETA on 2016/3/8.
 * Custom Logging class.
 */
public class LogX {
    private static org.apache.log4j.Logger logger=null;
    public static int logLevel = Log.VERBOSE;
    public static final String TAG_LOGX = "facehub_cloud";
    public static final String EMO_LOGX = "emoticon";
    public static final String LIST_LOGX = "user_list";
    public static final String TOUCH_LOGX = "touch";

    public static void init(Context context){
        LogConfig.configure(context);
        logger = org.apache.log4j.Logger.getLogger("Log");
        logger.trace("Log Init");
    }

    public static void fastLog( String s){
        LogX.v(TAG_LOGX, "" + s );
    }

    public static void fastLog(String content,Object... args){
        try {
            fastLog(String.format(content,args));
        }catch (Exception e){
            fastLog(content);
        }
    }

    public static void v(String s){
        v(TAG_LOGX,s);
    }
    public static void v(String tag , String s){
        if(logLevel<=Log.VERBOSE) {
            Log.v(tag , s);
        }
    }

    /** Log分级
     * @param s
     */
    public static void e( String s){
        e(TAG_LOGX, s);
    }
    public static void e( String tag , String s){
        if(logLevel<=Log.ERROR) {
            Log.e(tag , s);
            trace2File(tag,s);
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
        i(TAG_LOGX, s );
    }
    public static void i( String tag , String s){
        if(logLevel<=Log.INFO) {
            Log.i(tag, s);
            trace2File(tag,s);
        }
    }

    public static void w( String s){
        w(TAG_LOGX, s);
    }
    public static void w( String tag , String s){
        if(logLevel<=Log.WARN) {
            Log.w(tag, s);
            trace2File(tag,s);
        }
    }

    public static void dumpReq(String url , RequestParams params){
//        fastLog("url : " + url + "?" + params);
    }

    private static void trace2File(String tag,String msg){
        if(logger!=null) {
            logger.trace(tag + " " + msg);
        }
    }

    private static final String TTag = "tmp";
    public static void tLog(String msg){
        v(TTag,msg);
    }
}
