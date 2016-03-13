package com.azusasoft.facehubcloudsdk.api.models;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SETA on 2016/3/8.
 * 辅助操作表情数据
 */
public class EmoticonDAO {
    private final static String TABLENAME="EMOTICON";

    /**
     * 创建表
     * 属性:id,format,fsize,height,width,uid,medium_path,full_path
     *
     * @param database {@link SQLiteDatabase}对象
     */
    public static void createTable(SQLiteDatabase database){
        String sql="create table if not exists "+TABLENAME+" ("+
                " ID INTEGER PRIMARY KEY AUTOINCREMENT "+
                ", FORMAT TEXT  "+
                ", FSIZE INT  "+
                ", WIDTH INT  "+
                ", HEIGHT INT  "+
                ", UID TEXT  "+
                ", MEDIUM_PATH TEXT  "+
                ", FULL_PATH TEXT  "+
                " );";
        database.execSQL(sql);

    }


}
