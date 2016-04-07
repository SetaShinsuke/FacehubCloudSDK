package com.azusasoft.facehubcloudsdk.api.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/8.
 *
 * Tag的一个类目，一系列同类型Tag组成的包
 */
//public class TagBundle {
//    private String tagBundleName;
//    private ArrayList<String> tags = new ArrayList<>();
//
//    public TagBundle(String name){
//        this.tagBundleName = name;
//    }
//
//    /**
//     * {@link TagBundle}工厂方法，根据JSON数据修改tagBundle属性
//     *
//     * @param jsonArray JSON数据;eg : ["Section1","Section2","Section3","Section4","Section5"];
//     *                  eg2 : ["Author1","Author2","Author3","Author4","Author5"];
//     * @return {@link TagBundle}对象
//     * @throws JSONException
//     */
//    public TagBundle tagFactoryByJson(JSONArray jsonArray) throws JSONException {
//        tags.clear();
//        for(int i=0;i<jsonArray.length();i++ ) { //"Section1,Section2..."
//            String tagItem = jsonArray.getString(i);
//            tags.add(tagItem);
//        }
//        return this;
//    }
//
//    public String getName() {
//        return tagBundleName;
//    }
//
//    protected void setName(String tagTypeName) {
//        this.tagBundleName = tagTypeName;
//    }
//
//    public ArrayList<String> getTags() {
//        return tags;
//    }
//
//    protected void setTags(ArrayList<String> tags) {
//        this.tags = tags;
//    }
//
//    @Override
//    public String toString() {
//        return "[TagBundle] : " + "\nname : " + getName()
//                + "\ntags : " + tags;
//    }
//}
