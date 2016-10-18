package com.azusasoft.facehubcloudsdk.api;

/**
 * Created by SETA_WORK on 2016/10/18.
 */

public interface CacheApiInterface {
    void getCacheSize(ResultHandlerInterface resultHandlerInterface);
    void clearCache(ResultHandlerInterface resultHandlerInterface,ProgressInterface progressInterface);
}
