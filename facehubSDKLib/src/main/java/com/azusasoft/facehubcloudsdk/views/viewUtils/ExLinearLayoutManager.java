package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2015/9/9.
 * 一个让{@link RecyclerView}的高度可以wrap_content的{@link LinearLayoutManager}
 */
public class ExLinearLayoutManager extends LinearLayoutManager {

    public ExLinearLayoutManager(Context context) {
        super(context);
    }

    public ExLinearLayoutManager(Context context, int orientation, boolean reverseLayout, int[] mMeasuredDimension) {
        super(context, orientation, reverseLayout);
        this.mMeasuredDimension = mMeasuredDimension;
    }
//    public ExLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    public ExLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setmView(RecyclerView mView) {
        this.mView = mView;
    }

    private RecyclerView mView;
    private int[] mMeasuredDimension = new int[2];

//    @Override
//    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
//                          int widthSpec, int heightSpec) {
//        LogX.d("ilike", "onMeasure");
//        LogX.d("ilike", state.toString());
//
//        LogX.d("ilike", "" + state.didStructureChange());
//
//        final int widthMode = View.MeasureSpec.getMode(widthSpec);
//        final int heightMode = View.MeasureSpec.getMode(heightSpec);
//        final int widthSize = View.MeasureSpec.getSize(widthSpec);
//        final int heightSize = View.MeasureSpec.getSize(heightSpec);
//        int width = 0;
//        int height = 0;
//        if (state.didStructureChange()) {
//            for (int i = 0; i < state.getItemCount(); i++) {
//                measureScrapChild(recycler, i,
//                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
//                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
//                        mMeasuredDimension);
//
//                if (getOrientation() == HORIZONTAL) {
//                    width = width + mMeasuredDimension[0];
//                    if (i == 0) {
//                        height = mMeasuredDimension[1];
//                    }
//                } else {
//                    height = height + mMeasuredDimension[1];
//                    if (i == 0) {
//                        width = mMeasuredDimension[0];
//                    }
//                }
//            }
//        }
//        switch (widthMode) {
//            case View.MeasureSpec.EXACTLY:
//                width = widthSize;
//            case View.MeasureSpec.AT_MOST:
//            case View.MeasureSpec.UNSPECIFIED:
//        }
//
//        switch (heightMode) {
//            case View.MeasureSpec.EXACTLY:
//                height = heightSize;
//            case View.MeasureSpec.AT_MOST:
//            case View.MeasureSpec.UNSPECIFIED:
//        }
//        setMeasuredDimension(width, height);
//
//
//    }


   // @Override
    public void onMeasure1(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        int width = 0;
        int height = 0;

        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
            case View.MeasureSpec.AT_MOST:
                width = widthSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                width = ViewCompat.getMinimumWidth(this.mView);
                break;
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
            case View.MeasureSpec.AT_MOST:
                height = heightSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                height = ViewCompat.getMinimumHeight(this.mView);
                break;
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
        try {
            View view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);
                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        } catch (Exception e) {
//            LogX.e("ExLinearLayoutManager.measureScrapChild() Error !! Detail : " + e );
            e.printStackTrace();
        }

    }
}
