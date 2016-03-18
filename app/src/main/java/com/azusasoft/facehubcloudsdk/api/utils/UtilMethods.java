package com.azusasoft.facehubcloudsdk.api.utils;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.WindowManager;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SETA on 2016/3/8.
 */
public class UtilMethods {

    /**
     * 将http请求返回的错误throwable整合为Exception
     *
     * @param statusCode 返回码
     * @param throwable 异常
     * @param addition  其他异常信息
     * @return  整合后的异常
     */
    public static Exception parseHttpError( int statusCode , Throwable throwable , Object addition){
        String additionStr = "\nOther messages : ";
        try {
            additionStr += addition.toString();
        }catch (Exception e){
            additionStr += "Null.";
        }
        String msg = "Http Error!Status code : " + statusCode + "\nDetail : " + throwable + additionStr;
        Exception exception = new Exception( msg );
        LogX.fastLog( exception + "" );
        return exception;
    }

    // 讲自定义String参数param添加到params中
    public static void addString2Params(RequestParams params , String paramStr){
        try {
            String[] tmp = paramStr.split("&"); //划分为"key=value"字符串的数组
            for(String tmpKV:tmp){
                LogX.fastLog( "#\n" + tmpKV );
                String[] keyValue = tmpKV.split("=");   //每个"key=value"划分成key和value
                params.put( keyValue[0] , keyValue[1]);
            }
        }catch (Exception e){
            LogX.e("Error when adding String to Params : " + e);
        }
    }

    /**
     * Get String from a JsonObject.出错则返回null
     *
     * @param jsonObject JSON对象
     * @param key key值
     * @return 想要的String或null
     */
    public static String getStringFromJson(JSONObject jsonObject,String key){
        try {
            if( jsonObject.has(key) && (jsonObject.get(key) instanceof String) ){
                return jsonObject.getString( key );
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isJsonWithKey(JSONObject jsonObject , String key){
        try {
            return jsonObject.has(key) && !jsonObject.get(key).equals(null);
        }catch (JSONException e){
            return false;
        }
    }

}
