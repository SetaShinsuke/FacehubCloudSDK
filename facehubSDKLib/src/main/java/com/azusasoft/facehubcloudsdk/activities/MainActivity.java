package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
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
    //内网
    private final String USER_ID = "045978c8-5d13-4a81-beac-4ec28d1f304f";
    private final String AUTH_TOKEN = "02db12b9350f7dceb158995c01e21a2a";

    //外网
//    private final String USER_ID = "073c31aa-6bc3-437a-a1b1-0f25bdd88c28";
//    private final String AUTH_TOKEN = "6b722442f6eb3f1f8b3d545ea48b2c1e";

    private UserList tmpList;
    private String tmpEmoId = "94512ae6-0276-4ba1-81ea-dd3317f49c64";

    private ArrayList<UserList> userLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdk_main);
        mContext = this;
        responseText = (TextView) findViewById(R.id.text_view);

        FacehubApi.init(getApplicationContext());
        //如果使用SDK提供的Views
        FacehubApi.initViews(getApplicationContext());

        getApi().setAppId(APP_ID);

        //todo:模拟恢复用户
        FacehubApi.getApi().getUser().setUserId(USER_ID, AUTH_TOKEN);
    }

    private void setUser(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        getApi().setCurrentUserId(USER_ID, AUTH_TOKEN, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                fastLog("用户设置成功");
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                responseText.setText("用户设置成功.");
            }

            @Override
            public void onError(Exception e) {
                responseText.setText("用户设置  Error : " + e);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
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
        final HandlerDemo handlerDemo = new HandlerDemo();
        Intent intent;
        int i = view.getId();
        if (i == R.id.copy_db) {
            new HandlerDemo().onResponse("导出成功");

        } else if (i == R.id.sync) {
            setUser();

        } else if (i == R.id.jump_to_keyboard) {
            intent = new Intent(mContext, KeyboardActivity.class);
            mContext.startActivity(intent);
            handlerDemo.onResponse("跳转完成.");

        } else if (i == R.id.jump_to_manage_list) {
            intent = new Intent(mContext, ListsManageActivity.class);
            mContext.startActivity(intent);

        } else if (i == R.id.jump_to_manage) {
            intent = new Intent(mContext, ManageEmoticonsActivity.class);
            mContext.startActivity(intent);

        } else if (i == R.id.jump_to_store) {
            intent = new Intent(mContext, EmoStoreActivity.class);
            mContext.startActivity(intent);
            handlerDemo.onResponse("跳转完成.");

        } else if (i == R.id.btn_get_banner) {
            fastLog("开始拉取Banner");
            getApi().getBanners(new HandlerDemo());

        } else if (i == R.id.get_tags_by_param) {
            fastLog("开始拉取tags by param");
            getApi().getPackageTagsByParam("tag_type=section", handlerDemo);

        } else if (i == R.id.get_tags_by_section) {
            fastLog("开始拉取tags by section");
            getApi().getPackageTagsBySection(handlerDemo);

        } else if (i == R.id.get_pkgs_by_param) {
            fastLog("开始 自定义param获取packages");
//                getApi().getPackagesByParam("section=Section1&page=1&limit=8" , handlerDemo);
            getApi().getPackagesByParam("tags[]=Section1&page=1&limit=8", handlerDemo);

        } else if (i == R.id.get_pkgs_by_section) {
            fastLog("开始 获取packages by section");
            ArrayList<String> tags = new ArrayList<>();
            tags.add("Section1");
            getApi().getPackagesByTags(tags, 1, 8, handlerDemo);

        } else if (i == R.id.get_pkg_detail) {
            fastLog("开始拉取 包详情");
            getApi().getPackageDetailById("cea1a522-bf17-4381-916d-58ca6c55132a", handlerDemo);

        } else if (i == R.id.collect_emo) {
            fastLog("开始 收藏表情");
            if (deny()) {
                return;
            }
            getApi().collectEmoById(tmpEmoId, tmpList.getId(), handlerDemo);

        } else if (i == R.id.collect_pkg_2new) {
            fastLog("开始 收藏包到新列表");
//                if(deny()){
//                    return;
//                }
            getApi().collectEmoPackageById("b96bb5d2-2da4-4089-9cf1-cce017a4349a", handlerDemo);

        } else if (i == R.id.collect_pkg) {
            fastLog("开始 收藏包到已有列表");
//                if(deny()){
//                    return;
//                }
//                getApi().collectEmoPackageById("b6c3fbf0-7e71-4ae5-afbd-e71d0d4b4d18" , tmpList.getId() , handlerDemo);
            getApi().collectEmoPackageById("b96bb5d2-2da4-4089-9cf1-cce017a4349a", "f4347a79-fa89-4afa-bf43-3e7376890ea3", handlerDemo);

        } else if (i == R.id.get_emo) {
            fastLog("开始 获取单个表情");
            getApi().getEmoticonById("75a4b664-e22a-41e7-ad74-30cd4e0d30df", handlerDemo);


//            case R.id.get_user_list:
//                fastLog("开始 获取用户列表");
//                getApi().getUserList( handlerDemo );
//                break;

//            case R.id.remove_emos:
//                fastLog("开始 批量删除表情");
//                ArrayList<String> ids = new ArrayList<>();
//                ids.add("09453563-a55c-4e82-a773-8f08e0275b57");
//                ids.add("c3becc2f-8e04-4a1a-985a-52f77c8b9bd6");
//                ids.add("0733fb7f-622a-4461-8d3d-7ddb3098fc53");
//                String listId = "927059af-3b98-4deb-adee-c1d33e7be653"; //test-package55
//                getApi().removeEmoticonsByIds(ids,listId,handlerDemo);
//                break;
        } else if (i == R.id.remove_emo) {
            fastLog("开始 删除单个表情");
            if (deny()) {
                return;
            }
            getApi().removeEmoticonById(tmpEmoId, tmpList.getId(), handlerDemo);

        } else if (i == R.id.create_list) {
            fastLog("开始 创建列表");
            getApi().createUserListByName("安卓列表测试" + System.currentTimeMillis() % 10, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    handlerDemo.onResponse(response);
                    tmpList = (UserList) response;
                }

                @Override
                public void onError(Exception e) {
                    handlerDemo.onError(e);
                }
            });

        } else if (i == R.id.rename_list) {
            fastLog("开始 重命名列表");
            if (deny()) {
                return;
            }
            getApi().renameUserListById(tmpList.getId(), "贵族版重命名V" + (System.currentTimeMillis() % 100), handlerDemo);

        } else if (i == R.id.delete_list) {
            fastLog("开始 删除列表");
            if (deny()) {
                return;
            }
            getApi().removeUserListById(tmpList.getId());
            handlerDemo.onResponse("删除完毕.");
//                getApi().removeUserListById( "d8534080-7fe4-4227-9188-f359167573c2" , handlerDemo);

        } else if (i == R.id.move_emo) {
            fastLog("开始 移动表情");
            if (deny()) {
                return;
            }
            String fId = "a1f74206-f993-440c-ab5d-778df232c8d1"; //Test Package 55
            String tId = tmpList.getId();
            getApi().moveEmoticonById(tmpEmoId, fId, tId, handlerDemo);

        } else if (i == R.id.remove_all) {
            getApi().logout();
            handlerDemo.onResponse("退出成功!");

        } else {
        }
    }

    private boolean deny(){
        if(tmpList==null){
            Snackbar.make(responseText,"空列表",Snackbar.LENGTH_SHORT).show();
            responseText.setText( "空列表!" );
            return true;
        }else {
            return false;
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
