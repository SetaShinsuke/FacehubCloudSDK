package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.azusasoft.facehubcloudsdk.R;

import java.util.zip.Inflater;

/**
 * Created by SETA_WORK on 2016/8/9.
 */
public class DownloadFrameBtn extends FrameLayout {
    public DownloadFrameBtn(Context context) {
        super(context);
    }

    public DownloadFrameBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloadFrameBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DownloadFrameBtn(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void constructView(Context context){
        View mainView = LayoutInflater.from(context).inflate(R.layout.download_frame_btn,null);
        addView(mainView);
    }
}
