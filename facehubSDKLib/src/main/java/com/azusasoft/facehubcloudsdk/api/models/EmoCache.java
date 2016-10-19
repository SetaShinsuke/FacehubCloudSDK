package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA_WORK on 2016/10/18.
 */

public class EmoCache {
    private long size = -1;
    private int fileCount = -1;

    public EmoCache(){

    }

    public EmoCache(int size , int fileCount){
        this.size = size;
        this.fileCount = fileCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
}
