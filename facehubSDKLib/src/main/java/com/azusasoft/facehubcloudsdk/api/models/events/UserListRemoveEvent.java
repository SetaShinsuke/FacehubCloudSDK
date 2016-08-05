package com.azusasoft.facehubcloudsdk.api.models.events;

/**
 * Created by SETA on 2016/4/6.
 *
 * 列表删除事件，用于告知视图更新;
 */
public class UserListRemoveEvent {
    public String userListId;
//    public String forkFromId;
    public UserListRemoveEvent(String userListId){
        this.userListId = userListId;
//        this.forkFromId = forkFromId;
    }
}
