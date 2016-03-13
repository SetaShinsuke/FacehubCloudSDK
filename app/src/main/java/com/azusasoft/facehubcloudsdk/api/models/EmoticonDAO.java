package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;

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
        if (obj.getDbId() == null) {
            ret = db.insert(TABLENAME, null, values);
            obj.setDbId( ret );
        } else
            ret = db.update(TABLENAME, values, "ID = ?", new String[]{String.valueOf(obj.getDbId())});

        return ret > 0;
    }
}
