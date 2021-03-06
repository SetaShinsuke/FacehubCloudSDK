package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2016/3/28.
 */
public class Preview extends FrameLayout {
    private Context context;
    private Emoticon emoticon;
    private boolean isAnimating = false;
    private CollectEmoticonInterface collectEmoticonInterface = new CollectEmoticonInterface() {
        @Override
        public void onStartCollect(Emoticon emoticon) {

        }

        @Override
        public void onCollected(Emoticon emoticon, boolean success) {

        }
    };

    public Preview(Context context) {
        super(context);
        constructView(context);
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public Preview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Preview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.preview,null);
        addView(view);
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getVisibility() == VISIBLE) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        findViewById(R.id.main_card).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        findViewById(R.id.back_area).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAnimating){
                    close();
                }
            }
        });
        findViewById(R.id.collect_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAnimating){
                    return;
                }
                UserList defaultUserList = null;
                if(FacehubApi.getApi().getUser().getUserLists().size()>0) {
                    defaultUserList = FacehubApi.getApi().getUser().getUserLists().get(0);
                }
                if(emoticon!=null && !emoticon.isCollected()
                        && defaultUserList!=null && defaultUserList.getId()!=null){ //判断图片是否已下载
                    //收藏表情
                    LogX.i("收藏表情 : " + emoticon);
                    close();
                    collectEmoticonInterface.onStartCollect(emoticon);
                    emoticon.collect(defaultUserList.getId(), new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            collectEmoticonInterface.onCollected(emoticon,true);
                        }

                        @Override
                        public void onError(Exception e) {
                            collectEmoticonInterface.onCollected(emoticon,false);
                            LogX.e("error collect emo : " + e);
                        }
                    });
                }
            }
        });

        findViewById(R.id.close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAnimating){
                    close();
                }
            }
        });
        setVisibility(GONE);
    }

    public void setAuthor(String authorHeadPath,String authorName){
        SpImageView headImage = (SpImageView) findViewById(R.id.author_head);
        TextView nameText = (TextView) findViewById(R.id.author_name);
        if(authorHeadPath!=null){
            headImage.displayCircleImage(authorHeadPath);
        }else {
            headImage.setImageResource(R.drawable.author_default);
        }
        if(authorName!=null){
            nameText.setVisibility(VISIBLE);
            nameText.setText(authorName);
        }
    }

    public void show(final Emoticon emoticon){
        if(isAnimating){
            return;
        }
        isAnimating = true;
        LogX.i("预览表情 id : " + emoticon.getId());
        this.emoticon = emoticon;
        final GifViewFC gifView = (GifViewFC) findViewById(R.id.image_view_facehub);
        gifView.setGifPath("");
        gifView.setVisibility(GONE);
        TextView collectBtn = (TextView) findViewById(R.id.collect_btn);
        setVisibility(VISIBLE);
        //检查表情是否已收藏
        if(emoticon.isCollected()){
            collectBtn.setText("已收藏");
            collectBtn.setBackgroundResource(R.drawable.radius_bottom_rectangle_grey);
        }else {
            collectBtn.setText("收藏");
            collectBtn.setBackgroundResource(R.drawable.radius_bottom_rectangle_color);
            Drawable mDrawable = collectBtn.getBackground();
            ViewUtilMethods.addColorFilter(mDrawable,FacehubApi.getApi().themeOptions.getThemeColor());
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isAnimating = false;
            }
        },200);

//        gifView.setGifPath(emoticon.getFilePath(Image.Size.FULL));

        emoticon.downloadFull2File(false, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if(getVisibility()==VISIBLE) {
                    gifView.setGifPath(emoticon.getFullPath());
                    gifView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gifView.setVisibility(VISIBLE);
                        }
                    }, 120);
                }
            }

            @Override
            public void onError(Exception e) {
                if(getVisibility()==VISIBLE) {
                    gifView.setGifResource(R.drawable.load_fail);
                    gifView.setVisibility(VISIBLE);
                }
                LogX.e("预览 下载表情失败 : " + e);
            }
        });
    }

    public void close(){
        setVisibility(GONE);
    }

    public void setCollectEmoticonInterface(CollectEmoticonInterface collectEmoticonInterface) {
        this.collectEmoticonInterface = collectEmoticonInterface;
    }


    public void onPause(){
        GifViewFC gifView = (GifViewFC) findViewById(R.id.image_view_facehub);
        gifView.onPause();
    }

    public void onDestroy(){
        GifViewFC gifView = (GifViewFC) findViewById(R.id.image_view_facehub);
        gifView.onDestroy();
    }

    public interface CollectEmoticonInterface{
        public void onStartCollect(Emoticon emoticon);
        public void onCollected(Emoticon emoticon , boolean success);
    }
}
