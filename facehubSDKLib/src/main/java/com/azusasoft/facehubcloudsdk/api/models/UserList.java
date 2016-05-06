package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LATER_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class UserList extends List{
    private Long dbId;
    private String forkFromId;
    private String userId;

    // "contents"和"contents_details" 不可为空
    public UserList updateField(JSONObject jsonObject, boolean doSave) throws JSONException{
        super.updateField(jsonObject);
        //emoticons
        ArrayList<Emoticon> emoticonsTmp = new ArrayList<>();
        if(isJsonWithKey(jsonObject,"fork_from")){
            String forkFromId = jsonObject.getString("fork_from");
            setForkFromId(forkFromId);
        }

        if( isJsonWithKey(jsonObject,"contents") ) {
            JSONArray jsonArray = jsonObject.getJSONArray("contents");
            for(int i=0;i<jsonArray.length();i++){
                String emoId = jsonArray.getString(i);
                Emoticon emoticon = new Emoticon();
                emoticon.setId( emoId );
                emoticonsTmp.add(emoticon);
            }

        }

        ArrayList<Emoticon> emos2Set  = new ArrayList<>(); //要设置的emoticons

        if( isJsonWithKey(jsonObject,"contents_details") ){ //有"contents_details"字段
//            UserListDAO.deleteAll();
//            UserListDAO.delete(getId());
            fastLog("有contents_details");
            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
            for (Emoticon emoticon:emoticonsTmp){
                emoticon.updateField(emoDetailsJson.getJSONObject(emoticon.getId()));
                emos2Set.add(emoticon);
            }
            EmoticonDAO.saveEmoInTx(emos2Set);

        }else { //没有"content_details"字段
            fastLog("没有contents_details");
            ArrayList<Emoticon> emosNew  = new ArrayList<>(); //要新建的emoticons
            for (Emoticon emoticon : emoticonsTmp){
//                Image imageInDB = ImageDAO.findEmoticonById(emoticon.getId(), LATER_SAVE);
//                Emoticon emoticonInDB = (Emoticon)imageInDB;
                Emoticon emoticonInDB = EmoticonDAO.findEmoticonById(emoticon.getId(),LATER_SAVE);
                if( emoticonInDB==null ){ //数据库中没有
                    emosNew.add(emoticon);
                    emos2Set.add(emoticon);
                }else { //数据库中已经有
                    emos2Set.add(emoticonInDB);
                }
            }
            EmoticonDAO.saveEmoInTx(emosNew);
        }
        setEmoticons(emos2Set);

        if(doSave){
            save2DB();
        }
        return this;
    }
    public int size(){
        return getEmoticons().size();
    }
    public void removeEmoticons(ArrayList<Emoticon> emoticons2Remove){
        ArrayList<String> ids = new ArrayList<>();
        for(int i = 0;i<emoticons2Remove.size();i++){
            Emoticon emoticon = emoticons2Remove.get(i);
            ids.add(emoticon.getId());
            getEmoticons().remove(emoticon);
        }
        UserListDAO.deleteEmoticons(getId(), ids);
//        setEmoticons(emoticons2Remove);

        FacehubApi.getApi().removeEmoticonsByIds(ids, getId());
    }

    /**
     * Usages :
     *          1.{@link #updateField(JSONObject, boolean)} ;
     *          2.
     */
    private boolean save2DB(){
        return UserListDAO.save2DB(this);
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
    protected void setEmoticons(ArrayList<Emoticon> emoticons) {
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
    public void downloadCover(Image.Size size, ResultHandlerInterface resultHandlerInterface) {
        super.downloadCover(size, resultHandlerInterface);
    }

    /**
     *
     * @param resultHandlerInterface 返回当前UserList
     * @param progressInterface
     */
    public void download(ResultHandlerInterface resultHandlerInterface, ProgressInterface progressInterface){
        ArrayList<Emoticon> all=  new ArrayList<>(getEmoticons());
        all.add(getCover());
        retryTimes = 0;
        downloadEach(all,resultHandlerInterface,progressInterface);
    }

    private int retryTimes = 0;

    /**
     *
     * @param emoticons
     * @param resultHandlerInterface 返回当前UserList
     * @param progressInterface
     */
    public void downloadEach(final ArrayList<Emoticon> emoticons, final ResultHandlerInterface resultHandlerInterface, final ProgressInterface progressInterface) {
        //开始一个个下载
        final UserList self = this;
        final int[] totalCount = {0};
        final int[] success = {0};
        final int[] fail = {0};
//        final int[] retryTimes = {0};
        final ArrayList<Emoticon> failEmoticons = new ArrayList<>();
        totalCount[0] = emoticons.size();
        fastLog("开始逐个下载 total : " + totalCount);
        for (int i = 0; i < totalCount[0]; i++) {
            final Emoticon emoticon = emoticons.get(i);
            fastLog("开始下载 : " + i);
            emoticon.download2File(Image.Size.FULL, false, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    success[0]++;
                    double progress = success[0] * 1f / totalCount[0] * 100;
                    progressInterface.onProgress(progress);
                    fastLog("下载中，成功 : " + success[0] + " || " + progress + "%");
                    onFinish();
                }

                @Override
                public void onError(Exception e) {
                    fail[0]++;
                    failEmoticons.add(emoticon);
                    onFinish();
                    fastLog("下载中，失败 : " + fail[0]);
//                    fastLog("下载中，失败 : " + fail + "\nDetail : " + e);
                }

                private void onFinish() {
                    if (success[0] + fail[0] != totalCount[0]) {
                        return; //仍在下载中
                    }
                    if (fail[0] == 0) { //全部下载完成
                        EmoticonDAO.saveInTx(emoticons);
                        resultHandlerInterface.onResponse(self);
                        retryTimes = 0;
                    } else if (retryTimes < 5) { //重试次数5次
                        retryTimes++;
                        downloadEach(failEmoticons, resultHandlerInterface, progressInterface);
                    } else {
                        onError(new Exception("下载出错,失败个数 : "+ fail[0]));
                        retryTimes = 0;
                    }
                }
            });
        }
    }

    public String getUserId() {
        return FacehubApi.getApi().getUser().getUserId();
     //   return userId;
    }
}
