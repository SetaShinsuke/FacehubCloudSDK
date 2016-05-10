package com.azusasoft.facehubcloudsdk.api.containers;

import com.azusasoft.facehubcloudsdk.api.models.Image;

import java.util.HashMap;

/**
 * Created by SETA on 2016/5/9.
 * 用来管理内存中的{@link Image};
 */
public class ImageContainer {
    private HashMap<String,Image> imageHashMap = new HashMap<>();
}
