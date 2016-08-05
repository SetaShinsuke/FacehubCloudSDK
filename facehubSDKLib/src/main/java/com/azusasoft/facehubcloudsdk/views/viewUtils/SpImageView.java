package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.HashMap;

/**
 * Created by SETA on 2015/8/30.
 */
public class SpImageView extends ResizableImageView {
    private String imgUri;
    ImageLoader imageLoader = ImageLoader.getInstance();


    private final static DisplayImageOptions plainOption = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .bitmapConfig(Bitmap.Config.RGB_565)
//            .decodingOptions()
            .cacheInMemory(true)
            .build();

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

    public void displayCircleUri(String uri){
        this.imgUri = uri;
        DisplayImageOptions circleOption = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(1000))
                //.showStubImage(R.drawable.ic_app)
                .imageScaleType(ImageScaleType.NONE_SAFE)
//            .showImageOnLoading(R.drawable.default_cover)
                //.showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .build();
//        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageAware circleImageAware = new ImageViewAware(this, false);
        imageLoader.displayImage(uri, circleImageAware,
                circleOption);
    }

    public void displayCircleImage(int ResourceId) {
        String uri = "drawable://" + ResourceId;
        this.imgUri = uri;
        DisplayImageOptions circleOption = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(1000))
                //.showStubImage(R.drawable.ic_app)
                .imageScaleType(ImageScaleType.NONE_SAFE)
//            .showImageOnLoading(R.drawable.default_cover)
                //.showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .build();
//        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageAware circleImageAware = new ImageViewAware(this, false);
        imageLoader.displayImage(uri, circleImageAware,
                circleOption);
    }

    public void displayFile(String filePath) {
        if (filePath == null) {
            displayImage(null);
            return;
        }
        String uri = "file://" + filePath;
        displayImage(uri);
    }

    public void displayImage(final String imgUri) {
        this.imgUri = imgUri;
//        ImageLoader imageLoader = ImageLoader.getInstance();
//        ImageAware imageAware = new ImageViewAware(this, false);
//        imageLoader.cancelDisplayTask(this);

        imageLoader.displayImage(imgUri,this,plainOption);
//        imageLoader.displayImage(imgUri, this, plainOption, new ImageLoadingListener() {
//            @Override
//            public void onLoadingStarted(String imageUri, View view) {
//                check(imageUri);
//            }
//
//            @Override
//            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                check(imageUri);
//            }
//
//            @Override
//            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                check(imageUri);
//            }
//
//            @Override
//            public void onLoadingCancelled(String imageUri, View view) {
//                check(imageUri);
//            }
//            private void check(String uri){
//                if(uri!=null && !uri.equals(imgUri)){
//                    LogX.w("Uri 改变了! 原uri : " + imgUri + " || 现uri : " + uri);
//                }
//            }
//        });
    }

    public void displayCircleAssets(String assetPath) {
//        String uri = "file://" + assetName;
        this.imgUri = assetPath;
        DisplayImageOptions circleOption = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(1000))
                //.showStubImage(R.drawable.ic_app)
                .imageScaleType(ImageScaleType.EXACTLY)
//                .showImageForEmptyUri(R.drawable.default_cover)
//            .showImageOnLoading(R.drawable.default_cover)
                //.showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .build();
//        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageAware circleImageAware = new ImageViewAware(this, false);
        imageLoader.displayImage(assetPath, circleImageAware,
                circleOption);
    }

    public void displayCircleImage(String imagePath) {
        String uri = "file://" + imagePath;
        this.imgUri = uri;
        DisplayImageOptions circleOption = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(1000))
                //.showStubImage(R.drawable.ic_app)
                .imageScaleType(ImageScaleType.EXACTLY)
//                .showImageForEmptyUri(R.drawable.default_cover)
//            .showImageOnLoading(R.drawable.default_cover)
                //.showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .build();
//        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageAware circleImageAware = new ImageViewAware(this, false);
        imageLoader.displayImage(uri, circleImageAware,
                circleOption);
    }
}
