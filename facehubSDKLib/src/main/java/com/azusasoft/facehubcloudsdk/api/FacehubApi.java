package com.azusasoft.facehubcloudsdk.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.util.Log;

import com.azusasoft.facehubcloudsdk.activities.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.db.DAOHelper;
import com.azusasoft.facehubcloudsdk.api.models.*;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonsRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.dumpReq;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.addString2Params;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * Api
 */
public class FacehubApi {
    //    protected final static String HOST = "http://10.0.0.79:9292";  //内网
     final static String HOST = "https://yun.facehub.me";  //外网
//    public final static String HOST = "http://172.16.0.2:9292";  //外网

    private static FacehubApi api;
    public static String appId = null;
    private static User user;
    private AsyncHttpClient client;
    private String themeColorString = "#f33847";

    public UserListApi getUserListApi() {
        return userListApi;
    }

    private UserListApi userListApi;
    private EmoticonApi emoticonApi;
    private static Context appContext;
    private static DAOHelper dbHelper;
//    private boolean available = false;

    /**
     * FacehubApi的初始化;
     */
    public static void init(Context context) {
        appContext = context;
        //初始化API(数据库)
        dbHelper = new DAOHelper(context);
        //initViews(context);
        boolean isDebuggable =  ( 0 != ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        if (isDebuggable) {
            LogX.logLevel = Log.VERBOSE;
        }else {
            LogX.logLevel = Log.WARN;
        }
    }

    /**
     * 设置主题色;
     *
     * @param colorString 一个表示颜色RGB的字符串，例如<p>"#f33847"</p>;
     */
    public void setThemeColor(String colorString){
        this.themeColorString = colorString;
    }

    private FacehubApi() {
        this.client = new AsyncHttpClient();
        user = new User(appContext);
        user.restore();
        this.userListApi = new UserListApi(client);
        this.emoticonApi = new EmoticonApi(client);
    }

    /**
     * 返回一个API实例;
     * @return {@link FacehubApi};
     */
    public static FacehubApi getApi() {
        if (api == null) {
            api = new FacehubApi();
        }
        return api;
    }

    /**
     * @return 返回当前app context;
     */
    public static Context getAppContext() {
        return appContext;
    }

    public static DAOHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * 初始化appId
     *
     * @param id 开发者id;
     */
    public void setAppId(String id) {
        appId = id;
    }

    /**
     * Log Level设置
     * 默认设置Log.VERBOSE(debug打包),Log.WARN(release打包)
     * @param logLevel 设置Log等级;
     */
    public void setLogLevel(int logLevel) {
        LogX.logLevel = logLevel;
    }

    /**
     * 设置当前有效的用户token
     *
     * @param token 数据请求令牌;
     */
    private void setUserToken(String token) {
        user.setToken(token);
    }

//    public boolean isAvailable(){
//        return available;
//    }


    /**
     * 设置当前用户
     *
     * @param userId                 用户唯一id;
     * @param token                  数据请求令牌;
     * @param resultHandlerInterface 结果回调.返回当前{@link User}对象;
     * @param progressInterface 进度回调;
     */
    public void login(String userId, String token, final ResultHandlerInterface resultHandlerInterface,
                    final ProgressInterface progressInterface  ) {
        progressInterface.onProgress(0);
        user = new User(appContext);
        if (user.restore() && user.getUserId().equals(userId)) { //用户恢复成功，且与当前登录用户的ID相同
            fastLog("用户恢复成功!");
            resultHandlerInterface.onResponse( user );
            return;
        }
        get_user_info(user, userId,token,new ResultHandlerInterface(){
            @Override
            public void onResponse(Object response) {
                if(user.isModified()){
                    userListApi.getUserList(user,resultHandlerInterface,progressInterface);
                }else{
                    progressInterface.onProgress(99.9);
                    resultHandlerInterface.onResponse(user);
                }

            }

            @Override
            public void onError(Exception e) {
                resultHandlerInterface.onError(e);
            }
        });
    }

    /**
     * 用来获取上次用户账户修改的时间戳;
     *
     * @param user 要检查的用户;
     * @param userId 用户id;
     * @param token 用户token;
     * @param resultHandlerInterface 结果回调，返回一个{@link User}对象;
     */
    public void get_user_info(final User user, final String userId, final String token, final ResultHandlerInterface resultHandlerInterface){
        String url = HOST + "/api/v1/users/" + userId ;
        RequestParams params = new RequestParams();
        params.put("user_id" , userId);
        params.put("auth_token" , token);
        params.put("app_id" , FacehubApi.appId);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    String updated_at= response.getJSONObject("user").getString("updated_at");
                    user.setUserInfo(userId,token,updated_at);
                    resultHandlerInterface.onResponse(user);

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
                //todo 处理服务器错误
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });

    }

    /**
     * 返回当前用户;
     * @return {@link User};
     */
    public User getUser() {
        return user;
    }

    /**
     * 退出登录
     */
    public void logout() {
       // UserListDAO.deleteAll();
        RetryReqDAO.deleteAll();
        user.logout();
    }

    /**
     * 注册新账户(仅供实例Demo使用)
     *
     * @param accessKey accessKey;
     * @param sign sign;
     * @param deadLine deadLine;
     * @param resultHandlerInterface 结果回调，返回一个包括新用户id和token的{@link HashMap};
     */
    public void registerUser(String accessKey,
                             String sign,
                             int deadLine,
                             final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/";
        JSONObject params = new JSONObject();
        try {
            params.put("app_id", FacehubApi.appId);
            params.put("access_key", accessKey);
            params.put("sign", sign);
            params.put("deadline", deadLine);
        } catch (JSONException e) {
            e.printStackTrace();
            resultHandlerInterface.onError(e);
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    HashMap<String, String> userData = new HashMap<>();
                    userData.put("user_id", response.getJSONObject("user").getString("id"));
                    userData.put("auth_token",response.getJSONObject("user").getString("auth_token"));
                    resultHandlerInterface.onResponse(userData);
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

    //region 表情商店

    /**
     * 从服务器获取Banner信息
     *
     * @param resultHandlerInterface 结果回调,返回一个由 {@link Banner} 组成的 {@link ArrayList} ;
     */
    public void getBanners(final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/recommends/last";
        dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<Banner> banners = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("recommends");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        banners.add(new Banner(jsonObject));
                    }
                    resultHandlerInterface.onResponse(banners);
                } catch (JSONException e) {
                    e.printStackTrace();
                    resultHandlerInterface.onError(e);
                }
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
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }


    /**
     * 从服务器获取Section类型的Tags
     *
     * @param resultHandlerInterface 结果回调,返回由字符串组成的{@link ArrayList},包含了需要的tags;
     */
    public void getPackageTagsBySection(final ResultHandlerInterface resultHandlerInterface) {
        this.getPackageTagsByParam("tag_type=section", resultHandlerInterface);
    }

    /**
     * 从服务器获取Tags，可自定义参数，参数格式为REST请求参数
     *
     * @param paramStr               自定义参数，<p> eg: tag_type = "type=section" </p>;
     * @param resultHandlerInterface 结果回调,返回由字符串组成的{@link ArrayList},包含了需要的tags;
     */
    public void getPackageTagsByParam(String paramStr, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        addString2Params(params, paramStr);
//        String url = HOST + "/api/v1/package_tags"; //2016-3-23修改
        String url = HOST + "/api/v1/tags";
        dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
//                    ArrayList<TagBundle> tagBundles = new ArrayList<>();
                    ArrayList<String> tags = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("tags");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String tag = jsonArray.getString(i);
                        tags.add(tag);
                    }
                    resultHandlerInterface.onResponse(tags);
//                    Iterator iterator = response.keys();
//                    while (iterator.hasNext()) {
//                        String key = (String) iterator.next();
//                        TagBundle tagBundle = new TagBundle(key);
//                        tagBundles.add(tagBundle.tagFactoryByJson(response.getJSONArray(key)));
//                    }
//                    resultHandlerInterface.onResponse(tagBundles);
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
     * 从服务器获取表情包列表
     *
     * @param paramStr               自定义参数，<p> eg: "tags[]=Section1&page=1&limit=8" </p> ;
     * @param resultHandlerInterface 结果回调,返回一个由{@link EmoPackage}组成的{@link ArrayList}, 包含了所需要的表情包;
     */
    public void getPackagesByParam(String paramStr, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        addString2Params(params, paramStr);
        String url = HOST + "/api/v1/packages";
        dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<EmoPackage> results = new ArrayList<>();
                    JSONArray packagesJsonArray = response.getJSONArray("packages");
                    for (int i = 0; i < packagesJsonArray.length(); i++) {
                        JSONObject jsonObject = packagesJsonArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        EmoPackage emoPackage = StoreDataContainer.getDataContainer().getUniqueEmoPackage(id);
                        emoPackage.updateField(jsonObject);
                        results.add(emoPackage);
                    }
                    resultHandlerInterface.onResponse(results);
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
     * 从服务器获取表情包列表
     *
     * @param tags                   目标分区名
     * @param page                   分页数，该分页第几页  >=0
     * @param limit                  limit:当前分页package最大回传数 >=1
     * @param resultHandlerInterface completionHandler 结果回调,返回一个由{@link EmoPackage}组成的{@link ArrayList}, 包含了所需要的表情包;
     */
    public void getPackagesByTags(ArrayList<String> tags, int page, int limit, final ResultHandlerInterface resultHandlerInterface) {
        String tagParams = "";
        for (int i = 0; i < tags.size(); i++) {
            tagParams += ("tags[]=" + tags.get(i) + "&");
        }
        String paramStr = tagParams + "page=" + page + "&limit=" + limit;
        this.getPackagesByParam(paramStr, resultHandlerInterface);
    }

    /**
     * 获取指定ID的package详细信息
     *
     * @param packageId              表情包id
     * @param resultHandlerInterface 结果回调,返回一个{@link EmoPackage}对象;
     */
    public void getPackageDetailById(String packageId, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/packages/" + packageId;
        dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("package");
                    String id = jsonObject.getString("id");
                    EmoPackage emoPackage = StoreDataContainer.getDataContainer().getUniqueEmoPackage(id);
                    emoPackage = emoPackage.updateField(jsonObject);
                    resultHandlerInterface.onResponse(emoPackage);
                } catch (JSONException e) {
                    resultHandlerInterface.onError(e);
                    LogX.e("拉取emoPackage Detail解析Json出错 : " + e);
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
     * 收藏表情到指定分组
     *
     * @param emoticonId             表情唯一标识表情唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调,返回一个{@link UserList}对象;
     */
    public void collectEmoById(final String emoticonId, final String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.collectEmoById(user,emoticonId, toUserListId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.collectEmoById(user,emoticonId, toUserListId, resultHandlerInterface);
            }
        });
    }

    /**
     * 收藏表情包，默认为表情包【新建分组】
     *
     * @param packageId              表情包唯一标识
     * @param resultHandlerInterface 结果回调,返回一个 {@link UserList} ;
     */
    public void collectEmoPackageById(final String packageId, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.collectEmoPackageById(user,packageId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.collectEmoPackageById(user,packageId, resultHandlerInterface);
            }
        });
    }

    /**
     * 收藏表情包到指定分组，将表情包表情全部添加到【指定分组】;
     *
     * @param packageId              表情包唯一标识;
     * @param toUserListId           用户分组标识;
     * @param resultHandlerInterface 结果回调,返回一个 {@link UserList} ;
     */
    public void collectEmoPackageById(final String packageId, final String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.collectEmoPackageById(user,packageId, toUserListId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.collectEmoPackageById(user,packageId, toUserListId, resultHandlerInterface);
            }
        });
    }
    //endregion

    //region 表情资源请求

    /**
     * 通过表情唯一标识向服务器请求表情资源;
     *
     * @param emoticonId             表情包唯一标识;
     * @param resultHandlerInterface 结果回调,返回一个 {@link Emoticon} 对象;
     */
    public void getEmoticonById(String emoticonId, ResultHandlerInterface resultHandlerInterface) {
        this.emoticonApi.getEmoticonById(user , emoticonId, resultHandlerInterface);
    }
    //endregion

    //region 本地表情管理

    /**
     * 检查本地是否已收藏该表情;
     *
     * @param emoticonId 表情唯一标识;
     * @return 是否已收藏;
     */
    public boolean isEmoticonCollected(String emoticonId) {
        return this.emoticonApi.isEmoticonCollected(emoticonId);
    }

    /**
     * 获取数据库所有用户列表
     *
     * @return 由 {@link UserList} 组成的 {@link ArrayList} ;
     */
    public ArrayList<UserList> getAllUserLists() {
        return UserListDAO.findAll();
    }

    /**
     * 从指定分组批量删除表情;
     *
     * @param emoticonIds 要删除表情的表情ID数组;
     * @param userListId  指定的用户表情分组;
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true;
     */
    public boolean removeEmoticonsByIds(final ArrayList<String> emoticonIds, final String userListId) {
        final boolean[] flag = {true};
        final ResultHandlerInterface[] emptyCallback = {new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
            }

            @Override
            public void onError(Exception e) {
            }
        }};
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                flag[0] = userListApi.removeEmoticonsByIds(user,emoticonIds, userListId, emptyCallback[0]);
                EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
                EventBus.getDefault().post(event);
            }

            @Override
            public void onError(Exception e) {
                flag[0] = userListApi.removeEmoticonsByIds(user,emoticonIds, userListId, emptyCallback[0]);
                EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
                EventBus.getDefault().post(event);
            }
        });
        return flag[0];
    }

    /**
     * 从指定分组删除单张表情;
     *
     * @param emoticonId 要删除的表情ID;
     * @param userListId 指定的分组;
     * @return 是否删除成功;
     */
    public boolean removeEmoticonById(final String emoticonId, final String userListId, final ResultHandlerInterface resultHandlerInterface) {
        final boolean[] flag = {true};
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                flag[0] = userListApi.removeEmoticonById(user,emoticonId, userListId, resultHandlerInterface);
                EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
                EventBus.getDefault().post(event);
            }

            @Override
            public void onError(Exception e) {
                flag[0] = userListApi.removeEmoticonById(user,emoticonId, userListId, resultHandlerInterface);
                EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
                EventBus.getDefault().post(event);
            }
        });
        return flag[0];
    }

    /**
     * 新建分组
     *
     * @param listName               分组名
     * @param resultHandlerInterface 结果回调,返回{@link UserList};
     */
    public void createUserListByName(final String listName, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.createUserListByName(user,listName, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.createUserListByName(user,listName, resultHandlerInterface);
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
    public void renameUserListById(final String userListId, final String name, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.renameUserListById(user,userListId, name, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.renameUserListById(user,userListId, name, resultHandlerInterface);
            }
        });
    }

    /**
     * 删除分组;
     *
     * @param userListId 分组id;
     * @return 是否删除成功;
     */
    public boolean removeUserListById(final String userListId) {
        final boolean[] flag = {true};
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                flag[0] = userListApi.removeUserListById(user,userListId);
                UserListRemoveEvent event = new UserListRemoveEvent();
                EventBus.getDefault().post(event);
            }

            @Override
            public void onError(Exception e) {
                flag[0] = userListApi.removeUserListById(user,userListId);
                UserListRemoveEvent event = new UserListRemoveEvent();
                EventBus.getDefault().post(event);
            }
        });
        return flag[0];
    }

    /**
     * 将表情从一个分组移动到另一个分组
     *
     * @param emoticonId             要移动的表情ID;
     * @param fromId                 移出分组id;
     * @param toId                   移入分组id;
     * @param resultHandlerInterface 结果回调,返回一个{@link UserList}对象,为收藏到的列表;
     */
    public void moveEmoticonById(final String emoticonId, final String fromId, final String toId, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.moveEmoticonById(user,emoticonId, fromId, toId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.moveEmoticonById(user,emoticonId, fromId, toId, resultHandlerInterface);
            }
        });
    }
    //endregion


    //todo:重试结束之后的操作无需放在回调里！
    //// FIXME: 2016/4/6 ：修改回调逻辑，只有getAll时需要根据回调成功失败来操作
    int total = 0;
    int success = 0;
    int fail = 0;

    /**
     * 重试函数
     * 执行之前请求失败的操作
     * @param retryHandler 重试结束的操作,response类型不确定;
     */
    private void retryRequests(final ResultHandlerInterface retryHandler) {
        final ArrayList<RetryReq> retryReqs = RetryReqDAO.findAll();
        total = retryReqs.size();
        success = 0;
        fail = 0;
        if (total == 0) {
            retryHandler.onResponse("retry done.");
            return;
        }
        for (final RetryReq retryReq : retryReqs) {
            String listId = retryReq.getListId();
            ArrayList<String> emoIds = retryReq.getEmoIds();
            if (retryReq.getType() == RetryReq.REMOVE_EMO) { //删除表情
                this.userListApi.retryRemoveEmoticonsByIds(user,emoIds, listId, new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        success++;
                        if (total == success + fail && total == success) {
                            retryHandler.onResponse(response); //全部重试成功
                            fastLog("重试成功 " + success + " || total : " + total);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        fail++;
                        if (total == success + fail) {
                            retryHandler.onError(e); //有某个重试失败
                            fastLog("重试失败 " + fail + " || total : " + total);
                        }
                    }
                });
            } else { //重试删除列表
                this.userListApi.retryRemoveList(user,retryReq.getListId(), new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        success++;
                        if (total == success + fail && total == success) {
                            retryHandler.onResponse(response); //全部重试成功
                            fastLog("重试成功 " + success + " || total : " + total);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        success++;
                        if (total == success + fail && total == success) {
                            retryHandler.onError(e); //有某个重试失败
                            fastLog("重试失败 " + fail + " || total : " + total);
                        }
                    }
                });
            }
            retryReq.delete();
        }
    }


    /**
     * 初始化View相关内容
     */
    public static void initViews(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
//        config.writeDebugLogs(); // Remove for release app
        config.memoryCache(new WeakMemoryCache());
        config.memoryCacheSize(4 * 1024 * 1024);

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }


    public int getThemeColor(){
        return Color.parseColor(themeColorString);
    }

    public int getThemeColorDark(){
        int color = getThemeColor();
        float factor = 0.8f;
        int a = Color.alpha( color );
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb( a,
                Math.max( (int)(r * factor), 0 ),
                Math.max( (int)(g * factor), 0 ),
                Math.max( (int)(b * factor), 0 ) );
    }
}
