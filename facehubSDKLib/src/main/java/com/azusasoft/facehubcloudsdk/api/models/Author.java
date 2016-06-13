package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;

/**
 * Created by SETA on 2016/5/26.
 */
public class Author {
    private String name;
    private String description;
    private Image avatar;
    private Image authorBanner;

    protected Author(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        if(description==null){
            return "";
        }
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public Image getAvatar() {
        return avatar;
    }

    protected void setAvatar(Image avatar) {
        this.avatar = avatar;
    }

    public Image getAuthorBanner() {
        return authorBanner;
    }

    protected void setAuthorBanner(Image authorBanner) {
        this.authorBanner = authorBanner;
    }

    public void downloadAvatar(ResultHandlerInterface resultHandlerInterface){
        if(avatar!=null){
            avatar.downloadFull2Cache(resultHandlerInterface);
        }
    }

    public void downloadAuthorBanner(ResultHandlerInterface resultHandlerInterface){
        if(authorBanner!=null){
            authorBanner.downloadFull2Cache(resultHandlerInterface);
        }
    }
}
