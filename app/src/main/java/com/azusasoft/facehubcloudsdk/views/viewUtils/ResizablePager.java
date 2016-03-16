package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by SETA on 2015/7/27.
 */
public class ResizablePager extends ViewPager {
    public ResizablePager(Context context) {
        super(context);
    }

    public ResizablePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = 0;
//        for(int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//            int h = child.getMeasuredHeight()*2;
//            if(h > height) height = h;
//        }
//
//        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
//
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // find the first child view
        View view = getChildAt(0);
        if (view != null) {
            // measure the first child view with the specified measure spec
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(getMeasuredWidth(), measureHeight(heightMeasureSpec, view));
    }

        /**
         * Determines the height of this view
         *
         * @param measureSpec A measureSpec packed into an int
         * @param view the base view with already measured height
         *
         * @return The height of the view, honoring constraints from measureSpec
         */
        private int measureHeight(int measureSpec, View view) {
            int result = 0;
            int specMode = MeasureSpec.getMode(measureSpec);
            int specSize = MeasureSpec.getSize(measureSpec);

            if (specMode == MeasureSpec.EXACTLY) {
                result = specSize;
            } else {
                // set the height from the base view if available
                if (view != null) {
                    result = view.getMeasuredHeight();
                }
                if (specMode == MeasureSpec.AT_MOST) {
                    result = Math.min(result, specSize);
                }
            }
//            HelpMethods.fastLog("measureHeight : " + result);
            return result;
        }
}
