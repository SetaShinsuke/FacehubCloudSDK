package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException.ErrorType.mock_http_error;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.getDeviceId;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA_WORK on 2016/8/2.
 */
public class MockClient {
    AsyncHttpClient client = new AsyncHttpClient();
//    AsyncHttpClient client = new AsyncHttpClient(true,80,443); //不验证证书，不安全
    String host = "";
    private boolean checkHost = true;

    public MockClient(String host) {
        this.host = host;
    }

    public AsyncHttpClient getHttpClient(){
        return client;
    }

    private boolean isOffLineMode() {
        return FacehubApi.getApi().isOfflineMode();
    }

    public void setCheckHost(boolean checkHost) {
        this.checkHost = checkHost;
    }

    /**
     * Get
     */
    public void get(String url, RequestParams params, final JsonHttpResponseHandler responseHandler) {
        if (!isOffLineMode()) {
            client.get( url, params , responseHandler);
            return;
        }
        //判断url是否有误
        if (!isUrlAvailable(url)) {
            onFail(new FacehubSDKException(mock_http_error, "Url error!"), responseHandler);
            return;
        }

        String[] urlParts = url.split("/");
        int length = urlParts.length;
        //列表详情
        if (url.startsWith(host + "/api/v1/users/")
                && urlParts[length-2].equals("lists")) {
            final String userListId = urlParts[length-1];
            getUserListDetail(userListId,responseHandler);
            return;
        }
        client.get(url,params,responseHandler);
    }

    /**
     * Get主要是下载
     */
    public RequestHandle get(String url, ResponseHandlerInterface responseHandler) {
        return client.get(url, responseHandler);
    }

    /**
     * Put
     */
    public void put(Context context, String url, HttpEntity entity, String contentType, JsonHttpResponseHandler responseHandler) {
        if (!isOffLineMode()) {
            client.put(context, url, entity, contentType, responseHandler);
            return;
        }
        //判断url是否有误
        if (!isUrlAvailable(url)) {
            onFail(new FacehubSDKException(mock_http_error, "Url error!"), responseHandler);
            return;
        }
        String[] urlParts = url.split("/");
        int length = urlParts.length;
        //排序
        if (url.startsWith(host + "/api/v1/users/")
                && url.endsWith("/lists")) {
            reorderUserLists(entity, responseHandler);
            return;
        }

        //列表删除/重命名
        //表情添加/替换/删除
        if (url.startsWith(host + "/api/v1/users/")
                && urlParts[length - 2].equals("lists")) {
            editList(urlParts[length - 1], entity, responseHandler);
            return;
        }
        client.put(context,url,entity,contentType,responseHandler);
    }

    /**
     * Post
     */
    public void post(Context context, String url, HttpEntity entity, String contentType, JsonHttpResponseHandler responseHandler) {
        if (!isOffLineMode()) {
            client.post(context, url, entity, contentType, responseHandler);
            return;
        }
        //判断url是否有误
        if (!isUrlAvailable(url)) {
            onFail(new FacehubSDKException(mock_http_error, "Url error!"), responseHandler);
            return;
        }

        //发送记录
        if ((host + "/api/v1/emoticons/usage").equals(url)) {
            client.post(context, url, entity, contentType, responseHandler);
            return;
        }

//        String url = HOST + "/api/v1/users/" + user.getUserId()
//                + "/lists/batch";

        //收藏表情包
        if (url.startsWith(host + "/api/v1/users/")
                && url.endsWith("/lists/batch")) { //
            collectEmoPackage(context, entity, responseHandler);
            return;
        }

        //创建列表
        if (url.startsWith(host + "/api/v1/users/")
                && url.endsWith("/lists")) { //
            createList(context, entity, responseHandler);
            return;
        }

        client.post(context,url,entity,contentType,responseHandler);
    }

    public void post(String url, RequestParams params, ResponseHandlerInterface responseHandler){
        client.post(url,params,responseHandler);
    }

    /**
     * Delete
     */
    public void delete(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        if (!isOffLineMode()) {
            client.delete(url, params, responseHandler);
            return;
        }
        //判断url是否有误
        if (!isUrlAvailable(url)) {
            onFail(new FacehubSDKException(mock_http_error, "Url error!"), responseHandler);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        onSuccess(jsonObject,responseHandler);
    }

    private boolean isUrlAvailable(String url) {
        if (!checkHost) {
            return true;
        }
        return !(url == null || !url.startsWith(host));
    }


    /**
     * ================================= GET =================================
     */
    private void getUserListDetail(String userListId,JsonHttpResponseHandler responseHandler){
        LogX.d("离线模式 : " + "获取列表详情.");
        try {
            UserList userList = FacehubApi.getApi().getUser().getUserListById(userListId);
            ArrayList<Emoticon> emoticons2Remove = new ArrayList<>();
            ArrayList<Emoticon> emoticons = new ArrayList<>();
            if(userList.getCover()!=null){
                emoticons.add(userList.getCover());
            }
            emoticons.addAll(userList.getEmoticons());

            boolean hasForkFromId = userList.getForkFromId()!=null;
            String forkFromId = userList.getForkFromId();

            for(Emoticon emoticon:emoticons){
                if(emoticon.getThumbPath()==null
                        && emoticon.getFileUrl(Image.Size.MEDIUM)==null){
                    userList.setForkFromId(null);
                    emoticons2Remove.add(emoticon);
                }else if(emoticon.getFullPath()==null
                        && emoticon.getFileUrl(Image.Size.FULL)==null){
                    userList.setForkFromId(null);
                    emoticons2Remove.add(emoticon);
                }
            }

            for(Emoticon emoticon:userList.getEmoticons()){
                if(emoticon.getThumbPath()==null
                        && emoticon.getFileUrl(Image.Size.MEDIUM)==null){
                    userList.setForkFromId(null);
                    emoticons2Remove.add(emoticon);
                }else if(emoticon.getFullPath()==null
                        && emoticon.getFileUrl(Image.Size.FULL)==null){
                    userList.setForkFromId(null);
                    emoticons2Remove.add(emoticon);
                }
            }
            /**
             * 删除空列表
             * 1.有无效的表情;
             * 2.有forkFromId;
             */
            if( emoticons2Remove.size()!=0 && hasForkFromId){
                LogX.i("离线模式拉取列表详情,列表有误,准备移除. fork from : " + forkFromId);
                userList.setRemoveMe(true);
            }
            userList.getEmoticons().removeAll(emoticons2Remove);

            JSONObject resultJson = new JSONObject();
            JSONObject userListJson = userList.toJson();
            Emoticon cover = userList.getCover();
            if(cover==null
                    || cover.getFileUrl(Image.Size.MEDIUM)==null ){
                userListJson.put("cover",null);
                userListJson.put("cover_detail",null);
                userList.setCover(null);
            }
            resultJson.put("list",userList.toJson());
            LogX.fastLog("获取详情mock json : " + resultJson);
            onSuccess(resultJson,responseHandler);
        }catch (Exception e){
            FacehubSDKException facehubSDKException
                    = new FacehubSDKException(mock_http_error,"获取UserList详情出错 : " + e);
            onFail(facehubSDKException,responseHandler);
        }
    }

    /**
     * ================================= PUT =================================
     */
    //列表排序
    private void reorderUserLists(HttpEntity entity, JsonHttpResponseHandler responseHandler) {
        LogX.d("离线模式 : " + "列表排序.");
        try {
            JSONObject queryJson = new JSONObject(EntityUtils.toString(entity));
            JSONArray listsJsonArray = queryJson.getJSONArray("contents");

            JSONObject resultJson = new JSONObject();
            JSONObject userJsonObject = new JSONObject();
            resultJson.put("user", userJsonObject);
            JSONArray resultJsonArray = new JSONArray();
            userJsonObject.put("contents", resultJsonArray);
            for (int i = 0; i < listsJsonArray.length(); i++) {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("id", listsJsonArray.get(i));
                resultJsonArray.put(jsonObject1);
            }
            onSuccess(resultJson, responseHandler);
        } catch (Exception e) {
            FacehubSDKException exception = new FacehubSDKException(mock_http_error, "Mock http error : " + e);
            onFail(exception, responseHandler);
        }
    }

    //列表重命名
    //表情添加/替换/删除
    private void editList(String listId, HttpEntity entity, JsonHttpResponseHandler responseHandler) {
        try {
            UserList userList = FacehubApi.getApi().getUser().getUserListById(listId);
            JSONObject queryJson = new JSONObject(EntityUtils.toString(entity));
            //获取请求中的emoticons
            ArrayList<Emoticon> emoticons = new ArrayList<>();
            if (isJsonWithKey(queryJson, "contents")) {
                JSONArray queryEmoticonsJsonArray = queryJson.getJSONArray("contents");
                for (int i = 0; i < queryEmoticonsJsonArray.length(); i++) {
                    String emoId = queryEmoticonsJsonArray.getString(i);
                    Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer()
                            .getUniqueEmoticonById(emoId);
                    emoticons.add(emoticon);
                }
            }
            //重命名的列表名
            String name = userList.getName();
            if (isJsonWithKey(queryJson, "name")) {
                name = queryJson.getString("name");
            }

            String action = queryJson.getString("action");
            switch (action) {
                case "add": //添加表情
                    LogX.d("离线模式 : " + "添加表情.");
                    userList.getEmoticons().addAll(emoticons);
                    break;

                case "remove"://删除表情
                    userList.getEmoticons().removeAll(emoticons);
                    LogX.d("离线模式 : " + "删除表情.");
                    break;

                case "rename"://重命名列表
                    userList.setName(name);
                    LogX.d("离线模式 : " + "列表重命名.");
                    break;

                case "replace"://替换表情
                    LogX.d("离线模式 : " + "表情排序.");
                    userList.setEmoticons(emoticons);
                    break;
            }
            JSONObject resultJson = new JSONObject();
            resultJson.put("list", userList.toJson());
            onSuccess(resultJson, responseHandler);
        } catch (Exception e) {
            LogX.e("e : " + e);
            onFail(e, responseHandler);
        }
    }


    /**
     * ================================= POST =================================
     */
    //新建列表
    private void createList(Context context, HttpEntity entity, JsonHttpResponseHandler responseHandler) {
        LogX.d("离线模式 : " + "新建列表.");
        try {
            JSONObject queryJson = new JSONObject(EntityUtils.toString(entity));
            String listName = queryJson.getString("name");
            String listId = getDeviceId(context) + "list" + System.currentTimeMillis();
            UserList userList = FacehubApi.getApi().getUser()
                    .getUserListById(listId);
            userList.setName(listName);
            JSONObject resultJson = new JSONObject();
            resultJson.put("list", userList.toJson());
            onSuccess(resultJson, responseHandler);
        } catch (Exception e) {
            FacehubSDKException facehubSDKException
                    = new FacehubSDKException(mock_http_error, "创建列表出错 : " + e);
            onFail(facehubSDKException, responseHandler);
        }
    }

    //收藏表情包
    private void collectEmoPackage(Context context, HttpEntity entity, JsonHttpResponseHandler responseHandler) {
        LogX.d("离线模式 : " + "收藏包.");
        try {
            JSONObject queryJson = new JSONObject(EntityUtils.toString(entity));
            String emoPackageId = queryJson.getString("source_id");
            if (emoPackageId == null || emoPackageId.isEmpty()) {
                throw new Exception("Invalid emoticon package id.");
            }
            SendRecordDAO.recordEvent("pkg_"+emoPackageId);
            String listId = queryJson.getString("list_id");
            if (listId.equals("")) { //收藏为新列表
                listId = getDeviceId(context) + "list" + System.currentTimeMillis();
            }
            UserList userList = FacehubApi.getApi().getUser().getUserListById(listId);
            userList.setForkFromId(emoPackageId);
            EmoPackage emoPackage = StoreDataContainer.getDataContainer().getUniqueEmoPackage(emoPackageId);
            JSONObject emoPackageJson = emoPackage.toJson();
            emoPackageJson.put("id", listId);
            userList.updateField(emoPackageJson);
            for (Emoticon emoticon : userList.getEmoticons()) {
                if (emoticon.getFileUrl(Image.Size.MEDIUM) == null
                        || emoticon.getFileUrl(Image.Size.FULL) == null) {
                    FacehubSDKException facehubSDKException
                            = new FacehubSDKException(mock_http_error, "收藏包失败 : 表情url为空.");
                    onFail(facehubSDKException, responseHandler);
                    return;
                }
            }
            JSONObject resultJson = new JSONObject();
            resultJson.put("list",userList.toJson());
            onSuccess(resultJson,responseHandler);
        } catch (Exception e) {
            FacehubSDKException facehubSDKException
                    = new FacehubSDKException(mock_http_error, "收藏包出错 : " + e);
            onFail(facehubSDKException, responseHandler);
        }
    }


    /**
     * ================================= 成功/错误回调 =================================
     */
    private void onSuccess(JSONObject resultJson, JsonHttpResponseHandler responseHandler) {
        responseHandler.onSuccess(200, null, resultJson);
    }

    private void onFail(Throwable throwable, JsonHttpResponseHandler responseHandler) {
        //伪造status code 400,都是请求的错.
        this.onFail(400, throwable, responseHandler);
    }

    private void onFail(int status, Throwable throwable, JsonHttpResponseHandler responseHandler) {
        responseHandler.onFailure(status, null, throwable, new JSONObject());
    }
}
