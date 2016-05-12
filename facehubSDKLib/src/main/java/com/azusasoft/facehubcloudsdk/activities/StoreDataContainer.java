package com.azusasoft.facehubcloudsdk.activities;

import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by SETA on 2016/3/24.
 * 用来存储 :
 *      1.{@link Section};
 *      2.{@link EmoPackage}
 */
public class StoreDataContainer {
    private static StoreDataContainer dataContainer;

    private ArrayList<Section> sections = new ArrayList<>();

    public static StoreDataContainer getDataContainer(){
        if(dataContainer == null){
            dataContainer =  new StoreDataContainer();
        }
        return dataContainer;
    }

    public ArrayList<Section> getSections(){
        return this.sections;
    }

    public ArrayList<EmoPackage> getEmoPackagesOfSection(String sectionName){
        for(int i=0;i<sections.size();i++){
            Section section = sections.get(i);
            if(section.getTagName().equals(sectionName)){
                return section.getEmoPackages();
            }
        }
        return new ArrayList<>();
    }

    public ArrayList<EmoPackage> getEmoPackages(){
        ArrayList<EmoPackage> emoPackages = new ArrayList<>();
        for(int i=0;i<sections.size();i++){
            emoPackages.addAll(sections.get(i).getEmoPackages());
        }
        //去重
        HashSet<EmoPackage> h = new HashSet<>(emoPackages);
        emoPackages.clear();
        emoPackages.addAll(h);
        return emoPackages;
    }

    public EmoPackage getUniqueEmoPackage(String emoPackageId){
        for(int i=0;i<getEmoPackages().size();i++){
            EmoPackage emoPackage = getEmoPackages().get(i);
            if(emoPackage.getId().equals(emoPackageId)){
                return emoPackage;
            }
        }
        EmoPackage emoPackage = new EmoPackage();
        emoPackage.setId(emoPackageId);
        return emoPackage;
    }

//    public void putSection(Section section){
//        for (Section section1:sections){
//            if(section1.getTagName().equals(section.getTagName())){
//                section1.setEmoPackages( section.getEmoPackages() );
//                return;
//            }
//        }
//        sections.add(section);
//    }
}
