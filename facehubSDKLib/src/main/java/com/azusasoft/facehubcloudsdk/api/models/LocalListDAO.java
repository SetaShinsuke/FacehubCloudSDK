package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by SETA_WORK on 2016/7/22.
 */
public class LocalListDAO {
    private final static String TABLENAME = "LOCAL_LIST";

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

                //Local List增加的字段
                ", NUM_ROWS INT" +
                ", NUM_COLUMNS INT" +
                ", NEED_MIX_LAYOUT INT" +
                ", LOCAL_TYPE STRING" +
                " );";
        database.execSQL(sql);
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion < 4) { // 1/2版本升级而来
            updateTo4(db);
        }
    }
    private static void updateTo4(SQLiteDatabase db){
        createTable(db);
    }

    //region 保存
    static boolean save2DBWithClose(LocalList localList) {
        SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
        boolean result = save(localList, db);
        db.close();
        return result;
    }

    private static boolean save(LocalList obj, SQLiteDatabase db) {
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

        values.put("NUM_ROWS",obj.getRowNum());
        values.put("NUM_COLUMNS",obj.getColumnNum());
        int needMixLayout = 0;
        if(obj.isNeedMixLayout()){
            needMixLayout = 1;
        }
        values.put("NEED_MIX_LAYOUT",needMixLayout);
        values.put("LOCAL_TYPE",obj.getLocalType());

        long ret;
        LocalList localListDb = findById(obj.getId(), false);
        if (localListDb != null) {
            obj.setDbId(localListDb.getDbId());
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

    protected static void saveInTX(Collection<LocalList> objects) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();

        try {
            sqLiteDatabase.beginTransaction();

            for (LocalList object : objects) {
                boolean flag = save(object, sqLiteDatabase);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            LogX.e("Error in saving localLists in transaction " + e.getMessage());
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }
    //endregion

    //region 查找
    protected static ArrayList<LocalList> find(String whereClause, String[] whereArgs,
                                              String groupBy, String orderBy, String limit,
                                              boolean doClose) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        LocalList entity;
        ArrayList<LocalList> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new LocalList();
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

    protected static LocalList findById(String id, boolean doClose) {
        ArrayList<LocalList> localLists = find("UID=?", new String[]{String.valueOf(id)}, null, null, "1", doClose);
        if (localLists.isEmpty()) return null;
        return localLists.get(0);
    }

    /**
     *
     * @return 返回当前用户的所有列表
     */
    protected static ArrayList<LocalList> findAll() {
        return find(null, null , null, null, null, true);
    }

    private static void inflate(LocalList entity, Cursor c) {
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

        entity.setColumnNum(c.getInt(c.getColumnIndex("NUM_COLUMNS")));
        entity.setRowNum(c.getInt(c.getColumnIndex("NUM_ROWS")));
        entity.setNeedMixLayout(c.getInt(c.getColumnIndex("NEED_MIX_LAYOUT"))!=0);
        entity.setLocalType(c.getString(c.getColumnIndex("LOCAL_TYPE")));
    }
    //endregion

    //region 删除
    protected static void delete(String listId) {
        LocalList localList = findById(listId, true);
        if (localList == null || localList.getId() == null) {
            LogX.e("尝试删除空列表!");
            return;
        }
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME, "UID=?", new String[]{localList.getId()});
        sqLiteDatabase.close();
    }

    protected static void deleteAll() {
        String userId = FacehubApi.getApi().getUser().getUserId();
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME, "USER_ID=?", new String[]{userId});
        sqLiteDatabase.close();
    }

    protected static void deleteEmoticons(String listId, ArrayList<String> emoticonIds) {
        LocalList localList = findById(listId, true);
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        if (localList == null) {
            return;
        }
        emoticons = localList.getEmoticons();
        for(String id:emoticonIds){
            for(int i=0;i<emoticons.size();i++){
                if(emoticons.get(i).getId().equals(id)){
                    emoticons.remove( i );
                }
            }
        }
        save2DBWithClose(localList);
    }
    //endregion

    //region 修改
    protected static void rename(LocalList obj) {

    }
    //endregion
}
