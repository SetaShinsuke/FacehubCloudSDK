package com.azusasoft.facehubcloudsdk.api.models;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by SETA on 2016/3/10.
 * 辅助操作失败重试
 */
public class RetryReqDAO {
    private static final String TABLENAME = "RETRY_REQ";

    private static final int REMOVE_LIST = 0;
    private static final int REMOVE_EMOTICON = 1;
    private static final int NOT_RETRIED = 0;
    private static final int IS_RETRIED = 1;

    /**
     * 用来记录需要重试的操作
     * TYPE : 类型(删除列表/删除表情)
     * PARAMS : 网络请求参数(REST)
     * RETRIED : 标记已重试/未重试( TODO：或可直接删除行?　)
     *
     * @param database
     */
    public static void createTable(SQLiteDatabase database){
        String sql = "create table if not exists "+TABLENAME+" ("+
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", TYPE INT"+
                ", PARAMS TEXT "+
                ", RETRIED INT "+
                " );";
        database.execSQL(sql);
    }
}
