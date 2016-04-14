package com.azusasoft.facehubcloudsdk.views.viewUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Banner;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/24.
 */
public class BannerView extends FrameLayout {
    private Context context;
    private ArrayList<Banner> banners = new ArrayList<>();
    private ViewPager bannerPager;
    private BannerPagerAdapter bannerPagerAdapter;
    private HorizontalListView dotNav;
    private DotAdapter dotAdapter;
    private int pageDuration = 5000;

    public BannerView(Context context) {
        super(context);
        constructView(context);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context){
        this.context = context;
        View mainView = LayoutInflater.from(context).inflate(R.layout.banner_view_layout, null, false);
        addView(mainView);
        bannerPager = (ViewPager)mainView.findViewById(R.id.banner_pager_facehub);
        dotNav = (HorizontalListView)mainView.findViewById(R.id.dot_nav_facehub);

        LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(ViewUtilMethods.getScreenWidth(context),
                (int) (ViewUtilMethods.getScreenWidth(context) * 1f / 750 * 260.0f));
        bannerPager.setLayoutParams(lyp);

        bannerPagerAdapter = new BannerPagerAdapter(context);
        bannerPager.setAdapter(bannerPagerAdapter);

        bannerPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        pausePlay();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        beginPlay();
                        break;
                }
                return false;
            }
        });

        dotAdapter = new DotAdapter(context);
        dotNav.setAdapter(dotAdapter);

        bannerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                dotAdapter.setCurrent( bannerPagerAdapter.getRealPos(position) );
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setPageDuration(int pageDuration){
        this.pageDuration = pageDuration;
    }

    public void setBanners(ArrayList<Banner> banners){
        setVisibility(VISIBLE);
        if(banners.size()==0){
            setVisibility(GONE);
        }
        this.banners = banners;
        bannerPagerAdapter.setBanners(banners);
        bannerPager.setCurrentItem(banners.size() * 100);
        beginPlay();
        dotAdapter.setCount( banners.size() );
        final Resources resources = context.getResources();
        int navWidth = banners.size() *
                (resources.getDimensionPixelSize(R.dimen.facehub_banner_dot_size)
                        + 2 * resources.getDimensionPixelSize(R.dimen.facehub_banner_dot_margin));
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(navWidth, dotNav.getLayoutParams().height);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dotNav.setLayoutParams(params);

        for(int i=0;i<banners.size();i++){
            Banner banner = banners.get(i);
            banner.getImage().download2Cache(Image.Size.FULL, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    bannerPagerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    LogX.e("Error downloading banner : " + e);
                }
            });
        }
    }

    //开始自动轮播
    private Runnable nextPageRunnable;
    public void beginPlay(){
        bannerPager.removeCallbacks(nextPageRunnable);
        nextPageRunnable = new Runnable() {
            @Override
            public void run() {
                bannerPager.setCurrentItem(bannerPager.getCurrentItem() + 1);
                bannerPager.postDelayed(nextPageRunnable,pageDuration);
            }
        };
        bannerPager.postDelayed(nextPageRunnable,pageDuration);
    }
    public void pausePlay(){
        bannerPager.removeCallbacks(nextPageRunnable);
    }

}

/** ----------------------------------------------------------- */
class BannerPagerAdapter extends PagerAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Banner> banners = new ArrayList<>();

    public BannerPagerAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setBanners(ArrayList<Banner> banners){
        this.banners = banners;
        notifyDataSetChanged();
    }

    public int getRealPos(int page){
        if(this.banners.size()<=0){
            return 0;
        }
        return page%this.banners.size();
    }

    @Override
    public int getCount() {
        if(this.banners.size()<=0){
            return 0;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.banner_pager_item,container,false);
        int realPosition = getRealPos(position);
        final Banner banner = banners.get(getRealPos(position));
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:点击banner
                LogX.fastLog("点击banner : " + banner.getContent());
            }
        });
        container.addView(itemView);
        if(banner.getImage()!=null && banner.getImage().getFilePath(Image.Size.FULL)!=null){
            ((SpImageView)itemView.findViewById(R.id.banner_image_facehub)).displayFile(banner.getImage().getFilePath(Image.Size.FULL));
        }
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
        container.removeView((View) object);
    }
}


/** ----------------------------------------------------------- */
class DotAdapter extends RecyclerView.Adapter<DotAdapter.DotHolder>{
    private Context context;
    private LayoutInflater layoutInflater;
    private int count = 0;
    private int current = 0;

    public DotAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public DotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.banner_nav_dot_item,parent,false);
        DotHolder holder = new DotHolder(convertView);
        holder.unSelected = convertView.findViewById(R.id.dot_img_unselected_facehub);
        holder.selected = convertView.findViewById(R.id.dot_img_selected_facehub);
        return holder;
    }

    @Override
    public void onBindViewHolder(DotHolder holder, int position) {
        holder.unSelected.setVisibility(View.VISIBLE);
        holder.selected.setVisibility(View.GONE);
        if(position==current){
            holder.unSelected.setVisibility(View.GONE);
            holder.selected.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
        notifyDataSetChanged();
    }

    public void setCurrent(int current) {
        this.current = current;
        notifyDataSetChanged();
    }

    class DotHolder extends RecyclerView.ViewHolder{
        View selected,unSelected;
        public DotHolder(View itemView) {
            super(itemView);
        }
    }
}