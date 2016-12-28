package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ThemeOptions;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.getApi;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.addColorFilter;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.setBackgroundForView;

/**
 * Created by SETA on 2016/3/21.
 */
public class FacehubActionbar extends FrameLayout {
    private Context context;
    private View searchBtn;
    private View backBtn,closeBtn,editBtn,settingBtn;
    private ImageView searchBtnImg,backBtnImg,backBtnImgBig, closeBtnImage;
    private TextView titleText,editTextBtn,closeBtnText;
    private ImageView settingBtnImg;

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
        searchBtn.setVisibility(GONE);
        searchBtnImg = (ImageView) findViewById(R.id.search_image_btn);

        titleText = (TextView) findViewById(R.id.title_text);
        editTextBtn = (TextView) findViewById(R.id.edit_text_btn);
        backBtn = findViewById(R.id.back_btn);
        closeBtn = findViewById(R.id.close_btn);

        backBtnImg = (ImageView) findViewById(R.id.back_btn_image);
        backBtnImgBig = (ImageView) findViewById(R.id.back_btn_image_big); //自定义返回按钮图标
        closeBtnText = (TextView) findViewById(R.id.close_btn_text);
        closeBtnImage = (ImageView) findViewById(R.id.close_btn_image); //自定义关闭按钮图标

        editBtn = findViewById(R.id.edit_btn);
        settingBtn = findViewById(R.id.setting_btn);
        settingBtnImg = (ImageView) findViewById(R.id.setting_image_btn);

        ImageView[] imageViews = new ImageView[]{searchBtnImg,backBtnImg,settingBtnImg};
        TextView[] textViews = new TextView[]{titleText,editTextBtn,closeBtnText};

        backBtn.setOnTouchListener(new OnTouchEffect());
        closeBtn.setOnTouchListener(new OnTouchEffect());
        editBtn.setOnTouchListener(new OnTouchEffect());
        settingBtn.setOnTouchListener(new OnTouchEffect());
        searchBtn.setOnTouchListener(new OnTouchEffect());

        showBackBtn(true,false);
        if(!isInEditMode()) {
//            setBackgroundColor(FacehubApi.getApi().getActionbarColor());
            setBackgroundForView(this, getApi().themeOptions.getTitleBgDrawable());
            for(int i=0;i<imageViews.length;i++){
                addColorFilter(imageViews[i].getDrawable(),getApi().themeOptions.getTitleTextColor());
            }
            for(int i=0;i<textViews.length;i++){
                textViews[i].setTextColor(getApi().themeOptions.getTitleTextColor());
            }
            if(FacehubApi.getApi().themeOptions.getType()== ThemeOptions.THEME_LIGHT
                    || getApi().themeOptions.getType()==ThemeOptions.THEME_GREY){
                findViewById(R.id.title_divider).setVisibility(VISIBLE);
            }
//            titleText.setTextColor(themeOptions.getTitleTextColor());
//            addColorFilter(searchBtnImg.getDrawable(),themeOptions.getTitleTextColor());
        }
        mainView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void showBackBtn(boolean showBackBtn,boolean showCloseBtn){
        backBtn.setVisibility(GONE);
        backBtnImg.setVisibility(GONE);
        backBtnImgBig.setVisibility(GONE);
        closeBtn.setVisibility(GONE);
        closeBtnText.setVisibility(GONE);
        closeBtnImage.setVisibility(GONE);
        //显示返回按钮
        if(showBackBtn){
            backBtn.setVisibility(VISIBLE);
            if(FacehubApi.getApi().themeOptions.getType()==ThemeOptions.THEME_PO_SCHOOL){
                backBtnImgBig.setImageResource(R.drawable.back_go_school);
                backBtnImgBig.setVisibility(VISIBLE);
            }else {
                backBtnImg.setVisibility(VISIBLE);
            }
        }
        //显示关闭按钮
        if(showCloseBtn){
            closeBtn.setVisibility(VISIBLE);
            if(FacehubApi.getApi().themeOptions.getType()==ThemeOptions.THEME_PO_SCHOOL){
                closeBtnImage.setImageResource(R.drawable.back_go_school);
                closeBtnImage.setVisibility(VISIBLE);
            }else {
                closeBtnText.setVisibility(VISIBLE);
            }
        }
        if(showBackBtn && showCloseBtn){
            int left = backBtn.getPaddingLeft();
            backBtn.setPadding(left,0,ViewUtilMethods.dip2px(getContext(),5),0);
        }
    }

    public void showEdit(){
        editBtn.setVisibility(VISIBLE);
        settingBtn.setVisibility(GONE);
    }
    public void showSettings(){
        editBtn.setVisibility(GONE);
        settingBtn.setVisibility(VISIBLE);
    }
    public void hideBtns(){
        editBtn.setVisibility(GONE);
        settingBtn.setVisibility(GONE);
    }

    public void showSearchBtn(){
        searchBtn.setVisibility(VISIBLE);
    }

    public void hiseSearchBtn(){
        searchBtn.setVisibility(GONE);
    }

    public void setOnBackBtnClick(OnClickListener onClickListener){
        backBtn.setOnClickListener(onClickListener);
    }
    public void setOnCloseBtnClick(OnClickListener onClickListener){
        closeBtn.setOnClickListener(onClickListener);
    }

    public void setTitle(String title){
        titleText.setText(title + "");
    }

    public void setOnEditClick(OnClickListener onEditClick){
        editBtn.setOnClickListener(onEditClick);
    }

    public void setOnSettingsClick(OnClickListener onSettingsClick){
        settingBtn.setOnClickListener(onSettingsClick);
    }

    public void setSettingBtnImg(int res){
        settingBtnImg.setImageResource(res);
        addColorFilter(settingBtnImg.getDrawable(),getApi().themeOptions.getTitleTextColor());
    }

    public void setOnSearchBtnClick(OnClickListener onSearchBtnClick){
        searchBtn.setOnClickListener(onSearchBtnClick);
    }

    public void setEditBtnText(String text){
        editTextBtn.setText(text+"");
    }

    class OnTouchEffect implements OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction()==MotionEvent.ACTION_DOWN){
//                v.setBackgroundColor(v.getResources().getColor(R.color.facehub_color_dark));
                v.setBackgroundColor( getApi().themeOptions.getTitlePressedColor() );
            }
            if(event.getAction()==MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                v.setBackgroundColor(Color.parseColor("#00000000"));
            }
            return false;
        }
    }

}
