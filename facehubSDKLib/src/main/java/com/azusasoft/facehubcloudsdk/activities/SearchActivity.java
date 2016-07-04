package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnTouchEffect;

/**
 * Created by SETA on 2016/6/21.
 * 搜索页
 */
public class SearchActivity extends BaseActivity {
    private Context context;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        this.context = this;
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }
        findViewById(R.id.search_title_bar).setBackgroundColor(FacehubApi.getApi().getThemeColor());

        editText = (EditText) findViewById(R.id.edit_text_search);
        RecyclerViewEx hotHistoryRecyclerView = (RecyclerViewEx) findViewById(R.id.search_hot_tag_history);
        editText.setFocusableInTouchMode(false);

        View cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnTouchListener(new OnTouchEffect());
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
