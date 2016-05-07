package com.azusasoft.facehubcloudsdk.api.models.events;

/**
 * Created by SETA on 2016/4/5.
 *
 * 列表下载进度的事件
 * {@link #emoPackageId}下载进度改变的列表id;
 * {@link #percentage}为小于100的进度;
 */
public class DownloadProgressEvent {
    public String emoPackageId;

    public DownloadProgressEvent(String emoPackageId){
        this.emoPackageId = emoPackageId;
    }

    public float percentage;
}
