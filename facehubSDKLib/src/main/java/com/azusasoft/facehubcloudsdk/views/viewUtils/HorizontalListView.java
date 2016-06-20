package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by SETA on 2015/8/30.
 */
public class HorizontalListView extends RecyclerView {
    private Context context;
    private ExLinearLayoutManager linearLayoutManager;
//    private ArrayList<OnScrollListener> onScrollListeners=new ArrayList<>();

    public HorizontalListView(Context context) {
        super(context);
        initView(context);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context){
        this.context = context;
        linearLayoutManager = new ExLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//        linearLayoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
//        linearLayoutManager.onMeasure();
        super.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setmView(this);
    }

    public LinearLayoutManager getLayoutManager(){
        return this.linearLayoutManager;
    }



    public void addOnScrollListener(OnScrollListener listener) {
//        super.addOnScrollListener(listener);
//        this.onScrollListeners.add(listener);
        addOnScrollListener(listener);
    }

//    public void clearOnScrollListeners(){
////        OnScrollListener listener;
////        for(int i=0;i<onScrollListeners.size();i++){
////            listener = onScrollListeners.get(i);
////            removeOnScrollListener(listener);
////        }
//        setOnScrollListener(null);
//    }
}
