package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

import com.azusasoft.facehubcloudsdk.api.FacehubApi;

public class OnTouchEffect implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction()==MotionEvent.ACTION_DOWN){
//                v.setBackgroundColor(v.getResources().getColor(R.color.facehub_color_dark));
                v.setBackgroundColor( FacehubApi.getApi().getThemeColorDark() );
            }
            if(event.getAction()==MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                v.setBackgroundColor(Color.parseColor("#00000000"));
            }
            return false;
        }
    }