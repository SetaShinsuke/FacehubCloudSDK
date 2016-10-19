package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.util.Map;

/**
 * Created by SETA_WORK on 2016/10/19.
 */

public class LruCacheEx extends LruCache<String,Bitmap> {
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruCacheEx(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String path, Bitmap bitmap) {
        return super.sizeOf(path, bitmap);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
        if (!evicted) {
            return;
        }
        if (oldValue != null) {
            oldValue.recycle();
        }
    }

    public void clear(){
        Map<String, Bitmap> snapshot = this.snapshot();
        for (String id : snapshot.keySet()) {
            Bitmap bitmap = this.get(id);
            if(bitmap!=null){
                bitmap.recycle();
                bitmap = null;
            }
        }
        evictAll();
                //todo:迭代回收bitmap
    }
}
