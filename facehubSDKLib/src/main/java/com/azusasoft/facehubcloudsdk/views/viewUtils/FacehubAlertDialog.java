package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2016/3/27.
 */
public class FacehubAlertDialog extends FrameLayout {
    private final long DURATION = 2000;
    private boolean cancelable = true;
    private Runnable closeRunnable;
    private boolean isDestroyed = false;

    public FacehubAlertDialog(Context context) {
        super(context);
        constructView(context);
    }

    public FacehubAlertDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public FacehubAlertDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FacehubAlertDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.facehub_alert_dialog,null,false);
        addView(view);
        setVisibility(GONE);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cancelable) {
                    hide();
                }
            }
        });
    }

    public void showDownloadFail(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.download_fail);
        setVisibility(VISIBLE);
        cancelable = false;
    }

    public void showCollecting(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collecting);
        setVisibility(VISIBLE);
        cancelable = false;
    }

    public void showCollectFail(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collect_fail);
        setVisibility(VISIBLE);
//        closeInTime(DURATION+500);
        cancelable = true;
    }

    public void showCollectSuccess(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collect_success);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION);
    }

    public void showSyncHint(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_hint);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION);
    }

    public void showSyncing(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.syncing);
        setVisibility(VISIBLE);
        cancelable = false;
    }

    public void showSyncSuccess(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_success);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION);
    }

    public void showSyncFail(){
        if(isDestroyed){
            return;
        }
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_fail);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION+500);
    }

    public void hide(){
        if(isDestroyed){
            return;
        }
        setVisibility(GONE);
    }

    private void closeInTime(long duration){
        removeCallbacks(closeRunnable);
        closeRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    hide();
                }catch (Exception e){
                    LogX.w("FacehubAlertDialog自动关闭出错 : " + e);
                }
            }
        };
        postDelayed(closeRunnable,duration);
    }

    public void onDestroy(){
        isDestroyed = true;
        removeCallbacks(closeRunnable);
        closeRunnable = null;
        removeAllViews();
    }
}
