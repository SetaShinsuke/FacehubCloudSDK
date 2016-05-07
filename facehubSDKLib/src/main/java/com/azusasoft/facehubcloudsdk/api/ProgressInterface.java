package com.azusasoft.facehubcloudsdk.api;

/**
 * Created by ilike on 16/4/28.
 * 进度变化的接口
 */
public interface ProgressInterface {
    /**
     * 进度变化时
     * @param process 具体的进度值 0<=process<=100，需要配合{@link ResultHandlerInterface}确定成功还是失败
     */
    void onProgress(double process);
}
