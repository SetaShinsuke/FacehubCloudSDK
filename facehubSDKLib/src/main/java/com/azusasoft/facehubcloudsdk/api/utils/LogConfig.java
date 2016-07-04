package com.azusasoft.facehubcloudsdk.api.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.log4j.Level;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogConfig {


    static private String fileName = "face_cloud_log.log";

    private long maxFileSize = 1024 * 1024;
    public static int level = 1;
    public static int V = 1;
    public static int D = 2;
    public static int I = 3;
    public static int W  = 4;
    public static int E = 5;

    public static void configure(Context context) {
        final LogConfigurator logConfigurator = new LogConfigurator();
//        Log.v("hehe","getStorageState() : " + getStorageState());
        if(!getStorageState()){
            return;
        }
        try {
            File dir = context.getExternalFilesDir(null);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir = dir.getParentFile();
        logConfigurator.setFileName(dir.getAbsolutePath() + File.separator + fileName);
            Log.v(LogX.TAG_LOGX,"Log fold : "+dir.getAbsolutePath() + File.separator + fileName);
            logConfigurator.setRootLevel(i2L(level));
            logConfigurator.setUseLogCatAppender(false);
            // Set log level of a specific logger
            logConfigurator.setLevel("org.apache", Level.ERROR);
            logConfigurator.configure();
        }catch (Exception e){
            Log.e(LogX.TAG_LOGX, "Logger初始化失败!");
        }

    }
    private static Level i2L(int l){
        switch (l){
            case 1:
                return Level.TRACE;
            case 2:
                return Level.DEBUG;
            case 3:
                return Level.INFO;
            case 4:
                return Level.WARN;
            case 5:
                return Level.ERROR;

        }
        return null;
    }

    private static boolean getStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
//        return false;
        return false;
    }

}