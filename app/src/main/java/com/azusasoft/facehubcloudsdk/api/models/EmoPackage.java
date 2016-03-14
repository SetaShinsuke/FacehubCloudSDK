package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.UtilMethods.isJsonWithKey;

/**
 * Created by SETA on 2016/3/8.
 */
public class EmoPackage extends List {
    private String description;
    private String subTitle;
    private Image background; //TODO:可能为空!!
    private String authorName;

    /**
     * {@link EmoPackage}工厂方法
     * 注意!方法执行后的 {@link EmoPackage} 中的 {@link #emoticons} 可能只包含 {@link Emoticon#id} 这一个属性
     *
     * @param jsonObject 待处理的Json
     * @return {@link EmoPackage}对象
     * @throws JSONException
     */
    public EmoPackage emoPackageFactoryByJson(JSONObject jsonObject) throws JSONException{
        super.listFactoryByJson( jsonObject );
        this.setDescription(jsonObject.getString("description"));
        this.setSubTitle(jsonObject.getString("sub_title"));
        this.setAuthorName(jsonObject.getJSONObject("author").getString("name"));
        if( isJsonWithKey(jsonObject, "background") && isJsonWithKey(jsonObject,"background_detail") ){
            Image bkgImage = new Image();
            this.setBackground(
                    bkgImage.imageFactoryByJson(jsonObject.getJSONObject("background_detail")));
        }else {
            setBackground( null );
        }
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
        if( isJsonWithKey(jsonObject,"contents_details") ){
            ArrayList<Emoticon> emoticons = getEmoticons();
            JSONObject emoDetailsJson = jsonObject.getJSONObject("contents_details");
            for (Emoticon emoticon:emoticons){
                emoticon.emoticonFactoryByJson( emoDetailsJson.getJSONObject(emoticon.getId()) , false );
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "\n[EmoPackage] : " + "\nid : " + getId()
                +"\nname : " + getName()
                +"\ndescription : " + description
                +"\nsubTitle : " + subTitle
                +"\nauthor name : " + authorName
                +"\ncover : " + getCover()
                +"\nbackground : " + background
                +"\nemoticons : " + getEmoticons() ;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Image getBackground() {
        return background;
    }

    public void setBackground(Image background) {
        this.background = background;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
