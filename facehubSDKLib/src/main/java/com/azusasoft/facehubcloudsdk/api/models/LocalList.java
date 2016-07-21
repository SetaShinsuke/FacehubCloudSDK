package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA_WORK on 2016/7/21.
 */
public class LocalList extends UserList {
    private int rowNum,columnNum;
    private boolean needMixLayout = false;

    public int getRowNum() {
        return rowNum;
    }

    protected void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColumnNum() {
        return columnNum;
    }

    protected void setColumnNum(int columnNum) {
        this.columnNum = columnNum;
    }

    public boolean isNeedMixLayout() {
        return needMixLayout;
    }

    protected void setNeedMixLayout(boolean needMixLayout) {
        this.needMixLayout = needMixLayout;
    }
}
