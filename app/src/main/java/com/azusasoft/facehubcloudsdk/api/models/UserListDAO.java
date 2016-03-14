package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by SETA on 2016/3/8.
 * 辅助操作用户列表的数据
 */
public class UserListDAO {
    private final static String TABLENAME="LIST";

    /**
     * 创建表
     * 属性:id,uid,name
     *     ,user_id,modified_at
     *     ,emoticons_uids 列表内的表情id
     *
     * @param database
     */
    public static void createTable(SQLiteDatabase database){
        String sql = "create table if not exists "+TABLENAME+" ("+
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", UID TEXT"+
                ", NAME TEXT "+
//                ", MODIFIED_AT TEXT"+ //TODO:有用?
                ", EMOTICONS_UIDS TEXT"+
                " );";
        database.execSQL(sql);
    }

    //region 保存
    static boolean save2DB(UserList userList){
        SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
        boolean result = save(userList,db);
        db.close();
        return result;
    }
    private static boolean save(UserList obj,SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put("NAME", String.valueOf( obj.getName() ));
        values.put("UID", obj.getId() );
        StringBuilder sb=new StringBuilder();
        for (Emoticon e : obj.getEmoticons()) {
            sb.append( e.getId() );
            sb.append(",");
        }
        values.put("EMOTICONS_UIDS",sb.toString() );
        long ret;
        if (obj.getDbId() == null && (findById(obj.getId(),false)==null) ) {
            ret = db.insert(TABLENAME, null, values);
            obj.setDbId( ret );
        }
        else {
            ret = db.update(TABLENAME, values, "ID = ?", new String[]{String.valueOf(obj.getDbId())});
        }
        return ret>0;
    }
    protected static void saveInTX( Collection<UserList> objects ){
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();

        try{
            sqLiteDatabase.beginTransaction();

            for(UserList object: objects){
                save(object, sqLiteDatabase);
            }
            sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e){
            LogX.i( LogX.LOGX_LIST , "Error in saving in transaction " + e.getMessage());
        }finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }
    //endregion

    //region 查找
    public static ArrayList<UserList> find(String whereClause, String[] whereArgs,
                                           String groupBy, String orderBy, String limit,
                                           boolean doClose ) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        UserList entity;
        ArrayList<UserList> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new UserList();
                entity.setId( c.getString(c.getColumnIndex("UID")) );
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
    static UserList findById(String id , boolean doClose){
        ArrayList<UserList> userLists = find("UID=?", new String[]{String.valueOf(id)}, null, null, "1", doClose);
        if (userLists.isEmpty()) return null;
        return userLists.get(0);
    }

    private static void inflate( UserList entity , Cursor c){
        entity.setName( c.getString(c.getColumnIndex("NAME")) );
        String eUids=c.getString(c.getColumnIndex("EMOTICONS_UIDS"));
        entity.setDbId(c.getLong(c.getColumnIndex("ID")));
        ArrayList<Emoticon> emoticons=new ArrayList<>();
        for (String eUid : eUids.split(",")) {
            if(eUid.length()>0) {
                emoticons.add( EmoticonDAO.getUnique(eUid));
            }
        }
        entity.setEmoticons(emoticons);
    }
    //endregion

    //region 删除
    public static void delete(UserList userList){

    }
    public static void deleteAll(){

    }
    //endregion

    //region 修改
    public static void rename(UserList obj){

    }
    //endregion
}
