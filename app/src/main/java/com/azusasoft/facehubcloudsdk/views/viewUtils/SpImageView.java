package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

/**
 * Created by SETA on 2015/8/30.
 */
public class SpImageView extends ResizableImageView{

    public SpImageView(Context context) {
        super(context);
    }

    public SpImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SpImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void displayCircleImage(int ResourceId){
        String uri = "drawable://"+ResourceId;
//        this.imgUri = uri;
        DisplayImageOptions circleOption = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(1000))
                        //.showStubImage(R.drawable.ic_app)
                .imageScaleType(ImageScaleType.NONE_SAFE)
//            .showImageOnLoading(R.drawable.default_cover)
                        //.showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageAware circleImageAware = new ImageViewAware(this, false);
        imageLoader.displayImage(uri, circleImageAware,
                circleOption);
    }

    public void displayFile(String filePath){
        if(filePath==null){
            return;
        }
//        this.resId = -1;
//        if(filePath.equals(this.imgUri)){
//            return;
//        }
//        this.imgUri = filePath;
        String uri = "file://"+ filePath;
        displayImage(uri);
    }

    public void displayImage(String imgUri){
//        this.imgUri = imgUri;
        DisplayImageOptions plainOption = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
//        ImageAware imageAware = new ImageViewAware(this, false);
        imageLoader.displayImage(imgUri, this , plainOption);
    }
}
