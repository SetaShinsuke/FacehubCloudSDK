package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.FHanlder;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;

/**
 * Created by SETA on 2016/5/13.
 * 没有网络时的提示
 */
public class NoNetView extends FrameLayout {
    private Context context;

    private FHanlder handler = new FHanlder();
    private Runnable reloadTask , showNoNetTask , setNetBadTask;
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
        ViewUtilMethods.addColorFilter(mDrawable,FacehubApi.getApi().themeOptions.getThemeColor());
        hide();
        mainView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        this.setNetBadTask = new Runnable() {
            @Override
            public void run() {
                isNetBad = true;
            }
        };
    }

    public void setOnReloadClick(final OnClickListener onReloadClick){
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(final View v) {
//                tLog("点击重新加载 !!");
                hide();
                handler.removeCallbacks(reloadTask);
                reloadTask = new Runnable() {
                    @Override
                    public void run() {
//                        tLog("执行点击 !!");
                        if(NetHelper.getNetworkType(context)==NetHelper.NETTYPE_NONE){
                            show();
                            return;
                        }
                        onReloadClick.onClick(v);
                    }
                };
                handler.postDelayed(reloadTask,1000);
            }
        };
        findViewById(R.id.reload_btn).setOnClickListener(onClickListener);
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void hide(){
        setVisibility(GONE);
    }


    public void initNoNetHandler(int duration, final Runnable showNoNetTask) {
        this.duration = duration;
        this.showNoNetTask = showNoNetTask;
    }

    public void startBadNetJudge(){
//        tLog("开始弱网判断");
        isNetBad = false;
        handler.removeCallbacks(showNoNetTask);
        handler.removeCallbacks(setNetBadTask);
        handler.postDelayed(showNoNetTask,duration);
        handler.postDelayed(setNetBadTask,duration);
    }

    public void cancelBadNetJudge(){
//        tLog("中断弱网判断");
        handler.removeCallbacks(showNoNetTask);
        handler.removeCallbacks(setNetBadTask);
        showNoNetTask = null;
        setNetBadTask = null;
        isNetBad = false;
    }

    public boolean isNetBad() {
//        tLog("网络差? : " + isNetBad);
        return isNetBad;
    }
}
