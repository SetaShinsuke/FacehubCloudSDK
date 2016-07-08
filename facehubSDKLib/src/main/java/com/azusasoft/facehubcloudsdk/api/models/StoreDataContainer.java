package com.azusasoft.facehubcloudsdk.api.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by SETA on 2016/3/24.
 * 用来存储 :
 *      1.{@link Section};
 *      2.{@link EmoPackage}
 *      3.搜索记录;
 *      4.热门标签;
 */
public class StoreDataContainer {
    private static StoreDataContainer dataContainer;
    private final String SEARCH = "search";
    private final String HOT_TAGS = "hot_tags";
    private final String SEARCH_HISTORIES = "searchHistories";

    private ArrayList<Section> sections = new ArrayList<>();
    private HashMap<String,Section> sectionHashMap = new HashMap<>();
    private HashMap<String,EmoPackage> emoPackageHashMap = new HashMap<>();

    private ArrayList<String> hotTags = new ArrayList<>();
    private ArrayList<String> searchHistories = new ArrayList<>();

    public static StoreDataContainer getDataContainer(){
        if(dataContainer == null){
            dataContainer =  new StoreDataContainer();
        }
        return dataContainer;
    }

    public void restore(Context context){
        //TODO:恢复搜索记录和热门标签
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEARCH,Context.MODE_PRIVATE);
        //恢复热门标签
        if(sharedPreferences.contains(HOT_TAGS)){
            String hotTagString = sharedPreferences.getString(HOT_TAGS,"");
            this.hotTags = new ArrayList<>( Arrays.asList(hotTagString.split(",")) );
            while(hotTags.size()>0 && hotTags.get(hotTags.size()-1).length()==0){
                hotTags.remove(hotTags.size()-1);
            }
        }
        //恢复搜索记录
        if(sharedPreferences.contains(SEARCH_HISTORIES)){
            String searchHistoriesString = sharedPreferences.getString(SEARCH_HISTORIES,"");
            this.searchHistories = new ArrayList<>( Arrays.asList(searchHistoriesString.split(",")) );
            while(searchHistories.size()>0 && searchHistories.get(searchHistories.size()-1).length()==0){
                searchHistories.remove(searchHistories.size()-1);
            }
        }

//        //// FIXME: 2016/7/5 测试数据
        for(int i=0;i<10;i++){
            int j = (int)( Math.random()*3);
            String t1="";
            for(int k=0;k<j;k++){
                t1+="呵";
            }
            hotTags.add("标签"+t1);
        }
//        for(int i=0;i<5;i++){
//            searchHistories.add("搜索记录"+i);
//        }
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

    //region热门标签
    public ArrayList<String> getHotTags(){
        return this.hotTags;
    }

    public void setHotTags(Context context , ArrayList<String> hotTags){
        StringBuilder sb = new StringBuilder();
        for(String str : hotTags){
            sb.append(str);
            sb.append(",");
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEARCH,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HOT_TAGS,sb.toString());
        editor.apply();
    }
    //endregion

    //region搜索记录
    public ArrayList<String> getSearchHistories(){
        return searchHistories;
    }

    private void saveSearchHistories(Context context){
        StringBuilder sb = new StringBuilder();
        for(String str : searchHistories){
            sb.append(str);
            sb.append(",");
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEARCH,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SEARCH_HISTORIES,sb.toString());
        editor.apply();
    }

    public void addSearchHistoriy(Context context , String keyword){
        searchHistories.remove(keyword);
        searchHistories.add(0,keyword);
        if(searchHistories.size()>10){
            searchHistories.remove(10);
        }
        saveSearchHistories(context);
    }

    public void removeSearchHistory(Context context , String keyword){
        if(searchHistories.contains(keyword)) {
            searchHistories.remove(keyword);
            saveSearchHistories(context);
        }
    }

    public void clearSearchHistory(Context context) {
        searchHistories.clear();
        saveSearchHistories(context);
    }
    //endregion
}
