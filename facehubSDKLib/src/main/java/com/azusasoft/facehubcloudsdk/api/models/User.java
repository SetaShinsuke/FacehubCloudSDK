package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.LocalEmoPackageParseException;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.EmojiUtils;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LOCAL_EMO_CUSTOM;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LOCAL_EMO_EMOJI;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LOCAL_EMO_VOICE;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;


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
    private final String BINDING_RETRY = "facehub_sdk_binding_id_retry";
    private String userId = "";

    private String token = "";
    private Context context;
    private String retryId, retryToken;
    private String retryBindingId;


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

    public String getToken() {
        return token;
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
        if (preferences.contains(USER_ID_RETRY) && preferences.contains(TOKEN_RETRY)) {
            this.retryId = preferences.getString(USER_ID_RETRY, null);
            this.retryToken = preferences.getString(TOKEN_RETRY, null);
        } else {
            this.retryId = null;
            this.retryToken = null;
        }
        if (preferences.contains(BINDING_RETRY)) {
            this.retryBindingId = preferences.getString(BINDING_RETRY, null);
        }
        return true;
    }

    public void restoreLists() {
        userLists = new ArrayList<>(UserListDAO.findAll());
        LogX.fastLog("user.restore , lists size : " + userLists.size());
    }

    public void logout() {
        UserListDAO.deleteAll();
        clear();
    }

    public void clear() {
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
        this.retryBindingId = null;
        userLists.clear();
    }

    public boolean isLogin() {
        return userId != null && !userId.equals("");
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
        this.retryBindingId = null;
        editor.remove(USER_ID_RETRY);
        editor.remove(TOKEN_RETRY);
        editor.remove(BINDING_RETRY);
        editor.apply();
    }

    public void setUserRetryInfo(String retryId, String retryToken, String retryBindingId) {
        //清除掉用户信息，只记录需要重试登录的信息
        clear();
        this.retryId = retryId;
        this.retryToken = retryToken;
        this.retryBindingId = retryBindingId;
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID, userId);
        editor.putString(TOKEN, token);
        editor.putString(BINDING_RETRY, retryBindingId);
        editor.apply();
    }

    public String getRetryId() {
        return retryId;
    }

    public String getRetryToken() {
        return retryToken;
    }

    public String getRetryBindingId() {
        return retryBindingId;
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

    public void updateLists() {
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
        if (userLists.size() > 1) {
            userLists.add(1, userList);
        } else {
            userLists.add(userList);
        }
//        UserListDAO.save2DBWithClose(userList);
        return userList;
    }

    public UserList getUserListByForkFromId(String forkFromId){
        for (UserList userList : userLists) {
            if (forkFromId.equals(userList.getForkFromId())) {
                return userList;
            }
        }
        return null;
    }

    public void deleteUserList(String listId) {
        UserList userList = null;
        for (UserList l : userLists) {
            if (listId.equals(l.getId())) {
                userList = l;
            }
        }
        userLists.remove(userList);
        UserListDAO.delete(listId);
    }

    public void deleteEmoticonsFromList(String listId, ArrayList<String> emoticonIds) {
        for (UserList userList : userLists) {
            if (listId.equals(userList.getId())) {
                userList.removeEmoticons(emoticonIds);
            }
        }
    }

    // 移动列表
    public void changeListPosition(int from, int to) {
        if (from == to)
            return;
        UserList userList = getUserLists().remove(from);
        userLists.add(to, userList);
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
     *
     * @return 已下载的列表
     */
    public ArrayList<UserList> getAvailableUserLists() {
        ArrayList<UserList> result = new ArrayList<>();
        for (UserList userList : userLists) {
            if (userList.isDefaultFavorList() //默认列表
                    || userList.getForkFromId()==null
                    || userList.isPrepared()) { //已下载完成的列表
                result.add(userList);
            }
        }
        return result;
    }

    public Emoticon findEmoticonByDescription(String description) {
        Emoticon emoticon;
        for (UserList userList : getAvailableUserLists()) {
            emoticon = userList.findEmoByDes(description);
            if (emoticon != null) {
                return emoticon;
            }
        }
        return null;
    }

    public UserList getDefaultFavorList(){
        ArrayList<UserList> userLists = getUserLists();
        for(int i=0;i<userLists.size();i++){
            UserList userList = userLists.get(i);
            if(userList.isDefaultFavorList()){
                return userList;
            }
        }
        return null;
    }

    /**
     * 根据网络状态，静默下载所有表情
     *
     * @return 是否进行静默下载
     */
    public boolean silentDownloadAll() {
//        if(true){
//            return false;
//        }
        boolean flag = false;
        if (NetHelper.getNetworkType(FacehubApi.getAppContext()) == NetHelper.NETTYPE_WIFI) {
            LogX.i("网络类型wifi，后台静默下载所有列表. Size : " + userLists.size());
            for (final UserList userList : userLists) {
                if (userList.isPrepared() || userList.isDownloading()) {
                    continue;
                }
                userList.prepare(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        if(FacehubApi.getApi().isOfflineMode() && userList.isRemoveMe()){
                            /**
                             * 离线模式、列表信息有误，自动移除列表
                             */
                            FacehubApi.getApi().removeUserListById(userList.getId());
                        }
                        LogX.i("静默下载列表 " + userList.getId() + "成功!");
                    }

                    @Override
                    public void onError(Exception e) {
                        LogX.w("静默下载列表 " + userList.getId() + "出错 : " + e);
                    }
                }, new ProgressInterface() {
                    @Override
                    public void onProgress(double process) {
                        LogX.v(Constants.PROGRESS, "静默下载列表 : " + userList + "\n进度 : " + process + "%");
                    }
                });
            }
            flag = true;
        } else {
            LogX.i("网络类型不是wifi，不静默下载列表.");
        }
        return flag;
    }

    /**
     * =========================================== 本地表情 ===========================================
     */
    //region 本地表情
    private ArrayList<LocalList> localLists = new ArrayList<>();

    public ArrayList<LocalList> getLocalLists() {
        return localLists;
    }

    /**
     * 恢复/解析本地预置表情
     *
     * @param context              上下文
     * @param version              配置json文件版本
     * @param configJsonAssetsPath 配置json文件位置
     * @throws LocalEmoPackageParseException 解析出错，抛出异常
     */
    public void restoreLocalLists(Context context, int version, String configJsonAssetsPath) throws Exception {
        localLists.clear();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.LOCAL_EMOTICON, Context.MODE_PRIVATE);
        int currentVersion = sharedPreferences.getInt(Constants.LOCAL_EMOTICON_VERSION, -1);
        if ( version > currentVersion) { //没有存过local_emoticons -> 解析file
            LogX.i("解析默认表情配置文件,配置版本 : " + version);
            ArrayList<Emoticon> emoticons2Save = new ArrayList<>();
            //1.读取assets://emoji
            HashMap<String, String> localEmoPaths = new HashMap<>(); //<path,path>
            String[] faces = context.getAssets().list("emoji");
            //将Assets中的表情名称转为字符串一一添加进staticFacesList
            for (int i = 0; i < faces.length; i++) {
                localEmoPaths.put(faces[i], faces[i]);
            }

            //2.解析json文件
            JSONObject configJson;
            configJson = UtilMethods.loadJSONFromAssets(context, configJsonAssetsPath);

            //2.1 读取emoji列表
            if (isJsonWithKey(configJson, LOCAL_EMO_EMOJI)) {
                LocalList emojiList = new LocalList();
                emojiList.setLocalType(LOCAL_EMO_EMOJI)
                        .setId(LOCAL_EMO_EMOJI);
                ArrayList<Emoticon> emojiEmoticons = new ArrayList<>();
                JSONObject emojiJson = configJson.getJSONObject(LOCAL_EMO_EMOJI);
                //混合排版
                emojiList.setNeedMixLayout(emojiJson.getBoolean("needMixLayout"));
                //表情详情
                JSONArray emojiJsonArray = emojiJson.getJSONArray("emoticons");
                for (int i = 0; i < emojiJsonArray.length(); i++) {
                    JSONObject emoticonJson = emojiJsonArray.getJSONObject(i);
                    Emoticon emoticon = updateLocalEmo(emoticonJson,LOCAL_EMO_EMOJI,localEmoPaths);
                    emojiEmoticons.add(emoticon);
                }
                emoticons2Save.addAll(emojiEmoticons);
                emojiList.setEmoticons(emojiEmoticons);
                this.localLists.add(emojiList);
            }

            //2.2 读取自定义列表
            if(isJsonWithKey(configJson,LOCAL_EMO_CUSTOM)){
                JSONArray customListArray = configJson.getJSONArray(LOCAL_EMO_CUSTOM);
                for(int i=0;i<customListArray.length();i++){
                    JSONObject customListJson = customListArray.getJSONObject(i);
                    LocalList localList = new LocalList();
                    localList.setLocalType(LOCAL_EMO_CUSTOM)
                            .setColumnNum(customListJson.getInt("column"))
                            .setRowNum(customListJson.getInt("row"))
                            .setId(LOCAL_EMO_CUSTOM+i+"");
                    //混合排版
                    if(isJsonWithKey(customListJson,"needMixLayout")) {
                        localList.setNeedMixLayout(customListJson.getBoolean("needMixLayout"));
                    }

                    //封面
                    Emoticon cover;
                    if(isJsonWithKey(customListJson,"cover")) {
                        JSONObject coverJson = customListJson.getJSONObject("cover");
                        cover = updateLocalEmo(coverJson, LOCAL_EMO_CUSTOM, localEmoPaths);
                    }else {
                        cover = getDefaultLocalCover(LOCAL_EMO_CUSTOM);
                    }
                    localList.setCover(cover);

                    //表情
                    ArrayList<Emoticon> emoticons = new ArrayList<>();
                    JSONArray emoticonsJsonArray = customListJson.getJSONArray("emoticons");
                    for(int j=0;j<emoticonsJsonArray.length();j++){
                        JSONObject emoticonJson = emoticonsJsonArray.getJSONObject(j);
                        Emoticon emoticon = updateLocalEmo(emoticonJson,LOCAL_EMO_CUSTOM,localEmoPaths);
                        emoticon.setNeedMixLayout(localList.isNeedMixLayout());
                        emoticons.add(emoticon);
                    }
                    localList.setEmoticons(emoticons);
                    emoticons2Save.addAll(emoticons);
                    emoticons2Save.add(cover);
                    localLists.add(localList);
                }
            }

            //2.3 读取语音列表
            if(isJsonWithKey(configJson,LOCAL_EMO_VOICE)){
                JSONObject voiceListJson = configJson.getJSONObject(LOCAL_EMO_VOICE);
                LocalList localList = new LocalList();
                localList.setLocalType(LOCAL_EMO_VOICE)
                        .setColumnNum(voiceListJson.getInt("column"))
                        .setRowNum(voiceListJson.getInt("row"))
                        .setId(LOCAL_EMO_VOICE);
                //region封面
                JSONObject coverJson = voiceListJson.getJSONObject("cover");
                Emoticon cover = updateLocalEmo(coverJson,LOCAL_EMO_CUSTOM,localEmoPaths);
                localList.setCover(cover);
                //endregion 封面
                //表情
                ArrayList<Emoticon> emoticons = new ArrayList<>();
                JSONArray emoticonsJsonArray = voiceListJson.getJSONArray("emoticons");
                for(int i=0;i<emoticonsJsonArray.length();i++){
                    Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer()
                            .getUniqueEmoticonById(LOCAL_EMO_VOICE+i+"");
                    emoticon.setLocalType(LOCAL_EMO_VOICE)
                            .setLocal(true);
                    emoticon.setDescription(emoticonsJsonArray.getString(i));
                    emoticons.add(emoticon);
                }
                emoticons2Save.addAll(emoticons);
                emoticons2Save.add(cover);
                localList.setEmoticons(emoticons);
                localLists.add(localList);
            }

            //3.存储列表/表情到数据库
            FacehubApi.getApi().getEmoticonContainer().updateEmoticons2DB(emoticons2Save);
            LocalListDAO.saveInTX(localLists);
//            FacehubApi.getDbHelper().export();

            //4.解析完成，标记到sharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(Constants.LOCAL_EMOTICON_VERSION,version);
            editor.apply();
        } else {
            LogX.i("无需解析默认表情配置文件,直接恢复.当前配置文件版本 : " + version);
            localLists = LocalListDAO.findAll();
            for(LocalList localList:localLists){
                setLocalTypeForEmoticons(localList);
            }
        }
    }

    private void setLocalTypeForEmoticons(LocalList localList){
        String type = localList.getLocalType();
        for(Emoticon emoticon:localList.getEmoticons()){
            emoticon.setLocal(true);
            emoticon.setLocalType(type);
        }
    }

    private Emoticon updateLocalEmo(JSONObject emoticonJson
            ,String localType
            ,HashMap<String,String> localEmoPaths) throws Exception{
        String emoId = emoticonJson.getString("id");
        String description=null;
        String format = emoticonJson.getString("format");
        if(isJsonWithKey(emoticonJson,"description")) {
            description = emoticonJson.getString("description");
        }
        Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoId);
        String path = emoId + "." + format;
        if (localEmoPaths.containsKey(path)) {
            path = "emoji/" + emoId + "." + format;
        } else {
            throw new LocalEmoPackageParseException("未找到ID对应的表情资源:" + "\nid : " + emoId);
        }
        emoticon.setFormat(format);
        emoticon.setFilePath(Image.Size.FULL, path);
        emoticon.setFilePath(Image.Size.MEDIUM, path);
        emoticon.setDescription(description);
        emoticon.setLocal(true);
        emoticon.setLocalType(localType);
        return emoticon;
    }

    private Emoticon getDefaultLocalCover(String localType){
        String format = "png";
        Emoticon emoticon = FacehubApi.getApi()
                .getEmoticonContainer().getUniqueEmoticonById("local_cover_default");
        String path = "local_emoticon_cover_default.png";
        emoticon.setFormat(format);
        emoticon.setFilePath(Image.Size.FULL, path);
        emoticon.setFilePath(Image.Size.MEDIUM, path);
        emoticon.setLocal(true);
        emoticon.setLocalType(localType);
        return emoticon;
    }
    //endregion


    /**
     * =========================================== emoji字符表情 ===========================================
     */
    private UserList emojiList;

    public UserList getEmojiList(Context context){
        if(emojiList!=null){
            return emojiList;
        }
        emojiList = new UserList();
        emojiList.setIsEmojiList(true);
        emojiList.setId(Constants.EMOJI_LIST_ID);
        ArrayList<String> emojiStrings = EmojiUtils.getEmojiStrings(context);
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        for(int i=0;i<emojiStrings.size();i++){
            String emojiString = emojiStrings.get(i);
            Emoticon emoticon = FacehubApi.getApi()
                    .getEmoticonContainer().getUniqueEmoticonById("emoji_unicode_"+i);
            emoticon.setIsEmoji(true);
            emoticon.setNeedMixLayout(true);
            emoticon.setDescription(emojiString);
            emoticon.setFilePath(Image.Size.MEDIUM,emojiString);
            emoticon.setFilePath(Image.Size.FULL,emojiString);
            emoticons.add(emoticon);
        }
        if(emoticons.size()>0) {
            Emoticon cover = FacehubApi.getApi()
                    .getEmoticonContainer().getUniqueEmoticonById("emoji_unicode_cover");
            String path = "drawable://"+ R.drawable.emoji_cover;
            cover.setFilePath(Image.Size.FULL,path);
            cover.setFilePath(Image.Size.MEDIUM,path);
            emojiList.setCover(cover);
        }
        emojiList.setEmoticons(emoticons);
        return emojiList;
    }

    /**
     * =========================================== 颜文字表情 ===========================================
     */
    private UserList kaomojiList;
    public UserList getKaomojiList(Context context){
        if(kaomojiList!=null){
            return kaomojiList;
        }
        kaomojiList = new UserList();
        kaomojiList.setIsEmojiList(true);
        kaomojiList.setId(Constants.KAOMOJI_LIST_ID);
        ArrayList<String> emojiStrings = EmojiUtils.getKaomojiStrings(context);
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        for(int i=0;i<emojiStrings.size();i++){
            String emojiString = emojiStrings.get(i);
            Emoticon emoticon = FacehubApi.getApi()
                    .getEmoticonContainer().getUniqueEmoticonById("kaomoji_"+i);
            emoticon.setIsEmoji(true);
            emoticon.setNeedMixLayout(true);
            emoticon.setDescription(emojiString);
            emoticon.setFilePath(Image.Size.MEDIUM,emojiString);
            emoticon.setFilePath(Image.Size.FULL,emojiString);
            emoticons.add(emoticon);
        }
        if(emoticons.size()>0) {
            Emoticon cover = FacehubApi.getApi()
                    .getEmoticonContainer().getUniqueEmoticonById("kaomoji_cover");
            String path = "drawable://"+ R.drawable.kaomoji_cover;
            cover.setFilePath(Image.Size.FULL,path);
            cover.setFilePath(Image.Size.MEDIUM,path);
            kaomojiList.setCover(cover);
        }
        kaomojiList.setEmoticons(emoticons);
        return kaomojiList;
    }

//    private UserList localEmoticonList; //此列表只存在内存中，因为只需要用到emoticons或id
//    public UserList getLocalList() {
//        for(LocalList localList:localLists){
//            if(LOCAL_EMO_EMOJI.equals(localList.getLocalType()) ){
//                return localList;
//            }
//        }
//        return null;
////        if (localEmoticonList == null) {
////            localEmoticonList = new UserList();
////            localEmoticonList.setLocal(true);
////            localEmoticonList.setId("localEmoticonList");
////        }
////        return localEmoticonList;
//    }


//
//    public void restoreLocalEmoticons(Context context, int version, String configJsonAssetsPath) throws Exception {
//        UserList localEmoticonList = getLocalList();
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.LOCAL_EMOTICON,Context.MODE_PRIVATE);
//        String localEmoticonIds = sharedPreferences.getString("local_emoticon_ids",null);
//        int currentVersion = sharedPreferences.getInt(Constants.LOCAL_EMOTICON_VERSION,-1);
//        if(localEmoticonIds==null || version>currentVersion) { //没有存过local_emoticons -> 解析file
//            //解析配置文件
//            LogX.i("解析默认表情配置文件.");
//            ArrayList<Emoticon> emoticons = new ArrayList<>();
//            StringBuilder sb = new StringBuilder();
//            JSONObject configJson = UtilMethods.loadJSONFromAssets(context,configJsonAssetsPath);
//            JSONArray emoticonJsonArray = configJson.getJSONArray("emoticons");
//            HashMap<String,String> localEmoPaths = new HashMap<>(); //<path,path>
//            String[] faces = context.getAssets().list("emoji");
//            //将Assets中的表情名称转为字符串一一添加进staticFacesList
//            for (int i = 0; i < faces.length; i++) {
//                localEmoPaths.put(faces[i],faces[i]);
////                LogX.w("face " + i + " : " + faces[i]);
//            }
//            if(localEmoPaths.size()!=emoticonJsonArray.length()){
//                throw new LocalEmoPackageParseException("本地预置表情文件个数与配置文件不符！"
//                        + "\n文件个数 : " + localEmoPaths.size()
//                        + "\n配置文件表情数 : " + emoticonJsonArray.length());
//            }
//
//            for(int i=0;i<emoticonJsonArray.length();i++){
//                JSONObject emoJson = emoticonJsonArray.getJSONObject(i);
//                String emoId = emoJson.getString("id");
//                String description = emoJson.getString("description");
//                String format = emoJson.getString("format");
//                Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoId);
//                String path = emoId + "." + format;
////                LogX.w("path " + i + " : " + path);
//                if(localEmoPaths.containsKey(path)){
//                    path = "emoji/" + emoId + "." + format;
//                }else {
//                    throw new LocalEmoPackageParseException("未找到ID对应的表情资源:"+"\nid : "+emoId);
//                }
//                emoticon.setFilePath(Image.Size.FULL,path);
//                emoticon.setFilePath(Image.Size.MEDIUM,path);
//                emoticon.setDescription(description);
//                emoticon.setLocal(true);
//                emoticons.add(emoticon);
//                sb.append(emoId);
//                sb.append(",");
//            }
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString("local_emoticon_ids",sb.toString());
//            editor.putInt(Constants.LOCAL_EMOTICON_VERSION,version);
//            editor.apply();
//            localEmoticonList.setEmoticons(emoticons);
//            FacehubApi.getApi().getEmoticonContainer().updateEmoticons2DB(emoticons);
//        }else { //存过
//            LogX.i("无需解析默认表情配置文件,直接恢复.");
//            ArrayList<Emoticon> emoticons = new ArrayList<>();
//            for (String eUid : localEmoticonIds.split(",")) {
//                if (eUid.length() > 0) {
//                    Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(eUid);
//                    emoticon.setLocal(true);
//                    emoticons.add(emoticon);
//                }
//            }
//            localEmoticonList.setEmoticons(emoticons);
//        }
//    }
}
