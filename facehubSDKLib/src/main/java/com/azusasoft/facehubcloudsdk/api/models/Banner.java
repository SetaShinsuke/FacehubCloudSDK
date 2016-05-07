package com.azusasoft.facehubcloudsdk.api.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SETA on 2016/3/10.
 *
 * 主页展示的轮播图 :
 * {@link #image} 要显示的图片;
 * {@link #name} 名字;
 * {@link #type} 跳转到网页还是表情包;
 * {@link #content} 包ID或网页地址;
 *
 */
public class Banner {
    private Image image;
    private String name;
    private String type;
    private String content; //【包ID】或【网页地址】

    public Banner(){
    }

//    @Override
//    public String toString() {
//        return "\n[Banner]:\n" + "image : " + image
//                +"\nname : " + name
//                +"\ntype : " + type
//                + "\ncontent : " + content;
//    }

    public Banner(JSONObject jsonObject) throws JSONException {
        this.setName(jsonObject.getString("name"));
        this.setType(jsonObject.getString("type"));
        this.setContent(jsonObject.getString("content"));
        Image tmpImg = new Image();
        this.image = tmpImg.updateField(jsonObject.getJSONObject("image"));
    }

    public String getName() {
        return name;
    }

    protected Banner setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    protected Banner setType(String type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }

    protected Banner setContent(String content) {
        this.content = content;
        return this;
    }

    public Image getImage() {
        return image;
    }

    protected Banner setImage(Image image) {
        this.image = image;
        return this;
    }
}
