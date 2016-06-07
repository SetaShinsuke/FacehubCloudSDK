package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.azusasoft.facehubcloudsdk.R;


/**
 * Created by SETA on 2015/8/28.
 *
 */
public class GifViewFC extends FrameLayout {
    private Context context;
    private View mainView;
//    private Emoticon emoticon;
    private String imageUri;

    public GifViewFC(Context context) {
        super(context);
        initView(context);
    }
    public GifViewFC(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    public GifViewFC(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GifViewFC(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context){
        this.context = context;
        mainView = inflate(context, R.layout.gif_view_fc,null);
        addView(mainView);
//        WebView mWebView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
//        mWebView.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return true;
//            }
//        });
//        mWebView.setLongClickable(false);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mainView.findViewById(R.id.touch_shade).setOnClickListener(l);
    }

    public void setSize(int width,int height){
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        webGifView.setSize(width, height);
    }
    public void setByWidth(boolean byWidth){
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        webGifView.setByWidth(byWidth);
    }
    public void setShowAsCircle(boolean showAsCircle){
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        webGifView.setShowAsCircle(showAsCircle);
    }
    public void setGifResource(int resourceId){
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        webGifView.setGifResource(resourceId);
    }
//    public void setGifRaw(int rawId){
//        WebGifView webGifView = (WebGifView) findViewById(R.id.web_gif_view);
//        webGifView.setGifRaw(rawId);
//    }
    public void setGifAssets(String assetsName) {
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        String path="file:///android_asset/"+assetsName;
        webGifView.setGifPath(path);
        this.imageUri = path;
    }
    public void setGifPath(String path){
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        path = "file:///" + path;
        webGifView.setGifPath(path);
        this.imageUri = path;
    }
    public void setHttpPath(String path){
        WebGifViewFC webGifView = (WebGifViewFC) findViewById(R.id.web_gif_view_fc);
        webGifView.setGifPath(path);
        this.imageUri = path;
    }


//    public void setEmoticon(Emoticon emoticon){
//        if(emoticon == null || emoticon.getAutoPath()==null){
//            return;
//        }
//        if( this.imageUri==null || !this.imageUri.equals(emoticon.getAutoPath()) ){
//            this.emoticon = emoticon;
//            WebGifView webGifView = (WebGifView) findViewById(R.id.web_gif_view);
//            setGifPath(emoticon.getAutoPath());
////            this.imageUri = emoticon.getAutoPath();
//            LogEx.fastLog("Emoticon path : " + emoticon.getAutoPath());
//        }
//    }



    //不阻拦触摸事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return false;
    }
}
