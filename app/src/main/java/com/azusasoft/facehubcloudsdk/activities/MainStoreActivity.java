package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.getApi;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

public class MainStoreActivity extends AppCompatActivity {
    private Context mContext;
    private View btn;

    private final String APP_ID = "65737441-7070-6c69-6361-74696f6e4944";
    private final String USER_ID = "045978c8-5d13-4a81-beac-4ec28d1f304f";
    private final String AUTH_TOKEN = "02db12b9350f7dceb158995c01e21a2a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);

        FacehubApi.init();
        getApi().setAppId(APP_ID);
        getApi().setCurrentUserId(USER_ID, AUTH_TOKEN, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                fastLog("用户设置成功");
            }

            @Override
            public void onError(Exception e) {

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make( btn , "拉取中……" , Snackbar.LENGTH_SHORT).show();
                fastLog("开始拉取Banner");
                getApi().getBanners(new HandlerDemo());
            }
        });

    }

    class HandlerDemo implements ResultHandlerInterface{

        @Override
        public void onResponse(Object response) {
            fastLog("response : " + response);
            Snackbar.make( btn , "获取成功" , Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Exception e) {
            fastLog("error : " + e);
            Snackbar.make(btn, "获取失败", Snackbar.LENGTH_SHORT).show();
        }
    }
}
