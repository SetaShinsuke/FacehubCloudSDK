package com.azusasoft.facehubcloudsdk.api.utils.threadUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by SETA on 2016/4/14.
 */
public class ThreadPoolProxy {
    private ThreadPoolExecutor mExecutor = null;

    private int corePoolSize,maximumPoolSize;
    private long keepAliveTime;

    public ThreadPoolProxy(int corePoolSize , int maximumPoolSize , long keepAliveTime){
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
    }

    //双重加锁(只有在第一次实例化的时候才会弃用同步机制，提高性能)
    public void initThreadPoolExecutor(){
        if(mExecutor==null || mExecutor.isShutdown() || mExecutor.isTerminated()){
            synchronized (ThreadPoolProxy.class){
                if(mExecutor==null || mExecutor.isShutdown() || mExecutor.isTerminated()){
                    TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
                    mExecutor = new ThreadPoolExecutor(
                            corePoolSize, //核心线程数
                            maximumPoolSize, //最大线程数
                            keepAliveTime, //保持时间
                            timeUnit, //保持时间单位
                            workQueue, //工作队列
                            threadFactory, //线程工厂
                            handler //异常回调
                    );
                }
            }
        }
    }

    /**提交任务*/
    public Future<?> submit(Runnable task){
        initThreadPoolExecutor();
        return mExecutor.submit(task);
    }
    /**执行任务*/
    public void execute(Runnable task){
        initThreadPoolExecutor();
        mExecutor.execute(task);
    }
    /**移除任务*/
    public void remove(Runnable task){
        initThreadPoolExecutor();
        mExecutor.remove(task);
    }
}
