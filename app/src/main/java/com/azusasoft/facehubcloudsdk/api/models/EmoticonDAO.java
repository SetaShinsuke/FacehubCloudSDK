package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.util.*;
import java.util.List;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.LOGX_EMO;

/**
 * Created by SETA on 2016/3/8.
 * 辅助操作表情数据
 */
public class EmoticonDAO {
    private final static String TABLENAME = "EMOTICON";

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
                " );";
        database.execSQL(sql);

    }

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
        FacehubApi.getDbHelper().export();
        return ret;
    }

    /**
     * 保存 emoticon 到数据库
     *
     * @param obj {@link Emoticon}对象
     * @param db 数据库
     * @return  保存是否成功
     */
    static private boolean save(Emoticon obj, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("FORMAT", String.valueOf(obj.getFormat()));
        values.put("FSIZE", obj.getFsize());
        values.put("WIDTH", obj.getWidth());
        values.put("HEIGHT", obj.getHeight());
        values.put("UID", obj.getId());
        values.put("MEDIUM_PATH", obj.getFilePath(Image.Size.MEDIUM));
        values.put("FULL_PATH", obj.getFilePath(Image.Size.FULL));
        long ret;
        //如果数据库中已经有该id对应的数据，则进行update.否则insert.
        if (obj.getDbId() == null && (findById(obj.getId(),false)==null) ) {
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
    public static void saveInTx(Collection<Emoticon> objects, SQLiteDatabase db,boolean inTx) {
        try{
            if(!inTx)
                db.beginTransaction();
            for(Emoticon object: objects){
                EmoticonDAO.save(object, db);
            }
            if(!inTx)
                db.setTransactionSuccessful();
        }catch (Exception e){
            LogX.i( LOGX_EMO , "Error in saving in transaction " + e.getMessage());
        }finally {
            if(!inTx){
                db.endTransaction();
                db.close();
            }
        }
    }


    /**
     * 从数据库中查找
     */
    public static java.util.ArrayList<Emoticon> find(String whereClause, String[] whereArgs,
                                                     String groupBy, String orderBy, String limit , boolean doClose) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        Emoticon entity;
        java.util.ArrayList<Emoticon> toRet = new ArrayList<Emoticon>();
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
    static Emoticon findBy(String what,String value,boolean doClose){
        java.util.List<Emoticon> list = find(what.toUpperCase() + "=?", new String[]{value}, null, null, "1" , doClose);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    static Emoticon findById(String id , boolean doClose){
        List<Emoticon> list = find(  "UID=?", new String[]{String.valueOf(id)}, null, null, "1" , doClose);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    static Emoticon findByDbId(long dbId , boolean doClose){
        List<Emoticon> list = find(  "ID=?", new String[]{String.valueOf(dbId)}, null, null, "1" , doClose);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    static ArrayList<Emoticon> findAll(){
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
                    entity.setId(c.getString(c.getColumnIndex(name)) );
                    break;
                case "MEDIUM_PATH":
                    entity.setFilePath(Image.Size.MEDIUM , c.getString(c.getColumnIndex(name)) );
                    break;
                case "FULL_PATH":
                    entity.setFilePath(Image.Size.FULL , c.getString(c.getColumnIndex(name)) );
                    break;
                default:
                    LogX.e( LOGX_EMO , "unknow filed " + name);
            }
        }
    }

}
