package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2016/3/28.
 */
public class Preview extends FrameLayout {
    private Context context;
    private Emoticon emoticon;

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
                close();
            }
        });
        findViewById(R.id.collect_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emoticon!=null){ //判断图片是否已下载
                    //TODO:收藏表情
                    LogX.fastLog("收藏表情 : " + emoticon);
                }
            }
        });

        findViewById(R.id.close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        setVisibility(GONE);
    }

    public void show(Emoticon emoticon){
        show(emoticon,null,null);
    }

    public void show(Emoticon emoticon,String authorHeadPath,String authorName){
        SpImageView headImage = (SpImageView) findViewById(R.id.author_head);
        TextView nameText = (TextView) findViewById(R.id.author_name);
        View divider = findViewById(R.id.divider);
        headImage.setVisibility(INVISIBLE);
        nameText.setVisibility(INVISIBLE);
        divider.setVisibility(INVISIBLE);
        if(authorHeadPath!=null){
            headImage.setVisibility(VISIBLE);
            headImage.displayCircleImage(authorHeadPath);
        }
        if(authorName!=null){
            nameText.setVisibility(VISIBLE);
            nameText.setText(authorName+"");
        }
        if(authorHeadPath!=null || authorName!=null){
            divider.setVisibility(VISIBLE);
        }

        GifView imageView = (GifView) findViewById(R.id.image_view);
        TextView collectBtn = (TextView) findViewById(R.id.collect_btn);
        //todo:检查表情是否已收藏
        if(emoticon.isCollected()){
            collectBtn.setText("已收藏");
            collectBtn.setBackgroundResource(R.drawable.radius_bottom_rectangle_grey);
        }else {
            collectBtn.setText("收藏");
            collectBtn.setBackgroundResource(R.drawable.radius_bottom_rectangle_color);
        }
        setVisibility(VISIBLE);

        //TODO:显示、下载表情
//        imageView.setGifPath(emoticon.getFilePath(Image.Size.FULL));
        imageView.setGifAssets("demo.gif");
    }

    public void close(){
        setVisibility(GONE);
    }
}