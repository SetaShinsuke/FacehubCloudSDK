package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.EMO_LOGX;

/**
 * Created by SETA on 2016/3/8.
 * 辅助操作表情数据
 */
public class EmoticonDAO {
    private final static String TABLENAME = "Emoticon";

    /**
     * 创建表
     * 属性:id,format,fsize,height,width,uid,medium_path,full_path
     *
     * @param database {@link SQLiteDatabase}对象
     */
    public static void createTable(SQLiteDatabase database) {
        String sql = "create table if not exists " + TABLENAME + " (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", FORMAT TEXT  " +
                ", FSIZE INT  " +
                ", WIDTH INT  " +
                ", HEIGHT INT  " +
                ", UID TEXT  " +
                ", MEDIUM_PATH TEXT  " +
                ", FULL_PATH TEXT  " +
                ", DESCRIPTION TEXT " +
                " );";
        database.execSQL(sql);
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion < 2 ) {
            updateTo2(db);
        }
        if(oldVersion < 4) { // 2->3
            updateTo4(db);
        }
    }
    private static void updateTo2(SQLiteDatabase db){
        String addShareToListTable = "ALTER TABLE "+ TABLENAME + " ADD DESCRIPTION TEXT ";
        try {
            LogX.d("数据库Emoticon表添加description字段");
            db.execSQL(addShareToListTable);
        }catch (SQLiteException e){
            LogX.e("数据库Emoticon表添加description字段出错 : " + e );
        }
    }
    private static void updateTo4(SQLiteDatabase db){

    }


    //region 保存
    protected static boolean save2DB(final Emoticon emoticon) {
        boolean ret = false;
        try {
            SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
            ret = save(emoticon, db);
            db.close();
        } catch (Exception e) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Get new entry
                    SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
                    save(emoticon, db);
                    db.close();
                }
            }, 100);
        }
        return ret;
    }

    /**
     * 保存 emoticon 到数据库
     *
     * @param obj {@link Emoticon}对象
     * @param db 数据库
     * @return  保存是否成功
     */
    private static boolean save(Emoticon obj, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("FORMAT", String.valueOf(obj.getFormat()));
        values.put("FSIZE", obj.getFsize());
        values.put("WIDTH", obj.getWidth());
        values.put("HEIGHT", obj.getHeight());
        values.put("UID", obj.getId());
        values.put("DESCRIPTION",obj.getDescription());
        values.put("MEDIUM_PATH", obj.getThumbPath());
        values.put("FULL_PATH", obj.getFullPath());
//        fastLog("Saving , path : " + obj.getFilePath(Image.Size.FULL));
        long ret;
        //如果数据库中已经有该id对应的数据，则进行update.否则insert.
        Emoticon emoDb = findEmoticonById(obj.getId(), false);
        if( emoDb!=null){
//                && values.get("TYPE").equals(emoDb.getClass()+"")) { //Image.id 和 class都相同
            obj.setDbId( emoDb.getDbId() );
        }else {
            obj.setDbId(null);
        }

        if (obj.getDbId() == null ) {
            ret = db.insert(TABLENAME, null, values);
            obj.setDbId( ret );
        } else {
            ret = db.update(TABLENAME, values, "ID = ?", new String[]{String.valueOf(obj.getDbId())});
        }

        return ret > 0;
    }

    /**
     * 批量保存
     */
    protected static void saveInTx(Collection<Emoticon> objects) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        try{
            sqLiteDatabase.beginTransaction();
            for(Emoticon object: objects){
                save(object, sqLiteDatabase);
            }
            sqLiteDatabase.setTransactionSuccessful();
        }catch (Exception e){
            LogX.e("Error in saving in transaction " + e.getMessage());
        }finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

//    protected static void saveEmoInTx(Collection<Emoticon> objects) {
//        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
//        try{
//            sqLiteDatabase.beginTransaction();
//            for(Emoticon object: objects){
//                save(object, sqLiteDatabase);
//            }
//            sqLiteDatabase.setTransactionSuccessful();
//        }catch (Exception e){
//            LogX.i( LogX.LIST_LOGX, "Error in saving in transaction " + e.getMessage());
//        }finally {
//            sqLiteDatabase.endTransaction();
//            sqLiteDatabase.close();
//        }
//    }
    //endregion

    //region 查找

    /**
     * 如果数据库已有，则返回该对象
     * 否则新建数据
     */
    protected static Emoticon getUniqueEmoticon(String uid , boolean doClose){
        if(uid==null){
            return null;
        }
        Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(uid);
        Emoticon emoticonDB = findEmoticonById(uid, doClose);
        if(emoticonDB == null){
            save2DB( emoticon );
        }
        return emoticon;
    }

//    public static Emoticon getUniqueEmoticon(String uid, boolean doClose){
//        if(uid==null){
//            return null;
//        }
//
//        Emoticon emoticon = findEmoticonById(uid,doClose);
//        if(emoticon==null){
//            emoticon = new Emoticon();
//            emoticon.setId(uid);
//            save2DBWithClose( emoticon );
//        }
//        return emoticon;
//    }

    /**
     * 从数据库中查找
     */
    private static ArrayList<Emoticon> find(String whereClause, String[] whereArgs,
                                                     String groupBy, String orderBy, String limit , boolean doClose) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        Emoticon entity;
        java.util.ArrayList<Emoticon> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new Emoticon();
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

    private static Emoticon findEmoticonById(String id, boolean doClose){
        List<Emoticon> list = find("UID=?", new String[]{String.valueOf(id)}, null, null, "1", false);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
//    protected static Emoticon findEmoticonById(String id, boolean doClose){
//        List list = find(  "UID=?", new String[]{String.valueOf(id)}, null, null, "1" , doClose);
//        if (list.isEmpty()) return null;
//        Object obj = list.get(0);
//        if(obj instanceof Emoticon){
//            return (Emoticon)obj;
//        }
//        return null;
//    }
    protected static ArrayList<Emoticon> findAll(){
        return find(null, null, null, null, null , true);
    }

    /**
     * 从数据库中提取
     */
    private static void inflate(Emoticon entity, Cursor c) {
        for(String  name : c.getColumnNames()){
            switch (name){
                case "ID":
                    entity.setDbId(c.getLong(c.getColumnIndex(name)) );
                    break;
                case "FORMAT":
                    String str=c.getString(c.getColumnIndex(name));
                    if(str==null){
                        str="JPG";
                    }
                    entity.setFormat(str);
                    break;
                case "FSIZE":
                    entity.setFsize(c.getInt(c.getColumnIndex(name)));
                    break;
                case "WIDTH":
                    entity.setWidth(c.getInt(c.getColumnIndex(name)));
                    break;
                case "HEIGHT":
                    entity.setHeight(c.getInt(c.getColumnIndex(name)));
                    break;
                case "UID":
                    entity.setId(c.getString(c.getColumnIndex(name)));
                    break;
                case "DESCRIPTION":
                    entity.setDescription(c.getString(c.getColumnIndex(name)));
                    break;
                case "MEDIUM_PATH":
                    entity.setFilePath(Image.Size.MEDIUM , c.getString(c.getColumnIndex(name)) );
                    break;
                case "FULL_PATH":
                    entity.setFilePath(Image.Size.FULL , c.getString(c.getColumnIndex(name)) );
                    break;
                default:
                    LogX.e(EMO_LOGX, "unknown filed " + name);
            }
        }
    }
    //endregion


}
