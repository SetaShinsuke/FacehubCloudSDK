package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException;
import com.azusasoft.facehubcloudsdk.api.models.RetryReq;
import com.azusasoft.facehubcloudsdk.api.models.RetryReqDAO;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.ReorderEvent;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.HOST;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.DO_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LATER_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.dumpReq;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * 对个人列表进行处理的API
 */
public class UserListApi {
    private AsyncHttpClient client;

    UserListApi(AsyncHttpClient client) {
        this.client = client;
    }
    int count=0;
    float downloaded =0;
    int downLists =0;
    /**
     * 获取用户的分组
     *
     * @param getUserListHandler 结果回调,返回一个{@link User}对象;
     */
    void getUserList(final User user, final ResultHandlerInterface getUserListHandler,
                     final ProgressInterface progressInterface) {
        RequestParams params = user.getParams();
        params.put("skip_detail",true);
        final String url = HOST + "/api/v1/users/" + user.getUserId() + "/lists";
        dumpReq(url, params);
        progressInterface.onProgress(0);
        final String oldUserId = user.getUserId();
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(isUserChanged(oldUserId)){
                    LogX.w("getUserList成功，但用户发生改变，忽略登录结果 : " +
                            "\nOld User : " + oldUserId
                            + " || New User : " + FacehubApi.getApi().getUser().getUserId());
                    return;
                }
                try {
                    //所有列表
                    final ArrayList<UserList> userLists = new ArrayList<>();
                    JSONArray listsJsonArray = response.getJSONArray("lists");
                    progressInterface.onProgress(1);
                    for (int i = 0; i < listsJsonArray.length(); i++) {
                        JSONObject jsonObject = listsJsonArray.getJSONObject(i);
                        UserList userList = FacehubApi.getApi().getUser()
                                .getUserListById(jsonObject.getString("id"));
                        userList.updateField(jsonObject, LATER_SAVE);
                        userLists.add(userList);
//                        fastLog("userList fork from : " + userList.getForkFromId());
                    }
                    progressInterface.onProgress(2);
                    RetryReqDAO.deleteAll();
                    user.setUserLists(userLists);
//                    count=0;
//                    downloaded =0;
//                    downLists =0;
//                    for(UserList list:userLists){
//                        count=count+list.size()+1;
//                    }
//                    for(final UserList list:userLists){
//                        list.download(new ResultHandlerInterface() {
//                            @Override
//                            public void onResponse(Object response) {
//                                downLists +=1;
//                                if(downLists ==userLists.size()){
//                                    getUserListHandler.onResponse( user );
//                                }
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                getUserListHandler.onError(e);
//                            }
//                        }, new ProgressInterface() {
//
//                            @Override
//                            public void onProgress(double process) {
//                                downloaded += 1;
//
//                                progressInterface.onProgress((downloaded*1f / count)*98);
//                            }
//                        });
//                    }
//                    codeTimer.end("下载全部-总过程");
                    progressInterface.onProgress(100f);
                    getUserListHandler.onResponse(user);
                } catch (JSONException e) {
                    if(isUserChanged(oldUserId)){
                        LogX.w("getUserList出错，但用户发生改变，忽略登录结果 : " +
                                "\nOld User : " + oldUserId
                                + " || New User : " + FacehubApi.getApi().getUser().getUserId());
                        return;
                    }
                    FacehubSDKException exception
                            = new FacehubSDKException("拉取列表出错Json解析出错 : " + e);
                    exception.setErrorType(FacehubSDKException.ErrorType.loginError_needRetry);
                    getUserListHandler.onError(exception);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                if(isUserChanged(oldUserId)){
                    LogX.w("getUserList出错，但用户发生改变，忽略登录结果 : " +
                            "\nOld User : " + oldUserId
                            + " || New User : " + FacehubApi.getApi().getUser().getUserId());
                    return;
                }
                if(statusCode<400 || statusCode>500){
                    FacehubSDKException exception
                            = new FacehubSDKException("拉取列表出错 : " + parseHttpError(statusCode, throwable, addition));
                    exception.setErrorType(FacehubSDKException.ErrorType.loginError_needRetry);
                    getUserListHandler.onError(exception);
                }else {
                    getUserListHandler.onError(parseHttpError(statusCode, throwable, addition));
                }
            }

            private boolean isUserChanged(String oldUserId){
                return oldUserId==null || !oldUserId.equals(FacehubApi.getApi().getUser().getUserId());
            }
        });
    }


    /**
     * 收藏表情到指定分组;
     *
     * @param emoticonIds            要收藏的表情;
     * @param toUserListId           用户分组标识;
     * @param resultHandlerInterface 结果回调,返回一个{@link UserList}对象;
     */
    void collectEmoById(User user,ArrayList<String> emoticonIds, String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + toUserListId;
        JSONArray jsonArray = new JSONArray();
        for(String emoticonId : emoticonIds) {
            jsonArray.put(emoticonId);
        }
        // RequestParams params = this.user.getParams();
//        params.put("contents", jsonArray);
//        params.put("action", "add");
//        params.setUseJsonStreamer(true);
        //dumpReq(url, params);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("contents", jsonArray);
            jsonObject.put("action", "add");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            //        client.put(url,params,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = FacehubApi.getApi().getUser()
                                            .getUserListById(jsonObject.getString("id"));
                    userList.updateField(jsonObject, DO_SAVE);
                    resultHandlerInterface.onResponse(userList);
                } catch (JSONException e) {
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    /**
     * 拉取单个列表
     * @param listId 要拉取的列表id
     * @param resultHandlerInterface 回调，返回一个{@link UserList};
     */
    public void getUserListDetailById(final User user , final String listId, final ResultHandlerInterface resultHandlerInterface){
        RequestParams params = user.getParams();
        final String url = HOST + "/api/v1/users/" + user.getUserId() + "/lists/" + listId;
        dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                UserList userList = user.getUserListById(listId);
                try {
                    userList.updateField(response.getJSONObject("list"),false);
                    resultHandlerInterface.onResponse(userList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    /**
     * 新建分组;
     *
     * @param listName               分组名;
     * @param resultHandlerInterface 结果回调,返回 {@link UserList} ;
     */
    void createUserListByName(User user,String listName, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists";
//        RequestParams params = this.user.getParams();
//        params.setUseJsonStreamer(true);
//        params.put("name", listName);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("name", listName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        reorderingCount++;
//        client.post(url, params, new JsonHttpResponseHandler() {
        client.post(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    final UserList userList = FacehubApi.getApi().getUser()
                            .getUserListById(jsonObject.getString("id"));
                    userList.updateField(jsonObject, DO_SAVE);

                    ArrayList<String> listIds = new ArrayList<>();
                    for(UserList userList1:FacehubApi.getApi().getUser().getUserLists()){
                        listIds.add(userList1.getId());
                    }
                    //创建后自动排序
                    FacehubApi.getApi().reorderUserLists(listIds, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            reorderingCount--;
                            resultHandlerInterface.onResponse(userList);
                        }

                        @Override
                        public void onError(Exception e) { //暂时忽略掉所有排序的错误
                            reorderingCount--;
                            if(reorderingCount>0){
                                resultHandlerInterface.onResponse(userList);
                            }else {
//                                resultHandlerInterface.onError(e);
                                resultHandlerInterface.onResponse(userList);
                            }
                        }
                    });
//                    resultHandlerInterface.onResponse(userList);
                } catch (JSONException e) {
                    reorderingCount--;
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                reorderingCount--;
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    /**
     * 重命名分组;
     *
     * @param userListId             要重命名的列表id;
     * @param name                   重命名的名字;
     * @param resultHandlerInterface 结果回调,返回 {@link UserList} ;
     */
    void renameUserListById(User user, String userListId, String name, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + userListId;
//        RequestParams params = this.user.getParams();
//        params.setUseJsonStreamer(true);
//        params.put("action", "rename");
//        params.put("name", name);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("name", name);
            jsonObject.put("action", "rename");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        client.put(url, params, new JsonHttpResponseHandler() {
        client.put(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = FacehubApi.getApi().getUser()
                            .getUserListById(jsonObject.getString("id"));
                    userList.updateField(jsonObject, DO_SAVE);
                    resultHandlerInterface.onResponse(userList);
                } catch (JSONException e) {
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    /**
     * 删除分组;
     *
     * @param userListId 分组id;
     * @return 是否删除成功;
     */
//    public boolean removeUserListById(final String userListId) {
    boolean removeUserListById(User user, final String userListId) {

//        UserListDAO.delete(userListId);
        user.deleteUserList(userListId);

        RequestParams params = user.getParams();
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + userListId;
        dumpReq(url,params);

        client.delete(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                LogX.i("删除列表成功! ");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                //根据code判断是否记录重试
                LogX.e("删除列表出错 : " + parseHttpError(statusCode, throwable, addition));
                if(statusCode<400 || statusCode>500) {
                    RetryReq retryReq = new RetryReq(RetryReq.REMOVE_LIST, userListId, new ArrayList<String>());
                    retryReq.save2DB();
                }
            }
        });
        return true;
    }

    /**
     * 收藏表情包，默认为表情包【新建分组】;
     *
     * @param packageId              表情包唯一标识;
     * @param resultHandlerInterface 结果回调,返回一个 {@link UserList} ;
     */
    void collectEmoPackageById(User user,String packageId, final ResultHandlerInterface resultHandlerInterface) {
        this.collectEmoPackageById(user,packageId, "", resultHandlerInterface);
    }

    private static int reorderingCount = 0;
    /**
     * 收藏表情包到指定分组，将表情包表情全部添加到【指定分组】
     *
     * @param packageId              表情包唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调,返回一个 {@link UserList} ;
     */
    void collectEmoPackageById(final User user, String packageId, String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/batch";
//        RequestParams params = this.user.getParams();
//        params.setUseJsonStreamer(true);
//        params.put("source_id",packageId);
//        params.put("list_id", toUserListId);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("source_id", packageId);
            jsonObject.put("list_id", toUserListId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        reorderingCount++;
        client.post(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            //        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    final UserList userList = FacehubApi.getApi().getUser()
                                            .getUserListById(jsonObject.getString("id"));
                    userList.updateField(jsonObject, DO_SAVE);
                    FacehubApi.getApi().getUser().updateLists();
                    ArrayList<String> listIds = new ArrayList<>();
                    for(UserList userList1:FacehubApi.getApi().getUser().getUserLists()){
                        listIds.add(userList1.getId());
                    }
                    FacehubApi.getApi().reorderUserLists(listIds, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            reorderingCount--;
                            resultHandlerInterface.onResponse(userList);
                        }

                        @Override
                        public void onError(Exception e) { //暂时忽略掉所有排序的错误
                            reorderingCount--;
                            if(reorderingCount>0){
                                resultHandlerInterface.onResponse(userList);
                            }else {
//                                resultHandlerInterface.onError(e);
                                resultHandlerInterface.onResponse(userList);
                            }
                        }
                    });
                } catch (JSONException e) {
                    reorderingCount--;
                    resultHandlerInterface.onError(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                reorderingCount--;
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    /**
     * 从指定分组批量删除表情;
     *
     * @param emoticonIds            要删除表情的表情ID数组;
     * @param userListId             指定的用户表情分组;
     * @param resultHandlerInterface 结果回调,返回 {@link UserList} ;
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true;
     */
    boolean removeEmoticonsByIds(final User user, final ArrayList<String> emoticonIds, final String userListId, final ResultHandlerInterface resultHandlerInterface) {
        //删除表情
        //1.修改本地数据
        //2.请求服务器，若失败，则加入重试表
//        UserListDAO.deleteEmoticons(userListId, emoticonIds);
        user.deleteEmoticonsFromList(userListId,emoticonIds);

        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + userListId;
//        RequestParams params = this.user.getParams();
//        JSONArray jsonArray = new JSONArray(emoticonIds);
//        params.put("contents", jsonArray);
//        params.put("action", "remove");
//        params.setUseJsonStreamer(true);
//        client.put(url, params, new JsonHttpResponseHandler() {
        JSONArray jsonArray = new JSONArray(emoticonIds);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("contents", jsonArray);
            jsonObject.put("action", "remove");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, url, entity, "application/json", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    LogX.i("删除列表成功!");
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = FacehubApi.getApi().getUser()
                            .getUserListById(jsonObject.getString("id"));
                    userList.updateField(jsonObject, DO_SAVE);
                    resultHandlerInterface.onResponse( userList );
                } catch (JSONException e) {
                    resultHandlerInterface.onResponse(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                //根据code判断是否重试
                LogX.e("删除表情失败 : " + parseHttpError(statusCode, throwable, addition) + "");
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
                if (statusCode < 400 || statusCode > 500) { //记录重试
                    RetryReq retryReq = new RetryReq(RetryReq.REMOVE_EMO, userListId, emoticonIds);
                    retryReq.save2DB();
                    LogX.i("保存删表情重试记录");
                }

            }
        });
        return true;
    }

    /**
     * 替换指定分组的表情;
     *
     * @param emoticonIds            要替换的表情ID数组;
     * @param userListId             指定的用户表情分组;
     * @param resultHandlerInterface 结果回调,返回 {@link UserList} ;
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true;
     */
    boolean replaceEmoticonsByIds(final User user, final ArrayList<String> emoticonIds, final String userListId, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + userListId;
        JSONArray jsonArray = new JSONArray(emoticonIds);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("contents", jsonArray);
            jsonObject.put("action", "replace");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, url, entity, "application/json", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = FacehubApi.getApi().getUser()
                            .getUserListById(jsonObject.getString("id"));
                    userList.updateField(jsonObject, DO_SAVE);
                    resultHandlerInterface.onResponse( userList );

                    ReorderEvent event = new ReorderEvent();
                    EventBus.getDefault().post(event);
                } catch (JSONException e) {
                    resultHandlerInterface.onResponse(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                //根据code判断是否重试
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));

            }
        });
        return true;
    }

    /**
     * 从指定分组删除单张表情;
     *
     * @param emoticonId             要删除的表情ID;
     * @param userListId             指定的分组;
     * @param resultHandlerInterface 结果回调,返回 {@link UserList} ;
     * @return 是否删除成功
     */
    boolean removeEmoticonById(User user,String emoticonId, String userListId, ResultHandlerInterface resultHandlerInterface) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        return this.removeEmoticonsByIds(user,ids, userListId, resultHandlerInterface);
    }

    /**
     * 重试删除列表;
     *
     * @param userListId   列表id;
     * @param retryHandler 重试结束后的回调，继续重试前进行中的请求,response为列表的id;
     */
    void retryRemoveList(User user,final String userListId, final ResultHandlerInterface retryHandler) {
        RequestParams params = user.getParams();
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + userListId;
        dumpReq(url,params);

        client.delete(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                retryHandler.onResponse( userListId );
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
                //判断错误类型，是否需要重试
//                retryHandler.onError(new Exception(statusCode + ""));
                if (statusCode < 400 || statusCode > 500) {
                    RetryReq retryReq = new RetryReq(RetryReq.REMOVE_LIST, userListId, new ArrayList<String>());
                    retryReq.save2DB();
                    LogX.i("保存删列表重试记录");
                }
                retryHandler.onError(new Exception(parseHttpError(statusCode, throwable, addition)));
            }
        });
    }

    /**
     * 重试删除表情;
     *
     * @param emoticonIds  表情id;
     * @param userListId   列表id;
     * @param retryHandler 重试结束后的回调，继续重试前进行中的请求,response类型不确定;
     */
    void retryRemoveEmoticonsByIds(User user,final ArrayList<String> emoticonIds, final String userListId, final ResultHandlerInterface retryHandler) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists/" + userListId;
//        RequestParams params = this.user.getParams();
//        JSONArray jsonArray = new JSONArray(emoticonIds);
//        params.put("contents", jsonArray);
//        params.put("action", "remove");
//        params.setUseJsonStreamer(true);
//        client.put(url, params, new JsonHttpResponseHandler() {
        JSONArray jsonArray = new JSONArray(emoticonIds);
        JSONObject jsonObject = user.getParamsJson();
        try {
            jsonObject.put("contents", jsonArray);
            jsonObject.put("action", "remove");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, url, entity, "application/json", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                LogX.i("重试删除表情成功.");
                retryHandler.onResponse(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail(statusCode, throwable, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail(statusCode, throwable, errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode, Throwable throwable, Object addition) {
//                retryHandler.onError(new Exception(statusCode+""));
                if (statusCode < 400 || statusCode > 500) {
                    LogX.e("重试删除表情失败,错误码 : " + statusCode + " || 需要再次重试");
                    RetryReq retryReq = new RetryReq(RetryReq.REMOVE_EMO, userListId, emoticonIds);
                    retryReq.save2DB();
                } else {
                    LogX.e("重试删除表情失败,错误码 : " + statusCode + " || 服务器错误,不继续重试");
                }
                retryHandler.onError(new Exception(parseHttpError(statusCode, throwable, addition)));
            }
        });
    }

    /**
     * 将表情从一个分组移动到另一个分组;
     *
     * @param emoticonIds            要移动的表情ID;
     * @param fromId                 移出分组ID;
     * @param toId                   移入分组ID;
     * @param resultHandlerInterface 结果回调,返回一个{@link UserList}对象,为收藏到的列表;
     */
    void moveEmoticonById(final User user,final ArrayList<String> emoticonIds, String fromId, final String toId, final ResultHandlerInterface resultHandlerInterface) {
        //移动表情
        this.removeEmoticonsByIds(user,emoticonIds, fromId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                collectEmoById(user,emoticonIds, toId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(e);
            }
        });
    }
}
