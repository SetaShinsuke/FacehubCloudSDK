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
public class ImageDAO {
    private final static String TABLENAME = "IMAGE";

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
                ", TYPE TEXT " +
                " );";
        database.execSQL(sql);

    }

    //region 保存
    protected static boolean save2DB(final Image image) {
        boolean ret = false;
        try {
            SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
            ret = save(image, db);
            db.close();
        } catch (Exception e) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Get new entry
                    SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
                    save(image, db);
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
    static private boolean save(Image obj, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("FORMAT", String.valueOf(obj.getFormat()));
        values.put("FSIZE", obj.getFsize());
        values.put("WIDTH", obj.getWidth());
        values.put("HEIGHT", obj.getHeight());
        values.put("UID", obj.getId());
        values.put("MEDIUM_PATH", obj.getFilePath(Image.Size.MEDIUM));
        values.put("FULL_PATH", obj.getFilePath(Image.Size.FULL));
        values.put("TYPE", obj.getClass()+"");
        LogX.fastLog("image type : " + obj.getClass()+"");
        long ret;
        //如果数据库中已经有该id对应的数据，则进行update.否则insert.
        Image emoDb = findImageById(obj.getId(), false);
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
    protected static void saveInTx(Collection<Image> objects) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        try{
            sqLiteDatabase.beginTransaction();
            for(Image object: objects){
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

    protected static void saveEmoInTx(Collection<Emoticon> objects) {
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
    public static Image getUniqueImage(String uid, boolean doClose){
        Image image = findImageById(uid, doClose);
        if(image==null){
            image = new Image();
            image.setId(uid);
            save2DB( image );
        }
        return image;
    }

    public static Emoticon getUniqueEmoticon(String uid, boolean doClose){
        Emoticon emoticon =  findEmoticonById(uid, doClose);
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
    public static ArrayList<Image> find(String whereClause, String[] whereArgs,
                                                     String groupBy, String orderBy, String limit , boolean doClose) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        Image entity;
        java.util.ArrayList<Image> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new Image();
                String type = inflate(entity,c);
                if(type.equals(Emoticon.class.toString())){

                    toRet.add(entity.toEmoticon());
                }
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
    protected static Image findImageById(String id, boolean doClose){
        List<Image> list = find("UID=?", new String[]{String.valueOf(id)}, null, null, "1", doClose);
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    protected static Emoticon findEmoticonById(String id, boolean doClose){
        List list = find(  "UID=?", new String[]{String.valueOf(id)}, null, null, "1" , doClose);
        if (list.isEmpty()) return null;
        Object obj = list.get(0);
        if(obj instanceof Emoticon){
            return (Emoticon)obj;
        }
        return null;
    }
    static ArrayList<Image> findAll(){
        return find(null, null, null, null, null , true);
    }

    /**
     * 从数据库中提取
     */
    private static String inflate(Image entity, Cursor c) {
        String type=Image.class.toString();
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
                case "TYPE":
                    type = c.getString(c.getColumnIndex(name));
                    break;
                default:
                    LogX.e( LOGX_EMO , "unknown filed " + name);
            }
        }
        return type;
    }
    //endregion


}
