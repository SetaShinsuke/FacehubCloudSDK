package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.os.Bundle;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2016/6/21.
 * 搜索页
 */
public class SearchActivity extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        this.context = this;
    }
}
