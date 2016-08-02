package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListPrepareEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.models.Image.Size.MEDIUM;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 * 用户列表
 */
public class UserList extends List{
    private Long dbId;
    private String forkFromId;
    private String userId;
    private boolean downloading = false;
    private float percent = 0f;
    private boolean local = false;
    private boolean isEmojiList = false;

    protected UserList(){

    }

    // "contents"和"contents_details" 不可为空
    public UserList updateField(JSONObject jsonObject, boolean doSave) throws JSONException{
        super.updateField(jsonObject);
        //emoticons
        if(isJsonWithKey(jsonObject,"fork_from")){
            String forkFromId = jsonObject.getString("fork_from");
            setForkFromId(forkFromId);
        }

//        if( isJsonWithKey(jsonObject,"contents") ) {
//            ArrayList<Emoticon> emoticonsTmp = new ArrayList<>();
//            JSONArray jsonArray = jsonObject.getJSONArray("contents");
//            for(int i=0;i<jsonArray.length();i++){
//                String emoId = jsonArray.getString(i);
//                Emoticon emoticon = FacehubApi.getApi().getEmoticonContainer().getUniqueEmoticonById(emoId);
//                emoticonsTmp.add(emoticon);
//            }
//            setEmoticons(emoticonsTmp);
//        }
//
//        if( isJsonWithKey(jsonObject,"contents_details") ) { //有"contents_details"字段
//            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
//            for (Emoticon emoticon:getEmoticons()){
//                emoticon.updateField(emoDetailsJson.getJSONObject(emoticon.getId()));
//            }
//        }
        EmoticonDAO.saveInTx(getEmoticons());

//        ArrayList<Emoticon> emos2Set  = new ArrayList<>(); //要设置的emoticons
//
//        if( isJsonWithKey(jsonObject,"contents_details") ){ //有"contents_details"字段
////            UserListDAO.deleteAll();
////            UserListDAO.delete(getId());
////            fastLog("有contents_details");
//            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
//            for (Emoticon emoticon:emoticonsTmp){
//                emoticon.updateField(emoDetailsJson.getJSONObject(emoticon.getId()));
//                emos2Set.add(emoticon);
//            }
//            EmoticonDAO.saveEmoInTx(emos2Set);
//
//        }else { //没有"content_details"字段
////            fastLog("没有contents_details");
//            ArrayList<Emoticon> emosNew  = new ArrayList<>(); //要新建的emoticons
//            for (Emoticon emoticon : emoticonsTmp){
////                Image imageInDB = ImageDAO.findEmoticonById(emoticon.getId(), LATER_SAVE);
////                Emoticon emoticonInDB = (Emoticon)imageInDB;
//                Emoticon emoticonInDB = EmoticonDAO.findEmoticonById(emoticon.getId(),LATER_SAVE);
//                if( emoticonInDB==null ){ //数据库中没有
//                    emosNew.add(emoticon);
//                    emos2Set.add(emoticon);
//                }else { //数据库中已经有
//                    emos2Set.add(emoticonInDB);
//                }
//            }
//            EmoticonDAO.saveEmoInTx(emosNew);
//        }
//        setEmoticons(emos2Set);

        if(doSave){
            save2DB();
        }
        return this;
    }
    public int size(){
        return getEmoticons().size();
    }
    public void removeEmoticons(ArrayList<String> emoticonIds){
        ArrayList<Emoticon> toMove = new ArrayList<>();
        for(int i = 0;i<getEmoticons().size();i++){
            Emoticon emoticon = getEmoticons().get(i);
            if(emoticonIds.contains(emoticon.getId())){
//                getEmoticons().remove(emoticon);
                toMove.add(emoticon);
            }
        }

        for(int i=0;i<toMove.size();i++){
            getEmoticons().remove(toMove.get(i));
        }
        UserListDAO.deleteEmoticons(getId(), emoticonIds);
    }

    private boolean save2DB(){
        return UserListDAO.save2DBWithClose(this);
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    protected void setId(String id) {
        super.setId(id);
    }

    @Override
    public String getName() {
        return super.getName();
    }


    //TODO:protected
    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public Emoticon getCover() {
        return super.getCover();
    }

    @Override
    protected void setCover(Emoticon cover) {
        super.setCover(cover);
    }

    @Override
    public ArrayList<Emoticon> getEmoticons() {
        return super.getEmoticons();
    }

    @Override
    public void setEmoticons(ArrayList<Emoticon> emoticons) {
        super.setEmoticons(emoticons);
    }

    public Long getDbId() {
        return dbId;
    }

    protected void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getForkFromId() {
        return forkFromId;
    }

    public void setForkFromId(String forkFromId) {
        this.forkFromId = forkFromId;
    }

    @Override
    public void downloadCover( final ResultHandlerInterface resultHandlerInterface) {
        if(getCover()!=null && getCover().getFileUrl(MEDIUM)==null){
            FacehubApi.getApi().getUserListDetailById(getId(), new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    downloadCover(resultHandlerInterface);
                }

                @Override
                public void onError(Exception e) {
                    resultHandlerInterface.onError(new Exception("获取封面url出错 : " + e));
                }
            });
        }else {
            final Emoticon cover = getCover();
            if (cover != null ){
                if(cover.getThumbPath() == null) {
                    cover.downloadThumb2File(true,resultHandlerInterface);
                }else {
                    resultHandlerInterface.onResponse(cover);
                }
            }else {
                resultHandlerInterface.onResponse(new Exception("封面下载出错 : cover为空"));
            }
        }
    }

    // 移动表情
    public void changeEmoticonPosition(int from , int to) {
        if (from == to)
            return;
        Emoticon emo = getEmoticons().get(from);
        getEmoticons().remove(from);
        if (to >= getEmoticons().size()) {
            getEmoticons().add(emo);
        } else {
            getEmoticons().add(to, emo);
        }
    }

    /**
     * 是否下载中
     * @return 列表是否下载中
     */
    public boolean isDownloading(){
        return downloading;
    }

    /**
     * 下载进度
     * @return 下载进度百分比
     */
    public float getPercent(){
        return percent;
    }

    /**
     * 下载整个列表，包括封面;
     *
     * @param resultHandlerInterface 返回当前{@link UserList} ;
     * @param progressInterface 进度回调，返回小于100的进度;
     */
    public void download(final ResultHandlerInterface resultHandlerInterface, final ProgressInterface progressInterface){
        downloading = true;
        percent = 0;
        ArrayList<Emoticon> all=  new ArrayList<>(getEmoticons());
        if(getCover() != null) {
            all.add(getCover());
        }
        downloadEach(all, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                downloading = false;
                UserListPrepareEvent event = new UserListPrepareEvent(getId());
                EventBus.getDefault().post(event);
                resultHandlerInterface.onResponse(response);
            }

            @Override
            public void onError(Exception e) {
                downloading = false;
                UserListPrepareEvent event = new UserListPrepareEvent(getId());
                EventBus.getDefault().post(event);
                resultHandlerInterface.onError(e);
            }
        }, new ProgressInterface() {
            @Override
            public void onProgress(double process) {
                percent = (float) process;
                DownloadProgressEvent downloadProgressEvent = new DownloadProgressEvent(getId());
                downloadProgressEvent.percentage = (float) process;
                EventBus.getDefault().post(downloadProgressEvent);
                progressInterface.onProgress(process);
            }
        });
    }
//
    public void prepare(final ResultHandlerInterface resultHandlerInterface, final ProgressInterface progressInterface){
        downloading = true;
        FacehubApi.getApi().getUserListDetailById(getId(), new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                download(resultHandlerInterface,progressInterface);
            }

            @Override
            public void onError(Exception e) {
                downloading = false;
                UserListPrepareEvent event = new UserListPrepareEvent(getId());
                EventBus.getDefault().post(event);
                resultHandlerInterface.onError(e);
            }
        });
    }

    /**
     * 判断列表是否已全部下载完成
     * @return 表情全部已下载到本地
     */
    public boolean isPrepared(){
        for(Emoticon emoticon:getEmoticons()){
            if(emoticon.getThumbPath()==null || emoticon.getFullPath()==null){
                return false;
            }
        }
        return true;
    }

    /**
     * 获取已下载好的表情
     * @return 已经下载好的表情
     */
    public ArrayList<Emoticon> getAvailableEmoticons(){
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        if(isLocal() || isEmojiList()){
            return getEmoticons();
        }
        for(Emoticon emoticon:getEmoticons()){
            if(emoticon.getThumbPath()!=null && emoticon.getFullPath()!=null){
                emoticons.add(emoticon);
            }
        }
        return emoticons;
    }

    public Emoticon findEmoByDes( String description){
        for(Emoticon emo:getAvailableEmoticons()){
            if(description.equals(emo.getDescription())){
                return emo;
            }
        }
        return null;
    }

    /**
     * 执行逐个下载
     * -【缩略图】和【原图】都会去下载;
     * @param emoticons 要下载的表情;
     * @param resultHandlerInterface 返回当前UserList
     * @param progressInterface 进度回调，返回小于100的进度;
     */
    public void downloadEach(final ArrayList<Emoticon> emoticons, final ResultHandlerInterface resultHandlerInterface, final ProgressInterface progressInterface) {
        //开始一个个下载
        final UserList self = this;
        final int[] totalCount = {0};
        final int[] success = {0};
        final int[] fail = {0};
        totalCount[0] = emoticons.size();
        fastLog("开始逐个下载 total : " + totalCount[0]);
        if(emoticons.size()==0){ //空列表，直接回调下载成功
            resultHandlerInterface.onResponse(self);
            return;
        }
        for (int i = 0; i < totalCount[0]; i++){
            final Emoticon emoticon = emoticons.get(i);
            fastLog("开始下载 : " + i);
            emoticon.download2File(false, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    success[0]++;
                    double progress = success[0] * 1f / totalCount[0] * 100;
                    progressInterface.onProgress(progress);
                    LogX.v(Constants.PROGRESS,"下载中，成功 : " + success[0] + " || " + progress + "%");
                    onFinish();
                }

                @Override
                public void onError(Exception e) {
                    fail[0]++;
                    onFinish();
                    LogX.e(Constants.PROGRESS,"下载中，失败 : " + fail[0]);
                }

                private void onFinish() {
                    if (success[0] + fail[0] != totalCount[0]) {
                        return; //仍在下载中
                    }
                    EmoticonDAO.saveInTx(emoticons);
                    LogX.d("数据库保存emoticons.");
                    if (fail[0] == 0) { //全部下载结束,全部成功
                        resultHandlerInterface.onResponse(self);
                    } else { //全部下载结束，有失败
                        resultHandlerInterface.onError(new Exception("下载出错,失败个数 : "+ fail[0]));
                    }
                }
            });
        }
    }

    public String getUserId() {
        return FacehubApi.getApi().getUser().getUserId();
    }

    public boolean isLocal(){
        return local;
    }

    public void setLocal(boolean local){
        this.local = local;
    }

    public boolean isDefaultFavorList(){
        int count = 0;
        for(UserList userList : FacehubApi.getApi().getUser().getUserLists()){
            if( userList.isLocal() || userList.getForkFromId()!=null
                    || userList.getId()==null || getId()==null){
                continue;
            }
            if(getId().equals(userList.getId())){
                return count==0;
            }
            count++;
        }
        return false;
    }

    public boolean isEmojiList() {
        return isEmojiList;
    }

    protected void setIsEmojiList(boolean emojiList) {
        isEmojiList = emojiList;
    }
}
