package com.azusasoft.facehubcloudsdk.views.touchableGrid;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by SETA on 2016/3/18.
 */
public class STGVUtilMethods {
    public static final String TAG = "seta";

    public static int getScreenWidth(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static void changeViewPosition(View view, int marginLeft, int marginTop){
        if(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.leftMargin = marginLeft;
            params.topMargin  = marginTop;
            view.setLayoutParams(params);
//            LogEx.fastLog("改变位置 : ( %d , %d ) . ",marginLeft,marginTop);
        }else {
            Log.e(TAG,"不能通过Margin改变位置！");
        }
    }
    public static int dip2px(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + .5f);
    }
    public static boolean isInZoneOf(Context context, View viewHost, float x, float y, float padding){
        boolean flag = false;
        int left,top,right,bottom;
//        left   = viewHost.getLeft();
//        top    = viewHost.getTop();
//        right  = viewHost.getRight();
//        bottom = viewHost.getBottom();

        left   = getLeftOnWindow(viewHost)-dip2px(context,padding);
        top    = getTopOnWindow(viewHost)-dip2px(context,padding);
        right  = getLeftOnWindow(viewHost) + viewHost.getWidth()+dip2px(context,padding);
        bottom = getTopOnWindow(viewHost) + viewHost.getHeight()+dip2px(context,padding);
//        if(viewHost instanceof ViewPager) {
//            LogX.fastLog("x : %f -(%d , %d)" +
//                    " \n y : %f - (%d , %d)", x, left, right, y, top, bottom);
//        }
        if(viewHost.getVisibility()== View.VISIBLE //如果视图不可见，则返回false
                && x>=left && x<=right && y<=bottom && y>=top){
            flag = true;
        }
        return flag;
    }
    public static int getLeftOnWindow(View view){
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[0];
    }
    public static int getTopOnWindow(View view){
        int[] location = new int[2];
        view.getLocationInWindow(location);
//        view.getLocationOnScreen(location);
        return location[1];
    }

    public static void addColorFilter(Drawable drawable, int colorInt){
        drawable.setColorFilter(new
                PorterDuffColorFilter( colorInt , PorterDuff.Mode.MULTIPLY));
    }

    public static void setBackgroudForView(View view, Drawable drawable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        }else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
