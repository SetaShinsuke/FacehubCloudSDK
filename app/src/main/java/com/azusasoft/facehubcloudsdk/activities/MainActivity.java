package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.UserList;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.getApi;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private TextView responseText;

    private final String APP_ID = "65737441-7070-6c69-6361-74696f6e4944";
    private final String USER_ID = "045978c8-5d13-4a81-beac-4ec28d1f304f";
    private final String AUTH_TOKEN = "02db12b9350f7dceb158995c01e21a2a";

    private ArrayList<UserList> userLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        responseText = (TextView) findViewById(R.id.text_view);

        FacehubApi.init( getApplicationContext() );
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

    }


    public ArrayList<UserList> getUserLists() {
        return userLists;
    }

    public void setUserLists(ArrayList<UserList> userLists) {
        this.userLists = userLists;
    }

    public void onClick(View view) {
        Snackbar.make(view, "拉取中……", Snackbar.LENGTH_SHORT).show();
        responseText.setText("Pulling...");
        HandlerDemo handlerDemo = new HandlerDemo();
        switch (view.getId()) {
            case R.id.jump_to_store:
                break;

            case R.id.btn_get_banner:
                fastLog("开始拉取Banner");
                getApi().getBanners(new HandlerDemo());
                break;

            case R.id.get_tags_by_param:
                fastLog("开始拉取tags by param");
                getApi().getPackageTagsByParam("type=section", handlerDemo);
                break;

            case R.id.get_tags_by_section:
                fastLog("开始拉取tags by section");
                getApi().getPackageTagsBySection(handlerDemo);
                break;

            case R.id.get_pkgs_by_param:
                fastLog("开始 自定义param获取packages");
                getApi().getPackagesByParam("section=Section1&page=1&limit=8" , handlerDemo);
                break;

            case R.id.get_pkgs_by_section:
                fastLog("开始 获取packages by section");
                getApi().getPackagesBySection("Section1",1,8,handlerDemo );
                break;

            case R.id.get_pkg_detail:
                fastLog("开始拉取 包详情");
                getApi().getPackageDetailById( "cea1a522-bf17-4381-916d-58ca6c55132a" , handlerDemo );
                break;

            case R.id.collect_emo:
                fastLog("开始 收藏表情");
                getApi().collectEmoById("75a4b664-e22a-41e7-ad74-30cd4e0d30df",
                        "15088cc7-5e10-43cb-8613-14b83e860604", //我的收藏
                        handlerDemo);
                break;
            case R.id.collect_pkg_2new:
                fastLog("开始 收藏包到新列表");
                getApi().collectEmoPackageById("5b0cf1d8-ec5c-4e93-a7b0-0d6719f19981",handlerDemo);
                break;

            case R.id.collect_pkg:
                fastLog("开始 收藏包到已有列表");
                getApi().collectEmoPackageById("5b0cf1d8-ec5c-4e93-a7b0-0d6719f19981" , "eafaf90c-87af-44b5-a11e-57d1563edab6" , handlerDemo);
                break;

            case R.id.get_emo:
                fastLog("开始 获取单个表情");
                getApi().getEmoticonById("75a4b664-e22a-41e7-ad74-30cd4e0d30df",handlerDemo);
                break;

            case R.id.get_user_list:
                fastLog("开始 获取用户列表");
                getApi().getUserList( handlerDemo );
                break;

            case R.id.remove_emos:
                fastLog("开始 批量删除表情");
                ArrayList<String> ids = new ArrayList<>();
                ids.add("09453563-a55c-4e82-a773-8f08e0275b57");
                ids.add("c3becc2f-8e04-4a1a-985a-52f77c8b9bd6");
                ids.add("0733fb7f-622a-4461-8d3d-7ddb3098fc53");
                String listId = "927059af-3b98-4deb-adee-c1d33e7be653"; //test-package55
                getApi().removeEmoticonsByIds(ids,listId,handlerDemo);
                break;

            case R.id.remove_emo:
                fastLog("开始 删除单个表情");
                String emoId = "cf6d0b34-c4c9-4646-bf5f-22c6a7146407";
                String listId1 = "15088cc7-5e10-43cb-8613-14b83e860604"; //我的收藏
                getApi().removeEmoticonById(emoId,listId1,handlerDemo);
                break;

            case R.id.create_list:
                fastLog("开始 创建列表");
                getApi().createUserListByName("安卓测试列表"+System.currentTimeMillis()%10, handlerDemo);
                break;

            case R.id.rename_list:
                fastLog("开始 重命名列表");
                getApi().renameUserListById( "eafaf90c-87af-44b5-a11e-57d1563edab6", "贵族版重命名V" , handlerDemo );
                break;

            case R.id.delete_list:
                fastLog("开始 删除列表");
                getApi().removeUserListById("92b9b960-ba73-426e-b91e-da7bb5e535f0" , handlerDemo);
                break;

            case R.id.move_emo:
                fastLog("开始 移动表情");
                String emotId = "09453563-a55c-4e82-a773-8f08e0275b57";
                String fId = "f15d45a5-7648-47d4-8d81-23c5273141d1"; //Test Package 55
                String tId = "15088cc7-5e10-43cb-8613-14b83e860604";
                getApi().moveEmoticonById( emotId , fId , tId , handlerDemo );
                break;
            default:
                break;
        }
    }

    class HandlerDemo implements ResultHandlerInterface {

        @Override
        public void onResponse(Object response) {
            fastLog("response : " + response);
            responseText.setText("response : " + response);
            Snackbar.make(responseText, "获取成功", Snackbar.LENGTH_SHORT).show();
            FacehubApi.getDbHelper().export();
        }

        @Override
        public void onError(Exception e) {
            fastLog("error : " + e);
            responseText.setText("error : " + e);
            Snackbar.make(responseText, "获取失败", Snackbar.LENGTH_SHORT).show();
        }
    }

}
