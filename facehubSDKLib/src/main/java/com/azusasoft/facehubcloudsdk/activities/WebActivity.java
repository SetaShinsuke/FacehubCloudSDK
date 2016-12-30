package com.azusasoft.facehubcloudsdk.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

import de.greenrobot.event.EventBus;


/**
 * Created by SETA on 2016/5/8.
 * 点击banner跳转到活动网页
 */
public class WebActivity extends BaseActivity {
    private Context context;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_web);
        context = this;
//        setStatusBarColor(FacehubApi.getApi().getActionbarColor());
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.setTitle("详情");
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getExtras() != null) {
            if(getIntent().getExtras().containsKey("title")){
                actionbar.setTitle(getIntent().getExtras().getString("title"));
            }
        }

        webView = (WebView)findViewById(R.id.web_view);
        assert webView != null;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);

        if (getIntent().getExtras() != null) {
            String webUrl = getIntent().getExtras().getString("web_url");
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(webUrl);
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            EventBus.getDefault().unregister(this);
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }catch (Exception e){
            LogX.w(getClass().getName() + " || EventBus 反注册出错 : " + e);
        }
    }

    public void onEvent(ExitViewsEvent exitViewsEvent){
        finish();
    }
}
