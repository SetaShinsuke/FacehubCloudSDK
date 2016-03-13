package com.azusasoft.facehubcloudsdk.api.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.azusasoft.facehubcloudsdk.api.models.EmoticonDAO;
import com.azusasoft.facehubcloudsdk.api.models.RetryReqDAO;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;

/**
 * Created by SETA on 2016/3/13.
 */
public class DAOHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "";

    public DAOHelper(Context context){
        super(context, DATABASE_NAME , new CursorFactory(true), DATABASE_VERSION );
    }

    public DAOHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DAOHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable( db );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTable( SQLiteDatabase db){
        UserListDAO.createTable(db);
        EmoticonDAO.createTable(db);
        RetryReqDAO.createTable(db);
    }
}
