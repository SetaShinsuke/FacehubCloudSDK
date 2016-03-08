package com.azusasoft.facehubcloudsdk.api.utils;

/**
 * Created by SETA on 2016/3/8.
 */
public class UtilMethods {

    public static Exception parseHttpError( int statusCode , Throwable throwable){
        String msg = "Http Error!Status code : " + statusCode + "\nDetail : " + throwable;
        Exception exception = new Exception( msg );
        LogX.fastLog( exception + "" );
        return exception;
    }
}
