package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/24.
 * 用于辅助商店页显示
 */
public class Section {
    private String tagName;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();

    protected Section(){

    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    public String getTagName(){
        return this.tagName;
    }

    public void setEmoPackages(ArrayList<EmoPackage> emoPackages){
        this.emoPackages = emoPackages;
    }
    public ArrayList<EmoPackage> getEmoPackages(){
        return this.emoPackages;
    }

}
