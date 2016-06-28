package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/10.
 * 辅助操作失败重试
 */
public class RetryReqDAO {
    private static final String TABLENAME = "RETRY_REQ";

    private static final int REMOVE_LIST = 0;
    private static final int REMOVE_EMOTICON = 1;

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
                ", LIST_ID TEXT "+
                ", EMO_IDS TEXT "+
                " );";
        database.execSQL(sql);
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion<=2) { // 1/2->3
            updateFrom2(db);
        }
    }
    private static void updateFrom2(SQLiteDatabase db){

    }

    protected static boolean save2DB( RetryReq req ){
        SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
        boolean result = save(req, db);
        db.close();
        return result;
    }

    private static boolean save(RetryReq obj , SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put("TYPE", String.valueOf( obj.getType() ));
        values.put("LIST_ID", obj.getListId() );
        StringBuilder sb=new StringBuilder();
        for (String emoId : obj.getEmoIds()) {
            sb.append( emoId );
            sb.append(",");
        }
        values.put("EMO_IDS", sb.toString() );
        long ret;
        ret = db.insert(TABLENAME, null, values);
        obj.setDbId(ret);
        return ret>0;
    }

    public static ArrayList<RetryReq> find(String whereClause, String[] whereArgs,
                                           String groupBy, String orderBy, String limit,
                                           boolean doClose ) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        RetryReq entity;
        ArrayList<RetryReq> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new RetryReq();
                inflate(entity,c);
                toRet.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            if(doClose) {
                sqLiteDatabase.close();
            }
        }
        return toRet;
    }
    public static ArrayList<RetryReq> findAll(){
        return find(null, null, null, null, null , true);
    }
    private static void inflate( RetryReq entity , Cursor c){
        entity.setType(c.getInt(c.getColumnIndex("TYPE")));
        entity.setDbId(c.getLong(c.getColumnIndex("ID")));
        String idsStr = c.getString(c.getColumnIndex("EMO_IDS"));
        ArrayList<String> emoIds = new ArrayList<>();
        for (String eUid : idsStr.split(",")) {
            if (eUid.length() > 0) {
                emoIds.add( eUid );
            }
        }
        entity.setEmoIds( emoIds );
        entity.setListId(c.getString(c.getColumnIndex("LIST_ID") ));
    }

    protected static void delete( RetryReq req ){
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME,"ID=?",new String[]{ ""+req.getDbId() });
        sqLiteDatabase.close();
    }
    //TODO:退出时清空
    public static void deleteAll(){
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME, null ,null);
        sqLiteDatabase.close();
    }
}
