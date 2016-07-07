package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2016/7/5.
 */
public class SearchIndicator extends FrameLayout {
    private Context context;
    private View mainView,leftMarginView;
    private TextView tab0,tab1;
    private ImageView trace;
    private int selectedColor,unSelectedColor;
    private int traceLength = 0;
    private int currentPage = 0;
    private OnTabClickListener onTabClickListener = new OnTabClickListener() {
        @Override
        public void onTabClick(int page) {

        }
    };

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
        traceLength = (int) (ViewUtilMethods.getScreenWidth(context)/2f);
        selectedColor = context.getResources().getColor(R.color.facehub_color);
        unSelectedColor = context.getResources().getColor(R.color.tab_unselected);
        tab0 = (TextView) mainView.findViewById(R.id.tab_text0);
        tab1 = (TextView) mainView.findViewById(R.id.tab_text1);
        trace = (ImageView) mainView.findViewById(R.id.trace);
        leftMarginView = mainView.findViewById(R.id.trace_left_margin);
        ViewGroup.LayoutParams params = trace.getLayoutParams();
        params.width = traceLength;
        trace.setLayoutParams(params);

        tab0.setOnClickListener(new OnTabClick());
        tab1.setOnClickListener(new OnTabClick());
    }

    class OnTabClick implements OnClickListener{

        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.tab_text0){
                onTabClickListener.onTabClick(0);
            }else if(v.getId()==R.id.tab_text1){
                onTabClickListener.onTabClick(1);
            }
        }
    }

    public void setColor(int color){
        this.selectedColor = color;
        tab0.setTextColor(color);
        tab1.setTextColor(color);
        trace.setBackgroundColor(color);
    }

    private void setCurrentPage(int page){
        currentPage = page;
        switch (page){
            case 0:
                tab0.setTextColor(selectedColor);
                tab1.setTextColor(unSelectedColor);
                break;
            case 1:
                tab0.setTextColor(unSelectedColor);
                tab1.setTextColor(selectedColor);
                break;
            default:
                tab0.setTextColor(selectedColor);
                tab1.setTextColor(unSelectedColor);
                break;
        }
    }

    public void scroll2Page(int position, float offset){
        setCurrentPage(position);
        ViewGroup.LayoutParams params = leftMarginView.getLayoutParams();
        params.width = traceLength*currentPage + (int) (offset * traceLength);
        leftMarginView.setLayoutParams(params);
    }

    public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
        this.onTabClickListener = onTabClickListener;
    }
}

