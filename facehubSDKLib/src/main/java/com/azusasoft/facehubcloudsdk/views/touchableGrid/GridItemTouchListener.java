package com.azusasoft.facehubcloudsdk.views.touchableGrid;

import android.view.View;

/**
 * 表情点击/长按/松手的回调接口
 */
public interface GridItemTouchListener {
    /**
     * 点击条目
     * @param view 点击的视图
     * @param object 传递的数据
     */
    void onItemClick(View view, DataAvailable object);

    /**
     * 长按条目
     * @param view 长按的视图
     * @param data 传递的数据
     */
    void onItemLongClick(View view, DataAvailable data);

    /**
     *
     * @param view
     * @param object
     */
    void onItemOffTouch(View view, DataAvailable object);
}