package com.azusasoft.facehubcloudsdk.api.utils;


import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.threadUtils.ThreadPoolManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.SyncHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.d;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by ilike on 15/3/3.
 */
public class DownloadService {
//    static String SDCARD= Environment.getExternalStorageDirectory().getAbsolutePath();
//    static File DIR;
    static AsyncHttpClient client = new AsyncHttpClient();
//    static AsyncHttpClient client = new SyncHttpClient();
    static Handler handler = new Handler();
    static Queue<Task> downloading = new LinkedBlockingQueue<>();
    final static int MAX=10;
    final static int MAXRETRY=3;
    static int running = 0;
    private static boolean checkDir(File DIR){
        boolean result = true;
        if(DIR==null){
            return false;
        }
        if(!DIR.exists()){
            result =  DIR.mkdirs();
            fastLog("DIR : " + DIR.getPath());
        }
        return result;
    }

//    public static void setDIR(File file){
//        DIR = file;
//    }

    public static void download(String url,final File dir , final String path, final ResultHandlerInterface resultHandler){
        Task task = new Task(url,dir,path,0,resultHandler);
        downloading.add(task);
        next();
    }
    private  static void next(){
        while(!downloading.isEmpty()&&running<=MAX ){
            running+=1;
            final Task t=downloading.remove();
            down(t.url, t.dir, t.path, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    t.handler.onResponse(response);
                }

                @Override
                public void onError(Exception e) {
                    if(t.retry<=MAXRETRY){
                        t.retry+=1;
                        downloading.add(t);
                    }else{
                        t.handler.onError(e);
                    }
                }
            });
        }
    }
    /**
     * 下载函数
     *
     * @param url 下载地址
     * @param dir 下载文件夹
     * @param path 本地路径(文件名)
     * @param resultHandler 下载回调
     */
    private static void down(String url,final File dir , final String path, final ResultHandlerInterface resultHandler){
        LogX.d("down",running+"");
        if(dir==null){
            resultHandler.onError(new Exception("Download error ! Folder is null !"));
            running-=1;
            next();
            return;
        }
        File file0 = new File(dir.getAbsolutePath().concat(path));
        if(file0.exists()){
            resultHandler.onResponse(file0);
            running-=1;
            next();
            return;
        }
        if(url==null){
            LogX.e("Image url null !!");
            resultHandler.onError(new Exception("Image url null !!"));
            running-=1;
            next();
            return;
        }
        if(!checkDir(dir)){
            resultHandler.onError(new Exception("Fail to create download directory."));
            running-=1;
            next();
            return;
        }
//        fastLog("开始下载");
        client.get(url, new BinaryHttpResponseHandler() {
//        RequestHandle download = new AsyncHttpClient().get(url, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] binaryData) {
                OutputStream f = null;
                try {
                    File file = new File(dir.getAbsolutePath().concat(path));
                    f = new FileOutputStream(file);
                    f.write(binaryData); //your bytes
                    f.close();
                    resultHandler.onResponse(file);
                    running-=1;
                    next();
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onError(e);
                    running-=1;
                    next();
                }
            }

            @Override
            public String[] getAllowedContentTypes() {
                String[] oldTypes = super.getAllowedContentTypes();
                String[] types = new String[oldTypes.length + 1];
                for (int i = 0; i < oldTypes.length; ++i) {
                    types[i] = oldTypes[i];
                }
                types[types.length - 1] = "text/plain";
                return types;
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] binaryData, Throwable error) {
                LogX.e("facehub_cloud", "download err : " + error);
                resultHandler.onError(new Exception(error));
                running-=1;
                next();
            }

        });
    }

    static File fileDir,cacheDir;
    public static File getFileDir(){
        if(fileDir==null) {
            fileDir = FacehubApi.getAppContext().getExternalFilesDir(null);
        }
        return fileDir;
    }
    public static File getCacheDir(){
        if(cacheDir==null) {
            cacheDir = FacehubApi.getAppContext().getExternalCacheDir();
        }
        return cacheDir;
    }
    static class Task{
        String url,path;
        int retry=0;
        File dir;
        ResultHandlerInterface handler;
        Task(String url,final File dir , final String path,int retry, final ResultHandlerInterface resultHandler){
            this.url=url;
            this.dir=dir;
            this.retry = retry;
            this.path=path;
            this.handler= resultHandler;
         }
    }
}
