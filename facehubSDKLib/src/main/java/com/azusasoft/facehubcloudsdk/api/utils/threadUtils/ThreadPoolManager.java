package com.azusasoft.facehubcloudsdk.api.utils.threadUtils;

/**
 * Created by SETA on 2016/4/14.
 */
public class ThreadPoolManager {
    private static ThreadPoolProxy downloadThreadPool;
    private static ThreadPoolProxy dbThreadPool;

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

    public static ThreadPoolProxy getDbThreadPool(){
        if(dbThreadPool==null){
            synchronized (ThreadPoolManager.class){
                if(dbThreadPool==null){
                    dbThreadPool = new ThreadPoolProxy(1,1,2000);
                }
            }
        }
        return dbThreadPool;
    }
}
