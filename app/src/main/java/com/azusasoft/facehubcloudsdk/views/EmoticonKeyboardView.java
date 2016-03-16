package com.azusasoft.facehubcloudsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HorizontalListView;

/**
 * Created by SETA on 2016/3/16.
 */
public class EmoticonKeyboardView extends FrameLayout {
    private Context mContext;
    private View mainView;

    private ViewPager emoticonPager;
    private KeyboardPageNav keyboardPageNav;
    private HorizontalListView listNavListView;
    //TODO:根据屏幕宽度计算每页显示几张表情
    //TODO:屏幕旋转时刷新键盘的接口

    public EmoticonKeyboardView(Context context) {
        super(context);
        constructView(context);
    }

    public EmoticonKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public EmoticonKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmoticonKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        mContext = context;
        this.mainView = LayoutInflater.from(context).inflate(R.layout.emoticon_keyboard,null);
        addView(mainView);

        this.emoticonPager = (ViewPager) mainView.findViewById(R.id.emoticon_pager);
        this.keyboardPageNav = (KeyboardPageNav) mainView.findViewById(R.id.keyboard_page_nav);
        this.listNavListView = (HorizontalListView) mainView.findViewById(R.id.list_nav);

        ListNavAdapter listNavAdapter = new ListNavAdapter(mContext);
        listNavListView.setAdapter(listNavAdapter);
        keyboardPageNav.setCount(6, 0);

        EmoticonPagerAdapter emoticonPagerAdapter = new EmoticonPagerAdapter(context);
        this.emoticonPager.setAdapter(emoticonPagerAdapter);
    }


}

/**
 * 显示表情的Pager
 */
class EmoticonPagerAdapter extends PagerAdapter{
    private Context context;
    private LayoutInflater layoutInflater;

    public EmoticonPagerAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.keyboard_pager_item, container, false);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView( (View)object );
    }
}

/**
 * 页数指示 小点/滚动条
 */
class KeyboardPageNav extends FrameLayout{
    private Context context;
    private View mainView;
    private HorizontalListView dotListView;
    private DotAdapter dotAdapter;

    public KeyboardPageNav(Context context) {
        super(context);
        constructView(context);
    }

    public KeyboardPageNav(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public KeyboardPageNav(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KeyboardPageNav(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        this.context = context;
        mainView = LayoutInflater.from(context).inflate(R.layout.keyboard_nav_dots, null);
        addView(mainView);
        dotListView = (HorizontalListView) mainView.findViewById(R.id.nav_dots);
        dotAdapter = new DotAdapter(context);
        dotListView.setAdapter(dotAdapter);
    }

    int count = 0;
    int current = -1;

    public void setCount(int count , int current) {
        this.count = count;
        //计算navDots宽度
        final Resources resources = context.getResources();
        int navWidth = count *
                (resources.getDimensionPixelSize(R.dimen.keyboard_dot_height)
                        + 2 * resources.getDimensionPixelSize(R.dimen.keyboard_dot_margin));
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(navWidth, dotListView.getLayoutParams().height);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dotListView.setLayoutParams(params);
        this.current = current;
        dotAdapter.notifyDataSetChanged();
    }
    public void setCurrent(int current) {
        this.current = current;
        dotAdapter.notifyDataSetChanged();
    }

    class DotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Context context;
        private LayoutInflater layoutInflater;

        public DotAdapter(Context context){
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = layoutInflater.inflate(R.layout.float_nav_dot_item,parent,false);
            DotHolder holder = new DotHolder(convertView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return count;
        }

        class DotHolder extends RecyclerView.ViewHolder{
            public DotHolder(View itemView) {
                super(itemView);
            }
        }
    }
}

/**
 * 列表导航，显示封面
 */
class ListNavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;

    public ListNavAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.keyboard_list_nav_item,parent,false);
        //横向显示五个列表
        ListNavHolder holder = new ListNavHolder(convertView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 20;
    }

    class ListNavHolder extends RecyclerView.ViewHolder{
        public ListNavHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

}

