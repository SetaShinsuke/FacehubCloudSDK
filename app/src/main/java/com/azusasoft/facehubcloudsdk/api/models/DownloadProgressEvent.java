package com.azusasoft.facehubcloudsdk.api.models;

/**
 * Created by SETA on 2016/4/5.
 */
public class DownloadProgressEvent {
    public String emoPackageId;

    public DownloadProgressEvent(String emoPackageId){
        this.emoPackageId = emoPackageId;
    }

    public float percentage;
}
