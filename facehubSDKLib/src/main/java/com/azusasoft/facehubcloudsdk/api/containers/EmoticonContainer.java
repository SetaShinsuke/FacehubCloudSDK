package com.azusasoft.facehubcloudsdk.api.containers;

import com.azusasoft.facehubcloudsdk.api.models.Emoticon;

import java.util.HashMap;

/**
 * Created by SETA on 2016/5/9.
 * 用来管理内存中的{@link Emoticon};
 */
public class EmoticonContainer {
    private HashMap<String,Emoticon> emoticonHashMap = new HashMap<>();
}
