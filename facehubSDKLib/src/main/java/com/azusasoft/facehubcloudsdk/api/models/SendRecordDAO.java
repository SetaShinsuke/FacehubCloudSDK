package com.azusasoft.facehubcloudsdk.api.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/6/22.
 * 数据库-记录发送数据
 */
public class SendRecordDAO {
    private final static String TABLENAME = "SendRecord";


    public static SendRecord getUniqueSendRecord(String date,String emoId,String userId){
        SendRecord sendRecord = findSendRecordBy(date,emoId,userId);
        if(sendRecord==null){
            sendRecord = new SendRecord(date,emoId,userId);
        }
        return sendRecord;
    }


    /**
     * 创建表
     * @param database {@link SQLiteDatabase}对象
     */
    public static void createTable(SQLiteDatabase database) {
        String sql = "create table if not exists " + TABLENAME + " (" +
                " ID INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", DATE TEXT  " +
                ", EMO_ID TEXT  " +
                ", USER_ID TEXT  " +
                ", COUNT INT  " +
                " );";
        database.execSQL(sql);
    }

    //更新表
    public static void updateTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 2) { // 1/2版本升级过来
            updateFrom2(db);
        }
    }
    private static void updateFrom2(SQLiteDatabase db) {
        createTable(db);
    }

    //region 保存
    protected static boolean save2DB(final SendRecord sendRecord) {
        boolean ret = false;
        try {
            SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
            ret = save(sendRecord, db);
            db.close();
        } catch (Exception e) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Get new entry
                    SQLiteDatabase db = FacehubApi.getDbHelper().getWritableDatabase();
                    save(sendRecord, db);
                    db.close();
                }
            }, 100);
        }
        return ret;
    }

    /**
     * 保存 emoticon 到数据库
     *
     * @param sendRecord {@link SendRecord}对象
     * @param db         数据库
     * @return 保存是否成功
     */
    static private boolean save(SendRecord sendRecord, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("DATE", String.valueOf(sendRecord.date));
        values.put("EMO_ID", String.valueOf(sendRecord.emoId));
        values.put("USER_ID", String.valueOf(sendRecord.userId));
        values.put("COUNT", String.valueOf(sendRecord.count));
        long ret;
        //如果数据库中已经有该id对应的数据，则进行update.否则insert.
        SendRecord recordInDb = findSendRecordBy(sendRecord.date, sendRecord.emoId, sendRecord.userId);
        if (recordInDb != null) {
            sendRecord.dbId = recordInDb.dbId;
        } else {
            sendRecord.dbId = null;
        }

        if (sendRecord.dbId== null) {
            ret = db.insert(TABLENAME, null, values);
            sendRecord.dbId = ret;
        } else {
            ret = db.update(TABLENAME, values, "ID = ?", new String[]{String.valueOf(sendRecord.dbId)});
        }

        return ret > 0;
    }

    protected static SendRecord findSendRecordBy(String date, String emoId, String userId) {
        ArrayList<SendRecord> results = find("EMO_ID=?", new String[]{String.valueOf(emoId)}, null, null, "1", false);
        if (results.isEmpty()) return null;
        for(SendRecord sendRecord:results){
            if(date.equals(sendRecord.date) && userId.equals(sendRecord.userId)){
                return sendRecord;
            }
        }
        return null;
    }

    /**
     * 从数据库中查找
     */
    private static ArrayList<SendRecord> find(String whereClause, String[] whereArgs,
                                           String groupBy, String orderBy, String limit , boolean doClose) {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getReadableDatabase();
        SendRecord entity;
        ArrayList<SendRecord> toRet = new ArrayList<>();
        Cursor c = sqLiteDatabase.query(TABLENAME, null,
                whereClause, whereArgs, groupBy, null, orderBy, limit);
        try {
            while (c.moveToNext()) {
                entity = new SendRecord();
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

    private static void inflate(SendRecord entity, Cursor c) {
        for (String name : c.getColumnNames()) {
            switch (name) {
                case "ID":
                    entity.dbId = c.getLong(c.getColumnIndex(name));
                    break;
                case "DATE":
                    entity.date = c.getString(c.getColumnIndex(name));
                    break;
                case "EMO_ID":
                    entity.emoId = c.getString(c.getColumnIndex(name));
                    break;
                case "USER_ID":
                    entity.userId = c.getString(c.getColumnIndex(name));
                    break;
                case "COUNT":
                    entity.count = c.getInt(c.getColumnIndex(name));
                    break;
                default:
                    LogX.e("unknown field " + name);
            }
        }
    }

    public static ArrayList<SendRecord> findAll(){
        return find(null, null, null, null, null , true);
    }

    public static void deleteAll() {
        SQLiteDatabase sqLiteDatabase = FacehubApi.getDbHelper().getWritableDatabase();
        sqLiteDatabase.delete(TABLENAME, null , null);
        sqLiteDatabase.close();
    }
}

