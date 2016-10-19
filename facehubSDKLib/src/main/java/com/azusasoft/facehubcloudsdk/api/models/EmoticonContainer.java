package com.azusasoft.facehubcloudsdk.api.models;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA on 2016/5/9.
 * 用来管理内存中的{@link Emoticon};
 *
 * 应用启动时将数据库的表情复制到内存中，并记录是否已收藏的字段;
 * 注意：
 *      1.添加表情后，
 */
public class EmoticonContainer {
    private HashMap<String,Emoticon> emoticonHashMap = new HashMap<>();
    private HashMap<String,Emoticon> desIdMap = new HashMap<>();

    /**
     * 把{@link Emoticon}对象存在内存;
     *
     * @param id 表情id;
     * @param emoticon 表情对象;
     */
    public void put(String id,Emoticon emoticon){
        emoticonHashMap.put(id,emoticon);
//        if(id!=null && emoticon!=null && emoticon.getDescription()!=null){
//            Emoticon emo = desIdMap.get(emoticon.getDescription());
//            desIdMap.put(emoticon.getDescription(),emoticon);
//        }
    }

    /**
     * 通过id获取内存中的{@link Emoticon};
     *
     * @param id 表情id;
     * @return 表情对象;
     */
    public Emoticon getUniqueEmoticonById(String id){
        Emoticon emoticon = emoticonHashMap.get(id);
        if(emoticon==null){
            emoticon = new Emoticon(id);
            put(id, emoticon);
        }
        return emoticon;
    }

    /**
     * 将数据库中的表情恢复到内存中
     */
    public void restore(){
        ArrayList<Emoticon> emoticons = EmoticonDAO.findAll();
        for(Emoticon emoticon:emoticons){
            put(emoticon.getId(),emoticon);
        }
    }

    /**
     * 把指定表情存到数据库
     */
    public void updateEmoticons2DB(final ArrayList<Emoticon> emoticons) {
        EmoticonDAO.saveInTx(emoticons);
//        ThreadPoolManager.getDbThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                EmoticonDAO.saveInTx(emoticons);
//            }
//        });
    }
    /**
     * 把指定表情存到数据库
     */
    public void updateEmoticons2DB(Emoticon emoticon) {
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        emoticons.add(emoticon);
        EmoticonDAO.saveInTx(emoticons);
    }

    public HashMap<String,Emoticon> getAllEmoticons(){
        return emoticonHashMap;
    }
//
//    public void deleteAllEmoticons(){
//        emoticonHashMap.clearAll();
//        EmoticonDAO.deleteAll();
//    }

    public void updateAll(ArrayList<Emoticon> emoticons){
        emoticonHashMap.clear();
        EmoticonDAO.deleteAll();
        for(Emoticon emoticon:emoticons){
            put(emoticon.getId(),emoticon);
        }
        updateEmoticons2DB(emoticons);
    }
}