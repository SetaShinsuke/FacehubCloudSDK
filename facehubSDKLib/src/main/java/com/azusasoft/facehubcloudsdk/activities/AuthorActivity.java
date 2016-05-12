package com.azusasoft.facehubcloudsdk.activities;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

/**
 * Created by SETA on 2016/5/12.
 */
public class AuthorActivity extends AppCompatActivity {
    String authorName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getExtras() != null) {
            if(getIntent().getExtras().containsKey("author_name")){
                authorName = getIntent().getExtras().getString("author_name");
            }
        }
        actionbar.setTitle("["+authorName+"]的主页");
    }
}
