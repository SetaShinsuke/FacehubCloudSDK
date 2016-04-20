package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
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
    private Emoticon cover; //可能为空

    /**
     * {@link List}工厂方法
     * 注意!关于emoticons的具体处理在子类中进行( {@link UserList} 与 {@link EmoPackage} )
     *
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public List updateFiled(JSONObject jsonObject) throws JSONException {
        this.setId(jsonObject.getString("id"));
        this.setName(jsonObject.getString("name"));
        if (isJsonWithKey(jsonObject, "cover") && isJsonWithKey(jsonObject, "cover_detail")) {
            if (getCover()==null || getCover().getFilePath(Image.Size.FULL) == null) { //封面没有下载好
                Emoticon coverImage = new Emoticon(jsonObject.getJSONObject("cover_detail"), false);
//            Emoticon coverImage = EmoticonDAO.getUniqueEmoticon( jsonObject.getJSONObject("cover_detail").getString("id") , true );
                setCover(coverImage);
            }
        } else {
            setCover(null);
        }
        return this;
    }

    @Override
    public String toString() {
        return "\n[List] : " + "\nid : " + id
                + "\nname : " + name
                + "\nemoticons : " + emoticons
                + "\ncover : " + cover
                ;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public ArrayList<Emoticon> getEmoticons() {
        return emoticons;
    }

    protected void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = emoticons;
    }

    public Emoticon getCover() {
        if (cover != null) {
            return cover;
        }
        if (getEmoticons().size() > 0) {
            return getEmoticons().get(0);
        }
        return null;
    }

    protected void setCover(Emoticon cover) {
        this.cover = cover;
    }

    public void downloadCover(Image.Size size, ResultHandlerInterface resultHandlerInterface) {
        Emoticon cover = getCover();
        if (cover != null ){
            if(cover.getFilePath(size) == null) {
                cover.download2Cache(size, resultHandlerInterface);
            }else {
                resultHandlerInterface.onResponse(cover);
            }
        }
    }
}
