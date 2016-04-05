package com.azusasoft.facehubcloudsdk.api;

import android.content.Context;
import android.support.v4.BuildConfig;
import android.util.Log;

import com.azusasoft.facehubcloudsdk.api.db.DAOHelper;
import com.azusasoft.facehubcloudsdk.api.models.*;
import com.azusasoft.facehubcloudsdk.api.utils.DownloadService;
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

import java.util.ArrayList;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.dumpReq;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.addString2Params;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 */
public class FacehubApi {
    protected final static String HOST = "http://10.0.0.79:9292";  //内网
//    public final static String HOST = "http://115.28.208.104:9292";  //外网

    private static FacehubApi api;
    public static String appId = "test-app-id";
    private static User user;
    private AsyncHttpClient client;
    private UserListApi userListApi;
    private EmoticonApi emoticonApi;
    private static Context appContext;
    private static DAOHelper dbHelper;
//    private boolean available = false;

    /**
     * FacehubApi的初始化
     */
    public static void init( Context context ) {
        appContext = context;
        //TODO:初始化API(数据库)
        dbHelper = new DAOHelper(context);
//        DownloadService.setDIR(appContext.getExternalFilesDir(null));
    }

    private FacehubApi() {
        this.client = new AsyncHttpClient();
        user = new User( appContext );
        if (BuildConfig.DEBUG) {
            LogX.logLevel = Log.VERBOSE;
        } else {
            LogX.logLevel = Log.INFO;
        }
        this.userListApi = new UserListApi(user , client);
        this.emoticonApi = new EmoticonApi(user , client);
    }

    public static FacehubApi getApi() {
        if (api == null) {
            api = new FacehubApi();
        }
        return api;
    }

    public static Context getAppContext(){
        return appContext;
    }

    public static DAOHelper getDbHelper(){
        return dbHelper;
    }

    /**
     * 初始化appId( 可在AndroidManifest.xml 中设置)
     *
     * @param id 开发者id.
     */
    public void setAppId(String id) {
        appId = id;
    }

    /**
     * Log Level设置
     *
     * @param logLevel 设置Log等级
     */
    public void setLogLevel(int logLevel) {
        LogX.logLevel = logLevel;
    }

    /**
     * 设置当前有效的用户token
     *
     * @param token 数据请求令牌.
     */
    public void setUserToken(String token) {
        user.setToken(token);
    }

//    public boolean isAvailable(){
//        return available;
//    }

    /**
     * 切换当前用户
     *
     * @param userId                 用户唯一id.
     * @param token                  数据请求令牌.
     * @param resultHandlerInterface 结果回调.
     */
    public void setCurrentUserId(String userId, String token, final ResultHandlerInterface resultHandlerInterface) {
        user.setUserId(userId, token);
        //同步列表
        getUserList(resultHandlerInterface);
    }
    public User getUser(){
        return user;
    }

    //TODO:退出登录
    public void logout() {
        UserListDAO.deleteAll();
    }

    //region 表情商店

    /**
     * 从服务器获取Banner信息
     *
     * @param resultHandlerInterface 结果回调.
     */
    public void getBanners(final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/recommends/last";
        dumpReq( url , params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<Banner> banners = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("recommends");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        Banner banner = new Banner();
                        banners.add(banner.bannerFactoryByJson(jsonObject));
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
     * @param resultHandlerInterface 结果回调
     */
    public void getPackageTagsBySection(final ResultHandlerInterface resultHandlerInterface) {
        this.getPackageTagsByParam("tag_type=section", resultHandlerInterface);
    }

    /**
     * 从服务器获取Tags，可自定义参数，参数格式为REST请求参数
     *
     * @param paramStr               自定义参数，eg: tag_type = "type=section";
     * @param resultHandlerInterface 结果回调.
     */
    public void getPackageTagsByParam(String paramStr, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        addString2Params(params, paramStr);
//        String url = HOST + "/api/v1/package_tags"; //2016-3-23修改
        String url = HOST + "/api/v1/tags";
        dumpReq( url , params );
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
//                    ArrayList<TagBundle> tagBundles = new ArrayList<>();
                    ArrayList<String> tags = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("tags");
                    for(int i=0;i<jsonArray.length();i++){
                        String tag = jsonArray.getString(i);
                        tags.add(tag);
                    }
                    resultHandlerInterface.onResponse( tags );
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
     * @param paramStr               自定义参数，eg: "tags[]=Section1&page=1&limit=8"
     * @param resultHandlerInterface 结果回调
     */
    public void getPackagesByParam(String paramStr, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        addString2Params(params, paramStr);
        String url = HOST + "/api/v1/packages";
        dumpReq(url,params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    ArrayList<EmoPackage> emoPackages = new ArrayList<>();
                    JSONArray packagesJsonArray = response.getJSONArray("packages");
                    for (int i = 0; i < packagesJsonArray.length(); i++) {
                        JSONObject jsonObject = packagesJsonArray.getJSONObject(i);
                        EmoPackage emoPackage = new EmoPackage();
                        emoPackage.emoPackageFactoryByJson(jsonObject);
                        emoPackages.add(emoPackage);
                    }
                    resultHandlerInterface.onResponse(emoPackages);
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
     * @param tags            目标分区名
     * @param page                   分页数，该分页第几页  >=0
     * @param limit                  limit:当前分页package最大回传数 >=1
     * @param resultHandlerInterface completionHandler 结果回调
     */
    public void getPackagesByTags(ArrayList<String> tags, int page, int limit, final ResultHandlerInterface resultHandlerInterface) {
        String tagParams = "";
        for(int i=0;i<tags.size();i++){
            tagParams += ("tags[]=" + tags.get(i) + "&");
        }
        String paramStr = tagParams + "page=" + page + "&limit=" + limit;
        this.getPackagesByParam( paramStr , resultHandlerInterface );
    }

    /**
     * 获取指定ID的package详细信息
     *
     * @param packageId              表情包id
     * @param resultHandlerInterface 结果回调
     */
    public void getPackageDetailById(String packageId, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/packages/" + packageId;
        dumpReq( url , params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("package");
                    EmoPackage emoPackage = new EmoPackage();
                    emoPackage = emoPackage.emoPackageFactoryByJson( jsonObject );
                    resultHandlerInterface.onResponse( emoPackage );
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
     * 收藏表情到指定分组
     *
     * @param emoticonId             表情唯一标识表情唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoById(String emoticonId, String toUserListId, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.collectEmoById( emoticonId, toUserListId, resultHandlerInterface);
    }

    /**
     * 收藏表情包，默认为表情包【新建分组】
     *
     * @param packageId              表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoPackageById(String packageId, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.collectEmoPackageById(packageId, resultHandlerInterface);
    }

    /**
     * 收藏表情包到指定分组，将表情包表情全部添加到【指定分组】
     *
     * @param packageId              表情包唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调
     */
    public void collectEmoPackageById(String packageId, String toUserListId, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.collectEmoPackageById(packageId, toUserListId, resultHandlerInterface);
    }
    //endregion

    //region 表情资源请求

    /**
     * 通过表情唯一标识向服务器请求表情资源
     *
     * @param emoticonId             表情包唯一标识
     * @param resultHandlerInterface 结果回调
     */
    public void getEmoticonById(String emoticonId, ResultHandlerInterface resultHandlerInterface) {
        this.emoticonApi.getEmoticonById(emoticonId, resultHandlerInterface);
    }
    //endregion

    //region 本地表情管理 TODO:重试机制
    /**
     * 检查本地是否已收藏该表情
     *
     * @param emoticonId 表情唯一标识
     * @return 是否已收藏
     */
    public boolean isEmoticonCollected(String emoticonId) {
        return this.emoticonApi.isEmoticonCollected( emoticonId );
    }

    /**
     * 获取用户的分组
     *
     * @param resultHandlerInterface 结果回调
     */
    protected void getUserList(ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.getUserList(resultHandlerInterface);
    }

    public ArrayList<UserList> getAllUserLists(){
        return UserListDAO.findAll();
    }

    //TODO:删除表情
    /**
     * 从指定分组批量删除表情
     *
     * @param emoticonIds 要删除表情的表情ID数组
     * @param userListId  指定的用户表情分组
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true
     */
    public boolean removeEmoticonsByIds(ArrayList<String> emoticonIds, String userListId,ResultHandlerInterface resultHandlerInterface) {
        return this.userListApi.removeEmoticonsByIds(emoticonIds, userListId, resultHandlerInterface);
    }

    /**
     * 从指定分组删除单张表情
     *
     * @param emoticonId 要删除的表情ID
     * @param userListId 指定的分组
     * @return 是否删除成功
     */
    public boolean removeEmoticonById(String emoticonId, String userListId , ResultHandlerInterface resultHandlerInterface) {
        return this.userListApi.removeEmoticonById(emoticonId, userListId , resultHandlerInterface);
    }

    /**
     * 新建分组
     *
     * @param listName               分组名
     * @param resultHandlerInterface 结果回调
     */
    public void createUserListByName(String listName, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.createUserListByName(listName, resultHandlerInterface);
    }

    /**
     * 重命名分组
     *
     * @param userListId             要重命名的列表id
     * @param name                   重命名的名字
     * @param resultHandlerInterface 结果回调
     */
    public void renameUserListById(String userListId, String name, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.renameUserListById( userListId , name , resultHandlerInterface );
    }

    /**
     * 删除分组
     *
     * @param userListId 分组id
     * @return 是否删除成功
     */
    public boolean removeUserListById(String userListId) {
        return this.userListApi.removeUserListById(userListId);
    }

    /**
     * 将表情从一个分组移动到另一个分组
     *
     * @param emoticonId             要移动的表情ID
     * @param fromId                 移出分组id
     * @param toId                   移入分组id
     * @param resultHandlerInterface 结果回调
     */
    public void moveEmoticonById(String emoticonId, String fromId, String toId, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.moveEmoticonById( emoticonId , fromId , toId , resultHandlerInterface);
    }
    //endregion

    /**
     * 重试函数
     *
     * @param retryHandler 重试结束的操作
     */
    private void retryReq(final ResultHandlerInterface retryHandler){
        final ArrayList<RetryReq> retryReqs = RetryReqDAO.findAll();
        for(final RetryReq retryReq:retryReqs){
            retryReq.setIsFinished(false); //开始重试
            ResultHandlerInterface handler = new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    retryHandler.onResponse(response);
                    retryReq.setIsFinished(true);
                    retryReq.delete();
                }

                @Override
                public void onError(Exception e) {
                    retryHandler.onError(e);
                    retryReq.setIsFinished(true);
                    LogX.e("重试删除出错");
                    //TODO:根据返回的错误类型进行判断
                    if(e.getMessage().equals("need_retry")){
                        return;
                    }
                    RetryReq req = new RetryReq(retryReq.getType(),retryReq.getListId(),retryReq.getEmoIds());
                    retryReq.delete();
                    req.save2DB();
                }
            };
            if(retryReq.getType()==RetryReq.REMOVE_EMO){ //删除表情
                this.userListApi.removeEmoticonsByIds(retryReq.getEmoIds(), retryReq.getListId(), handler);
            }else { //删除列表
                this.userListApi.retryRemoveList(retryReq.getListId(),handler);
            }
        }
    }


    /**
     * 初始化ImageLoader
     */
    public static void initImageLoader(Context context) {
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
        config.writeDebugLogs(); // Remove for release app
        config.memoryCache(new WeakMemoryCache());
        config.memoryCacheSize( 2*1024*1024 );

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
}
