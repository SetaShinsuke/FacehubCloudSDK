package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LATER_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class UserList extends List{
    private Long dbId;
    private String forkFromId;

    // "contents"和"contents_details" 不可为空
    public UserList userListFactoryByJson(JSONObject jsonObject , boolean doSave) throws JSONException{
        super.listFactoryByJson( jsonObject );
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
                Emoticon emoNew = emoticon.emoticonFactoryByJson(emoDetailsJson.getJSONObject(emoticon.getId()), LATER_SAVE);
                emos2Set.add(emoNew);
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
        setEmoticons( emos2Set );

        if(doSave){
            save2DB();
        }
        return this;
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
     *          1.{@link #userListFactoryByJson(JSONObject, boolean)};
     *          2.
     */
    private boolean save2DB(){
        return UserListDAO.save2DB( this );
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

    // 移动表情
    public void changeEmoticonPosition(int from , int to){
        if(from==to)
            return;
        Emoticon emo= getEmoticons().get(from);
        getEmoticons().remove(from);
        if(to>=getEmoticons().size()){
            getEmoticons().add(emo);
        }else {
            getEmoticons().add(to, emo);
        }
        save2DB();
        //TODO:排序后上传服务器
//        Collections.swap(getEmoticons(),from,to);
    }
}
