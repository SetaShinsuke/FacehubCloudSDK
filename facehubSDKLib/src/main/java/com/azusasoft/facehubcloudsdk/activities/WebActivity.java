package com.azusasoft.facehubcloudsdk.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }
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
    }
}
