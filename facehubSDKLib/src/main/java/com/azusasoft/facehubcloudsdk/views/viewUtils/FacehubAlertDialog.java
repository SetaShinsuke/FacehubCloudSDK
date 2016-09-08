package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOADING;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOAD_FAIL;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOAD_OVERSIZE;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.UPLOAD_SUCCESS;

/**
 * Created by SETA on 2016/3/27.
 */
public class FacehubAlertDialog extends FrameLayout {
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
        cancelable = false;
    }

    public void showCollecting(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collecting);
        setVisibility(VISIBLE);
        cancelable = false;
    }

    public void showCollectFail(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collect_fail);
        setVisibility(VISIBLE);
//        closeInTime(DURATION+500);
        cancelable = true;
    }

    public void showCollectSuccess(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.collect_success);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION);
    }

    public void showSyncHint(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_hint);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION);
    }

    public void showSyncing(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.syncing);
        setVisibility(VISIBLE);
        cancelable = false;
    }

    public void showSyncSuccess(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_success);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION);
    }

    public void showSyncFail(){
        ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.sync_fail);
        setVisibility(VISIBLE);
        cancelable = true;
        closeInTime(DURATION+500);
    }

    public void showUploadAlert(String type){
        switch (type){
            case UPLOAD_SUCCESS:
                ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.upload_success);
                setVisibility(VISIBLE);
                cancelable = true;
                closeInTime(DURATION);
                break;
            case UPLOAD_FAIL:
                ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.upload_fail);
                setVisibility(VISIBLE);
                cancelable = true;
                break;
            case UPLOAD_OVERSIZE:
                ((ImageView)findViewById(R.id.image_view_facehub)).setImageResource(R.drawable.upload_oversize);
                setVisibility(VISIBLE);
                cancelable = true;
                break;
        }
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
