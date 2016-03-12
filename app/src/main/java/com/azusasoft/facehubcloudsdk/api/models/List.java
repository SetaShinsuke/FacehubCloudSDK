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
public class List {
    private String id;
    private String name;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private Image cover; //可能为空

    /**
     * {@link List}工厂方法
     * 注意!关于emoticons的具体处理在子类中进行( {@link UserList} 与 {@link EmoPackage} )
     *
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public List listFactoryByJson(JSONObject jsonObject) throws JSONException{
        this.setId(jsonObject.getString("id"));
        this.setName(jsonObject.getString("name"));
        if( isJsonWithKey(jsonObject, "cover") && isJsonWithKey(jsonObject,"cover_detail")){
            Image coverImage = new Image();
            coverImage.imageFactoryByJson( jsonObject.getJSONObject("cover_detail") );
        }else {
            setCover( null );
        }
        return this;
    }

    @Override
    public String toString() {
        return "\n[List] : " + "\nid : " + id
                +"\nname : " + name
                +"\nemoticons : " + emoticons
                +"\ncover : " + cover
                ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Emoticon> getEmoticons() {
        return emoticons;
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = emoticons;
    }

    public Image getCover() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover = cover;
    }
}
