package com.azusasoft.facehubcloudsdk.api.models.events;

/**
 * Created by SETA on 2016/4/6.
 */
public class PackageCollectEvent {
//    public boolean isSuccess = true;
    public String emoPackageId="";
    public PackageCollectEvent(String emoPackageId){
        this.emoPackageId = emoPackageId;
    }
}
