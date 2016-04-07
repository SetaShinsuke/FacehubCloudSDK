package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.Image;

/**
 * Created by SETA on 2016/3/27.
 */
public class FacehubAlertDialog extends FrameLayout {
    private Context context;
    private final long DURATION = 1000;
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
        ((ImageView)findViewById(R.id.image_view)).setImageResource(R.drawable.download_fail);
        setVisibility(VISIBLE);
    }

    public void showCollecting(){
        ((ImageView)findViewById(R.id.image_view)).setImageResource(R.drawable.collecting);
        setVisibility(VISIBLE);
    }

    public void showCollectFail(){
        ((ImageView)findViewById(R.id.image_view)).setImageResource(R.drawable.collect_fail);
        setVisibility(VISIBLE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, DURATION);
    }

    public void showCollectSuccess(){
        ((ImageView)findViewById(R.id.image_view)).setImageResource(R.drawable.collect_success);
        setVisibility(VISIBLE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        },DURATION);
    }

    public void hide(){
        setVisibility(GONE);
    }
}
