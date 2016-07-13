package com.azusasoft.facehubcloudsdk.activities;

import android.os.Build;
import android.support.v4.app.FragmentActivity;

/**
 * Created by SETA on 2016/5/21.
 * 所有Activity的父类
 */
public class BaseActivity extends FragmentActivity {

    public void exitThis(){
        super.onBackPressed();
    }

    public void setStatusBarColor(int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }
}
