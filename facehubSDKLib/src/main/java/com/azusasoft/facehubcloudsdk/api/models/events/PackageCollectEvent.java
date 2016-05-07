package com.azusasoft.facehubcloudsdk.api.models.events;

/**
 * Created by SETA on 2016/4/6.
 *
 * 表情包收藏完成事件，不区分收藏成功或失败，仅告知收藏操作完成;
 */
public class PackageCollectEvent {
//    public boolean isSuccess = true;
    public String emoPackageId="";
    public PackageCollectEvent(String emoPackageId){
        this.emoPackageId = emoPackageId;
    }
}
