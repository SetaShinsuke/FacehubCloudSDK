package com.azusasoft.facehubcloudsdk.api;

import android.content.Context;

import com.azusasoft.facehubcloudsdk.api.models.EmoCache;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.EmoticonContainer;
import com.azusasoft.facehubcloudsdk.api.models.EmoticonDAO;
import com.azusasoft.facehubcloudsdk.api.models.List;
import com.azusasoft.facehubcloudsdk.api.models.User;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.CacheClearEvent;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.DownloadService;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.models.MockClient;
import com.azusasoft.facehubcloudsdk.api.utils.threadUtils.ThreadPoolManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.HOST;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.parseHttpError;

/**
 * Created by SETA on 2016/3/8.
 * Emoticon操作接口
 */
public class EmoticonApi {
//    private AsyncHttpClient client;
    private MockClient client;

     EmoticonApi(MockClient client) {
        this.client = client;
    }

    /**
     * 通过表情唯一标识向服务器请求表情资源;
     *
     * @param emoticonId             表情包唯一标识;
     * @param resultHandlerInterface 结果回调,返回一个 {@link Emoticon} 对象;
     */
    void getEmoticonById(User user , final String emoticonId, final ResultHandlerInterface resultHandlerInterface) {
        RequestParams params = user.getParams();
        String url = HOST + "/api/v1/emoticons/" + emoticonId;
        LogX.dumpReq(url, params);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("emoticon");
                    Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoticonId);
                    emoticon.updateField(jsonObject);
                    resultHandlerInterface.onResponse(emoticon);
                } catch (Exception e) {
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
     * 检查本地是否已收藏该表情;
     *
     * @param emoticonId 表情唯一标识;
     * @return 是否已收藏;
     */
    boolean isEmoticonCollected(String emoticonId) {
        //TODO:检查本地是否已收藏:比较耗时?
        CodeTimer codeTimer = new CodeTimer();
        codeTimer.start("检查是否收藏 findAll");
        ArrayList<UserList> allLists = FacehubApi.getApi().getUser().getUserLists();
        codeTimer.end("检查是否收藏 findAll");
//        for(UserList userList:allLists){ //所有个人列表
//            ArrayList<Emoticon> emoticons = userList.getEmoticons();
//            for(Emoticon emoticon:emoticons){ //列表内所有表情
//                if(emoticon.getId().equals(emoticonId)){
//                    return true;
//                }
//            }
//        }


        codeTimer.start("遍历emoticons");
        if (allLists.size() > 0) {
            ArrayList<Emoticon> emoticons = allLists.get(0).getEmoticons();
            for (int i = 0; i < emoticons.size(); i++) { //列表内所有表情
                if (emoticons.get(i).getId().equals(emoticonId)) {
                    return true;
                }
            }
        }
        codeTimer.end("遍历emoticons");
        return false;
    }

    void getCache(User user, EmoticonContainer emoticonContainer, ResultHandlerInterface resultHandlerInterface){
        EmoCache emoCache = new EmoCache();
        emoCache.setSize(10*1000*1000);
        emoCache.setFileCount(500);
        resultHandlerInterface.onResponse(new EmoCache());
    }

    void clearCache(User user , EmoticonContainer emoticonContainer, final ResultHandlerInterface resultHandlerInterface, final ProgressInterface progressInterface){
        //保留列表内表情、列表封面
        final ArrayList<Emoticon> emoticonsOfUser = new ArrayList<>();
        ArrayList<UserList> allUserLists = new ArrayList<>(user.getUserLists());
        allUserLists.addAll(user.getLocalLists());
        for( UserList userList:allUserLists){
            emoticonsOfUser.addAll(userList.getEmoticons());
            emoticonsOfUser.add(userList.getCover());
        }
        emoticonContainer.updateAll(emoticonsOfUser);
        ThreadPoolManager.getDbThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //所有文件
                File cacheFolder = DownloadService.getCacheDir();
                File fileFolder = DownloadService.getFileDir();
                File[] allCacheFiles = cacheFolder.listFiles();
                File[] allFileFiles = fileFolder.listFiles();
                ArrayList<File> allFiles = new ArrayList<>();
                Collections.addAll(allFiles,allCacheFiles);
                Collections.addAll(allFiles,allFileFiles);
                //所有表情的目录
                ArrayList<String> emoPaths = new ArrayList<String>();
                for(Emoticon emoticon:emoticonsOfUser){
                    String thumbPath = emoticon.getThumbPath();
                    String fullPath = emoticon.getFullPath();
                    if(thumbPath!=null){
                        emoPaths.add(thumbPath);
                    }
                    if(fullPath!=null){
                        emoPaths.add(fullPath);
                    }
                }

                //嵌套循环查找需要删除的文件
                ArrayList<File> files2Delete = new ArrayList<>();
                for(File file:allFiles){
                    if(file==null){
                        continue;
                    }
                    boolean need2Delete = true;
                    for(String path:emoPaths){
                        if(file.getAbsolutePath().equals(path)){ //是列表内的表情，不需要删除
                            need2Delete = false;
                            break;
                        }
                    }
                    if(need2Delete){
                        files2Delete.add(file);
                    }
                }

                //实施删除操作
                int needDeleteSize = files2Delete.size();
                LogX.fastLog("需要删除的缓存文件个数 : " + needDeleteSize);
                for(int i=0;i<needDeleteSize;i++){
                    File file = files2Delete.get(i);
                    boolean deleteSuccess = file.delete();
                    CacheClearEvent cacheClearEvent = new CacheClearEvent();
                    EventBus.getDefault().post(cacheClearEvent);
                    progressInterface.onProgress(i*1f/needDeleteSize*100);
                    if(i==needDeleteSize-1){
                        resultHandlerInterface.onResponse("Clear cache done.Emoticon count : " + emoticonsOfUser.size());
                    }
                }

            }
        });
    }
}
