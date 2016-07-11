package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

/**
 * Created by SETA on 2016/7/11.
 */
public class EmoStoreActivityWC extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_emoticon_store_wc);
        setStatusBarColor(FacehubApi.getApi().getActionbarColor());
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.showSettings();
        actionbar.setTitle(FacehubApi.getApi().getEmoStoreTitle());
        actionbar.showBackBtn(false,true);
        actionbar.setOnCloseBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitThis();
            }
        });
        actionbar.setOnSettingsClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListsManageActivityNew.class);
                context.startActivity(intent);
            }
        });
        actionbar.setSettingBtnImg(R.drawable.setting_wc);
    }
}