package com.azusasoft.facehubcloudsdk.views.uiModels;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/24.
 * 用来存储 :
 *      1.Sections;
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
