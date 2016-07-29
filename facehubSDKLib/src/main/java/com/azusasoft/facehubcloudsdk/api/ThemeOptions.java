package com.azusasoft.facehubcloudsdk.api;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.addColorFilter;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.getDrawable;

/**
 * Created by SETA_WORK on 2016/7/28.
 *
 * 标题栏背景drawable (渐变resource/主题色)
 * # 标题栏颜色(填充)
 * 标题栏按下颜色
 * 标题栏字体颜色
 *
 * 下载边框-未下载
 * 下载边框-已下载
 * 下载边框-drawable-未下载
 * 下载边框-drawable-已下载
 *
 * 下载按钮背景-未下载(渐变drawable/纯色drawable)
 * 下载按钮背景-已下载(渐变drawable/纯色drawable)
 * 下载按钮字体颜色-已下载
 * 下载按钮字体颜色-未下载
 *
 * 进度条背景
 * 进度条已完成
 * 进图条未完成
 *
 */
public class ThemeOptions {
    public static final int THEME_DEFAULT = 0;
    public static final int THEME_CUSTOM = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_LIGHT = 3;
    public static final int THEME_GREY = 4;

    private int type = 0;

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
    private Drawable downloadFrameDrawable, downloadFinFrameDrawable;
    //下载边框/字体-颜色
    private int downloadFrameColor,downloadFrameFinColor;

    //----------------------------------------
    //实心下载按钮
    private Drawable downloadBtnBgSolidDrawable,downloadBtnBgSolidFinDrawable;
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
        downloadFrameDrawable = getDrawable(context, R.drawable.radius_rectangle_white_frame);
        downloadFinFrameDrawable = getDrawable(context, R.drawable.radius_rectangle_white_frame);
        downloadBtnBgSolidDrawable = getDrawable(context,R.drawable.download_bg_none);
        downloadBtnBgSolidFinDrawable = getDrawable(context,R.drawable.download_bg_none);
        progressDrawable = getDrawable(context, R.drawable.radius_rectangle_color);
        switch (type) {
            case THEME_DEFAULT: //默认(面馆色)
            case THEME_CUSTOM:
                //标题
                statusBarColor = res.getColor(R.color.title_color_start);
                titleBgDrawable = getDrawable(context, R.drawable.actionbar_bg_default);
                titleBgColor = res.getColor(R.color.title_color_end);
                titlePressedColor = ViewUtilMethods.getDarkerColor(titleBgColor, 0.8f);
                titleTextColor = res.getColor(R.color.title_text_color);

                //下载-frame
                downloadFrameColor = res.getColor(R.color.download_frame);
                downloadFrameFinColor = res.getColor(R.color.download_frame_fin);
                addColorFilter(downloadFrameDrawable,downloadFrameColor);
                addColorFilter(downloadFinFrameDrawable,downloadFrameFinColor);

                //下载-实心
                addColorFilter(downloadBtnBgSolidDrawable,res.getColor(R.color.download_solid));
                addColorFilter(downloadBtnBgSolidFinDrawable,res.getColor(R.color.download_solid_fin));
                downloadSolidBtnTextColor = res.getColor(R.color.download_solid_text);

                //进度条
                progressBgColor = res.getColor(R.color.progressbar_bg);
                progressFinColor = res.getColor(R.color.progressbar_fin);
                progressTodoColor = res.getColor(R.color.progressbar_todo);
                addColorFilter(progressDrawable,progressBgColor);
                break;

//            case THEME_CUSTOM: //只改主题色

//                break;

            case THEME_DARK: //黑色主题
                break;

            case THEME_LIGHT: //白色主题
                break;

            case THEME_GREY:
                break;

            default:
                break;
        }
    }

    public int getType() {
        return type;
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

    public Drawable getDownloadFrameDrawable() {
        return downloadFrameDrawable;
    }

    public Drawable getDownloadFinFrameDrawable() {
        return downloadFinFrameDrawable;
    }

    public int getDownloadFrameColor() {
        return downloadFrameColor;
    }

    public int getDownloadFrameFinColor() {
        return downloadFrameFinColor;
    }

    public Drawable getDownloadBtnBgSolidDrawable() {
        return downloadBtnBgSolidDrawable;
    }

    public Drawable getDownloadBtnBgSolidFinDrawable() {
        return downloadBtnBgSolidFinDrawable;
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