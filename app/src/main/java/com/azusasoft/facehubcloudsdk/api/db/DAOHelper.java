package com.azusasoft.facehubcloudsdk.api.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.azusasoft.facehubcloudsdk.api.models.EmoticonDAO;
import com.azusasoft.facehubcloudsdk.api.models.RetryReqDAO;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by SETA on 2016/3/13.
 */
public class DAOHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "facehubcloudv1.db";
    private Context context;

    public DAOHelper(Context context){
        super(context, DATABASE_NAME , new CursorFactory(true), DATABASE_VERSION );
        this.context = context;
    }

    public DAOHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DAOHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTable( SQLiteDatabase db){
        UserListDAO.createTable(db);
        EmoticonDAO.createTable(db);
        RetryReqDAO.createTable(db);
    }

    //TODO:调试用
    public void export(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME;
        String backupDBPath = "facehub_sdk.db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
