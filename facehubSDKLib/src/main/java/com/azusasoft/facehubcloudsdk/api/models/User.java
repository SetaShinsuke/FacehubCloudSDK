package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.LocalEmoPackageParseException;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by SETA on 2016/3/8.
 * 用户对象
 */
public class User {
    private final String USER = "facehub_sdk_user";
    private final String UPDATE_AT = "facehub_sdk_updated_at";
    private final String USER_ID = "facehub_sdk_user_id";
    private final String TOKEN = "facehub_sdk_auth_token";
    private final String USER_ID_RETRY = "facehub_sdk_user_id_retry";
    private final String TOKEN_RETRY = "facehub_sdk_auth_token_retry";
    private String userId = "";
    private String token = "";
    private Context context;
    private String retryId,retryToken;


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
        if(preferences.contains(USER_ID_RETRY) && preferences.contains(TOKEN_RETRY)){
            this.retryId = preferences.getString(USER_ID_RETRY,null);
            this.retryToken = preferences.getString(TOKEN_RETRY,null);
        }else {
            this.retryId = null;
            this.retryToken = null;
        }
        return true;
    }

    public void restoreLists(){
        userLists = new ArrayList<>(UserListDAO.findAll());
        LogX.fastLog("user.restore , lists size : " + userLists.size());
    }

    public void logout() {
        UserListDAO.deleteAll();
        clear();
    }

    public void clear(){
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(USER_ID);
        editor.remove(TOKEN);
        editor.remove(USER_ID_RETRY);
        editor.remove(TOKEN_RETRY);
        editor.apply();
        this.userId = "";
        this.token = "";
        this.updated_at = "";
        this.modified = false;
        this.retryId = null;
        this.retryToken = null;
        userLists.clear();
    }

    public boolean isLogin(){
        return userId!=null && !userId.equals("");
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
        //清除重试登录的信息
        this.retryId = null;
        this.retryToken = null;
        editor.remove(USER_ID_RETRY);
        editor.remove(TOKEN_RETRY);
        editor.apply();
    }

    public void setUserRetryInfo(String retryId,String retryToken){
        //清除掉用户信息，只记录需要重试登录的信息
        clear();
        this.retryId = retryId;
        this.retryToken = retryToken;
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID, userId);
        editor.putString(TOKEN, token);
        editor.apply();
    }

    public String getRetryId(){
        return retryId;
    }
    public String getRetryToken(){
        return retryToken;
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
        UserList userList = getUserLists().remove(from);
        userLists.add(to,userList);
//        UserList list = getUserLists().get(from);
//        getUserLists().remove(from);
//        if (to >= getUserLists().size()) {
//            getUserLists().add(list);
//        } else {
//            getUserLists().add(to, list);
//        }

//        save2DB();
//        Collections.swap(getEmoticons(),from,to);
    }

    /**
     * 获取已下载好的列表
     * @return 已下载的列表
     */
    public ArrayList<UserList> getAvailableUserLists(){
        ArrayList<UserList> result = new ArrayList<>();
        for(UserList userList:userLists){
            if(userList.isDefaultFavorList() //默认列表
                    || userList.isPrepared()){ //已下载完成的列表
                result.add(userList);
            }
        }
        return result;
    }

    public Emoticon findEmoticonByDescription(String description){
        Emoticon emoticon;
        for (UserList userList:getAvailableUserLists()){
            emoticon = userList.findEmoByDes(description);
            if(emoticon!=null){
                return emoticon;
            }
        }
        return null;
    }

    /**
     * 根据网络状态，静默下载所有表情
     * @return 是否进行静默下载
     */
    public boolean silentDownloadAll(){
//        if(true){
//            return false;
//        }
        boolean flag = false;
        if(NetHelper.getNetworkType(FacehubApi.getAppContext())==NetHelper.NETTYPE_WIFI){
            LogX.i("网络类型wifi，后台静默下载所有列表. Size : " + userLists.size());
            for(final UserList userList:userLists){
                if( userList.isPrepared() || userList.isDownloading() ){
                    continue;
                }
                userList.prepare(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        LogX.i("静默下载列表 " + userList.getId() + "成功!");
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


    private UserList localEmoticonList; //此列表只存在内存中，因为只需要用到emoticons或id
    public UserList getLocalList(){
        if(localEmoticonList==null) {
            localEmoticonList = new UserList();
            localEmoticonList.setLocal(true);
            localEmoticonList.setId("localEmoticonList");
        }
        return localEmoticonList;
    }
    public void restoreLocalEmoticons(Context context, int version, String configJsonAssetsPath) throws Exception {
        getLocalList();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.LOCAL_EMOTICON,Context.MODE_PRIVATE);
        String localEmoticonIds = sharedPreferences.getString("local_emoticon_ids",null);
        int currentVersion = sharedPreferences.getInt(Constants.LOCAL_EMOTICON_VERSION,-1);
        if(localEmoticonIds==null || version>currentVersion) { //没有存过local_emoticons -> 解析file
            //解析配置文件
            LogX.i("解析默认表情配置文件.");
            ArrayList<Emoticon> emoticons = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            JSONObject configJson = UtilMethods.loadJSONFromAssets(context,configJsonAssetsPath);
            JSONArray emoticonJsonArray = configJson.getJSONArray("emoticons");
            HashMap<String,String> localEmoPaths = new HashMap<>(); //<path,path>
            String[] faces = context.getAssets().list("emoji");
            //将Assets中的表情名称转为字符串一一添加进staticFacesList
            for (int i = 0; i < faces.length; i++) {
                localEmoPaths.put(faces[i],faces[i]);
//                LogX.w("face " + i + " : " + faces[i]);
            }
            if(localEmoPaths.size()!=emoticonJsonArray.length()){
                throw new LocalEmoPackageParseException("本地预置表情文件个数与配置文件不符！"
                        + "\n文件个数 : " + localEmoPaths.size()
                        + "\n配置文件表情数 : " + emoticonJsonArray.length());
            }

            for(int i=0;i<emoticonJsonArray.length();i++){
                JSONObject emoJson = emoticonJsonArray.getJSONObject(i);
                String emoId = emoJson.getString("id");
                String description = emoJson.getString("description");
                String format = emoJson.getString("format");
                Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoId);
                String path = emoId + "." + format;
//                LogX.w("path " + i + " : " + path);
                if(localEmoPaths.containsKey(path)){
                    path = "emoji/" + emoId + "." + format;
                }else {
                    throw new LocalEmoPackageParseException("未找到ID对应的表情资源:"+"\nid : "+emoId);
                }
                emoticon.setFilePath(Image.Size.FULL,path);
                emoticon.setFilePath(Image.Size.MEDIUM,path);
                emoticon.setDescription(description);
                emoticon.setLocal(true);
                emoticons.add(emoticon);
                sb.append(emoId);
                sb.append(",");
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("local_emoticon_ids",sb.toString());
            editor.putInt(Constants.LOCAL_EMOTICON_VERSION,version);
            editor.apply();
            localEmoticonList.setEmoticons(emoticons);
            FacehubApi.getApi().getEmoticonContainer().updateEmoticons2DB(emoticons);
        }else { //存过
            LogX.i("无需解析默认表情配置文件,直接恢复.");
            ArrayList<Emoticon> emoticons = new ArrayList<>();
            for (String eUid : localEmoticonIds.split(",")) {
                if (eUid.length() > 0) {
                    Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(eUid);
                    emoticon.setLocal(true);
                    emoticons.add(emoticon);
                }
            }
            localEmoticonList.setEmoticons(emoticons);
        }
    }
}
