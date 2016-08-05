package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.themeOptions;

/**
 * Created by SETA_WORK on 2016/8/5.
 */
public class DownloadSolidBtn extends FrameLayout {
    private Context context;

    private View downloadBtn ;
    private View downloadIcon;
    private View progressbarArea;
    private TextView downloadText;
    private CollectProgressBar progressBar;
    private View cancelBtn;

    public DownloadSolidBtn(Context context) {
        super(context);
        constructView(context);
    }

    public DownloadSolidBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public DownloadSolidBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DownloadSolidBtn(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        View mainView = LayoutInflater.from(context).inflate(R.layout.download_solid_btn,null);
        addView(mainView);

        downloadBtn = mainView.findViewById(R.id.download_btn);
        downloadIcon = mainView.findViewById(R.id.download_icon);
        downloadText = (TextView) mainView.findViewById(R.id.download_text);
        cancelBtn = mainView.findViewById(R.id.cancel_download);

        progressbarArea = mainView.findViewById(R.id.progress_area);
        progressBar = (CollectProgressBar) mainView.findViewById(R.id.progress);
        if(!isInEditMode()) {
            downloadText.setTextColor(themeOptions.getDownloadSolidBtnTextColor());
        }
        showDownloadBtn();
    }

    //下载按钮
    public void showDownloadBtn(){
        downloadBtn.setBackgroundColor(themeOptions.getDownloadBtnBgSolidColor());
        downloadIcon.setVisibility(View.VISIBLE);
        downloadText.setVisibility(View.VISIBLE);
        downloadText.setText("下载");
        progressbarArea.setVisibility(View.GONE);
    }

    //已下载
    public void showDownloaded(){
        downloadBtn.setBackgroundColor(themeOptions.getDownloadBtnBgSolidFinColor());
        downloadIcon.setVisibility(View.GONE);
        downloadText.setVisibility(View.VISIBLE);
        downloadText.setText("已下载");
        progressbarArea.setVisibility(View.GONE);

    }

    //下载中
    public void showProgress(){
        downloadBtn.setBackgroundColor(themeOptions.getProgressBgColor());
        downloadIcon.setVisibility(View.GONE);
        downloadText.setVisibility(View.GONE);
        progressbarArea.setVisibility(View.VISIBLE);
    }

    public void setProgress(float percentage){
        progressBar.setPercentage(percentage);
    }

    public void setOnDownloadCLick(OnClickListener onClickListener){
        downloadBtn.setOnClickListener(onClickListener);
    }

    public void setOnCancelClick(OnClickListener onCancelClick){
        cancelBtn.setOnClickListener(onCancelClick);
    }
}
