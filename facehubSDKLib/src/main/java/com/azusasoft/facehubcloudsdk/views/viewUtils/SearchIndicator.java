package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2016/7/5.
 */
public class SearchIndicator extends FrameLayout {
    private Context context;
    View mainView;
    private TextView tab0,tab1;

    public SearchIndicator(Context context) {
        super(context);
        constructView(context);
    }

    public SearchIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public SearchIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SearchIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        this.context = context;
        mainView = LayoutInflater.from(context).inflate(R.layout.pager_indicator,null);
        addView(mainView);
        tab0 = (TextView) mainView.findViewById(R.id.tab_text0);
        tab1 = (TextView) mainView.findViewById(R.id.tab_text1);
    }

    public void setColor(int color){
        tab0.setTextColor(color);
        tab1.setTextColor(color);
    }
}
