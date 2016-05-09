package com.azusasoft.facehubcloudsdk.api;

/**
 * 基本的回调接口,成功时回调一个{@link Object}对象，失败时回调一个{@link Exception}异常;
 */
public interface ResultHandlerInterface {
    void onResponse(Object response);

    void onError(Exception e);
}
