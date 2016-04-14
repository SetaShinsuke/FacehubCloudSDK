package com.azusasoft.facehubcloudsdk.api.utils.threadUtils;

/**
 * Created by SETA on 2016/4/14.
 */
public class ThreadPoolManager {
    private static ThreadPoolProxy downloadThreadPool;

    public static ThreadPoolProxy getDownloadThreadPool(){
        if(downloadThreadPool==null){
            synchronized (ThreadPoolManager.class){
                if(downloadThreadPool==null){
                    downloadThreadPool = new ThreadPoolProxy(5,5,2000);
                }
            }
        }
        return downloadThreadPool;
    }
}
