package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.transition.Fade;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;

/**
 * Created by SETA on 2016/5/13.
 * 没有网络时的提示
 */
public class NoNetView extends FrameLayout {
    private Context context;

    private Handler handler = new Handler();
    private Runnable task , reloadTask;
    private int duration;
    private boolean isNetBad = false;

    public NoNetView(Context context) {
        super(context);
        constructView(context);
    }

    public NoNetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public NoNetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NoNetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    public void constructView(Context context){
        this.context = context;
        View mainView = LayoutInflater.from(context).inflate(R.layout.no_net,null);
        addView(mainView);
        View btn = findViewById(R.id.reload_btn);
        btn.setBackgroundResource(R.drawable.radius_rectangle_white);
        Drawable mDrawable = btn.getBackground();
        mDrawable.setColorFilter(new
                PorterDuffColorFilter( FacehubApi.getApi().getThemeColor() , PorterDuff.Mode.MULTIPLY));
        hide();
        mainView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void setOnReloadClick(final OnClickListener onReloadClick){
        //延迟1秒后再执行重新加载的操作
        findViewById(R.id.reload_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                handler.removeCallbacks(reloadTask);
                reloadTask = new Runnable() {
                    @Override
                    public void run() {
                        if(NetHelper.getNetworkType(context)==NetHelper.NETTYPE_NONE){
                            show();
                            return;
                        }
                        onReloadClick.onClick(v);
                    }
                };
                handler.postDelayed(reloadTask,1000);
                hide();
            }
        });
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void hide(){
        setVisibility(GONE);
    }


    public void initNoNetHandler(int duration, final Runnable showNoNetTask) {
        this.duration = duration;
        this.task = new Runnable() {
            @Override
            public void run() {
                isNetBad = true;
                showNoNetTask.run();
            }
        };
    }

    public void startBadNetJudge(){
        isNetBad = false;
        handler.removeCallbacks(task);
        handler.postDelayed(task,duration);
    }

    public void cancelBadNetJudge(){
        handler.removeCallbacks(task);
        isNetBad = false;
    }

    public boolean isNetBad() {
        return isNetBad;
    }
}
