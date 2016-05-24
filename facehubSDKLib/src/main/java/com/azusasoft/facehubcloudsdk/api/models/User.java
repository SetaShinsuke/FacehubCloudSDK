package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by SETA on 2016/3/8.
 * 用户对象
 */
public class User {
    private final String USER = "facehub_sdk_user";
    private final String UPDATE_AT = "facehub_sdk_updated_at";
    private final String USER_ID = "facehub_sdk_user_id";
    private final String TOKEN = "facehub_sdk_auth_token";
    private String userId = "";
    private String token = "";
    private Context context;

    private String updated_at = "";
    private boolean modified;
    private ArrayList<UserList> userLists = new ArrayList<>();

    public User(Context context) {
        this.context = context;
        this.modified = false;
    }

    //设置用户token
    public void setToken(String token) {
        this.token = token;
    }

    //设置当前用户id & token
    public void setUserId(String userId, String token) {

    }

    public boolean restore() {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        if (!preferences.contains(USER_ID)) {
            return false;
        }
        this.userId = preferences.getString(USER_ID, "");
        this.token = preferences.getString(TOKEN, "");
        this.updated_at = preferences.getString(UPDATE_AT, "");
        return true;
    }

    public void restoreLists(){
        userLists = new ArrayList<>(UserListDAO.findAll());
        LogX.fastLog("user.restore , lists size : " + userLists.size());
    }

    public void logout() {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(USER_ID);
        editor.remove(TOKEN);
        editor.apply();
        this.userId = "";
        this.token = "";
        this.updated_at = "";
    }

    public String getUserId() {
        return this.userId;
    }

    public RequestParams getParams() {
        RequestParams params = new RequestParams();
        params.put("user_id", this.userId);
        params.put("auth_token", this.token);
        params.put("app_id", FacehubApi.appId);
        return params;
    }

    public JSONObject getParamsJson() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", this.userId);
            params.put("auth_token", this.token);
            params.put("app_id", FacehubApi.appId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return params;
    }

    public void setUpdated_at(String updated_at) {
        modified = !this.updated_at.equals(updated_at);
        this.updated_at = updated_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public boolean isModified() {
        return modified;
    }

    public void setUserInfo(String userId, String token, String updated_at) {
        this.userId = userId;
        setToken(token);
        setUpdated_at(updated_at);
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID, userId);
        editor.putString(TOKEN, token);
        editor.putString(UPDATE_AT, updated_at);
        editor.apply();
    }

    public void setUserLists(ArrayList<UserList> userLists) {
        UserListDAO.deleteAll();
        this.userLists = userLists;
        UserListDAO.saveInTX(userLists);
        LogX.fastLog("setUserLists size : " + userLists.size());
    }

    public ArrayList<UserList> getUserLists() {
        return this.userLists;
    }

    public void updateLists(){
        UserListDAO.deleteAll();
        UserListDAO.saveInTX(userLists);

//        ThreadPoolManager.getDbThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                UserListDAO.deleteAll();
//                LogX.fastLog("update lists : " + userLists);
//                UserListDAO.saveInTX(userLists);
//                FacehubApi.getDbHelper().export();
//            }
//        });
    }

    public UserList getUserListById(String id) {
        for (UserList userList : userLists) {
            if (id.equals(userList.getId())) {
                return userList;
            }
        }
        UserList userList = new UserList();
        userList.setId(id);
        if(userLists.size()>1) {
            userLists.add(1, userList);
        }else {
            userLists.add(userList);
        }
//        UserListDAO.save2DBWithClose(userList);
        return userList;
    }

    public void deleteUserList(String listId){
        UserList userList=null;
        for (UserList l : userLists){
            if(listId.equals(l.getId())){
                userList = l;
            }
        }
        userLists.remove(userList);
        UserListDAO.delete(listId);
    }

    public void deleteEmoticonsFromList(String listId,ArrayList<String> emoticonIds){
        for(UserList userList:userLists){
            if(listId.equals(userList.getId())){
                userList.removeEmoticons(emoticonIds);
            }
        }
    }

    // 移动列表
    public void changeListPosition(int from , int to) {
        if (from == to)
            return;
        UserList list = getUserLists().get(from);
        getUserLists().remove(from);
        if (to >= getUserLists().size()) {
            getUserLists().add(list);
        } else {
            getUserLists().add(to, list);
        }
//        save2DB();
        //TODO:排序后上传服务器
//        Collections.swap(getEmoticons(),from,to);
    }

    /**
     * 获取已下载好的列表
     * @return 已下载的列表
     */
    public ArrayList<UserList> getAvailableUserLists(){
        ArrayList<UserList> result = new ArrayList<>();
        for(UserList userList:userLists){
            if(userList.getForkFromId()==null //默认列表
                    || userList.isPrepared()){ //已下载完成的列表
                result.add(userList);
            }
        }
        return result;
    }

    /**
     * 根据网络状态，静默下载所有表情
     * @return 是否进行静默下载
     */
    public boolean silentDownloadAll(){
        boolean flag = false;
        if(NetHelper.getNetworkType(FacehubApi.getAppContext())==NetHelper.NETTYPE_WIFI){
            LogX.i("网络类型wifi，后台静默下载所有列表.");
            for(final UserList userList:userLists){
                if(userList.isPrepared() || userList.isDownloading()){
                    break;
                }
                userList.prepare(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        LogX.i("静默下载列表 " + userList.getId() + "成功!");
                        FacehubApi.getDbHelper().export();
                    }

                    @Override
                    public void onError(Exception e) {
                        LogX.w("静默下载列表 " + userList.getId() + "出错 : " + e);
                    }
                }, new ProgressInterface() {
                    @Override
                    public void onProgress(double process) {
                        LogX.v(Constants.PROGRESS,"静默下载列表 : " + userList + "\n进度 : " + process + "%");
                    }
                });
            }
            flag = true;
        }else {
            LogX.i("网络类型不是wifi，不静默下载列表.");
        }
        return flag;
    }
}
