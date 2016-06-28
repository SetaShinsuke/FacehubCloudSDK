package com.azusasoft.facehubcloudsdk.api.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA on 2016/3/24.
 * 用来存储 :
 *      1.{@link Section};
 *      2.{@link EmoPackage}
 */
public class StoreDataContainer {
    private static StoreDataContainer dataContainer;

    private ArrayList<Section> sections = new ArrayList<>();
    private HashMap<String,Section> sectionHashMap = new HashMap<>();
    private HashMap<String,EmoPackage> emoPackageHashMap = new HashMap<>();

    public static StoreDataContainer getDataContainer(){
        if(dataContainer == null){
            dataContainer =  new StoreDataContainer();
        }
        return dataContainer;
    }

    public ArrayList<Section> getSections(){
        return this.sections;
    }

    public Section getUniqueSection(String tagName){
        Section section;
        if(sectionHashMap.containsKey(tagName)){
            section = sectionHashMap.get(tagName);
        }else {
            section = new Section();
            section.setTagName(tagName);
            sectionHashMap.put(tagName,section);
        }
        return section;
    }

    public EmoPackage getUniqueEmoPackage(String packageId){
        EmoPackage emoPackage;
        if(emoPackageHashMap.containsKey(packageId)){
            emoPackage = emoPackageHashMap.get(packageId);
        }else {
            emoPackage = new EmoPackage();
            emoPackage.setId(packageId);
            emoPackageHashMap.put(packageId,emoPackage);
        }
        return emoPackage;
    }
}
