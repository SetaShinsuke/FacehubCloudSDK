package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SETA on 2016/3/8.
 */
public class Emoticon extends Image {


    /**
     * @param doSave2DB 批量操作/获取包详情 时不单个记录数据库，在外面批量保存
     */
    public Emoticon emoticonFactoryByJson(JSONObject jsonObject , boolean doSave2DB) throws JSONException{
        super.imageFactoryByJson( jsonObject , doSave2DB);
//        if(doSave2DB) {
//            save2Db();
//        }
        return this;
    }

    public boolean isCollected(){
        return FacehubApi.getApi().isEmoticonCollected(getId());
    }

    /**
     * 保存表情到数据库
     *
     * @return 保存是否成功.
     */
//    public boolean save2Db(){
//        return ImageDAO.save2DB(this);
//    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    protected Image setId(String id) {
        return super.setId(id);
    }

    @Override
    public int getFsize() {
        return super.getFsize();
    }

    @Override
    protected Image setFsize(int size) {
        return super.setFsize(size);
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    protected Image setHeight(int height) {
        return super.setHeight(height);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    protected Image setWidth(int width) {
        return super.setWidth(width);
    }

    @Override
    public Format getFormat() {
        return super.getFormat();
    }

    @Override
    protected Image setFormat(String format) {
        return super.setFormat(format);
    }

    @Override
    public String getFileUrl(Size imgSize) {
        return super.getFileUrl(imgSize);
    }

    @Override
    protected Image setFileUrl(JSONObject obj) {
        return super.setFileUrl(obj);
    }

    @Override
    public boolean hasFile(Size size) {
        return super.hasFile(size);
    }

    @Override
    public String getFilePath(Size size) {
        return super.getFilePath(size);
    }

    @Override
    protected void setFilePath(Size size, String path) {
        super.setFilePath(size, path);
    }

    @Override
    public String toString() {
        return "\n[Emoticon] : " + "\nid : " + getId()
                + "\nfsize : " + getFsize()
                +"\nheight : " + getHeight()
                +"\nwidth : " + getWidth()
                +"\nformat : " + getFormat()
                +"\nmediumUrl : " + getFileUrl(Size.MEDIUM)
                +"\nfullUrl : " + getFileUrl(Size.FULL);
    }



//    @Override
//    public boolean equals(Object o) {
//        return (o instanceof Emoticon)
//                && ((Emoticon) o).getId().equals(getId())
//                && ((Emoticon) o).getFormat().equals(getFormat())
//                && ((Emoticon) o).getFsize() == getFsize()
//                && ((Emoticon) o).getHeight() == getHeight()
//                && ((Emoticon) o).getWidth() == getWidth();
//    }


    @Override
    public void download2Cache(Size size, ResultHandlerInterface resultHandlerInterface) {
        super.download2Cache(size, resultHandlerInterface);
    }

    @Override
    public void download2File(Size size, ResultHandlerInterface resultHandlerInterface) {
        super.download2File(size, resultHandlerInterface);
    }


}
