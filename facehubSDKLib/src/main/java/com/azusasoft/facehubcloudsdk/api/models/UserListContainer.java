package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.utils.threadUtils.ThreadPoolManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA on 2016/5/9.
 * 用来管理内存中的{@link UserList};
 */
public class UserListContainer {
    private HashMap<String,UserList> userListHashMap = new HashMap<>();

    public ArrayList<UserList> getAllUserLists(){
        ArrayList<UserList> userLists = new ArrayList<>();
//        for(Map.Entry<String,UserList> entry : userListHashMap){
//
//        }
        return userLists;
    }

    public void put(String id, final UserList userList){
        userListHashMap.put(id,userList);
        //后台把列表同步到数据库
        Runnable userListSyncTask = new Runnable() {
            @Override
            public void run() {
                UserListDAO.save2DBWithClose(userList);
            }
        };
        ThreadPoolManager.getDbThreadPool().execute(userListSyncTask);
    }

    public UserList getUserListById(String id){
        UserList userList = userListHashMap.get(id);
        if(userList == null){
            userList = new UserList();
            userList.setId(id);
        }
        return userList;
    }


    public void restore(){
        ArrayList<UserList> userLists = UserListDAO.findAll();
        for(UserList userList : userLists){
            userListHashMap.put(userList.getId(),userList);
        }
    }

}
