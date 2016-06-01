package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by SETA on 2016/5/31.
 */
public class ItemNoneChangeAnimator extends RecyclerView.ItemAnimator {
    private DefaultItemAnimator defaultItemAnimator;

    public ItemNoneChangeAnimator(){
        this.defaultItemAnimator = new DefaultItemAnimator();
    }

    @Override
    public void runPendingAnimations() {
        defaultItemAnimator.runPendingAnimations();
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        return defaultItemAnimator.animateRemove(holder);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        return defaultItemAnimator.animateAdd(holder);
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        return defaultItemAnimator.animateRemove(holder);
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
        return false;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        defaultItemAnimator.endAnimation(item);
    }

    @Override
    public void endAnimations() {
        defaultItemAnimator.endAnimations();
    }

    @Override
    public boolean isRunning() {
        return defaultItemAnimator.isRunning();
    }
}
