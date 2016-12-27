package com.azusasoft.facehubcloudsdk.views.advrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by SETA on 2016/6/3.
 */
public class RecyclerViewEx extends RecyclerView {
    public RecyclerViewEx(Context context) {
        super(context);
    }

    public RecyclerViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void disableItemAnimation(){
        getItemAnimator().setSupportsChangeAnimations(false);
    }

    public void enableItemAnimation(){
        getItemAnimator().setSupportsChangeAnimations(true);
    }
}
