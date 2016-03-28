package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2016/3/28.
 */
public class Preview extends FrameLayout {
    private Context context;

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

        ((SpImageView)findViewById(R.id.author_head)).displayCircleImage(R.drawable.test);
//        ((GifView)findViewById(R.id.image_view)).setGifAssets("demo.gif");
        ((GifView)findViewById(R.id.image_view)).setGifAssets("demo2.jpg");
    }
}
