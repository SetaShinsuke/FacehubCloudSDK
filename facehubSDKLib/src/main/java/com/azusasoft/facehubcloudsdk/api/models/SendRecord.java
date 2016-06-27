package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA on 2016/6/22.
 * 发送记录
 */
public class SendRecord {
    Long dbId;
    public String date;
    public String emoId;
    public String userId;
    public int count = 0;

    public SendRecord(){

    }

    public SendRecord(String date,String emoId,String userId){
        this.date = date;
        this.emoId = emoId;
        this.userId = userId;
    }

    public void save(){
        SendRecordDAO.save2DB(this);
    }

    @Override
    public String toString() {
        return super.toString()
                + "\ndate : " + date
                + "\nemoId : " + emoId
                + "\nuserId : " + userId
                + "\ncount : " + count
                ;
    }
}
