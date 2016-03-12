package com.azusasoft.facehubcloudsdk.api.models;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/8.
 */
public class List {
    private String id;
    private String name;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private Image cover;

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
