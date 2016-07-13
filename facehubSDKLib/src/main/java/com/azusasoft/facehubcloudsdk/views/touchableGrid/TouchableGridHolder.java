package com.azusasoft.facehubcloudsdk.views.touchableGrid;

import android.view.View;

/**
 * Created by SETA on 2016/7/13.
 */
public class TouchableGridHolder{
    public DataAvailable data;

    public TouchableGridHolder(View itemView) {
        itemView.setTag(this);
    }

    /**
     * 触摸时的效果
     */
    public void onTouchedEffect(){

    }

    /**
     * 脱手时的效果
     */
    public void offTouchEffect(){

    }

}
