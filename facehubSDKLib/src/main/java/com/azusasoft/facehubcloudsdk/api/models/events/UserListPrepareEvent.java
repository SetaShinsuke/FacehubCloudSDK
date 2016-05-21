package com.azusasoft.facehubcloudsdk.api.models.events;

/**
 * Created by SETA on 2016/5/21.
 * 用来通知列表下载完成的事件,不区分下载是否成功,只负责通知.
 */
public class UserListPrepareEvent {
    public String listId;

    public UserListPrepareEvent(String id){
        this.listId = listId;
    }
}
