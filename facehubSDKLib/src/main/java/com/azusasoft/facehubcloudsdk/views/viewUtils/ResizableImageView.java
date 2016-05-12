package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2015/4/17.
 */
public class ResizableImageView extends ImageView {
    private boolean byWidth=true;
    private boolean doResize = true;

    public ResizableImageView(Context context) {
        super(context);
    }
    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }
    public ResizableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ResizableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context,AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ResizableImageView,
                0, 0);
        try {
            byWidth = a.getBoolean(R.styleable.ResizableImageView_resize_by_width, true);
        } finally {
            a.recycle();
        }
    }

    private double heightRatio=0;
    private double widthRatio=0;
    public void setHeightRatio(double ratio){
        if(ratio!=heightRatio){
            this.heightRatio = ratio;
            this.widthRatio  = 0;
            byWidth = true;
            invalidate();
            requestLayout();
        }
    }
    public void setWidthRatio(double ratio){
        if(ratio!=widthRatio){
            this.widthRatio = ratio;
            this.heightRatio = 0;
            byWidth = false;
            invalidate();
            requestLayout();
        }
    }

    public void setDoResize(boolean doResize){
        this.doResize = doResize;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(!doResize){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            return;
        }
        Drawable d = getDrawable();

        if (d != null) {
            if(byWidth){
                if ( heightRatio > 0.0 ) {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = (int) (width * heightRatio);
                    setMeasuredDimension(width, height);
                }else {
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = (int) Math.ceil((float) width
                            * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
                    setMeasuredDimension(width, height);
                }
            }else {
                if( widthRatio > 0.0){
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    int width = (int) (height * widthRatio);
                    setMeasuredDimension(width, height);
                }else {
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    int width = (int) Math.ceil((float) height
                            * (float) d.getIntrinsicWidth() / (float) d.getIntrinsicHeight());
                    setMeasuredDimension(width, height);
                }
            }

            // ceil not round - avoid thin vertical gaps along the left/right edges
//            int width = MeasureSpec.getSize(widthMeasureSpec);
//            int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
//            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
