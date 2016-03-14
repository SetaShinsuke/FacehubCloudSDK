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
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

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

    public static boolean isCollected( String emoticonId ){
        ArrayList<UserList> allLists = UserListDAO.findAll();
        for(UserList userList:allLists){ //所有个人列表
            ArrayList<Emoticon> emoticons = userList.getEmoticons();
            for(Emoticon emoticon:emoticons){ //列表内所有表情
                if(emoticon.getId().equals(emoticonId)){
                    return true;
                }
            }
        }
        return false;
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
        Emoticon emoDb = findById(obj.getId(),false);
        if( emoDb!=null ) {
            obj.setDbId( emoDb.getDbId() );
        }else {
            obj.setDbId(null);
        }

        if (obj.getDbId() == null ) {
            fastLog( "insert emoticons." );
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
            LogX.i( LogX.LOGX_LIST , "Error in saving in transaction " + e.getMessage());
        }finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }
    //endregion

    //region 查找

    /**
     * 如果数据库已有，则返回该对象
     * 否则新建数据
     */
    public static Emoticon getUnique( String uid , boolean doClose){
        Emoticon emoticon = findById(uid, doClose);
        if(emoticon==null){
            emoticon = new Emoticon();
            emoticon.setId(uid);
            save2DB( emoticon );
        }
        return emoticon;
    }

    /**
     * 从数据库中查找
     */
    public static ArrayList<Emoticon> find(String whereClause, String[] whereArgs,
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
    static Emoticon findBy(String what,String value,boolean doClose){
        java.util.List<Emoticon> list = find(what.toUpperCase() + "=?", new String[]{value}, null, null, "1" , doClose);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    protected static Emoticon findById(String id , boolean doClose){
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
    //endregion


}
