package com.azusasoft.facehubcloudsdk.api.models;

import com.azusasoft.facehubcloudsdk.api.models.Image;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SETA on 2016/5/9.
 * 用来管理内存中的{@link Image};
 */
public class ImageContainer {
    private HashMap<String,Image> imageHashMap = new HashMap<>();

    /**
     * 把{@link Image}对象存在内存;
     *
     * @param id 表情id;
     * @param image 图片对象;
     */
    public void put(String id,Image image){
        imageHashMap.put(id,image);
    }

    /**
     * 通过id获取内存中的{@link Image};
     *
     * @param id 图片id;
     * @return 图片对象;
     */
    public Image getUniqueEmoticonById(String id){
        Image image = imageHashMap.get(id);
        if(image==null){
            image = new Image();
            image.setId(id);
        }
        return image;
    }

    /**
     * 将数据库中的图片恢复到内存中
     */
    public void restore(){
        imageHashMap = new HashMap<>();
    }

    public HashMap<String,Image> getAllImages(){
        return imageHashMap;
    }
}
