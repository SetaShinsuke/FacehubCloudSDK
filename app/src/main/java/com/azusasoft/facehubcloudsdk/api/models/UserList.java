package com.azusasoft.facehubcloudsdk.api.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class UserList extends List{

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
        return this;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public Image getCover() {
        return super.getCover();
    }

    @Override
    public void setCover(Image cover) {
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
}
