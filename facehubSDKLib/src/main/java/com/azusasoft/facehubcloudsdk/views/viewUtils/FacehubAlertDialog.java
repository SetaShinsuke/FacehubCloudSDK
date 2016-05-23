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
public class
FacehubAlertDialog extends FrameLayout {
    private Context context;
    private final long DURATION = 2000;
    private boolean cancelable = true;

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
        this.context = context;
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
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.download_fail);
        setVisibility(VISIBLE);
    }

    public void showCollecting(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collecting);
        setVisibility(VISIBLE);
    }

    public void showCollectFail(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collect_fail);
        setVisibility(VISIBLE);
        closeInTime(DURATION+500);
    }

    public void showCollectSuccess(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collect_success);
        setVisibility(VISIBLE);
        closeInTime(DURATION);
    }

    public void showSycnHint(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_hint);
        setVisibility(VISIBLE);
        closeInTime(DURATION);
    }

    public void showSyncing(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.syncing);
        setVisibility(VISIBLE);
    }

    public void showSyncSuccess(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_success);
        setVisibility(VISIBLE);
        closeInTime(DURATION);
    }

    public void showSyncFail(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_fail);
        setVisibility(VISIBLE);
        closeInTime(DURATION+500);
    }

    public void hide(){
        setVisibility(GONE);
    }

    private void closeInTime(long duration){
        postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    hide();
                }catch (Exception e){
                    LogX.w("FacehubAlertDialog自动关闭出错 : " + e);
                }
            }
        },duration);
    }
}
