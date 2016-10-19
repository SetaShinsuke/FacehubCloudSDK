package com.azusasoft.facehubcloudsdk.api.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.MockClient;
import com.loopj.android.http.BinaryHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by ilike on 15/3/3.
 */
public class DownloadService {
//    static String SDCARD= Environment.getExternalStorageDirectory().getAbsolutePath();
//    static File DIR;
    static MockClient client = new MockClient("");
    static Queue<Task> waitForDownload = new LinkedBlockingQueue<>();
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

    public static void clearDownloadQueue(){
        waitForDownload.clear();
    };

    /**
     * 下载函数(实际上是添加到下载队列);
     *
     * @param url 下载url;
     * @param dir 下载目录;
     * @param path 下载路径;
     * @param resultHandler 下载回调，返回下载成功的{@link File}对象;
     */
    public static void download(String url,final File dir , final String path, final ResultHandlerInterface resultHandler){
        Task task = new Task(url,dir,path,0,resultHandler);
        waitForDownload.add(task);
        next();
    }
    private  static void next(){
        while(!waitForDownload.isEmpty()&&running<=MAX ){
            running+=1;
            final Task t= waitForDownload.remove();
            down(t.url, t.dir, t.path, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    t.handler.onResponse(response);
                }

                @Override
                public void onError(Exception e) {
                    if(t.retry<=MAXRETRY){
                        t.retry+=1;
                        waitForDownload.add(t);
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
     * @param url 下载地址;
     * @param dir 下载文件夹;
     * @param path 本地路径(文件名);
     * @param resultHandler 下载回调,返回下载好的{@link File}对象;
     */
    private static void down(String url,final File dir , final String path, final ResultHandlerInterface resultHandler){
//        LogX.d("down",running+"");
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
//            LogX.e("Image url null !!");
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
            SharedPreferences sharedPreferences = FacehubApi.getAppContext().getSharedPreferences(Constants.CONSTANTS, Context.MODE_PRIVATE);
            if(sharedPreferences.contains(Constants.SD_AVAILABLE) && !sharedPreferences.getBoolean(Constants.SD_AVAILABLE,true)){
                //曾经有过sd_card不可用的情况
                fileDir = FacehubApi.getAppContext().getFilesDir();
            }else {
                fileDir = FacehubApi.getAppContext().getExternalFilesDir(null);
            }
        }
        //获取file目录失败
        if(fileDir==null){
            SharedPreferences sharedPreferences = FacehubApi.getAppContext().getSharedPreferences(Constants.CONSTANTS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.SD_AVAILABLE,false);
            editor.apply();
            fileDir = FacehubApi.getAppContext().getFilesDir();
        }
        return fileDir;
    }
    public static File getCacheDir(){
        if(cacheDir==null) {
            SharedPreferences sharedPreferences = FacehubApi.getAppContext().getSharedPreferences(Constants.CONSTANTS, Context.MODE_PRIVATE);
            if(sharedPreferences.contains(Constants.SD_AVAILABLE) && !sharedPreferences.getBoolean(Constants.SD_AVAILABLE,true)){
                //曾经有过sd_card不可用的情况
                cacheDir = FacehubApi.getAppContext().getCacheDir();
            }else {
                cacheDir = FacehubApi.getAppContext().getExternalCacheDir();
            }
        }
        //获取file目录失败
        if(cacheDir==null){
            SharedPreferences sharedPreferences = FacehubApi.getAppContext().getSharedPreferences(Constants.CONSTANTS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.SD_AVAILABLE,false);
            editor.apply();
            cacheDir = FacehubApi.getAppContext().getCacheDir();
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
