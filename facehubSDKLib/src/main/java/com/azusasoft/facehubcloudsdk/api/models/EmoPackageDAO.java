//package com.azusasoft.facehubcloudsdk.api.models;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//
//import com.azusasoft.facehubcloudsdk.api.FacehubApi;
//import com.azusasoft.facehubcloudsdk.api.utils.LogX;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
///**
// * Created by SETA_WORK on 2016/7/27.
// */
//public class EmoPackageDAO {
//    private final static String TABLENAME = "EMO_PACKAGE";
//
//    private final static String COLUMN_UID = "UID";
//    private final static String COLUMN_NAME = "NAME";
//    private final static String COLUMN_EMO_UIDS = "EMOTICONS_UIDS";
//    private final static String COLUMN_COVER_ID = "COVER_ID";
//    private final static String COLUMN_DES = "DESCRIPTION";
//    private final static String COLUMN_SUBTITLE = "SUBTITLE";
//    private final static String COLUMN_BACKGROUND_ID = "BACKGROUND_ID";
//    private final static String COLUMN_AUTHOR_NAME = "AUTHOR_NAME";
//    private final static String COLUMN_COPYRIGHT = "COPYRIGHT";
//
//    public static void createTable(SQLiteDatabase database){
//        String sql = "create table if not exists " + TABLENAME + " (" +
//                " ID INTEGER PRIMARY KEY AUTOINCREMENT," +
//                COLUMN_UID +" TEXT," +
//                COLUMN_NAME + " TEXT, " +
//                COLUMN_EMO_UIDS + " TEXT," +
//                COLUMN_COVER_ID + " TEXT," +
//                COLUMN_DES + " TEXT," +
//                COLUMN_SUBTITLE + " TEXT," +
//                COLUMN_BACKGROUND_ID + " TEXT," +
//                COLUMN_AUTHOR_NAME + " TEXT," +
//                COLUMN_COPYRIGHT + " TEXT," +
//                " );";
//        database.execSQL(sql);
//    }
//
//    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion){
//        if(oldVersion<5) { // 1/2版本升级而来
//            updateTo5(db);
//        }
//    }
//
//    private static void updateTo5(SQLiteDatabase db){
//        createTable(db);
//    }
//
//    //region保存
//    private static boolean save(EmoPackage obj , SQLiteDatabase db){
//        ContentValues values = new ContentValues();
//        values.put("NAME", String.valueOf(obj.getName()));
//        values.put("UID", obj.getId());
//        values.put("USER_ID", obj.getUserId());
//        if(obj.getCover()!=null){
//            values.put("COVER_ID",obj.getCover().getId());
//        }
//        values.put("FORK_FROM",obj.getForkFromId());
//        StringBuilder sb = new StringBuilder();
//        for (Emoticon e : obj.getEmoticons()) {
//            sb.append(e.getId());
//            sb.append(",");
//        }
//        values.put("EMOTICONS_UIDS", sb.toString());
//        long ret;
//        UserList userListDb = findById(obj.getId(), false);
//        if (userListDb != null) {
//            obj.setDbId(userListDb.getDbId());
//        }else{
//            obj.setDbId(null);
//        }
//
//        if (obj.getDbId() == null) {
//            ret = db.insert(TABLENAME, null, values);
//            obj.setDbId(ret);
//        } else {
//            ret = db.update(TABLENAME, values, "ID = ?", new String[]{String.valueOf(obj.getDbId())});
//        }
//        return ret > 0;
//    }
//
//    protected static boolean save2DBWithClose(EmoPackage emoPackage) {
//        SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
//        boolean result = save(emoPackage, db);
//        db.close();
//        return result;
//    }
//
//    protected static void saveInTX(Collection<EmoPackage> objects) {
//        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
//
//        try {
//            sqLiteDatabase.beginTransaction();
//
//            for (EmoPackage object : objects) {
//                boolean flag = save(object, sqLiteDatabase);
//            }
//            sqLiteDatabase.setTransactionSuccessful();
//        } catch (Exception e) {
//            LogX.e("Error in saving userLists in transaction " + e.getMessage());
//        } finally {
//            sqLiteDatabase.endTransaction();
//            sqLiteDatabase.close();
//        }
//    }
//    //endregion
//
//    //region查找
//    protected static ArrayList<EmoPackage> find(String whereClause, String[] whereArgs,
//                                                String groupBy, String orderBy, String limit,
//                                                boolean doClose) {
//        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
//        EmoPackage entity;
//        ArrayList<EmoPackage> toRet = new ArrayList<>();
//        Cursor c = sqLiteDatabase.query(TABLENAME, null,
//                whereClause, whereArgs, groupBy, null, orderBy, limit);
//        try {
//            while (c.moveToNext()) {
//                entity = new EmoPackage();
//                entity.setId(c.getString(c.getColumnIndex("UID")));
//                inflate(entity, c);
//                toRet.add(entity);
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//            LogX.e(e+"");
//        } finally {
//            c.close();
//            if (doClose) {
//                sqLiteDatabase.close();
//            }
//        }
//        return toRet;
//    }
//
//    protected static EmoPackage findById(String id, boolean doClose) {
//        ArrayList<EmoPackage> emoPackages = find("UID=?", new String[]{String.valueOf(id)}, null, null, "1", doClose);
//        if (emoPackages.isEmpty()) return null;
//        return emoPackages.get(0);
//    }
//
//    protected static ArrayList<EmoPackage> findAll() {
//        return find(null,null, null, null, null, true);
//    }
//
//    private static void inflate(EmoPackage entity, Cursor c) {
//        entity.setName(c.getString(c.getColumnIndex("NAME")));
//        String eUids = c.getString(c.getColumnIndex("EMOTICONS_UIDS"));
//        entity.setDbId(c.getLong(c.getColumnIndex("ID")));
//        entity.setForkFromId(c.getString(c.getColumnIndex("FORK_FROM")));
//        Emoticon coverImage = EmoticonDAO.getUniqueEmoticon(c.getString(c.getColumnIndex("COVER_ID")), false);
//        entity.setCover(coverImage);
//        ArrayList<Emoticon> emoticons = new ArrayList<>();
//        for (String eUid : eUids.split(",")) {
//            if (eUid.length() > 0) {
//                Emoticon emoticon = EmoticonDAO.getUniqueEmoticon(eUid, false);
//                emoticons.add(emoticon);
//            }
//        }
//        entity.setEmoticons(emoticons);
//    }
//    //endregion
//
//    //region 删除
//    protected static void delete(String emoPackageId) {
//        EmoPackage emoPackage = findById(emoPackageId, true);
//        if (emoPackage == null || emoPackage.getId() == null) {
//            LogX.e("尝试删除空EmoPackage!");
//            return;
//        }
//        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
//        sqLiteDatabase.delete(TABLENAME, "UID=?", new String[]{emoPackage.getId()});
//        sqLiteDatabase.close();
//    }
//
//    protected static void deleteAll() {
//        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
//        sqLiteDatabase.delete(TABLENAME, null , null);
//        sqLiteDatabase.close();
//    }
//
//    protected static void deleteEmoticons(String listId, ArrayList<String> emoticonIds) {
//        EmoPackage emoPackage = findById(listId, true);
//        ArrayList<Emoticon> emoticons = new ArrayList<>();
//        if (emoPackage == null) {
//            return;
//        }
//        emoticons = emoPackage.getEmoticons();
//        for(String id:emoticonIds){
//            for(int i=0;i<emoticons.size();i++){
//                if(emoticons.get(i).getId().equals(id)){
//                    emoticons.remove( i );
//                }
//            }
//        }
//        save2DBWithClose(emoPackage);
//    }
//    //endregion
//
//}