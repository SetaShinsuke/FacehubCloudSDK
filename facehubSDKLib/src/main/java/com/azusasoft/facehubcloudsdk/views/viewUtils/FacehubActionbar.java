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
import com.azusasoft.facehubcloudsdk.api.FacehubApi;

/**
 * Created by SETA on 2016/3/21.
 */
public class FacehubActionbar extends FrameLayout {
    private Context context;
    private View searchBtn;

    public FacehubActionbar(Context context) {
        super(context);
        constructView(context);
    }

    public FacehubActionbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public FacehubActionbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FacehubActionbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        this.context = context;
        View mainView = LayoutInflater.from(context).inflate(R.layout.emoticon_cloud_actionbar,null);
        addView(mainView);
        searchBtn = findViewById(R.id.search_btn);
        findViewById(R.id.back_btn)     .setOnTouchListener(new OnTouchEffect());
        findViewById(R.id.edit_btn)     .setOnTouchListener(new OnTouchEffect());
        findViewById(R.id.setting_btn)  .setOnTouchListener(new OnTouchEffect());
        searchBtn.setOnTouchListener(new OnTouchEffect());
        searchBtn.setVisibility(GONE);
        if(!isInEditMode()) {
            setBackgroundColor(FacehubApi.getApi().getThemeColor());
        }
        mainView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void showEdit(){
        findViewById(R.id.edit_btn)   .setVisibility(VISIBLE);
        findViewById(R.id.setting_btn).setVisibility(GONE);
    }
    public void showSettings(){
        findViewById(R.id.edit_btn)   .setVisibility(GONE);
        findViewById(R.id.setting_btn).setVisibility(VISIBLE);
    }
    public void hideBtns(){
        findViewById(R.id.edit_btn)   .setVisibility(GONE);
        findViewById(R.id.setting_btn).setVisibility(GONE);
    }

    public void showSearchBtn(){
        searchBtn.setVisibility(VISIBLE);
    }

    public void hiseSearchBtn(){
        searchBtn.setVisibility(GONE);
    }

    public void setOnBackBtnClick(OnClickListener onClickListener){
        findViewById(R.id.back_btn).setOnClickListener(onClickListener);
    }

    public void setTitle(String title){
        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setText(title + "");
    }

    public void setOnEditClick(OnClickListener onEditClick){
        findViewById(R.id.edit_btn).setOnClickListener(onEditClick);
    }

    public void setOnSettingsClick(OnClickListener onSettingsClick){
        findViewById(R.id.setting_btn).setOnClickListener(onSettingsClick);
    }

    public void setOnSearchBtnClick(OnClickListener onSearchBtnClick){
        searchBtn.setOnClickListener(onSearchBtnClick);
    }

    public void setEditBtnText(String text){
        ((TextView)findViewById(R.id.edit_text_btn)).setText(text+"");
    }

}
