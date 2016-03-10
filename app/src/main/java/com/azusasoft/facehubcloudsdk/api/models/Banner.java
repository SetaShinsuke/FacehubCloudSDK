package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.models.Image;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by SETA on 2016/3/10.
 */
public class Banner {
    private Image image;
    private String name;
    private String type;
    private String content; //【包ID】或【网页地址】

    public Banner(){
    }

    @Override
    public String toString() {
        return "\n[Banner]:\n" + "image : " + image
                +"\nname : " + name
                +"\ntype : " + type
                + "\ncontent : " + content;
    }

    public Banner bannerFactoryByJson(JSONObject jsonObject) throws JSONException {
        this.setName(jsonObject.getString("name"));
        this.setType(jsonObject.getString("type"));
        this.setContent(jsonObject.getString("content"));
        Image tmpImg = new Image();
        this.image = tmpImg.imageFactoryByJson( jsonObject.getJSONObject("image") );
        return this;
    }

    public String getName() {
        return name;
    }

    public Banner setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Banner setType(String type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Banner setContent(String content) {
        this.content = content;
        return this;
    }

    public Image getImage() {
        return image;
    }

    public Banner setImage(Image image) {
        this.image = image;
        return this;
    }
}
