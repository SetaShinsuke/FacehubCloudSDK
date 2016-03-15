package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA on 2016/3/10.
 */
public class RetryReq {
    private Long dbId;
    public static final int REMOVE_EMO = 0;
    public static final int REMOVE_LIST = 1;

    private int type = REMOVE_EMO;
    private String listId;
    private ArrayList<String> emoIds = new ArrayList<>();
    private Context context;
    private boolean isFinished = true;

    protected RetryReq(){

    }

    public RetryReq(int type , String listId , ArrayList<String> emoIds){
        this.type = type;
        this.listId = listId;
        this.emoIds = emoIds;
    }

    public void delete(){
        RetryReqDAO.delete(this);
    }

    protected void setDbId(Long dbId){
        this.dbId = dbId;
    }
    protected Long getDbId(){
        return this.dbId;
    }

    public int getType(){
        return type;
    }
    protected void setType(int type){
        this.type = type;
    }

    public boolean save2DB(){
        return RetryReqDAO.save2DB(this);
    }

    public String getListId() {
        return listId;
    }

    protected void setListId(String listId) {
        this.listId = listId;
    }

    public ArrayList<String> getEmoIds() {
        return emoIds;
    }

    protected void setEmoIds(ArrayList<String> emoIds) {
        this.emoIds = emoIds;
    }


    public boolean isFinished() {
        return isFinished;
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }
}
