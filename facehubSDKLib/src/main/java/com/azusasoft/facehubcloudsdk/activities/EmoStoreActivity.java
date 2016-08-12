package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Banner;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.Section;
import com.azusasoft.facehubcloudsdk.api.models.SendRecordDAO;
import com.azusasoft.facehubcloudsdk.api.models.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.BannerView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HorizontalListView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by SETA on 2016/3/23.
 * 表情商店主页
 */
public class EmoStoreActivity extends BaseActivity {
    private static final int LIMIT_PER_PAGE = 8; //每次拉取的分区个数
    private static final int LIMIT_PER_SECTION = 8; //每个分区显示的包的个数
    //此处的分页加载是指 {@link Section} 的分页

    private Context context;
    private RecyclerViewEx recyclerView;
    private BannerView bannerView;
    private SectionAdapter sectionAdapter;
    private int currentPage = 0; //已加载的tags的页数
    private boolean isAllLoaded = false;
    private boolean isLoadingNext = false;
    private ArrayList<Section> sections = new ArrayList<>();
    private NoNetView noNetView;

    Handler handler = new Handler();
    Runnable loadNextTask;
//    Runnable showNoNetTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoticon_store);
        SendRecordDAO.recordEvent(Constants.RECORD_ENTER_SHOP_DEFAULT);
        context = this;
        //通知栏颜色
//        setStatusBarColor(FacehubApi.getApi().getActionbarColor());

        this.sections = StoreDataContainer.getDataContainer().getSections();
        sections.clear();

        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.showSettings();
        actionbar.setTitle(FacehubApi.getApi().getEmoStoreTitle());
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitThis();
            }
        });
        actionbar.setOnSettingsClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListsManageActivityNew.class);
                context.startActivity(intent);
            }
        });
//        actionbar.showSearchBtn();
        actionbar.setOnSearchBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);
            }
        });

        noNetView = (NoNetView) findViewById(R.id.no_net);
        assert noNetView != null;
        noNetView.setOnReloadClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noNetView.startBadNetJudge();
                initData();
            }
        });

        recyclerView = (RecyclerViewEx) findViewById(R.id.recycler_view_facehub);
        assert recyclerView != null;
        recyclerView.disableItemAnimation();

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        //Banner
        final View bView = LayoutInflater.from(context).inflate(R.layout.banner_layout, recyclerView, false);
        bannerView = (BannerView) bView.findViewById(R.id.banner_view_facehub);

        sectionAdapter = new SectionAdapter(context);
        sectionAdapter.setSections(sections);
        recyclerView.setAdapter(sectionAdapter);
        sectionAdapter.setBannerView(bView);

        //滚动加载
        isLoadingNext = true;

        //处理网络差的情况
        noNetView.initNoNetHandler(8000, new Runnable() {
            @Override
            public void run() {
                sections.clear();
                sectionAdapter.setSections(sections);
                showNoNet();
            }
        });

        noNetView.startBadNetJudge();
        initData();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                if(layoutManager.findLastVisibleItemPosition()>=(sectionAdapter.getItemCount()-1)){
                    if(!isLoadingNext && !isAllLoaded) {
//                        tLog("滚到底，加载下一页");
                        handler.removeCallbacks(loadNextTask);
//                        loadNextPage();
                        loadNextTask = new Runnable() {
                            @Override
                            public void run() {
                                loadNextPage();
                            }
                        };
                        handler.postDelayed(loadNextTask,250);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        hideContent();
        FacehubApi.getApi().getUser().silentDownloadAll();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            EventBus.getDefault().unregister(this);
        }catch (Exception e){
            LogX.w(getClass().getName() + " || EventBus 反注册出错 : " + e);
        }
    }

    public void onEvent(ExitViewsEvent exitViewsEvent){
        finish();
    }

    private void showNoNet(){
        Toast.makeText(context,"网络不可用!",Toast.LENGTH_SHORT).show();
        noNetView.setVisibility(View.VISIBLE);
        sectionAdapter.notifyDataSetChanged();
    }

    /**
     * 拉取tag & package ，banner;
     */
    private void initData(){
//        tLog("init data");
        int netType = NetHelper.getNetworkType(this);
        if(netType==NetHelper.NETTYPE_NONE) {
            LogX.w("商店页 : 网络不可用!");
            showNoNet();
            return;
        }

        isAllLoaded = false;
        sections.clear();
        currentPage = 0;
        sectionAdapter.notifyDataSetChanged();
        FacehubApi.getApi().getPackageTagsByParam("tag_type=custom",new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if(noNetView.isNetBad()){
                    showNoNet();
                    return;
                }
                noNetView.cancelBadNetJudge();
                sections.clear();
                ArrayList responseArray = (ArrayList) response;
                for (Object obj : responseArray) {
                    if (obj instanceof String) {
                        Section section = StoreDataContainer.getDataContainer().getUniqueSection((String) obj);
                        section.getEmoPackages().clear();
                        sections.add(section);
                    }
                }
                sectionAdapter.setSections(sections);
                loadNextPage();
            }

            @Override
            public void onError(Exception e) {
                LogX.e("Error gettingTags : " + e);
                showNoNet();
            }
        });

        FacehubApi.getApi().getBanners(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if(noNetView.isNetBad()){
                    return;
                }
                ArrayList<Banner> banners = (ArrayList<Banner>) response;
                bannerView.setBanners(banners);
            }

            @Override
            public void onError(Exception e) {
                LogX.w("商店页Banner拉取失败 : " + e);
                sectionAdapter.notifyDataSetChanged();
            }
        });
    }


    private void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
        sectionAdapter.setAllLoaded(isAllLoaded);
    }

    //继续拉取section
    private void loadNextPage() {
//        tLog("loadNext.");
        isLoadingNext = true;
        int end = Math.min(LIMIT_PER_PAGE * (currentPage + 1), sections.size());
        if (end == sections.size() && sections.size()!=0) {
            setAllLoaded(true);
        } else {
            setAllLoaded(false);
        }

        for (int i = currentPage * LIMIT_PER_PAGE; i < end; i++) {
            final Section section = sections.get(i);
            ArrayList<String> tags = new ArrayList<>();
            tags.add(section.getTagName());
            FacehubApi.getApi().getPackagesByTags(tags, 1 , LIMIT_PER_SECTION, new ResultHandlerInterface() { //拉取前8个包
                @Override
                public void onResponse(Object response) {
                    if(currentPage==0 && noNetView.isNetBad()){
                        showNoNet();
                        return;
                    }
                    ArrayList responseArray = (ArrayList) response;
                    section.getEmoPackages().clear();
                    for (Object obj : responseArray) {
                        if (obj instanceof EmoPackage) {
                            EmoPackage emoPackage = (EmoPackage) obj;
                            section.getEmoPackages().add(emoPackage);
                        }
                    }
                    sectionAdapter.setSections(sections);
                    isLoadingNext = false;
                    showContent();
                }

                @Override
                public void onError(Exception e) {
                    LogX.e("商店页 loadNextPage 出错 : " + e);
                    if(currentPage==0) {
                        showNoNet();
                    }else {
                        isLoadingNext = false;
                        isAllLoaded = true;
                        sectionAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        currentPage++;
    }

    private void showContent(){
        recyclerView.setVisibility(View.VISIBLE);
    }
    private void hideContent(){
        recyclerView.setVisibility(View.GONE);
    }

}

/**
 * 商店页分区Adapter
 */
class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_BANNER = 0;
    private static final int TYPE_SECTION = 1;
    private static final int TYPE_FOOTER = 2;

    private Context context;
    private LayoutInflater layoutInflater;
    ArrayList<Section> sections = new ArrayList<>();
    ArrayList<Section> preparedSections = new ArrayList<>();

    private boolean isAllLoaded = false;
    private View bannerView;

    public SectionAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setSections(ArrayList<Section> sections) {
        this.sections = sections;
        notifyDataSetChanged();
        for(int i=0;i<sections.size();i++){
            Section section = sections.get(i);
            final int finalI = i+1; //Banner
            for(EmoPackage emoPackage:section.getEmoPackages()){
                emoPackage.downloadCover(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
//                        notifyItemChanged(finalI);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
//                        notifyItemChanged(finalI);
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
        notifyDataSetChanged();
    }

    public void setBannerView(View bannerView){
        this.bannerView = bannerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView;
        switch (viewType) {
            case TYPE_BANNER:
//                convertView = layoutInflater.inflate(R.layout.banner_layout,parent,false);
                BannerHolder bannerHolder = new BannerHolder(bannerView);
                return bannerHolder;
            case TYPE_SECTION:
                convertView = layoutInflater.inflate(R.layout.section_item, parent, false);
                SectionHolder sectionHolder = new SectionHolder(convertView);
                sectionHolder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
                Drawable drawable = sectionHolder.tagName.getBackground();
                ViewUtilMethods.addColorFilter(drawable,FacehubApi.themeOptions.getThemeColor());

                sectionHolder.indexListView = (HorizontalListView) convertView.findViewById(R.id.section_index);
                sectionHolder.indexListView.setHasFixedSize(true);
//                sectionHolder.indexListView.setItemAnimator(new ItemNoneChangeAnimator());
                sectionHolder.indexListView.disableItemAnimation();
                sectionHolder.moreBtn = convertView.findViewById(R.id.more_btn);
                sectionHolder.indexAdapter = new SectionIndexAdapter(context);
                sectionHolder.indexListView.setAdapter(sectionHolder.indexAdapter);
                sectionHolder.setMoreBtnClick();
                return sectionHolder;
            case TYPE_FOOTER:
                convertView = layoutInflater.inflate(R.layout.loading_footer, parent, false);
                LoadingHolder loadingHolder = new LoadingHolder(convertView);
//                loadingHolder.loadingImage = (GifView) convertView.findViewById(R.id.loading_image);
                return loadingHolder;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_BANNER;
//            return TYPE_FOOTER;
        } else if (position > getItemCount()-2) {
            return TYPE_FOOTER;
        }
        return TYPE_SECTION;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BANNER:
                ViewGroup.LayoutParams params = bannerView.getLayoutParams();
                if(((BannerView)bannerView.findViewById(R.id.banner_view_facehub)).getCount()==0){
                    params.height = 0;
                }else {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                break;
            case TYPE_SECTION:
                final Section section = preparedSections.get(position-1);
                SectionHolder sectionHolder = (SectionHolder)viewHolder;
                sectionHolder.tagName.setText(section.getTagName());
                SectionIndexAdapter adapter = sectionHolder.indexAdapter;
                adapter.setEmoPackages(section.getEmoPackages());
                sectionHolder.section = section;
                break;
            case TYPE_FOOTER:
                LoadingHolder loadingHolder = (LoadingHolder)viewHolder;
                loadingHolder.itemView.setVisibility(View.VISIBLE);
                loadingHolder.cancelCloseLoading();
                if(isAllLoaded){ //拉取完毕
                    if(getItemCount()== sections.size()+2){ //全部加载成功
                        loadingHolder.itemView.setVisibility(View.GONE);
                    }else {
                        loadingHolder.closeInSec(10); //10秒还没反应，取消显示loading
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        preparedSections.clear();
        for(Section section : sections){
            if(!section.getEmoPackages().isEmpty()){ //分区有包
                for(EmoPackage emoPackage:section.getEmoPackages()){
                    if(emoPackage.getCover()!=null
                            && emoPackage.getCover().getThumbPath()!=null){
                        //有封面下载好
                        preparedSections.add(section);
                        break;
                    }
                }
            }
//            preparedSections.add(section);
        }
        return preparedSections.size() + 2;
//        return sections.size() + 2;
    }

    class SectionHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        View moreBtn;
        HorizontalListView indexListView;
        SectionIndexAdapter indexAdapter;
        Section section;

        public SectionHolder(View itemView) {
            super(itemView);
        }

        public void setMoreBtnClick(){
            moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (section == null) {
                        LogX.e("Section空!");
                        return;
                    }
                    Intent intent = new Intent(moreBtn.getContext(), MorePackageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("section_name", section.getTagName());
                    intent.putExtras(bundle);
                    moreBtn.getContext().startActivity(intent);
                }
            });
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder {
        Runnable closeTask;
        public LoadingHolder(View itemView) {
            super(itemView);
        }

        public void cancelCloseLoading(){
            itemView.removeCallbacks(closeTask);
        }

        public void closeInSec(int sec){
            itemView.removeCallbacks(closeTask);
            closeTask = new Runnable() {
                @Override
                public void run() {
                    itemView.setVisibility(View.GONE);
                }
            };
            itemView.postDelayed(closeTask,sec*1000);
        }
    }

    class BannerHolder extends RecyclerView.ViewHolder{

        public BannerHolder(View itemView) {
            super(itemView);
        }
    }
}

/**
 * 商店页分区内预览包Adapter
 */
class SectionIndexAdapter extends RecyclerView.Adapter<SectionIndexAdapter.SectionIndexHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();

    public SectionIndexAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setEmoPackages(ArrayList<EmoPackage> emoPackagesParam) {
        this.emoPackages = emoPackagesParam;
        notifyDataSetChanged();
    }

    @Override
    public SectionIndexHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.section_index_item, parent, false);
        SectionIndexHolder holder = new SectionIndexHolder(convertView);
        holder.leftMargin = convertView.findViewById(R.id.left_margin);
        holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
        holder.coverImage.setHeightRatio(1f);
        holder.listName = (TextView) convertView.findViewById(R.id.list_name);
        holder.setCoverImageClick();
        return holder;
    }

    @Override
    public void onBindViewHolder(SectionIndexHolder holder, int position) {

        if (position == 0) {
            holder.leftMargin.setVisibility(View.VISIBLE);
        }else{
            holder.leftMargin.setVisibility(View.GONE);
        }
        EmoPackage emoPackage = emoPackages.get(position);
        String name = "";
        if (emoPackage.getName() != null) {
            name = emoPackage.getName();
        }
        holder.listName.setText(name);

        if(emoPackage.getCover()!=null && emoPackage.getCover().getDownloadStatus()== Image.DownloadStatus.fail){
            holder.coverImage.setImageResource(R.drawable.load_fail);
        }else {
            if (emoPackage.getCover() != null && emoPackage.getCover().getThumbPath() != null) {
                holder.coverImage.displayFile(emoPackage.getCover().getThumbPath());
            } else {
                holder.coverImage.displayFile(null);
            }
        }
        holder.emoPackage = emoPackage;
    }

    @Override
    public int getItemCount() {
        int size = emoPackages.size();
        return size>8?8:size;
    }

    class SectionIndexHolder extends RecyclerView.ViewHolder {
        View leftMargin;
        SpImageView coverImage;
        TextView listName;
        EmoPackage emoPackage;

        public SectionIndexHolder(View itemView) {
            super(itemView);
        }

        public void setCoverImageClick(){
            coverImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(coverImage.getContext(), EmoPackageDetailActivity.class);
                    Bundle bundle = new Bundle();
                    if (emoPackage == null) {
                        LogX.e("emoPackage空!!");
                        return;
                    }
                    bundle.putString("package_id", emoPackage.getId());
                    intent.putExtras(bundle);
                    coverImage.getContext().startActivity(intent);
                }
            });

        }
    }
}


