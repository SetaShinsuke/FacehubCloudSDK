package com.azusasoft.facehubcloudsdk.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Handler;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.util.Log;

import com.azusasoft.facehubcloudsdk.api.db.DAOHelper;
import com.azusasoft.facehubcloudsdk.api.models.AuthorContainer;
import com.azusasoft.facehubcloudsdk.api.models.Banner;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.EmoticonContainer;
import com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException;
import com.azusasoft.facehubcloudsdk.api.models.ImageContainer;
import com.azusasoft.facehubcloudsdk.api.models.RetryReq;
import com.azusasoft.facehubcloudsdk.api.models.RetryReqDAO;
import com.azusasoft.facehubcloudsdk.api.models.SendRecord;
import com.azusasoft.facehubcloudsdk.api.models.SendRecordDAO;
import com.azusasoft.facehubcloudsdk.api.models.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonsRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.LoginEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ReorderEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import de.greenrobot.event.EventBus;
import im.fir.sdk.FIR;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LATER_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.dumpReq;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.addString2Params;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * Api
 */
public class FacehubApi {

    private String themeColorString = "#f33847";
    private String actionBarColorString;
    private String emoStoreTitle = "面馆表情";
    private boolean mixLayoutEnabled = false;
    private int viewStyle = Constants.VIEW_STYLE_DEFAULT;
    private static boolean isSingleUser = false;

    final static String HOST = "https://yun.facehub.me";  //外网
//        protected final static String HOST = "http://106.75.15.179:9292";  //测服
//        protected final static String HOST = "http://10.0.0.79:9292";  //内网

    private static FacehubApi api;
    public static String appId = null;
    private static User user;
    private AsyncHttpClient client;

    private UserListApi userListApi;
    private EmoticonApi emoticonApi;
    private static Context appContext;
    private static DAOHelper dbHelper;

    private static EmoticonContainer emoticonContainer = new EmoticonContainer();
    private static ImageContainer imageContainer = new ImageContainer();
    private static AuthorContainer authorContainer = new AuthorContainer();

    //region初始化

    /**
     * FacehubApi的初始化;
     */
    public static void init(Context context, String appId) {
        init(context, appId, false);
    }

    public static void init(Context context, String appId, boolean singleUser) {
        appContext = context;
        FIR.init(context);
        getApi().setAppId(appId);

        //初始化API(数据库)
        dbHelper = new DAOHelper(context);
        //initViews(context);
        boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        if (isDebuggable) {
            LogX.logLevel = Log.VERBOSE;
        } else {
            LogX.logLevel = Log.WARN;
//            LogX.logLevel = Log.VERBOSE;
        }
        LogX.init(context);

        //先恢复emoticons，在恢复列表
        CodeTimer codeTimer = new CodeTimer();
        codeTimer.start("表情 restore . ");
        emoticonContainer.restore();
        LogX.fastLog("表情Restore , Container size : " + emoticonContainer.getAllEmoticons().size());
        codeTimer.end("表情 restore . ");
        user.restoreLists();

        getApi().syncSendRecords();

        //恢复商店页数据(主要是搜索)
        StoreDataContainer.getDataContainer().restore(context);

        //使用单一用户，自动注册用户并登录
        if (singleUser && !user.isLogin()) {
            LogX.fastLog("使用唯一用户,自动注册登录.");
            try {
                getApi().initSingleUser();
            } catch (FacehubSDKException e) {
                LogX.e("注册唯一用户出错 : " + e);
            }
        }
        isSingleUser = singleUser;
    }

    private void initSingleUser() throws FacehubSDKException {
        String singleUserId = UtilMethods.getDeviceId(appContext)+ (int)(Math.random()*10000);
        registerUser( singleUserId , new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                LogX.fastLog("注册response : " + response);
            }

            @Override
            public void onError(Exception e) {
                LogX.e("注册唯一用户出错 : " + e);
            }
        });
//        getApi().registerUser("1a06168089802ae14bc245bccbda0c30"
//                , "4A4ZhtmNWPFsnv+DiDyXiZYcmVA=\n"
//                , 1784105243L, new ResultHandlerInterface() {
//
//                    @Override
//                    public void onResponse(Object response) {
//                        //
//                        HashMap<String, String> userData = (HashMap) response;
//                        String userId = userData.get("user_id");
//                        String authToken = userData.get("auth_token");
//                        getApi().login(userId, authToken, new ResultHandlerInterface() {
//                            @Override
//                            public void onResponse(Object response) {
//                                LogX.i("唯一用户登录成功!");
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                LogX.i("唯一用户登录出错 : " + e);
//                            }
//                        }, new ProgressInterface() {
//                            @Override
//                            public void onProgress(double process) {
//                                LogX.fastLog("唯一用户登录进度 : " + process + " %");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        LogX.e("注册唯一用户出错 : " + e);
//                    }
//                });
    }

    public boolean isSingleUser() {
        return isSingleUser;
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

    /**
     * 退出SDK的视图
     */
    public void exitViews() {
        ExitViewsEvent exitViewsEvent = new ExitViewsEvent();
        EventBus.getDefault().post(exitViewsEvent);
    }

    /**
     * 设置主题色;
     *
     * @param colorString 一个表示颜色RGB的字符串，例如<p>"#f33847"</p>;
     */
    public void setThemeColor(String colorString) {
        this.themeColorString = colorString;
    }

    /**
     * 设置主题色;
     *
     * @param colorString 一个表示颜色RGB的字符串，例如<p>"#f33847"</p>;
     */
    public void setActionBarColor(String colorString) {
        this.actionBarColorString = colorString;
    }

    /**
     * 设置商店页标题
     *
     * @param title 商店页标题
     */
    public void setEmoStoreTitle(String title) {
        this.emoStoreTitle = title;
    }

    /**
     * 获取商店页标题
     *
     * @return 商店页标题
     */
    public String getEmoStoreTitle() {
        return emoStoreTitle;
    }

    private FacehubApi() {
        this.client = new AsyncHttpClient();
        this.userListApi = new UserListApi(client);
        this.emoticonApi = new EmoticonApi(client);
        user = new User(appContext);
        user.restore();
    }

    /**
     * 返回一个API实例;
     *
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
    private void setAppId(String id) {
        appId = id;
    }

    /**
     * Log Level设置
     * 默认设置Log.VERBOSE(debug打包),Log.WARN(release打包)
     *
     * @param logLevel 设置Log等级;
     */
    public void setLogLevel(int logLevel) {
        LogX.logLevel = logLevel;
    }

    //endregion

    //region 账户设置

    /**
     * 设置当前有效的用户token
     *
     * @param token 数据请求令牌;
     */
    private void setUserToken(String token) {
        user.setToken(token);
    }

    /**
     * 设置当前用户
     *
     * @param userId                 用户唯一id;
     * @param token                  数据请求令牌;
     * @param resultHandlerInterface 结果回调.返回当前{@link User}对象;
     * @param progressInterface      进度回调;
     */
    public void login(final String userId, final String token, final ResultHandlerInterface resultHandlerInterface,
                      final ProgressInterface progressInterface) {

        if (isSingleUser() && user.isLogin()) {
            FacehubSDKException loginException = new FacehubSDKException("设置唯一用户时请勿手动调用登录函数!");
            loginException.setErrorType(FacehubSDKException.ErrorType.single_user_config);
            resultHandlerInterface.onError(loginException);
            return;
        }
        progressInterface.onProgress(0);
//        user = new User(appContext);
        user.clear();
//        //// 2016/5/10 有用???
//        if (user.restore() && user.getUserId().equals(userId)) { //用户恢复成功，且与当前登录用户的ID相同
//            LogX.i("用户恢复成功!");
//            resultHandlerInterface.onResponse( user );
//            return;
//        }
        get_user_info(user, userId, token, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if (user.isModified()) {
                    userListApi.getUserList(user, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            if (isUserChanged(userId)) {
                                LogX.w("登录成功，但用户发生了改变，忽略登录结果!" +
                                        "\nOld User : " + userId
                                        + " || New User : " + FacehubApi.getApi().getUser().getUserId());
                                return;
                            }
                            resultHandlerInterface.onResponse(response);
                            LoginEvent loginEvent = new LoginEvent();
                            EventBus.getDefault().post(loginEvent);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (e instanceof FacehubSDKException
                                    && ((FacehubSDKException) e).getErrorType() == FacehubSDKException.ErrorType.loginError_needRetry) {
                                user.setUserRetryInfo(userId, token);
                            }
                            resultHandlerInterface.onError(e);
                            LogX.e("登录get_user_info -> getUserList出错 : " + e);
                        }
                    }, progressInterface);
                } else {
                    if (isUserChanged(userId)) {
                        LogX.w("登录成功，但用户发生了改变，忽略登录结果!" +
                                "\nOld User : " + userId
                                + " || New User : " + FacehubApi.getApi().getUser().getUserId());
                        return;
                    }
                    progressInterface.onProgress(99.9);
                    resultHandlerInterface.onResponse(user);
                }
            }

            @Override
            public void onError(Exception e) {
                if (isUserChanged(userId)) {
                    LogX.w("登录成功，但用户发生了改变，忽略登录结果!" +
                            "\nOld User : " + userId
                            + " || New User : " + FacehubApi.getApi().getUser().getUserId());
                    return;
                }
                user.clear();
                //判断是否是应该重试登录的错误
                if (e instanceof FacehubSDKException
                        && ((FacehubSDKException) e).getErrorType() == FacehubSDKException.ErrorType.loginError_needRetry) {
                    user.setUserRetryInfo(userId, token);
                }
                resultHandlerInterface.onError(e);
                LogX.e("登录get_user_info出错 : " + e);
            }

            private boolean isUserChanged(String oldUserId) {
                return oldUserId == null || !oldUserId.equals(FacehubApi.getApi().getUser().getUserId());
            }
        });
    }

    /**
     * 根据User里存储的retry_info进行重试
     *
     * @param resultHandlerInterface 重试回调
     */
    public void retryLogin(final ResultHandlerInterface resultHandlerInterface) {
        String userId = user.getRetryId();
        String token = user.getRetryToken();
        if (user.isLogin() || userId == null || token == null) {
            //已登录、retry_info为空
            LogX.i("重试登录停止:无需重试.");
            return;
        }
        LogX.i("需要重试登录:开始重试…\nUserId : " + userId + " || token : " + token);
        try {
            login(userId, token, resultHandlerInterface, new ProgressInterface() {
                @Override
                public void onProgress(double process) {
                    LogX.d("登录重试中 : " + process + " %");
                }
            });
        } catch (Exception e) {
            LogX.e("重试登录出错 : " + e);
        }
    }

    /**
     * 用来获取上次用户账户修改的时间戳;
     *
     * @param user                   要检查的用户;
     * @param userId                 用户id;
     * @param token                  用户token;
     * @param resultHandlerInterface 结果回调，返回一个{@link User}对象;
     */
    public void get_user_info(final User user, final String userId, final String token, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + userId;
        RequestParams params = new RequestParams();
        params.put("user_id", userId);
        params.put("auth_token", token);
        params.put("app_id", FacehubApi.appId);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    String updated_at = response.getJSONObject("user").getString("updated_at");
                    user.setUserInfo(userId, token, updated_at);
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
                if (statusCode < 400 || statusCode > 500) {
                    FacehubSDKException exception
                            = new FacehubSDKException("GetUserInfo出错，需要重试 : " + parseHttpError(statusCode, throwable, addition));
                    exception.setErrorType(FacehubSDKException.ErrorType.loginError_needRetry);
                    resultHandlerInterface.onError(exception);
                } else {
                    resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
                }
            }
        });

    }

    /**
     * 拉取单个列表
     *
     * @param listId                 要拉取的列表id
     * @param resultHandlerInterface 回调，返回一个{@link UserList};
     */
    public void getUserListDetailById(String listId, ResultHandlerInterface resultHandlerInterface) {
        this.userListApi.getUserListDetailById(user, listId, resultHandlerInterface);
    }

    /**
     * 返回当前用户;
     *
     * @return {@link User};
     */
    public User getUser() {
        return user;
    }

    /**
     * 退出登录
     *
     * @throws FacehubSDKException 设置使用唯一用户时，退出登录则抛出异常;
     */
    public void logout() throws FacehubSDKException {
        if (isSingleUser() && user.isLogin()) {
            FacehubSDKException loginException = new FacehubSDKException("使用唯一用户时请勿调用退出!");
            loginException.setErrorType(FacehubSDKException.ErrorType.single_user_config);
            throw loginException;
        }
        // UserListDAO.deleteAll();
        RetryReqDAO.deleteAll();
        user.logout();
    }

    public void registerUser(String bindingId, final ResultHandlerInterface resultHandlerInterface) throws FacehubSDKException{
        if(isSingleUser && user.isLogin()){
            FacehubSDKException loginException = new FacehubSDKException("使用唯一用户时请勿调用调用注册!");
            loginException.setErrorType(FacehubSDKException.ErrorType.single_user_config);
            throw loginException;
        }
        String url = HOST + "/api/v1/users/binding";
        RequestParams params = new RequestParams();
        params.put("app_id",appId);
        params.put("platform","android");
        params.put("binding",bindingId);
        params.put("app_package_id",appContext.getPackageName());
        params.put("app_package_sign", UtilMethods.getSignatureString(appContext));
        params.put("auto_create",true);
        dumpReq(url,params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject userJson = response.getJSONObject("user");
                    String userId = userJson.getString("id");
                    String authToken = userJson.getString("auth_token");
                    String updatedAt = userJson.getString("updated_at");
                    user.setUserInfo(userId,authToken,updatedAt);

                    final ArrayList<UserList> userLists = new ArrayList<>();
                    JSONArray listsJsonArray = userJson.getJSONArray("contents");
                    for (int i = 0; i < listsJsonArray.length(); i++) {
                        JSONObject jsonObject = listsJsonArray.getJSONObject(i);
                        UserList userList = FacehubApi.getApi().getUser()
                                .getUserListById(jsonObject.getString("id"));
                        userList.updateField(jsonObject, LATER_SAVE);
                        userLists.add(userList);
                    }
                    RetryReqDAO.deleteAll();
                    user.setUserLists(userLists);
                    resultHandlerInterface.onResponse(user);
                    LoginEvent loginEvent = new LoginEvent();
                    EventBus.getDefault().post(loginEvent);
                }catch (Exception e){
                    FacehubSDKException exception = new FacehubSDKException("注册唯一用户Json解析出错 : " + e);
                    resultHandlerInterface.onError(exception);
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
                if(statusCode<400 || statusCode>500){
                    FacehubSDKException exception
                            = new FacehubSDKException("注册唯一用户出错 : " + parseHttpError(statusCode, throwable, addition));
                    exception.setErrorType(FacehubSDKException.ErrorType.singleRegisterError_needRetry);
                    resultHandlerInterface.onError(exception);
                }else {
                    resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
                }
            }
        });
    }

//    /**
//     * 注册新账户(仅供示例Demo使用)
//     *
//     * @param accessKey              accessKey;
//     * @param sign                   sign;
//     * @param deadLine               deadLine;
//     * @param resultHandlerInterface 结果回调，返回一个包括新用户id和token的{@link HashMap};
//     */
//    public void registerUser(String accessKey,
//                             String sign,
//                             long deadLine,
//                             final ResultHandlerInterface resultHandlerInterface) {
//        //禁用
//        if (isSingleUser() && user.isLogin()) {
//            FacehubSDKException loginException = new FacehubSDKException("使用唯一用户时请勿调用退出!");
//            loginException.setErrorType(FacehubSDKException.ErrorType.single_user_config);
//            resultHandlerInterface.onError(loginException);
//            return;
//        }
//
//        String url = HOST + "/api/v1/users/";
//        JSONObject params = new JSONObject();
//        try {
//            params.put("app_id", FacehubApi.appId);
//            params.put("access_key", accessKey);
//            params.put("sign", sign);
//            params.put("deadline", deadLine);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            resultHandlerInterface.onError(e);
//        }
//        ByteArrayEntity entity = null;
//        try {
//            entity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        client.post(null, url, entity, "application/json", new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                try {
//                    HashMap<String, String> userData = new HashMap<>();
//                    userData.put("user_id", response.getJSONObject("user").getString("id"));
//                    userData.put("auth_token", response.getJSONObject("user").getString("auth_token"));
//                    resultHandlerInterface.onResponse(userData);
//                } catch (JSONException e) {
//                    resultHandlerInterface.onError(e);
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
//                onFail(statusCode, throwable, responseString);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                onFail(statusCode, throwable, errorResponse);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                onFail(statusCode, throwable, errorResponse);
//            }
//
//            //打印错误信息
//            private void onFail(int statusCode, Throwable throwable, Object addition) {
//                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
//            }
//        });
//    }

    public void retryRegister() {

    }
    //endregion

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
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
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
     * @param paramStr               自定义参数，<p> eg: tag_type=section </p>;
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
     * 从服务器获取表情包列表
     *
     * @param tag                    目标分区名
     * @param page                   分页数，该分页第几页  >=0
     * @param limit                  limit:当前分页package最大回传数 >=1
     * @param resultHandlerInterface completionHandler 结果回调,返回一个由{@link EmoPackage}组成的{@link ArrayList}, 包含了所需要的表情包;
     */
    public void getPackagesByTags(String tag, int page, int limit, final ResultHandlerInterface resultHandlerInterface) {
        ArrayList<String> tags = new ArrayList<>();
        tags.add(tag);
        this.getPackagesByTags(tags, page, limit, resultHandlerInterface);
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
                if (statusCode == 403) {
                    FacehubSDKException facehubSDKException = new FacehubSDKException(parseHttpError(statusCode, throwable, addition));
                    facehubSDKException.setErrorType(FacehubSDKException.ErrorType.emo_package_unavailable);
                    resultHandlerInterface.onError(facehubSDKException);
                    return;
                }
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
    public void collectEmoById(String emoticonId, String toUserListId, ResultHandlerInterface resultHandlerInterface) {
        ArrayList<String> emoIds = new ArrayList<>();
        emoIds.add(emoticonId);
        this.collectEmoById(emoIds, toUserListId, resultHandlerInterface);
    }

    /**
     * 收藏表情到指定分组
     *
     * @param emoticonIds            表情唯一标识
     * @param toUserListId           用户分组标识
     * @param resultHandlerInterface 结果回调,返回一个{@link UserList}对象;
     */
    public void collectEmoById(final ArrayList<String> emoticonIds, final String toUserListId, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.collectEmoById(user, emoticonIds, toUserListId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.collectEmoById(user, emoticonIds, toUserListId, resultHandlerInterface);
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
                userListApi.collectEmoPackageById(user, packageId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.collectEmoPackageById(user, packageId, resultHandlerInterface);
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
                userListApi.collectEmoPackageById(user, packageId, toUserListId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.collectEmoPackageById(user, packageId, toUserListId, resultHandlerInterface);
            }
        });
    }
    //endregion

    //region搜索
    public ArrayList<String> getHotTags(final ResultHandlerInterface resultHandlerInterface) {
        //TODO:服务器拉取热门标签
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resultHandlerInterface.onResponse(StoreDataContainer.getDataContainer().getHotTags());
            }
        }, 3000);
        return StoreDataContainer.getDataContainer().getHotTags();
    }

    public ArrayList<String> getSearchHistories() {
        //TODO:服务器拉取热门标签
        return StoreDataContainer.getDataContainer().getSearchHistories();
    }

    public void clearSearchHistory() {
        StoreDataContainer.getDataContainer().clearSearchHistory(appContext);
    }
    //endregion

    //region 表情资源请求

    /**
     * 通过表情唯一标识向服务器请求表情资源;
     *
     * @param emoticonId             表情包唯一标识;
     * @param resultHandlerInterface 结果回调,返回一个 {@link Emoticon} 对象;
     */
    public void getEmoticonById(final String emoticonId, final ResultHandlerInterface resultHandlerInterface) {
        Emoticon emoticon = emoticonContainer.getUniqueEmoticonById(emoticonId);
        if (emoticon.getThumbPath() == null || emoticon.getFullPath() == null) {
            this.emoticonApi.getEmoticonById(user, emoticonId, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    Emoticon emoticon1 = (Emoticon) response;
                    emoticon1.download2File(true, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            Emoticon resultEmo = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoticonId);
                            resultHandlerInterface.onResponse(resultEmo);
                        }

                        @Override
                        public void onError(Exception e) {
                            resultHandlerInterface.onError(e);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    resultHandlerInterface.onError(new Exception("拉取单个表情出错 : " + e));
                }
            });
        } else {
            resultHandlerInterface.onResponse(emoticon);
        }
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

    public Emoticon findEmoticonByDescription(String description) {
        if (description == null) {
            return null;
        }
        CodeTimer codeTimer = new CodeTimer();
        codeTimer.start("查找表情");
        Emoticon emoticon = user.findEmoticonByDescription(description);
        codeTimer.end("查找表情 result : " + emoticon);
        return emoticon;
    }

//    /**
//     * 获取数据库所有用户列表
//     *
//     * @return 由 {@link UserList} 组成的 {@link ArrayList} ;
//     */
//    public ArrayList<UserList> getAllUserLists() {
//        return UserListDAO.findAll();
//    }

    /**
     * 从指定分组批量删除表情;
     *
     * @param emoticonIds 要删除表情的表情ID数组;
     * @param userListId  指定的用户表情分组;
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true;
     */
    public boolean removeEmoticonsByIds(final ArrayList<String> emoticonIds, final String userListId) {
        ResultHandlerInterface emptyCallback = new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
            }

            @Override
            public void onError(Exception e) {
            }
        };
        retryRequests(emptyCallback);

        boolean flag = userListApi.removeEmoticonsByIds(user, emoticonIds, userListId, emptyCallback);
        EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
        EventBus.getDefault().post(event);
        return flag;
//        retryRequests(new ResultHandlerInterface() {
//            @Override
//            public void onResponse(Object response) {
//                flag[0] = userListApi.removeEmoticonsByIds(user, emoticonIds, userListId, emptyCallback);
//                EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
//                EventBus.getDefault().post(event);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                flag[0] = userListApi.removeEmoticonsByIds(user, emoticonIds, userListId, emptyCallback);
//                EmoticonsRemoveEvent event = new EmoticonsRemoveEvent();
//                EventBus.getDefault().post(event);
//            }
//        });
//        return flag[0];
    }

    /**
     * 从指定分组删除单张表情;
     *
     * @param emoticonId 要删除的表情ID;
     * @param userListId 指定的分组;
     * @return 是否删除成功;
     */
    public boolean removeEmoticonById(final String emoticonId, final String userListId) {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        return this.removeEmoticonsByIds(ids, userListId);
    }

    /**
     * 替换指定分组的表情;
     *
     * @param emoticonIds            要替换的表情ID数组;
     * @param userListId             指定的用户表情分组;
     * @param resultHandlerInterface 结果回调,返回 {@link UserList} ;
     * @return 是否删除成功，若一部分成功，一部分不成功依然会返回true;
     */
    public boolean replaceEmoticonsByIds(final User user, final ArrayList<String> emoticonIds, final String userListId, final ResultHandlerInterface resultHandlerInterface) {
        return this.userListApi.replaceEmoticonsByIds(user, emoticonIds, userListId, resultHandlerInterface);
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
                userListApi.createUserListByName(user, listName, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.createUserListByName(user, listName, resultHandlerInterface);
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
                userListApi.renameUserListById(user, userListId, name, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.renameUserListById(user, userListId, name, resultHandlerInterface);
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
        ResultHandlerInterface emptyCallback = new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
            }

            @Override
            public void onError(Exception e) {
            }
        };
        retryRequests(emptyCallback);
        boolean flag = userListApi.removeUserListById(user, userListId);
        UserListRemoveEvent event = new UserListRemoveEvent();
        EventBus.getDefault().post(event);
        return flag;
//        retryRequests(new ResultHandlerInterface() {
//            @Override
//            public void onResponse(Object response) {
//                flag[0] = userListApi.removeUserListById(user, userListId);
//                UserListRemoveEvent event = new UserListRemoveEvent();
//                EventBus.getDefault().post(event);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                flag[0] = userListApi.removeUserListById(user, userListId);
//                UserListRemoveEvent event = new UserListRemoveEvent();
//                EventBus.getDefault().post(event);
//            }
//        });
//        return flag[0];
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
        ArrayList<String> ids = new ArrayList<>();
        ids.add(emoticonId);
        this.moveEmoticonById(ids, fromId, toId, resultHandlerInterface);
    }

    /**
     * 将表情从一个分组移动到另一个分组
     *
     * @param emoticonIds            要移动的表情ID;
     * @param fromId                 移出分组id;
     * @param toId                   移入分组id;
     * @param resultHandlerInterface 结果回调,返回一个{@link UserList}对象,为收藏到的列表;
     */
    public void moveEmoticonById(final ArrayList<String> emoticonIds, final String fromId, final String toId, final ResultHandlerInterface resultHandlerInterface) {
        retryRequests(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                userListApi.moveEmoticonById(user, emoticonIds, fromId, toId, resultHandlerInterface);
            }

            @Override
            public void onError(Exception e) {
                userListApi.moveEmoticonById(user, emoticonIds, fromId, toId, resultHandlerInterface);
            }
        });
    }


    private int reorderTimes = 0;

    /**
     * 列表排序
     *
     * @param ids                    列表id的数组;
     * @param resultHandlerInterface 结果回调，返回一个{@link User}对象.
     */
    public void reorderUserLists(ArrayList<String> ids, final ResultHandlerInterface resultHandlerInterface) {
        String url = HOST + "/api/v1/users/" + user.getUserId()
                + "/lists";
        JSONObject jsonObject = user.getParamsJson();
        JSONArray jsonArray = new JSONArray(ids);
        try {
            jsonObject.put("contents", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        reorderTimes++;
        client.put(null, url, entity, "application/json", new JsonHttpResponseHandler() {
            //        client.put(url,params,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                reorderTimes--;
                resultHandlerInterface.onResponse(user);
                ReorderEvent event = new ReorderEvent();
                EventBus.getDefault().post(event);
//                try {
//                    JSONObject jsonObject = response.getJSONObject("list");
//                    UserList userList = FacehubApi.getApi().getUser()
//                            .getUserListById(jsonObject.getString("id"));
//                    userList.updateField(jsonObject, DO_SAVE);
//                    resultHandlerInterface.onResponse(userList);
//                } catch (JSONException e) {
//                    resultHandlerInterface.onError(e);
//                }
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
                reorderTimes--;
                if (reorderTimes > 0 || statusCode == 400) { //还有下一次排序要执行,忽略此次错误
                    resultHandlerInterface.onResponse(user);
                    ReorderEvent event = new ReorderEvent();
                    EventBus.getDefault().post(event);
                    return;
                }
                ReorderEvent event = new ReorderEvent();
                EventBus.getDefault().post(event);
                resultHandlerInterface.onError(parseHttpError(statusCode, throwable, addition));
            }
        });
    }

    //endregion

    //region重试
    //todo:重试结束之后的操作无需放在回调里！
    //// FIXME: 2016/4/6 ：修改回调逻辑，只有getAll时需要根据回调成功失败来操作
    int total = 0;
    int success = 0;
    int fail = 0;

    /**
     * 重试函数
     * 执行之前请求失败的操作
     *
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
                this.userListApi.retryRemoveEmoticonsByIds(user, emoIds, listId, new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        success++;
                        if (total == success + fail && total == success) {
                            retryHandler.onResponse(response); //全部重试成功
                            LogX.d("重试请求成功 " + success + " || total : " + total);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        fail++;
                        if (total == success + fail) {
                            retryHandler.onError(e); //有某个重试失败
                            LogX.d("重试请求失败 " + fail + " || total : " + total);
                        }
                    }
                });
            } else { //重试删除列表
                this.userListApi.retryRemoveList(user, retryReq.getListId(), new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        success++;
                        if (total == success + fail && total == success) {
                            retryHandler.onResponse(response); //全部重试成功
                            LogX.d("重试请求失败 " + success + " || total : " + total);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        success++;
                        if (total == success + fail && total == success) {
                            retryHandler.onError(e); //有某个重试失败
                            LogX.d("重试请求失败 " + fail + " || total : " + total);
                        }
                    }
                });
            }
            retryReq.delete();
        }
    }
    //endregion

    public int getViewStyle() {
        return viewStyle;
    }

    public void setViewStyle(int viewStyle) {
        this.viewStyle = viewStyle;
    }

    /**
     * 从文件读取默认表情配置
     *
     * @param version              版本号
     * @param configJsonAssetsPath 配置文件，在assets文件夹内的具体路径
     * @param mixLayoutEnabled     是否允许图文混排;
     * @throws LocalEmoPackageParseException 配置JSON解析出错时抛出异常
     */
    public void loadEmoticonFromLocal(int version, @NonNull String configJsonAssetsPath, boolean mixLayoutEnabled) throws LocalEmoPackageParseException {
        this.mixLayoutEnabled = mixLayoutEnabled;
        try {
            CodeTimer codeTimer = new CodeTimer();
            codeTimer.start("开始解析JSON");
            user.restoreLocalEmoticons(appContext, version, configJsonAssetsPath);
            codeTimer.end("解析JSON完成");
        } catch (Exception e) {
            throw new LocalEmoPackageParseException("解析本地表情配置出错" + e);
        }
    }

    public boolean isMixLayoutEnabled() {
        return mixLayoutEnabled;
    }
    //endregion

    //region其他Getter等
    public int getThemeColor() {
        return Color.parseColor(themeColorString);
    }

    public int getActionbarColor() {
        if (actionBarColorString != null) {
            return Color.parseColor(actionBarColorString);
        } else {
            return getThemeColor();
        }
    }

    public int getThemeColorDark() {
        int color = getThemeColor();
        float factor = 0.8f;
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public int getActionbarColorDark() {
        int color = getActionbarColor();
        float factor = 0.8f;
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }


    public EmoticonContainer getEmoticonContainer() {
        return emoticonContainer;
    }

    public ImageContainer getImageContainer() {
        return imageContainer;
    }

    public AuthorContainer getAuthorContainer() {
        return authorContainer;
    }

    //endregion

    private void syncSendRecords() {
        final SharedPreferences sharedPreferences = appContext.getSharedPreferences(Constants.SEND_RECORD, Context.MODE_PRIVATE);
        Long lastSyncTime = sharedPreferences.getLong(Constants.SEND_RECORD_UPDATED_AT, 0);
        if (!user.isLogin()
                || System.currentTimeMillis() - lastSyncTime < 24 * 1000 * 60 * 60) { //上次同步未超过24小时
            LogX.v("上次同步未超过24小时，跳过同步.");
            return;
        }
        LogX.d("上次同步超过24小时，再次同步.");
        ArrayList<SendRecord> sendRecords = SendRecordDAO.findAll();
        if (sendRecords.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(Constants.SEND_RECORD_UPDATED_AT, System.currentTimeMillis());
            editor.apply();
            return;
        }

        String url = HOST + "/api/v1/emoticons/usage";
        JSONObject jsonObject = user.getParamsJson();
        JSONArray jsonArray = new JSONArray();
        for (SendRecord record : sendRecords) {
            jsonArray.put(record.date + "," + record.emoId + "," + record.userId + "," + record.count);
        }
        try {
            jsonObject.put("usage", jsonArray);
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
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                LogX.d("同步发送记录成功!");
                //TODO:发送 记录到服务器
                SendRecordDAO.deleteAll();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(Constants.SEND_RECORD_UPDATED_AT, System.currentTimeMillis());
                editor.apply();
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
                LogX.e("同步发送记录出错 : " + parseHttpError(statusCode, throwable, addition));
            }
        });
    }
}
