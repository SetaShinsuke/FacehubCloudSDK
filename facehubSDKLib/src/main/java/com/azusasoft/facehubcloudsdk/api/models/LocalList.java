package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA_WORK on 2016/7/21.
 */
public class LocalList extends UserList {
    //id,needMixLayout,rowNum,columnNum,localType,isLocal
    private int rowNum,columnNum;
    private boolean needMixLayout = false;
    private String localType;

    public LocalList(){
        setLocal(true);
    }

    public int getRowNum() {
        return rowNum;
    }

    protected LocalList setRowNum(int rowNum) {
        this.rowNum = rowNum;
        return this;
    }

    public int getColumnNum() {
        return columnNum;
    }

    protected LocalList setColumnNum(int columnNum) {
        this.columnNum = columnNum;
        return this;
    }

    public boolean isNeedMixLayout() {
        return needMixLayout;
    }

    protected LocalList setNeedMixLayout(boolean needMixLayout) {
        this.needMixLayout = needMixLayout;
        return this;
    }

    public String getLocalType() {
        return localType;
    }

    /**
     * 获取本地列表的类型
     * @param localType 获取本地列表的类型，目前有三种:local_emoticons,custom_list,voice
     */
    protected LocalList setLocalType(String localType) {
        this.localType = localType;
        return this;
    }
}
