package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.EmoticonApi;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.EmoticonDAO;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    /**
     * 把{@link Emoticon}对象存在内存;
     *
     * @param id 表情id;
     * @param emoticon 表情对象;
     */
    public void put(String id,Emoticon emoticon){
        emoticonHashMap.put(id,emoticon);
    }

    /**
     * 通过id获取内存中的{@link Emoticon};
     *
     * @param id 表情id;
     * @return 表情对象;
     */
    public Emoticon getEmoticonById(String id){
        Emoticon emoticon = emoticonHashMap.get(id);
        if(emoticon==null){
            emoticon = new Emoticon();
            emoticon.setId(id);
        }
        return emoticon;
    }

    /**
     * 将数据库中的表情恢复到内存中
     */
    public void restore(){
        ArrayList<Emoticon> emoticons = EmoticonDAO.findAll();
        for(Emoticon emoticon:emoticons){
            emoticonHashMap.put(emoticon.getId(),emoticon);
        }
    }

    /**
     * 把指定表情存到数据库
     */
    public void updateEmoticons2DB(ArrayList<Emoticon> emoticons) {
        EmoticonDAO.saveInTx(emoticons);
    }
    /**
     * 把指定表情存到数据库
     */
    public void updateEmoticons2DB(Emoticon emoticon) {
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        emoticons.add(emoticon);
        EmoticonDAO.saveInTx(emoticons);
    }

}