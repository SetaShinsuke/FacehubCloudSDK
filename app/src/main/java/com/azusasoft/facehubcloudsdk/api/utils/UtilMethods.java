package com.azusasoft.facehubcloudsdk.api.utils;

import android.support.annotation.Nullable;

import com.loopj.android.http.RequestParams;

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
}
