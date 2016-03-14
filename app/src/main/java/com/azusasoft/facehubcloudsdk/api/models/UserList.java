package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class UserList extends List{
    private Long dbId;

    public UserList userListFactoryByJson(JSONObject jsonObject) throws JSONException{
        super.listFactoryByJson( jsonObject );
        //emoticons
        if( isJsonWithKey(jsonObject,"contents") ) {
            ArrayList<Emoticon> emoticons = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("contents");
            for(int i=0;i<jsonArray.length();i++){
                String emoId = jsonArray.getString(i);
                Emoticon emoticon = new Emoticon();
                emoticon.setId( emoId );
                emoticons.add(emoticon);
            }
            setEmoticons(emoticons);
        }
        //如果有emoticons的详情，则直接设置进去
        //TODO:与本地数据进行对比
        if( isJsonWithKey(jsonObject,"contents_details") ){
            ArrayList<Emoticon> emoticons = getEmoticons();
            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
            for (Emoticon emoticon:emoticons){
                emoticon.emoticonFactoryByJson( emoDetailsJson.getJSONObject( emoticon.getId() ) );
            }
        }
        save2DB();
        FacehubApi.getDbHelper().export();
        return this;
    }

    /**
     * Usages :
     *          1.{@link #userListFactoryByJson(JSONObject)};
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
