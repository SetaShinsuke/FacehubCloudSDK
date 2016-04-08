package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2016/4/5.
 */
public class CollectProgressBar extends FrameLayout {
    private Context context;
    private float percentage;

    public CollectProgressBar(Context context) {
        super(context);
        constructView(context);
    }

    public CollectProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public CollectProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CollectProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    public void constructView(Context context){
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.collect_progress,null);
        addView(view);
        setPercentage(30);
    }

    Runnable updateRunnable;
    public void setPercentage(float percentage){
        final View view = findViewById(R.id.percentage_facehub);
        if(percentage<0){
            percentage = 0;
        }else if(percentage>100){
            percentage = 100;
        }
        this.percentage = percentage;
        final View view2 = findViewById(R.id.view2_facehub);
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        final LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) view2.getLayoutParams();
        params.weight = percentage;
        params2.weight = 100-percentage;
        view2.removeCallbacks(updateRunnable);
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                view.setLayoutParams(params);
                view2.setLayoutParams(params2);
                view.forceLayout();
                view2.forceLayout();
            }
        };
        view2.post(updateRunnable);
    }

    public float getPercentage(){
        return percentage;
    }
}
