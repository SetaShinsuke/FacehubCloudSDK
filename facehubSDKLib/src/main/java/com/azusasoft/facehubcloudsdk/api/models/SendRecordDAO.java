package com.azusasoft.facehubcloudsdk.api.models;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SETA on 2016/6/22.
 */
public class SendRecordDAO {
    private final static String TABLENAME = "SendRecord";

    /**
     * 创建表
     * @param database {@link SQLiteDatabase}对象
     */
    public static void createTable(SQLiteDatabase database) {
        String sql = "create table if not exists " + TABLENAME + " (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", EMO_ID TEXT  " +
                ", USER_ID TEXT  " +
                ", COUNT INT  " +
                " );";
        database.execSQL(sql);
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion<=2) { // 1/2版本升级过来
            updateFrom2(db);
        }
    }

    private static void updateFrom2(SQLiteDatabase db){
        createTable(db);
    }
}
