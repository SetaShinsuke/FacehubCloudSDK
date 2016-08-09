package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
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
import com.azusasoft.facehubcloudsdk.api.models.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.BannerView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.themeOptions;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.addColorFilter;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.setBackgroundForView;

/**
 * Created by SETA on 2016/7/11.
 */
public class EmoStoreActivityWC extends BaseActivity {
    //    private static final int LIMIT_PER_PAGE = 8; //每次拉取的分区个数
    private static final int LIMIT_PER_SECTION = 10; //每个分区显示的包的个数
    //此处的分页加载是指 {@link Section} 的分页

    private Context context;
    private RecyclerViewEx recyclerView;
    private BannerView bannerView;
    private SectionAdapterWC sectionAdapter;
    private int currentPage = 0; //当前tag已拉取的包的页数
    private boolean isAllLoaded = false;
    private boolean isLoadingNext = false;
    private ArrayList<Section> sections = new ArrayList<>();
    private int currentSectionIndex = -1;
    private NoNetView noNetView;

    Handler handler = new Handler();
    Runnable loadNextTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_emoticon_store_wc);
//        setStatusBarColor(FacehubApi.getApi().getActionbarColor());
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.showSettings();
        actionbar.setTitle(FacehubApi.getApi().getEmoStoreTitle());
        actionbar.showBackBtn(false, true);
        actionbar.setOnCloseBtnClick(new View.OnClickListener() {
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
        actionbar.setSettingBtnImg(R.drawable.setting_wc);

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
//        recyclerView.setItemAnimator(new ItemNoneChangeAnimator());
        recyclerView.disableItemAnimation();

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        //Banner
        final View bView = LayoutInflater.from(context).inflate(R.layout.banner_layout, recyclerView, false);
        bannerView = (BannerView) bView.findViewById(R.id.banner_view_facehub);
        bannerView.showIndicator(false);

        sectionAdapter = new SectionAdapterWC(context);
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
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findLastVisibleItemPosition() >= (sectionAdapter.getItemCount() - 1)) {
                    if (!isLoadingNext && !isAllLoaded) {
                        handler.removeCallbacks(loadNextTask);
//                        loadNextPage();
                        loadNextTask = new Runnable() {
                            @Override
                            public void run() {
                                loadNextPage();
                            }
                        };
                        handler.postDelayed(loadNextTask, 250);
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
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            LogX.w(getClass().getName() + " || EventBus 反注册出错 : " + e);
        }
    }

    public void onEvent(DownloadProgressEvent event) {
        LogX.d(Constants.PROGRESS, "emoStoreWc on event 进度 : " + event.percentage);
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            ArrayList<EmoPackage> emoPackages = section.getEmoPackages();
            for (int j = 0; j < emoPackages.size(); j++) {
                if (event.listId.equals(emoPackages.get(j).getId())) {
                    sectionAdapter.notifyItemChanged(sectionAdapter.getPositionByIndex(i, j));
                }
            }
        }
    }

    public void onEvent(PackageCollectEvent event) {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            ArrayList<EmoPackage> emoPackages = section.getEmoPackages();
            for (int j = 0; j < emoPackages.size(); j++) {
                if (event.emoPackageId.equals(emoPackages.get(j).getId())) {
                    sectionAdapter.notifyItemChanged(sectionAdapter.getPositionByIndex(i, j));
                    fastLog("包收藏成功 : notify " + i + " changed.");
                }
            }
        }
    }

    public void onEvent(UserListRemoveEvent event){
        sectionAdapter.notifyDataSetChanged();
    }

    public void onEvent(ExitViewsEvent exitViewsEvent) {
        finish();
    }

    private void showNoNet() {
        Toast.makeText(context, "网络不可用!", Toast.LENGTH_SHORT).show();
        noNetView.setVisibility(View.VISIBLE);
        sectionAdapter.notifyDataSetChanged();
    }

    /**
     * 拉取tag & package ，banner;
     */
    private void initData() {
//        tLog("init data");
        int netType = NetHelper.getNetworkType(this);
        if (netType == NetHelper.NETTYPE_NONE) {
            LogX.w("商店页 : 网络不可用!");
            showNoNet();
            return;
        }

        setAllLoaded(false);
        sections.clear();
        currentSectionIndex = -1;
        currentPage = 0;
        sectionAdapter.notifyDataSetChanged();
        FacehubApi.getApi().getPackageTagsByParam("tag_type=custom", new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if (noNetView.isNetBad()) {
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
                currentSectionIndex = 0;
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
                if (noNetView.isNetBad()) {
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

    private void loadNextPage() {
        isLoadingNext = true;
        if (isAllLoaded || sections.size() == 0 || currentSectionIndex>=sections.size()) {
            setAllLoaded(true);
            currentPage = 0;
            return;
        }
        final Section section = sections.get(currentSectionIndex);
        FacehubApi.getApi().getPackagesByTags(section.getTagName(), currentPage + 1, LIMIT_PER_SECTION, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if (currentSectionIndex <= 0 && currentPage == 0 && noNetView.isNetBad()) {
                    showNoNet();
                    return;
                }
                ArrayList responseArray = (ArrayList) response;
                for (Object obj : responseArray) {
                    if (obj instanceof EmoPackage) {
                        EmoPackage emoPackage = (EmoPackage) obj;
                        section.getEmoPackages().add(emoPackage);
                    }
                }
                sectionAdapter.setSections(sections);
                isLoadingNext = false;
                showContent();
                currentPage++;

                downloadCovers(section);
                if(responseArray.size()==0 || responseArray.size()<LIMIT_PER_SECTION){ //这个section拉取完毕
                    currentSectionIndex++;
                    currentPage = 0;
                    if(currentSectionIndex>=sections.size()){
                        setAllLoaded(true);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (currentSectionIndex <= 0 && currentPage == 0) {
                    showNoNet();
                } else {
                    isLoadingNext = false;
                    isAllLoaded = true;
                    sectionAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //继续拉取section
//    private void loadNextPage(int a) {
//        isLoadingNext = true;
//        int end = Math.min(LIMIT_PER_PAGE * (currentPage + 1), sections.size());
//        if (end == sections.size() && sections.size()!=0) {
//            setAllLoaded(true);
//        } else {
//            setAllLoaded(false);
//        }
//
//        for (int i = currentPage * LIMIT_PER_PAGE; i < end; i++) {
//            final Section section = sections.get(i);
//            ArrayList<String> tags = new ArrayList<>();
//            tags.add(section.getTagName());
//            FacehubApi.getApi().getPackagesByTags(tags, 1 , LIMIT_PER_SECTION, new ResultHandlerInterface() { //拉取前8个包
//                @Override
//                public void onResponse(Object response) {
//                    if(currentPage==0 && noNetView.isNetBad()){
//                        showNoNet();
//                        return;
//                    }
//                    ArrayList responseArray = (ArrayList) response;
//                    section.getEmoPackages().clear();
//                    for (Object obj : responseArray) {
//                        if (obj instanceof EmoPackage) {
//                            EmoPackage emoPackage = (EmoPackage) obj;
//                            section.getEmoPackages().add(emoPackage);
//                        }
//                    }
//                    sectionAdapter.setSections(sections);
//                    isLoadingNext = false;
//                    showContent();
//
//                    downloadCovers(section);
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    LogX.e("商店页 loadNextPage 出错 : " + e);
//                    if(currentPage==0) {
//                        showNoNet();
//                    }else {
//                        isLoadingNext = false;
//                        isAllLoaded = true;
//                        sectionAdapter.notifyDataSetChanged();
//                    }
//                }
//            });
//        }
//        currentPage++;
//    }

    private void downloadCovers(final Section section) {
        for (final EmoPackage emoPackage : section.getEmoPackages()) {
            if (emoPackage.getCover() != null && emoPackage.getCover().getThumbPath() == null) {
                emoPackage.downloadCover(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        int sectionIndex = sections.indexOf(section);
                        int emoPackageIndex = section.getEmoPackages().indexOf(emoPackage);
                        int position = sectionAdapter.getPositionByIndex(sectionIndex, emoPackageIndex);
                        sectionAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Exception e) {
                        int sectionIndex = sections.indexOf(section);
                        int emoPackageIndex = section.getEmoPackages().indexOf(emoPackage);
                        int position = sectionAdapter.getPositionByIndex(sectionIndex, emoPackageIndex);
                        sectionAdapter.notifyItemChanged(position);
                    }
                });
            }
        }
    }

//    private void loadNextPage(){ //1.加载sections
//
//    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void hideContent() {
        recyclerView.setVisibility(View.GONE);
    }
}

class SectionAdapterWC extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER_COUNT = 1; //目前只有banner
    private static final int FOOTER_COUNT = 1; //目前只有banner
    private static final int TYPE_BANNER = 0;
    private static final int TYPE_TITLE = 1;
    private static final int TYPE_SECTION = 2;
    private static final int TYPE_FOOTER = 3;

    private Context context;
    private LayoutInflater layoutInflater;
    ArrayList<Section> sectionsAll = new ArrayList<>();
//    ArrayList<Section> preparedSections = new ArrayList<>();

//    private Drawable downloadBackDrawable;
    private LruCache<String, Bitmap> mLruCache;

    private boolean isAllLoaded = false;
    private View bannerView;

    public SectionAdapterWC(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            downloadBackDrawable = context.getDrawable(R.drawable.radius_rectangle_white_frame);
//        } else {
//            downloadBackDrawable = context.getResources().getDrawable(R.drawable.radius_rectangle_white_frame);
//        }
//        ViewUtilMethods.addColorFilter(downloadBackDrawable, FacehubApi.getApi().getThemeColor());

        //初始化bitmap缓存
        int maxSize = (int) (Runtime.getRuntime().freeMemory() / 4);
        if (maxSize <= 0) {
            maxSize = 100000; //1M
        }
        mLruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String path, Bitmap bitmap) {
                return super.sizeOf(path, bitmap);
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if (!evicted) {
                    return;
                }
                if (oldValue != null) {
                    oldValue.recycle();
                }
            }
        };
    }

    public void setSections(ArrayList<Section> sections) {
        this.sectionsAll = sections;
        notifyDataSetChanged();
    }

    public ArrayList<Section> getAvailableSections(){
        ArrayList<Section> availableSections = new ArrayList<>();
        for(int i=0;i<sectionsAll.size();i++){
            if(sectionsAll.get(i).getEmoPackages().size()>0){
                availableSections.add(sectionsAll.get(i));
            }
        }
        return availableSections;
    }

    public void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
        notifyDataSetChanged();
    }

    public void setBannerView(View bannerView) {
        this.bannerView = bannerView;
    }

    @Override
    public int getItemCount() {
        int count = HEADER_COUNT + FOOTER_COUNT; //banner + footer
        for (Section section : getAvailableSections()) {
            count++;
            count += section.getEmoPackages().size();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_BANNER;
        } else if (position >= getItemCount() - HEADER_COUNT) {
            return TYPE_FOOTER;
        }
        int cursor = HEADER_COUNT - 1; //从banner下一个开始循环
        ArrayList<Section> availableSections = getAvailableSections();
        for (int i = 0; i < availableSections.size(); i++) {
            Section section = availableSections.get(i);
            cursor++;
            if (position == cursor) {
                return TYPE_TITLE;
            }
            for (int j = 0; j < section.getEmoPackages().size(); j++) {
                cursor++;
                if (position == cursor) {
                    return TYPE_SECTION;
                }
            }
        }
        return TYPE_SECTION;
    }

    public int getEmoPackageIndexByPosition(int position) {
        if (position < HEADER_COUNT || position > getItemCount() - HEADER_COUNT) {
            return -1;
        }
        int cursor = 0; //从banner下一个开始循环
        int titleCount = 0;
        ArrayList<Section> availableSections = getAvailableSections();
        for (int i = 0; i < availableSections.size(); i++) {
            Section section = availableSections.get(i);
            cursor++;
            titleCount++;
            if (position == cursor) {
                return -1; //标题,index = -1
            }
            for (int j = 0; j < section.getEmoPackages().size(); j++) {
                cursor++;
                if (position == cursor) {
                    return j; //包:返回在section里的下标
                }
            }
        }
        return -1;
    }

    public int getSectionIndexByPosition(int position) {
        if (position < HEADER_COUNT || position > getItemCount() - HEADER_COUNT) {
            return -1;
        }
        int cursor = 0; //从banner下一个开始循环
        int titleCount = 0;
        ArrayList<Section> availableSections = getAvailableSections();
        for (int i = 0; i < availableSections.size(); i++) {
            Section section = availableSections.get(i);
            cursor++;
            titleCount++;
            if (position == cursor) {
                return i; //标题,index = -1
            }
            for (int j = 0; j < section.getEmoPackages().size(); j++) {
                cursor++;
                if (position == cursor) {
                    return i; //包:返回section序号
                }
            }
        }
        return 0;
    }

    public int getPositionByIndex(int sectionIndex, int emoPackageIndex) {
        int cursor = HEADER_COUNT - 1;
        for (int i = 0; i <= sectionIndex; i++) {
            cursor++; //加上标题
            if (i == sectionIndex) {
                return cursor + emoPackageIndex + 1;
            }
            cursor += getAvailableSections().get(i).getEmoPackages().size();
        }
        return cursor;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView;
        switch (viewType) {
            case TYPE_BANNER:
//                convertView = layoutInflater.inflate(R.layout.banner_layout,parent,false);
                return new BannerHolder(bannerView);
            case TYPE_TITLE:
                convertView = layoutInflater.inflate(R.layout.title_item_wc, parent, false);
                return new TitleHolder(convertView);
            case TYPE_SECTION:
                convertView = layoutInflater.inflate(R.layout.section_item_wc, parent, false);
                return new SectionHolderWC(convertView);
            case TYPE_FOOTER:
                convertView = layoutInflater.inflate(R.layout.loading_footer, parent, false);
                return new LoadingHolder(convertView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Section section;
        switch (getItemViewType(position)) {
            case TYPE_BANNER:
                break;
            case TYPE_TITLE:
                section = getAvailableSections().get(getSectionIndexByPosition(position));
                ((TitleHolder) viewHolder).setTitle(section.getTagName() + "");
                break;
            case TYPE_SECTION:
                section = getAvailableSections().get(getSectionIndexByPosition(position));
                EmoPackage emoPackage = section.getEmoPackages().get(getEmoPackageIndexByPosition(position));
                SectionHolderWC holder = (SectionHolderWC) viewHolder;
                holder.loadData(emoPackage);
                break;
            case TYPE_FOOTER:
                LoadingHolder loadingHolder = (LoadingHolder) viewHolder;
                loadingHolder.mainView.setVisibility(View.VISIBLE);
                if (isAllLoaded) {
                    loadingHolder.mainView.setVisibility(View.GONE);
                }
                break;
        }
    }

    //Banner
    class BannerHolder extends RecyclerView.ViewHolder {

        public BannerHolder(View itemView) {
            super(itemView);
        }
    }

    //分区标题
    class TitleHolder extends RecyclerView.ViewHolder {
        TextView titleText;

        public TitleHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
        }

        public void setTitle(String title) {
            titleText.setText(title);
        }
    }

    //分区内容
    class SectionHolderWC extends RecyclerView.ViewHolder {

        SpImageView coverView;
        TextView listName, listSubtitle, downloadText;
        View left0, center0, downloadBtnArea;
        CollectProgressBar progressBar;

        public SectionHolderWC(View itemView) {
            super(itemView);
            coverView = (SpImageView) itemView.findViewById(R.id.cover_image);
            coverView.setHeightRatio(1f);
            listName = (TextView) itemView.findViewById(R.id.list_name);
            listSubtitle = (TextView) itemView.findViewById(R.id.list_subtitle);
            downloadText = (TextView) itemView.findViewById(R.id.download_text);
            downloadBtnArea = itemView.findViewById(R.id.download_btn_area);
            left0 = itemView.findViewById(R.id.left0);
            center0 = itemView.findViewById(R.id.center0);
            progressBar = (CollectProgressBar) itemView.findViewById(R.id.progress_bar);
        }

        public void loadData(final EmoPackage emoPackage) {
            listName.setText(emoPackage.getName());
            String subTitle = UtilMethods.formatString(emoPackage.getSubTitle());
            listSubtitle.setText(subTitle);
            if (emoPackage.isCollecting()) {
                showProgressBar(emoPackage.getPercent());
            } else {
                if (emoPackage.isCollected()) {
                    showDownloaded();
                } else {
                    showDownloadBtn();
                }
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("package_id", emoPackage.getId());
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            };
            downloadBtnArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (emoPackage.isCollecting() || emoPackage.isCollected()) {
                        return;
                    }
                    showProgressBar(0f);
                    emoPackage.collect(new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
//                                    notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) {
                            LogX.e("更多页下载出错 : " + e);
                            Toast.makeText(v.getContext(), "网络连接失败，请稍后重试", Toast.LENGTH_SHORT).show();
//                                    notifyDataSetChanged();
                        }
                    });

                }
            });

            left0.setOnClickListener(listener);
            center0.setOnClickListener(listener);

            if (emoPackage.getCover() != null && emoPackage.getCover().getDownloadStatus() == Image.DownloadStatus.fail) {
                coverView.setImageResource(R.drawable.load_fail);
            } else {
                if (emoPackage.getCover() != null && emoPackage.getCover().getThumbPath() != null) {
                    String path = emoPackage.getCover().getThumbPath();
                    Bitmap bitmap = mLruCache.get(path);
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeFile(path);
                        mLruCache.put(path, bitmap);
                    }
                    coverView.setImageBitmap(bitmap);
                } else {
                    coverView.displayFile(null);
                }
            }
        }

        public void showDownloaded() {
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("已下载");
//            setBackgroundForView(downloadText, themeOptions.getDownloadFinFrameDrawable());
            addColorFilter(downloadText.getBackground(),themeOptions.getDownloadFrameFinColor());
            downloadText.setTextColor(themeOptions.getDownloadFrameFinColor());
            progressBar.setVisibility(View.GONE);
        }

        public void showDownloadBtn() {
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("下载");
//            setBackgroundForView(downloadText, themeOptions.getDownloadFrameDrawable());
            addColorFilter(downloadText.getBackground(),themeOptions.getDownloadFrameColor());
            downloadText.setTextColor(themeOptions.getDownloadFrameColor());
            progressBar.setVisibility(View.GONE);
        }

        public void showProgressBar(final float percent) {
            downloadText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor(themeOptions.getDownloadFrameColor());
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setPercentage(percent);
        }
    }

    //加载中
    class LoadingHolder extends RecyclerView.ViewHolder {
        View mainView;
        Runnable closeTask;

        public LoadingHolder(View itemView) {
            super(itemView);
            mainView = itemView.findViewById(R.id.main_view);
        }

        public void cancelCloseLoading() {
            itemView.removeCallbacks(closeTask);
        }

        public void closeInSec(int sec) {
            itemView.removeCallbacks(closeTask);
            closeTask = new Runnable() {
                @Override
                public void run() {
                    itemView.setVisibility(View.GONE);
                }
            };
            itemView.postDelayed(closeTask, sec * 1000);
        }
    }
}