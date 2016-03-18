package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.azusasoft.facehubcloudsdk.api.utils.LogX;

/**
 * Created by SETA on 2016/3/18.
 */
public class ViewUtilMethods {

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

    public static void changeViewPosition(View view,int marginLeft,int marginTop){
        if(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.leftMargin = marginLeft;
            params.topMargin  = marginTop;
            view.setLayoutParams(params);
//            LogEx.fastLog("改变位置 : ( %d , %d ) . ",marginLeft,marginTop);
        }else {
            LogX.e("不能通过Margin改变位置！");
        }
    }

    public static int getLeftOnWindow(View view){
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[0];
    }
    public static int getTopOnWindow(View view){
        int[] location = new int[2];
        view.getLocationInWindow(location);
//        if(hasStatusBar) {
//            return location[1] ;
//        }else {
            return location[1];
//        }
    }
}
