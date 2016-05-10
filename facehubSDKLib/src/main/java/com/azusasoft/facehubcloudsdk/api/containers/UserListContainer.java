package com.azusasoft.facehubcloudsdk.api.containers;

import com.azusasoft.facehubcloudsdk.api.models.UserList;

import java.util.HashMap;

/**
 * Created by SETA on 2016/5/9.
 * 用来管理内存中的{@link UserList};
 */
public class UserListContainer {
    private HashMap<String,UserList> userListHashMap = new HashMap<>();
}
