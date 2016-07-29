package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/27.
 * 显示分区内所有表情包的页面
 */
public class MorePackageActivity extends BaseActivity {
    private static final int LIMIT_PER_PAGE = 10; //每次拉取的分区个数
    //    private static final int LIMIT_PER_PAGE = 30; //每次拉取的分区个数
    private Context context;
    private RecyclerViewEx recyclerView;
    private NoNetView noNetView;
    private MoreAdapter moreAdapter;
    private FacehubAlertDialog dialog;
    private int currentPage = 0; //已加载的tags的页数
    private boolean isAllLoaded = false;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private String sectionName;
    private boolean isLoadingNext = false;

    private Drawable downloadIconDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_more_package);
        //通知栏颜色
        setStatusBarColor(FacehubApi.getApi().getActionbarColor());

        dialog = (FacehubAlertDialog) findViewById(R.id.alert_dialog);
        dialog.hide();
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        actionbar.hideBtns();
        String title = "分区详情";
        actionbar.setTitle(title);
        //标题设置为分区名
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noNetView = (NoNetView) findViewById(R.id.no_net);
        assert noNetView != null;
        noNetView.setOnReloadClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noNetView.startBadNetJudge();
                loadNextPage();
            }
        });
        noNetView.initNoNetHandler(8000, new Runnable() {
            @Override
            public void run() {
                noNetView.setVisibility(View.VISIBLE);
                emoPackages.clear();
                moreAdapter.setEmoPackages(emoPackages);
            }
        });

        recyclerView = (RecyclerViewEx) findViewById(R.id.recycler_view_facehub);
        recyclerView.disableItemAnimation();
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        moreAdapter = new MoreAdapter(context);
        recyclerView.setAdapter(moreAdapter);

        sectionName = getIntent().getExtras().getString("section_name");
        actionbar.setTitle(sectionName);
        emoPackages = StoreDataContainer.getDataContainer().getUniqueSection(sectionName).getEmoPackages();
        emoPackages.clear();
        moreAdapter.setEmoPackages(emoPackages);

        int netType = NetHelper.getNetworkType(this);
        if (netType == NetHelper.NETTYPE_NONE) {
            LogX.w("商店页 : 网络不可用!");
            noNetView.show();
        } else {
            noNetView.startBadNetJudge();
            loadNextPage();
        }
//        FacehubApi.getApi().getPackagesByTags();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findLastVisibleItemPosition() >= (moreAdapter.getItemCount() - 1)) {
                    loadNextPage();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                switch (newState){
//                    case RecyclerView.SCROLL_STATE_DRAGGING:
//                        //正在滑动
//                        imageLoader.pause();
//                        break;
//                    case RecyclerView.SCROLL_STATE_IDLE:
//                        //滑动停止
//                        imageLoader.resume();
//                        break;
//                    case RecyclerView.SCROLL_STATE_SETTLING:
//                        imageLoader.pause();
//                        break;
//                }
            }
        });

        recyclerView.setVisibility(View.GONE);
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
//        moreAdapter.notifyDataSetChanged();

        LogX.d(Constants.PROGRESS, "more on event 进度 : " + event.percentage);
        for (int i = 0; i < emoPackages.size(); i++) {
            if (event.listId.equals(emoPackages.get(i).getId())) {
                moreAdapter.notifyItemChanged(i);
//                fastLog("notify " + i + " changed.");
            }
        }
    }

    public void onEvent(PackageCollectEvent event) {
//        moreAdapter.notifyDataSetChanged();
        for (int i = 0; i < emoPackages.size(); i++) {
            if (event.emoPackageId.equals(emoPackages.get(i).getId())) {
                moreAdapter.notifyItemChanged(i);
                fastLog("包收藏成功 : notify " + i + " changed.");
            }
        }
    }

    public void onEvent(ExitViewsEvent exitViewsEvent) {
        finish();
    }

    private void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
        moreAdapter.setAllLoaded(isAllLoaded);
    }

    public void loadNextPage() { //拉取package
        if (isAllLoaded || isLoadingNext) {
            return;
        }
        LogX.fastLog("更多页,拉取下一页,current page : " + currentPage);
        isLoadingNext = true;
        ArrayList<String> tags = new ArrayList<>();
        tags.add(sectionName);
//        FacehubApi.getApi().getPackagesByTags(tags, currentPage , LIMIT_PER_PAGE, new ResultHandlerInterface() {
        FacehubApi.getApi().getPackagesByTags(tags, currentPage + 1, LIMIT_PER_PAGE, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if (noNetView.isNetBad()) {
                    return;
                }
                noNetView.cancelBadNetJudge();
                ArrayList<EmoPackage> responseArray = (ArrayList<EmoPackage>) response;
                emoPackages.addAll(responseArray);
                if (responseArray.size() == 0 || responseArray.size() < LIMIT_PER_PAGE) {
                    setAllLoaded(true);
                } else {
                    setAllLoaded(false);
                }
                currentPage++;
                isLoadingNext = false;

                //下载封面图
                for (int i = 0; i < responseArray.size(); i++) {
                    final EmoPackage emoPackage = responseArray.get(i);
                    emoPackage.downloadCover(new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            moreAdapter.notifyDataSetChanged();
//                            for (int i = 0; i < emoPackages.size(); i++) {
//                                if (emoPackage.getId().equals(emoPackages.get(i).getId())) {
//                                    moreAdapter.notifyItemChanged(i);
//                                }
//                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            LogX.e("更多页封面下载失败 : " + e);
                            moreAdapter.notifyDataSetChanged();
                        }
                    });
                }
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                LogX.w("更多页 拉取包出错 : " + e);
                noNetView.cancelBadNetJudge();
                if (currentPage == 0) {
                    noNetView.show();
                } else {
                    isLoadingNext = false;
                    isAllLoaded = true;
                    moreAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}

/**
 * 分区所有包页面的Adapter
 */
class MoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private final static int TYPE_NORMAL = 0;
    private final static int TYPE_LOADING = 1;
    private boolean isAllLoaded = false;
    private Drawable downloadBackDrawable;

    private LruCache<String, Bitmap> mLruCache;

//    int maxSize = (int) (Runtime.getRuntime().freeMemory()/4);
//    private LruCache<String,Bitmap> mLruCache = new LruCache<String,Bitmap>(maxSize){
//        @Override
//        protected int sizeOf(String path, Bitmap bitmap) {
//            return super.sizeOf(path, bitmap);
//        }
//
//        @Override
//        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
//            super.entryRemoved(evicted, key, oldValue, newValue);
//            if(!evicted){
//                return;
//            }
//            if(oldValue!=null){
//                oldValue.recycle();
//            }
//        }
//    };


    public MoreAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            downloadBackDrawable = context.getDrawable(R.drawable.radius_rectangle_white_frame);
        } else {
            downloadBackDrawable = context.getResources().getDrawable(R.drawable.radius_rectangle_white_frame);
        }
        ViewUtilMethods.addColorFilter(downloadBackDrawable, FacehubApi.getApi().getThemeColor());

        //初始化bitmap缓存
        int maxSize = (int) (Runtime.getRuntime().freeMemory() / 4);
        fastLog("More Max Size : " + maxSize);
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

    public void setEmoPackages(ArrayList<EmoPackage> emoPackages) {
        this.emoPackages = emoPackages;
        notifyDataSetChanged();
    }

    public void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_NORMAL:
                view = layoutInflater.inflate(R.layout.more_item, parent, false);
                MoreHolder moreHolder = new MoreHolder(view);
                moreHolder.coverImage = (SpImageView) view.findViewById(R.id.cover_image);
                moreHolder.listName = (TextView) view.findViewById(R.id.list_name);
                moreHolder.downloadBtnArea = view.findViewById(R.id.download_btn_area);
                moreHolder.listSubtitle = (TextView) view.findViewById(R.id.list_subtitle);
                moreHolder.downloadText = (TextView) view.findViewById(R.id.download_text);
                moreHolder.coverImage.setHeightRatio(1f);
                moreHolder.left0 = view.findViewById(R.id.left0);
                moreHolder.center0 = view.findViewById(R.id.center0);
                moreHolder.progressBar = (CollectProgressBar) view.findViewById(R.id.progress_bar);
//                moreHolder.setOnClick();
                return moreHolder;
            case TYPE_LOADING:
                view = layoutInflater.inflate(R.layout.loading_footer, parent, false);
                LoadingHolder loadingHolder = new LoadingHolder(view);
                loadingHolder.mainView = view.findViewById(R.id.main_view);
                return loadingHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        synchronized (this) {
            switch (getItemViewType(position)) {
                case TYPE_NORMAL:
                    final MoreHolder moreHolder = (MoreHolder) holder;
//                    EmoPackage emoPackage = emoPackages.get(position);
                    moreHolder.listName.setText(emoPackages.get(position).getName() + "");
                    String subTitle = UtilMethods.formatString(emoPackages.get(position).getSubTitle());
                    moreHolder.listSubtitle.setText(subTitle);
                    if (emoPackages.get(position).isCollecting()) {
                        moreHolder.showProgressBar(emoPackages.get(position).getPercent());
                    } else {
                        if (emoPackages.get(position).isCollected()) {
                            moreHolder.showDownloaded();
                        } else {
                            moreHolder.showDownloadBtn();
                        }
                    }
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("package_id", emoPackages.get(position).getId());
                            intent.putExtras(bundle);
                            v.getContext().startActivity(intent);
                        }
                    };
                    moreHolder.downloadBtnArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            if (emoPackages.get(position).isCollecting() || emoPackages.get(position).isCollected()) {
                                return;
                            }
                            //emoPackage.setIsCollecting(true);
                            moreHolder.showProgressBar(0f);
                            emoPackages.get(position).collect(new ResultHandlerInterface() {
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

                    moreHolder.left0.setOnClickListener(listener);
                    moreHolder.center0.setOnClickListener(listener);

                    if (emoPackages.get(position).getCover() != null && emoPackages.get(position).getCover().getDownloadStatus() == Image.DownloadStatus.fail) {
                        moreHolder.coverImage.setImageResource(R.drawable.load_fail);
                    } else {
                        if (emoPackages.get(position).getCover() != null && emoPackages.get(position).getCover().getThumbPath() != null) {
//                            moreHolder.coverImage.displayFile(emoPackage.getCover().getThumbPath());
                            String path = emoPackages.get(position).getCover().getThumbPath();
                            Bitmap bitmap = mLruCache.get(path);
                            if (bitmap == null) {
                                bitmap = BitmapFactory.decodeFile(path);
                                mLruCache.put(path, bitmap);
                            }
                            moreHolder.coverImage.setImageBitmap(bitmap);
                        } else {
                            LogX.w("position " + position + "\n封面为空 , path: " + emoPackages.get(position).getCover().getThumbPath());
                            moreHolder.coverImage.displayFile(null);
                        }
                    }

//                    final Emoticon cover = emoPackages.get(position).getCover();
//                    moreHolder.coverImage.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            LogX.w("点击Cover : " + cover + "\nPosition : " + position);
//                            notifyDataSetChanged();
//                        }
//                    });

                    break;
                case TYPE_LOADING:
                    LoadingHolder loadingHolder = (LoadingHolder) holder;
                    loadingHolder.mainView.setVisibility(View.VISIBLE);
                    if (isAllLoaded) {
                        loadingHolder.mainView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position > emoPackages.size() - 1) {
            return TYPE_LOADING;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return emoPackages.size() + 1;
    }

    class MoreHolder extends RecyclerView.ViewHolder {
        SpImageView coverImage;
        TextView listName, listSubtitle;
        View downloadBtnArea;
        TextView downloadText;
        View left0, center0;
        CollectProgressBar progressBar;
//        EmoPackage emoPackage;

        public MoreHolder(View itemView) {
            super(itemView);
        }

//        public void setOnClick(){
//            downloadBtnArea.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(final View v) {
//                    if (emoPackage==null || emoPackage.isCollecting() || emoPackage.isCollected()) {
//                        return;
//                    }
//                    emoPackage.setIsCollecting(true);
//                    showProgressBar(0f);
//                    FacehubApi.getApi().getPackageDetailById(emoPackage.getId(), new ResultHandlerInterface() {
//                        @Override
//                        public void onResponse(Object response) {
//                            fastLog("More 开始下载.");
//                            emoPackage.collect(new ResultHandlerInterface() {
//                                @Override
//                                public void onResponse(Object response) {
//                                    notifyDataSetChanged();
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    notifyDataSetChanged();
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onError(Exception e) {
//                            Snackbar.make(v, "网络连接失败，请稍后重试", Snackbar.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            });
//        }

        public void showDownloaded() {
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("已下载");
            downloadText.setBackgroundResource(0);
            downloadText.setTextColor(Color.parseColor("#3fa142"));
            progressBar.setVisibility(View.GONE);
        }

        public void showDownloadBtn() {
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("下载");
            ViewUtilMethods.setBackgroundForView(downloadText, downloadBackDrawable);
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.GONE);
        }

        public void showProgressBar(final float percent) {
            downloadText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.VISIBLE);
            fastLog("More 更新进度 : " + percent);
            progressBar.setPercentage(percent);
        }
    }


    class LoadingHolder extends RecyclerView.ViewHolder {
        View mainView;

        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }
}

