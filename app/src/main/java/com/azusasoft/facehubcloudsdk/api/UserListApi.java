package com.azusasoft.facehubcloudsdk.api;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.List;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
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
        dumpReq( url , params);
        client.get(url , params , new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //所有列表
                    ArrayList<UserList> userLists = new ArrayList<>();
                    JSONArray listsJsonArray = response.getJSONArray("lists");
                    for (int i=0;i<listsJsonArray.length();i++){
                        UserList userList = new UserList();
                        userList.userListFactoryByJson( listsJsonArray.getJSONObject(i) , LATER_SAVE );
                        userLists.add(userList);
                    }
                    UserListDAO.saveInTX(userLists);
                    resultHandlerInterface.onResponse( userLists );
                } catch (JSONException e) {
                    resultHandlerInterface.onError( e );
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                onFail( statusCode , throwable , responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail( statusCode , throwable , errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                onFail( statusCode , throwable , errorResponse);
            }

            //打印错误信息
            private void onFail(int statusCode , Throwable throwable , Object addition){
                resultHandlerInterface.onError( parseHttpError( statusCode , throwable , addition) );
            }
        });
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
        dumpReq( url , params );
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson( jsonObject , DO_SAVE );
                    resultHandlerInterface.onResponse(userList);
                } catch (JSONException e) {
                    resultHandlerInterface.onError( e );
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
        params.put("name",listName);
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
                    resultHandlerInterface.onResponse( userList );
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
        params.put("name",name);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
        fastLog("url : " + url + "\nparams : " + params);

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("list");
                    UserList userList = new UserList();
                    userList.userListFactoryByJson( jsonObject , true );
                    resultHandlerInterface.onResponse( userList );
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
    public boolean removeUserListById(String userListId , final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = this.user.getParams();
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
        fastLog("url : " + url + "\nparams : " + params);

        client.delete(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                resultHandlerInterface.onResponse(response);
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
        return true;
    }

    /**
     * 收藏表情包，默认为表情包【新建分组】
     *
     * @param packageId              表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoPackageById(String packageId,final ResultHandlerInterface resultHandlerInterface) {
        this.collectEmoPackageById(packageId,"",resultHandlerInterface);
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
                    userList.userListFactoryByJson( jsonObject , true );
                    resultHandlerInterface.onResponse( userList );
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
        RequestParams params = this.user.getParams();
        JSONArray jsonArray = new JSONArray(emoticonIds);
        params.put("contents",jsonArray);
        params.put("action", "remove");
        params.setUseJsonStreamer(true);
        String url = HOST + "/api/v1/users/" + this.user.getUserId()
                + "/lists/" + userListId;
//        fastLog("url : " + url + "\nparams : " + params);

        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                resultHandlerInterface.onResponse(response);
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
