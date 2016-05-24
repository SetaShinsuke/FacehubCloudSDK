package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2016/5/24.
 * 本地预存表情
 */
public class LocalEmoticon extends Emoticon {
    private String id;
    private String path;
    private String description;

    public LocalEmoticon(){

    }

    public String getId() {
        return id;
    }

    public LocalEmoticon setId(String id) {
        this.id = id;
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
