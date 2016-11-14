package com.azusasoft.facehubcloudsdk.api;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.addColorFilter;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.getDrawable;

/**
 * Created by SETA_WORK on 2016/7/28.
 *
 */
public class ThemeOptions {
    public static final int THEME_DEFAULT = 0;
    public static final int THEME_CUSTOM = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_LIGHT = 3;
    public static final int THEME_GREY = 4;
    public static final int THEME_PO_SCHOOL = 101;

    private int type = 0;
    //主题色
    private int themeColor;

    //状态栏颜色
    private int statusBarColor = 0;

    //----------------------------------------
    //标题栏颜色(填充)
    private int titleBgColor;
    //标题栏背景drawable (渐变resource/主题色)
    private Drawable titleBgDrawable;
    //标题栏按下颜色
    private int titlePressedColor;
    //标题栏字体颜色
    private int titleTextColor = 0;

    //----------------------------------------
    //下载边框/字体-drawable
//    private Drawable downloadFrameDrawable, downloadFinFrameDrawable;
    //下载边框/字体-颜色
    private int downloadFrameColor,downloadFrameFinColor;

    //----------------------------------------
    //实心下载按钮
    private int downloadBtnBgSolidColor,downloadBtnBgSolidFinColor;
    //实心下载字体
    private int downloadSolidBtnTextColor;

    //----------------------------------------
    //进度条背景
    private int progressBgColor,progressFinColor,progressTodoColor;
    private Drawable progressDrawable;

    public ThemeOptions(){

    }

    public void setType(Context context , int type , String themeColorString) {
        this.type = type;
        Resources res = context.getResources();
        themeColor = res.getColor(R.color.theme_color_blue);
//        downloadFrameDrawable = getDrawable(context, R.drawable.radius_rectangle_white_frame);
//        downloadFinFrameDrawable = getDrawable(context, R.drawable.radius_rectangle_white_frame);
        progressDrawable = getDrawable(context, R.drawable.radius_rectangle_color);
        switch (type) {
            case THEME_DEFAULT: //默认(面馆色)
                //标题
                themeColor = res.getColor(R.color.facehub_color);
                statusBarColor = res.getColor(R.color.title_color_start);
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_default);
                titleBgColor = res.getColor(R.color.title_color_end);
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color);

                //下载-frame
                downloadFrameColor = res.getColor(R.color.download_frame);
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin);
//                addColorFilter(downloadFrameDrawable,downloadFrameColor);
//                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                downloadBtnBgSolidColor = res.getColor(R.color.download_solid);
                downloadBtnBgSolidFinColor = res.getColor(R.color.download_solid_fin);
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg);
                progressFinColor = res.getColor(R.color.progressbar_fin);
                progressTodoColor = res.getColor(R.color.progressbar_todo);
                addColorFilter(progressDrawable,progressFinColor);
                break;

            case THEME_CUSTOM: //只改主题色
                if(themeColorString!=null) {
                    themeColor = Color.parseColor(themeColorString);
                }

                //标题
                statusBarColor = themeColor;
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_none);
                addColorFilter(titleBgDrawable,themeColor);
                titleBgColor = themeColor;
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color);

                //下载-frame
                downloadFrameColor = themeColor;
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin);
//                addColorFilter(downloadFrameDrawable,downloadFrameColor);
//                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                downloadBtnBgSolidColor = themeColor;
                downloadBtnBgSolidFinColor = res.getColor(R.color.download_solid_fin);
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg);
                progressFinColor = themeColor;
                progressTodoColor = res.getColor(R.color.progressbar_todo);
                addColorFilter(progressDrawable,progressFinColor);
                break;

            case THEME_DARK: //黑色主题
                //标题
                statusBarColor = res.getColor(R.color.title_color_start_dark);
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_dark);
                titleBgColor = res.getColor(R.color.title_color_end_dark);
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color_dark);

                //下载-frame
                downloadFrameColor = res.getColor(R.color.download_frame_dark);
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin_dark);
//                addColorFilter(downloadFrameDrawable,downloadFrameColor);
//                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                downloadBtnBgSolidColor = res.getColor(R.color.download_solid_dark);
                downloadBtnBgSolidFinColor = res.getColor(R.color.download_solid_fin_dark);
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text_dark);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg_dark);
                progressFinColor = res.getColor(R.color.progressbar_fin_dark);
                progressTodoColor = res.getColor(R.color.progressbar_todo_dark);
                addColorFilter(progressDrawable,progressFinColor);
                break;

            case THEME_LIGHT: //白色主题
                //标题
                statusBarColor = res.getColor(R.color.status_bar_dark);
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_none);
                titleBgColor = res.getColor(R.color.title_color_light);
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color_light);

                //下载-frame
                downloadFrameColor = res.getColor(R.color.download_frame_light);
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin_light);
//                addColorFilter(downloadFrameDrawable,downloadFrameColor);
//                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                downloadBtnBgSolidColor = res.getColor(R.color.download_solid_light);
                downloadBtnBgSolidFinColor = res.getColor(R.color.download_solid_fin_light);
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text_light);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg_light);
                progressFinColor = res.getColor(R.color.progressbar_fin_light);
                progressTodoColor = res.getColor(R.color.progressbar_todo_light);
                addColorFilter(progressDrawable,progressFinColor);
                break;

            case THEME_GREY:
                //标题
                statusBarColor = res.getColor(R.color.status_bar_dark);
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_grey);
                titleBgColor = res.getColor(R.color.title_color_end_grey);
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color_grey);

                //下载-frame
                downloadFrameColor = res.getColor(R.color.download_frame_grey);
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin_grey);
//                addColorFilter(downloadFrameDrawable,downloadFrameColor);
//                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                downloadBtnBgSolidColor = res.getColor(R.color.download_solid_grey);
                downloadBtnBgSolidFinColor = res.getColor(R.color.download_solid_fin_grey);
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text_grey);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg_grey);
                progressFinColor = res.getColor(R.color.progressbar_fin_grey);
                progressTodoColor = res.getColor(R.color.progressbar_todo_grey);
                addColorFilter(progressDrawable,progressFinColor);
                break;

            //##特殊定制主题
            case THEME_PO_SCHOOL:
                themeColor = res.getColor(R.color.facehub_color);

                //标题
                statusBarColor = res.getColor(R.color.go_school_theme_color);
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_none);
                addColorFilter(titleBgDrawable,statusBarColor);
                titleBgColor = statusBarColor;
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color_go_school);

                //下载-frame
                downloadFrameColor = res.getColor(R.color.download_frame);
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin);
//                addColorFilter(downloadFrameDrawable,downloadFrameColor);
//                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                downloadBtnBgSolidColor = res.getColor(R.color.download_frame);
                downloadBtnBgSolidFinColor = res.getColor(R.color.download_solid_fin);
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg);
                progressFinColor = res.getColor(R.color.facehub_color);
                progressTodoColor = res.getColor(R.color.progressbar_todo);
                addColorFilter(progressDrawable,progressFinColor);
                break;

            default:
                break;
        }
    }

    public int getType() {
        return type;
    }

    public int getThemeColor(){
        return themeColor;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public int getTitleBgColor() {
        return titleBgColor;
    }

    public Drawable getTitleBgDrawable() {
        return titleBgDrawable;
    }

    public int getTitlePressedColor() {
        return titlePressedColor;
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

//    public Drawable getDownloadFrameDrawable() {
//        return downloadFrameDrawable;
//    }
//
//    public Drawable getDownloadFinFrameDrawable() {
//        return downloadFinFrameDrawable;
//    }

    public int getDownloadFrameColor() {
        return downloadFrameColor;
    }

    public int getDownloadFrameFinColor() {
        return downloadFrameFinColor;
    }

    public int getDownloadBtnBgSolidColor(){
        return downloadBtnBgSolidColor;
    }

    public int getDownloadBtnBgSolidFinColor(){
        return downloadBtnBgSolidFinColor;
    }

    public int getDownloadSolidBtnTextColor() {
        return downloadSolidBtnTextColor;
    }

    public int getProgressBgColor() {
        return progressBgColor;
    }

    public int getProgressFinColor() {
        return progressFinColor;
    }

    public int getProgressTodoColor() {
        return progressTodoColor;
    }

    public Drawable getProgressDrawable() {
        return progressDrawable;
    }
}