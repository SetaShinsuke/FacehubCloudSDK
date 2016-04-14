package com.azusasoft.facehubcloudsdk.api.utils;


import android.os.Environment;
import android.os.Handler;
import android.util.Log;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    /**
     * 下载函数
     *
     * @param url 下载地址
     * @param dir 下载文件夹
     * @param path 本地路径(文件名)
     * @param resultHandler 下载回调
     */
    public static void download(String url,final File dir , final String path, final ResultHandlerInterface resultHandler){
//        DownloadTask task = new DownloadTask(url,dir,path,resultHandler);
//        Future future = ThreadPoolManager.getDownloadThreadPool().submit( task );
//        try {
//            future.get();
//        } catch (InterruptedException | ExecutionException e) {
//            LogX.e("下载出错 : " + e);
//        }
        /** ======================================================================= */

        File file0 = new File(dir.getAbsolutePath().concat(path));
        if(file0.exists()){
//            fastLog("图片已下载,不重复下载");
            resultHandler.onResponse(file0);
            return;
        }
        if(url==null){
            LogX.e("Image url null !!");
            resultHandler.onError(new Exception("Image url null !!"));
            return;
        }
        if(!checkDir(dir)){
            resultHandler.onError(new Exception("Fail to create download directory."));
            return;
        }
//        fastLog("开始下载");
        RequestHandle download = client.get(url, new BinaryHttpResponseHandler() {
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    resultHandler.onError(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    resultHandler.onError(e);
                }
                resultHandler.onResponse("success.");

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
            }

        });
    }

    static class DownloadTask implements Runnable{
        private String url,path;
        private File dir;
        private ResultHandlerInterface resultHandler;

        public DownloadTask(String url,final File dir , final String path, final ResultHandlerInterface resultHandler){
            this.url = url;
            this.dir = dir;
            this.path = path;
            client.setMaxConnections(5);
            this.resultHandler = new ResultHandlerInterface() {
                @Override
                public void onResponse(final Object response) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultHandler.onResponse(response);
                        }
                    });
                }

                @Override
                public void onError(final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultHandler.onError(e);
                        }
                    });
                }
            };
        }

        @Override
        public void run() {
            File file0 = new File(dir.getAbsolutePath().concat(path));
            if(file0.exists()){
//            fastLog("图片已下载,不重复下载");
                resultHandler.onResponse(file0);
                return;
            }
            if(url==null){
                LogX.e("Image url null !!");
                resultHandler.onError(new Exception("Image url null !!"));
                return;
            }
            if(!checkDir(dir)){
                resultHandler.onError(new Exception("Fail to create download directory."));
                return;
            }
//        fastLog("开始下载");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    RequestHandle download = client.get(url, new BinaryHttpResponseHandler() {
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
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                resultHandler.onError(e);
                            } catch (IOException e) {
                                e.printStackTrace();
                                resultHandler.onError(e);
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
                        }

                    });
                }
            });
        }
    }
}
