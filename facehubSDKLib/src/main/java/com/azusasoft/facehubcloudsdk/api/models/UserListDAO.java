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
    private final static String TABLENAME = "LIST";

    /**
     * 创建表
     * 属性:id,uid,name
     * ,user_id,modified_at
     * ,emoticons_uids 列表内的表情id
     *
     * @param database
     */
    public static void createTable(SQLiteDatabase database) {
        String sql = "create table if not exists " + TABLENAME + " (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", UID TEXT" +
                ", NAME TEXT " +
                ", USER_ID TEXT" + //TODO:有用?
                ", EMOTICONS_UIDS TEXT" +
                ", FORK_FROM TEXT" +
                ", COVER_ID TEXT" +
                " );";
        database.execSQL(sql);
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion<4) { // 1/2版本升级而来
            updateTo4(db);
        }
    }
    private static void updateTo4(SQLiteDatabase db){

    }

    //region 保存
    static boolean save2DBWithClose(UserList userList) {
        SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
        boolean result = save(userList, db);
        db.close();
        return result;
    }

    private static boolean save(UserList obj, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("NAME", String.valueOf(obj.getName()));
        values.put("UID", obj.getId());
        values.put("USER_ID", obj.getUserId());
        if(obj.getCover()!=null){
            values.put("COVER_ID",obj.getCover().getId());
        }
        values.put("FORK_FROM",obj.getForkFromId());
        StringBuilder sb = new StringBuilder();
        for (Emoticon e : obj.getEmoticons()) {
            sb.append(e.getId());
            sb.append(",");
        }
        values.put("EMOTICONS_UIDS", sb.toString());
        long ret;
        UserList userListDb = findById(obj.getId(), false);
        if (userListDb != null) {
            obj.setDbId(userListDb.getDbId());
        }else{
            obj.setDbId(null);
        }

        if (obj.getDbId() == null) {
            ret = db.insert(TABLENAME, null, values);
            obj.setDbId(ret);
        } else {
            ret = db.update(TABLENAME, values, "ID = ?", new String[]{String.valueOf(obj.getDbId())});
        }
        return ret > 0;
    }

    protected static void saveInTX(Collection<UserList> objects) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();

        try {
            sqLiteDatabase.beginTransaction();

            for (UserList object : objects) {
                boolean flag = save(object, sqLiteDatabase);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogX.e("Error in saving userLists in transaction " + e.getMessage());
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }
    //endregion

    //region 查找
    protected static ArrayList<UserList> find(String whereClause, String[] whereArgs,
                                           String groupBy, String orderBy, String limit,
                                           boolean doClose) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        UserList entity;
        ArrayList<UserList> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new UserList();
                entity.setId(c.getString(c.getColumnIndex("UID")));
                inflate(entity, c);
                toRet.add(entity);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            LogX.e(e+"");
        } finally {
            c.close();
            if (doClose) {
                sqLiteDatabase.close();
            }
        }
        return toRet;
    }

    protected static UserList findById(String id, boolean doClose) {
        ArrayList<UserList> userLists = find("UID=?", new String[]{String.valueOf(id)}, null, null, "1", doClose);
        if (userLists.isEmpty()) return null;
        return userLists.get(0);
    }

    protected static UserList findByForkFrom(String forkFromId, boolean doClose) {
        ArrayList<UserList> userLists = find("FORK_FROM=?", new String[]{String.valueOf(forkFromId)}, null, null, "1", doClose);
        if (userLists.isEmpty()) return null;
        return userLists.get(0);
    }

    /**
     *
     * @return 返回当前用户的所有列表
     */
    protected static ArrayList<UserList> findAll() {
        return find("USER_ID=?", new String[]{FacehubApi.getApi().getUser().getUserId()}, null, null, null, true);
    }

    private static void inflate(UserList entity, Cursor c) {
        entity.setName(c.getString(c.getColumnIndex("NAME")));
        String eUids = c.getString(c.getColumnIndex("EMOTICONS_UIDS"));
        entity.setDbId(c.getLong(c.getColumnIndex("ID")));
        entity.setForkFromId(c.getString(c.getColumnIndex("FORK_FROM")));
        Emoticon coverImage = EmoticonDAO.getUniqueEmoticon(c.getString(c.getColumnIndex("COVER_ID")), false);
        entity.setCover(coverImage);
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        for (String eUid : eUids.split(",")) {
            if (eUid.length() > 0) {
                Emoticon emoticon = EmoticonDAO.getUniqueEmoticon(eUid, false);
                emoticons.add(emoticon);
            }
        }
        entity.setEmoticons(emoticons);
    }
    //endregion

    //region 删除
    protected static void delete(String listId) {
        UserList userList = findById(listId, true);
        if (userList == null || userList.getId() == null) {
            LogX.e("尝试删除空列表!");
            return;
        }
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME, "UID=?", new String[]{userList.getId()});
        sqLiteDatabase.close();
    }

    protected static void deleteAll() {
        String userId = FacehubApi.getApi().getUser().getUserId();
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME, "USER_ID=?", new String[]{userId});
        sqLiteDatabase.close();
    }

    protected static void deleteEmoticons(String listId, ArrayList<String> emoticonIds) {
        UserList userList = findById(listId, true);
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        if (userList == null) {
            return;
        }
        emoticons = userList.getEmoticons();
        for(String id:emoticonIds){
            for(int i=0;i<emoticons.size();i++){
                if(emoticons.get(i).getId().equals(id)){
                    emoticons.remove( i );
                }
            }
        }
        save2DBWithClose(userList);
    }
    //endregion

    //region 修改
    protected static void rename(UserList obj) {

    }
    //endregion
}
