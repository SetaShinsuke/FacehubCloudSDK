package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.EmoticonDAO;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.RetryReq;
import com.azusasoft.facehubcloudsdk.api.models.RetryReqDAO;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
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

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.HOST;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.DO_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LATER_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.dumpReq;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * 对个人列表进行处理的API
 */
public class UserListApi {
    private User user;
    private AsyncHttpClient client;

    UserListApi(User user, AsyncHttpClient client) {
        this.user = user;
        this.client = client;
    }

    /**
     * 获取用户的分组
     *
     * @param resultHandlerInterface 结果回调
     */
    void getUserList(final ResultHandlerInterface resultHandlerInterface,
                     final ProgressInterface progressInterface) {
        RequestParams params = this.user.getParams();
        String url = HOST + "/api/v1/users/" + this.user.getUserId() + "/lists";
        dumpReq(url, params);
        progressInterface.onProgress(0);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //所有列表
                    final ArrayList<UserList> userLists = new ArrayList<>();
                    JSONArray listsJsonArray = response.getJSONArray("lists");
                    progressInterface.onProgress(10);
                    for (int i = 0; i < listsJsonArray.length(); i++) {
                        UserList userList = new UserList();
                        userList.updateField(listsJsonArray.getJSONObject(i), LATER_SAVE);
                        userLists.add(userList);
                        fastLog("userList fork from : " + userList.getForkFromId());
                        progressInterface.onProgress(10+20.0*(i+1)/listsJsonArray.length());
                    }
                    CodeTimer codeTimer = new CodeTimer();
                    UserListDAO.deleteAll();
                    RetryReqDAO.deleteAll();
                    UserListDAO.saveInTX(userLists);

                    codeTimer.start("下载全部-总过程");
                    downloadAll(userLists, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            resultHandlerInterface.onResponse(userLists);
                        }

                        @Override
                        public void onError(Exception e) {
                            resultHandlerInterface.onError(e);
                        }
                    },progressInterface);
                    codeTimer.end("下载全部-总过程");

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

    private ArrayList<Emoticon> allEmoticons = new ArrayList<>();
    private int retryTimes = 0;
    private float progress = 0f;

    void downloadAll(final ArrayList<UserList> userLists, final ResultHandlerInterface resultHandlerInterface,
                     ProgressInterface progressInterface) {
        retryTimes = 0;
        progress = 0f;
        allEmoticons.clear();

        CodeTimer c = new CodeTimer();
        c.start("下载全部");

        final ArrayList<Image> covers = new ArrayList<>();
        for (int i = 0; i < userLists.size(); i++) {
            for (int j = 0; j < userLists.get(i).getEmoticons().size(); j++) {
                Emoticon emoticon = userLists.get(i).getEmoticons().get(j);
//                if(emoticon.getId().equals("01d76322-ea9b-43c4-85ba-30fff9216ccd")){
//                    fastLog("来断点");
//                    allEmoticons.add(emoticon);
//                }
                allEmoticons.add(emoticon);
                if (userLists.get(i).getCover() != null) {
                    allEmoticons.add(userLists.get(i).getCover());
                }
            }
        }
        c.end("计算需要下载的表情数");

        if (allEmoticons.size() == 0) { //没有表情,不执行逐个下载
            progressInterface.onProgress(99.9);
            resultHandlerInterface.onResponse(userLists);
        } else {
            c.start("download each.");
            downloadEach(new ArrayList<>(allEmoticons), resultHandlerInterface,progressInterface);
            c.end("download each.");
        }

//        c.start("下载封面");
//        for (int i = 0; i < covers.size(); i++) {
//            final int finalI = i;
//            covers.get(i).download2File(Image.Size.FULL, new ResultHandlerInterface() {
//                @Override
//                public void onResponse(Object response) {
////                    covers.get(finalI).save2Db();
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    LogX.e("封面下载失败 : " + e);
//                }
//            });
//        }
//        c.end("下载封面");
    }

    private int totalCount = 0;
    private int success = 0;
    private int fail = 0;

    private void downloadEach(final ArrayList<Emoticon> emoticons, final ResultHandlerInterface resultHandlerInterface, final ProgressInterface progressInterface) {
        //开始一个个下载
        totalCount = emoticons.size();
        success = 0;
        fail = 0;
        final ArrayList<Emoticon> failEmoticons = new ArrayList<>();
        fastLog("开始逐个下载 total : " + totalCount);
        for (int i = 0; i < totalCount; i++) {
            final Emoticon emoticon = emoticons.get(i);
            fastLog("开始下载 : " + i);
            emoticon.download2File(Image.Size.FULL, false, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    success++;
                    progress = success * 1f / totalCount * 100;
                    progressInterface.onProgress(30+progress*0.69);
                    fastLog("下载中，成功 : " + success + " || " + progress + "%");
                    onFinish();
                }

                @Override
                public void onError(Exception e) {
                    fail++;
                    failEmoticons.add(emoticon);
                    onFinish();
                    fastLog("下载中，失败 : " + fail);
//                    fastLog("下载中，失败 : " + fail + "\nDetail : " + e);
                }

                private void onFinish() {
                    if (success + fail != totalCount) {
                        return; //仍在下载中
                    }
                    if (fail == 0) { //全部下载完成
                        EmoticonDAO.saveInTx(allEmoticons);
                        resultHandlerInterface.onResponse("success.");
                    } else if (retryTimes < 3) { //重试次数两次
                        retryTimes++;
                        downloadEach(failEmoticons, resultHandlerInterface, progressInterface);
                    } else {
                        onError(new Exception("下载出错,失败个数 : " + allEmoticons.size()));
                    }
                }
            });
        }
    }

    /**
     * 收藏表情到指定分组
     *
     * @param emoticonId             表情唯一标识表情唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    void collectEmoById(String emoticonId, String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + toUserListId;
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(emoticonId);
        // RequestParams params = this.user.getParams();
//        params.put("contents", jsonArray);
//        params.put("action", "add");
//        params.setUseJsonStreamer(true);
        //dumpReq(url, params);
        JSONObject jsonObject = this.user.getParamsJson();
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
                    UserList userList = new UserList();
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
     * 新建分组
     *
     * @param listName               分组名
     * @param resultHandlerInterface 结果回调
     */
    void createUserListByName(String listName, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists";
//        RequestParams params = this.user.getParams();
//        params.setUseJsonStreamer(true);
//        params.put("name", listName);
        JSONObject jsonObject = this.user.getParamsJson();
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
//        client.post(url, params, new JsonHttpResponseHandler() {
        client.post(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
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
     * 重命名分组
     *
     * @param userListId             要重命名的列表id
     * @param name                   重命名的名字
     * @param resultHandlerInterface 结果回调
     */
    void renameUserListById(String userListId, String name, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
//        RequestParams params = this.user.getParams();
//        params.setUseJsonStreamer(true);
//        params.put("action", "rename");
//        params.put("name", name);
        JSONObject jsonObject = this.user.getParamsJson();
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
                    UserList userList = new UserList();
                    userList.updateField(jsonObject, true);
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
     * 删除分组
     *
     * @param userListId 分组id
     * @return 是否删除成功
     */
//    public boolean removeUserListById(final String userListId) {
    boolean removeUserListById(final String userListId) {
        //TODO:删除本地列表
        UserListDAO.delete(userListId);

        RequestParams params = this.user.getParams();
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
        fastLog("url : " + url + "\nparams : " + params);

        client.delete(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                fastLog("删除列表成功! ");
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
                //TODO:根据code判断是否记录重试
                LogX.e("删除列表出错 : " + parseHttpError(statusCode, throwable, addition));
                RetryReq retryReq = new RetryReq(RetryReq.REMOVE_LIST, userListId, new ArrayList<String>());
                retryReq.save2DB();
            }
        });
        return true;
    }

    /**
     * 收藏表情包，默认为表情包【新建分组】
     *
     * @param packageId              表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    void collectEmoPackageById(String packageId, final ResultHandlerInterface resultHandlerInterface) {
        this.collectEmoPackageById(packageId, "", resultHandlerInterface);
    }

    /**
     * 收藏表情包到指定分组，将表情包表情全部添加到【指定分组】
     *
     * @param packageId              表情包唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    void collectEmoPackageById(String packageId, String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/batch";
//        RequestParams params = this.user.getParams();
//        params.setUseJsonStreamer(true);
//        params.put("source_id",packageId);
//        params.put("list_id", toUserListId);
        JSONObject jsonObject = this.user.getParamsJson();
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
        client.post(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            //        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.updateField(jsonObject, true);
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
     * 从指定分组批量删除表情
     *
     * @param emoticonIds            要删除表情的表情ID数组
     * @param userListId             指定的用户表情分组
     * @param resultHandlerInterface 结果回调
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true
     */
    boolean removeEmoticonsByIds(final ArrayList<String> emoticonIds, final String userListId, final ResultHandlerInterface resultHandlerInterface) {
        //TODO:删除表情
        //1.修改本地数据
        //2.请求服务器，若失败，则加入重试表
        UserListDAO.deleteEmoticons(userListId, emoticonIds);

        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
//        RequestParams params = this.user.getParams();
//        JSONArray jsonArray = new JSONArray(emoticonIds);
//        params.put("contents", jsonArray);
//        params.put("action", "remove");
//        params.setUseJsonStreamer(true);
//        client.put(url, params, new JsonHttpResponseHandler() {
        JSONArray jsonArray = new JSONArray(emoticonIds);
        JSONObject jsonObject = this.user.getParamsJson();
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
                    fastLog("删除列表成功!");
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.updateField(jsonObject, DO_SAVE);
                    resultHandlerInterface.onResponse(response);
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
                //TODO:根据code判断是否重试
                LogX.e("删除表情失败 : " + parseHttpError(statusCode, throwable, addition) + "");
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
                if (statusCode < 400 || statusCode > 500) { //记录重试
                    RetryReq retryReq = new RetryReq(RetryReq.REMOVE_EMO, userListId, emoticonIds);
                    retryReq.save2DB();
                    fastLog("保存重试记录");
                }

            }
        });
        return true;
    }

    /**
     * 从指定分组删除单张表情
     *
     * @param emoticonId             要删除的表情ID
     * @param userListId             指定的分组
     * @param resultHandlerInterface 结果回调
     * @return 是否删除成功
     */
    boolean removeEmoticonById(String emoticonId, String userListId, ResultHandlerInterface resultHandlerInterface) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        return this.removeEmoticonsByIds(ids, userListId, resultHandlerInterface);
    }

    /**
     * 重试删除列表
     *
     * @param userListId   列表id
     * @param retryHandler 重试结束后的回调，继续重试前进行中的请求
     */
    void retryRemoveList(final String userListId, final ResultHandlerInterface retryHandler) {
        RequestParams params = this.user.getParams();
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
        fastLog("url : " + url + "\nparams : " + params);

        client.delete(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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
                //TODO:判断错误类型，是否需要重试
//                retryHandler.onError(new Exception(statusCode + ""));
                if (statusCode < 400 || statusCode > 500) {
                    RetryReq retryReq = new RetryReq(RetryReq.REMOVE_LIST, userListId, new ArrayList<String>());
                    retryReq.save2DB();
                    fastLog("保存重试记录");
                }
                retryHandler.onError(new Exception(parseHttpError(statusCode, throwable, addition)));
            }
        });
    }

    /**
     * 重试删除表情
     *
     * @param emoticonIds  表情id
     * @param userListId   列表id
     * @param retryHandler 重试结束后的回调，继续重试前进行中的请求
     */
    void retryRemoveEmoticonsByIds(final ArrayList<String> emoticonIds, final String userListId, final ResultHandlerInterface retryHandler) {
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
//        RequestParams params = this.user.getParams();
//        JSONArray jsonArray = new JSONArray(emoticonIds);
//        params.put("contents", jsonArray);
//        params.put("action", "remove");
//        params.setUseJsonStreamer(true);
//        client.put(url, params, new JsonHttpResponseHandler() {
        JSONArray jsonArray = new JSONArray(emoticonIds);
        JSONObject jsonObject = this.user.getParamsJson();
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
                fastLog("重试删除表情成功.");
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
     * 将表情从一个分组移动到另一个分组
     *
     * @param emoticonId             要移动的表情ID
     * @param fromId                 移出分组id
     * @param toId                   移入分组id
     * @param resultHandlerInterface 结果回调
     */
    void moveEmoticonById(final String emoticonId, String fromId, final String toId, final ResultHandlerInterface resultHandlerInterface) {
        //TODO:移动表情
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        this.removeEmoticonsByIds(ids, fromId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                collectEmoById(emoticonId, toId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(e);
            }
        });
    }
}
