package com.azusasoft.facehubcloudsdk.api.models;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SETA on 2016/3/8.
 * 辅助操作用户列表的数据
 */
public class UserListDAO {
    private final static String TABLENAME="LIST";

    /**
     * 创建表
     * 属性:id,uid,name
     *     ,user_id,modified_at (todo:需要用到?)
     *     ,emoticons_uids 列表内的表情id
     *
     * @param database
     */
    public static void createTable(SQLiteDatabase database){
        String sql = "create table if not exists "+TABLENAME+" ("+
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", UID TEXT"+
                ", NAME TEXT "+
                ", USER_ID TEXT "+
                ", MODIFIED_AT TEXT"+
                ", EMOTICONS_UIDS TEXT"+
                " );";
        database.execSQL(sql);
    }

}
