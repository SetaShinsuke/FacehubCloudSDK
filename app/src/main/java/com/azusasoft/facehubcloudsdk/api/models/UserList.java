package com.azusasoft.facehubcloudsdk.api.models;

import android.animation.FloatArrayEvaluator;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.utils.CodeTimer;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LATER_SAVE;
import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class UserList extends List{
    private Long dbId;

    // "contents"和"contents_details" 不可为空
    public UserList userListFactoryByJson(JSONObject jsonObject , boolean doSave) throws JSONException{
        super.listFactoryByJson( jsonObject );
        //emoticons
        ArrayList<Emoticon> emoticonsTmp = new ArrayList<>();
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
            //TODO:与本地数据进行对比
            UserListDAO.deleteAll();
            LogX.fastLog("有contents_details");
            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
            for (Emoticon emoticon:emoticonsTmp){
                Emoticon emoNew = emoticon.emoticonFactoryByJson(emoDetailsJson.getJSONObject(emoticon.getId()), LATER_SAVE);
                emos2Set.add(emoNew);
            }
            EmoticonDAO.saveInTx(emos2Set);

        }else { //没有"content_details"字段
            LogX.fastLog("没有contents_details");
            ArrayList<Emoticon> emosNew  = new ArrayList<>(); //要新建的emoticons
            for (Emoticon emoticon : emoticonsTmp){
                Emoticon emoticonInDB = EmoticonDAO.findById(emoticon.getId(), LATER_SAVE);
                if( emoticonInDB==null ){ //数据库中没有
                    emosNew.add(emoticon);
                    emos2Set.add(emoticon);
                }else { //数据库中已经有
                    emos2Set.add(emoticonInDB);
                }
            }
            EmoticonDAO.saveInTx(emosNew);
        }
        setEmoticons( emos2Set );

        if(doSave){
            save2DB();
        }
        return this;
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

    @Override
    protected void setName(String name) {
        super.setName(name);
    }

    @Override
    public Image getCover() {
        return super.getCover();
    }

    @Override
    protected void setCover(Image cover) {
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
}
