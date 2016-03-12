package com.azusasoft.facehubcloudsdk.api.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SETA on 2016/3/8.
 */
public class Emoticon extends Image {

    public Emoticon emoticonFactoryByJson(JSONObject jsonObject) throws JSONException{
        super.imageFactoryByJson( jsonObject );
        return this;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public Image setId(String id) {
        return super.setId(id);
    }

    @Override
    public int getFsize() {
        return super.getFsize();
    }

    @Override
    public Image setFsize(int size) {
        return super.setFsize(size);
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    public Image setHeight(int height) {
        return super.setHeight(height);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    public Image setWidth(int width) {
        return super.setWidth(width);
    }

    @Override
    public Format getFormat() {
        return super.getFormat();
    }

    @Override
    public Image setFormat(String format) {
        return super.setFormat(format);
    }

    @Override
    public String getFileUrl(Size imgSize) {
        return super.getFileUrl(imgSize);
    }

    @Override
    public Image setFileUrl(JSONObject obj) {
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
    public void setFilePath(Size size, String path) {
        super.setFilePath(size, path);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
