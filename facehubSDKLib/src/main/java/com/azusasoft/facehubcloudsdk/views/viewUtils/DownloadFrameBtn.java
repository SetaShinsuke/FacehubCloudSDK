package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;

import java.util.zip.Inflater;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.themeOptions;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.addColorFilter;

/**
 * Created by SETA_WORK on 2016/8/9.
 */
public class DownloadFrameBtn extends FrameLayout {
    private TextView downloadText;
    private CollectProgressBar progressBar;

    public DownloadFrameBtn(Context context) {
        super(context);
        constructView(context);
    }

    public DownloadFrameBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public DownloadFrameBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DownloadFrameBtn(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        View mainView = LayoutInflater.from(context).inflate(R.layout.download_frame_btn,null);
        addView(mainView);
        downloadText = (TextView) mainView.findViewById(R.id.download_text);
        progressBar = (CollectProgressBar) mainView.findViewById(R.id.progress_bar);
    }

    public void setTextWidth(int width){
        ViewGroup.LayoutParams params = downloadText.getLayoutParams();
        params.width = width;
        downloadText.setLayoutParams(params);
    }

    public void showDownloaded() {
        downloadText.setVisibility(View.VISIBLE);
        downloadText.setText("已下载");
        addColorFilter(downloadText.getBackground(),themeOptions.getDownloadFrameFinColor());
        downloadText.setTextColor(themeOptions.getDownloadFrameFinColor());
        progressBar.setVisibility(View.GONE);
    }

    public void showDownloadBtn() {
        downloadText.setVisibility(View.VISIBLE);
        downloadText.setText("下载");
        addColorFilter(downloadText.getBackground(),themeOptions.getDownloadFrameColor());
        downloadText.setTextColor(themeOptions.getDownloadFrameColor());
        progressBar.setVisibility(View.GONE);
    }

    public void showProgressBar(final float percent) {
        downloadText.setVisibility(View.GONE);
        downloadText.setText("下载");
        downloadText.setTextColor(themeOptions.getDownloadFrameColor());
        progressBar.setVisibility(View.VISIBLE);
        fastLog("More 更新进度 : " + percent);
        progressBar.setPercentage(percent);
    }
}
