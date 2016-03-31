package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.RetryReq;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

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

    public UserListApi(User user , AsyncHttpClient client){
        this.user = user;
        this.client = client;
    }

    /**
     * 获取用户的分组
     *
     * @param resultHandlerInterface 结果回调
     */
    public void getUserList(final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        String url = HOST + "/api/v1/users/" + this.user.getUserId() + "/lists";
        dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //所有列表
                    final ArrayList<UserList> userLists = new ArrayList<>();
                    JSONArray listsJsonArray = response.getJSONArray("lists");
                    for (int i = 0; i < listsJsonArray.length(); i++) {
                        UserList userList = new UserList();
                        userList.userListFactoryByJson(listsJsonArray.getJSONObject(i), LATER_SAVE);
                        userLists.add(userList);
                    }
                    UserListDAO.saveInTX(userLists);
                    downloadAll(userLists, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            resultHandlerInterface.onResponse(userLists);
                        }

                        @Override
                        public void onError(Exception e) {
                            resultHandlerInterface.onError(e);
                        }
                    });

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
    private float progress=0f;
    public void downloadAll(final ArrayList<UserList> userLists , final ResultHandlerInterface resultHandlerInterface){
        retryTimes = 0;
        progress = 0f;
        allEmoticons.clear();
        final ArrayList<Image> covers = new ArrayList<>();
        for(int i=0;i<userLists.size();i++){
            for (int j=0;j<userLists.get(i).getEmoticons().size();j++){
                Emoticon emoticon = userLists.get(i).getEmoticons().get(j);
                allEmoticons.add(emoticon);
                if(userLists.get(i).getCover()!=null) {
                    covers.add(userLists.get(i).getCover());
                }
            }
        }
        downloadEach(new ArrayList<>(allEmoticons), resultHandlerInterface);

        for(int i=0;i<covers.size();i++){
            final int finalI = i;
            covers.get(i).download2File(Image.Size.FULL, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    covers.get(finalI).save2Db();
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    private int totalCount = 0;
    private int success = 0;
    private int fail = 0;
    private void downloadEach(ArrayList<Emoticon> emoticons , final ResultHandlerInterface resultHandlerInterface){
        //开始一个个下载
        totalCount = emoticons.size();
        success = 0;
        fail = 0;
        final ArrayList<Emoticon> failEmoticons = new ArrayList<>();
        fastLog("开始逐个下载 total : " + totalCount);
        for(int i=0;i<totalCount;i++){
            final Emoticon emoticon = emoticons.get(i);
            fastLog("开始下载 : " + i);
            emoticon.download2File(Image.Size.FULL, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    success++;
                    progress = success * 1f / totalCount * 100;
                    fastLog("下载中，成功 : " + success + " || " + progress + "%");
                    onFinish();
                    emoticon.save2Db();
                }

                @Override
                public void onError(Exception e) {
                    fail++;
                    failEmoticons.add(emoticon);
                    onFinish();
                    fastLog("下载中，失败 : " + success);
                }

                private void onFinish() {
                    if (success + fail != totalCount) {
                        return; //仍在下载中
                    }
                    if (fail == 0) { //全部下载完成
                        resultHandlerInterface.onResponse("success.");
                    } else if (retryTimes < 3) { //重试次数两次
                        retryTimes++;
                        downloadEach(failEmoticons, resultHandlerInterface);
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
    public void collectEmoById(String emoticonId, String toUserListId,final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(emoticonId);
        params.put("contents",jsonArray);
        params.put("action", "add");
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                        + "/lists/" + toUserListId;
        dumpReq(url, params);
        client.put(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson(jsonObject, DO_SAVE);
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
    public void createUserListByName(String listName,final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        params.setUseJsonStreamer(true);
        params.put("name", listName);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists";
        fastLog("url : " + url + "\nparams : " + params);

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson(jsonObject, DO_SAVE);
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
    public void renameUserListById(String userListId, String name,final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        params.setUseJsonStreamer(true);
        params.put("action", "rename");
        params.put("name", name);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
        fastLog("url : " + url + "\nparams : " + params);

        client.put(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson(jsonObject, true);
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
    public boolean removeUserListById(final String userListId) {
//    public boolean removeUserListById(String userListId , final ResultHandlerInterface resultHandlerInterface) {
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
//                resultHandlerInterface.onResponse(response);
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
                //TODO:根绝code判断是否记录重试
                RetryReq retryReq = new RetryReq(RetryReq.REMOVE_LIST,userListId,new ArrayList<String>());
                retryReq.save2DB();
//                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
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
    public void collectEmoPackageById(String packageId,final ResultHandlerInterface resultHandlerInterface) {
        this.collectEmoPackageById(packageId, "", resultHandlerInterface);
    }

    /**
     * 收藏表情包到指定分组，将表情包表情全部添加到【指定分组】
     *
     * @param packageId              表情包唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoPackageById(String packageId, String toUserListId , final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        params.setUseJsonStreamer(true);
        params.put("list_id",packageId);
        params.put("dest_id", toUserListId);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/batch";

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson(jsonObject, true);
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
     * @param emoticonIds 要删除表情的表情ID数组
     * @param userListId  指定的用户表情分组
     * @param resultHandlerInterface 结果回调
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true
     */
    public boolean removeEmoticonsByIds(ArrayList<String> emoticonIds, String userListId ,final ResultHandlerInterface resultHandlerInterface) {
        //TODO:删除表情
        //1.修改本地数据
        //2.请求服务器，若失败，则加入重试表
//        UserListDAO.delete( userListId );

        RequestParams params = this.user.getParams();
        JSONArray jsonArray = new JSONArray(emoticonIds);
        params.put("contents", jsonArray);
        params.put("action", "remove");
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
//        fastLog("url : " + url + "\nparams : " + params);

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson(jsonObject, DO_SAVE);
                    resultHandlerInterface.onResponse(userList);
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
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
        return true;
    }

    /**
     * 从指定分组删除单张表情
     *
     * @param emoticonId 要删除的表情ID
     * @param userListId 指定的分组
     * @param resultHandlerInterface 结果回调
     * @return 是否删除成功
     */
    public boolean removeEmoticonById(String emoticonId, String userListId ,ResultHandlerInterface resultHandlerInterface) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        return this.removeEmoticonsByIds( ids ,userListId , resultHandlerInterface);
    }

    /**
     * 重试删除列表
     *
     * @param userListId 列表id
     * @param retryHandler 回调结果
     */
    public void retryRemoveList(String userListId,final ResultHandlerInterface retryHandler){
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
                retryHandler.onError(new Exception(statusCode + ""));
            }
        });
    }

    /**
     * 重试删除表情
     *
     * @param emoticonIds 表情id
     * @param userListId 列表id
     * @param retryHandler 回调结果
     */
    public void retryRemoveEmoticon(ArrayList<String> emoticonIds,String userListId,final ResultHandlerInterface retryHandler){
        RequestParams params = this.user.getParams();
        JSONArray jsonArray = new JSONArray(emoticonIds);
        params.put("contents", jsonArray);
        params.put("action", "remove");
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
//        fastLog("url : " + url + "\nparams : " + params);

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson(jsonObject, LATER_SAVE);
                    retryHandler.onResponse(userList);
                } catch (JSONException e) {
                    retryHandler.onResponse(e);
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
                retryHandler.onError(new Exception(statusCode+""));
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
    public void moveEmoticonById(final String emoticonId, String fromId, final String toId, final ResultHandlerInterface resultHandlerInterface) {
        //TODO:移动表情
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        this.removeEmoticonsByIds(ids, fromId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                collectEmoById( emoticonId , toId , resultHandlerInterface );
            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(e);
            }
        });
    }
}
