package com.azusasoft.facehubcloudsdk.views.viewUtils;

import com.azusasoft.facehubcloudsdk.api.models.UserList;

/**
 * Created by SETA on 2016/3/18.
 * 用于监听表情键盘的列表切换事件
 */
public interface KeyboardListChangeListener {
    public void onListChange(UserList lastList , UserList currentList);
}
