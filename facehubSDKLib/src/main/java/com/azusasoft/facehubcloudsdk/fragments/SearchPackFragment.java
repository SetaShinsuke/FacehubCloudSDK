package com.azusasoft.facehubcloudsdk.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.azusasoft.facehubcloudsdk.activities.EmoPackageDetailActivity;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.events.CacheClearEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.DownloadFrameBtn;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/7/6.
 */
public class SearchPackFragment extends BaseFragment {
    private final int LIMIT_PER_PAGE = 10;
    private Context context;
    private RecyclerViewEx recyclerView;
    private ResultPackAdapter adapter;
    private View noResult;
    private NoNetView noNetView;
    ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private String keyword;
    private int currentPage = 1;
    private boolean isLoadingNext = false;
    private boolean allLoaded = false;

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_result, null);
        this.context = view.getContext();
        noResult = view.findViewById(R.id.no_result);
        noNetView = (NoNetView) view.findViewById(R.id.no_net);
        recyclerView = (RecyclerViewEx) view.findViewById(R.id.result_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new ResultPackAdapter(getActivity());
        adapter.setEmoPackages(emoPackages);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new OnScroll());
        recyclerView.disableItemAnimation();
        noResult.setVisibility(View.GONE);
        initNoNetView();
        return view;
    }

    @Override
    protected void finishView() {
        adapter.clearLruCache();
        noNetView.cancelBadNetJudge();
    }

    private void initNoNetView() {
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
                adapter.setEmoPackages(emoPackages);
            }
        });
        int netType = NetHelper.getNetworkType(context);
        if (netType == NetHelper.NETTYPE_NONE) {
            LogX.w("商店页 : 网络不可用!");
            noNetView.show();
        } else {
            noNetView.startBadNetJudge();
            loadNextPage();
        }
    }

    class OnScroll extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(isLoadingNext || adapter.isAllLoaded()){
                return;
            }
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager.findLastVisibleItemPosition() >= (adapter.getItemCount() - 1)) {
                loadNextPage();
            }
        }
    }

    public void search(String keyword) {
        noResult.setVisibility(View.GONE);
        noNetView.setVisibility(View.GONE);
        currentPage = 1;
        this.keyword = keyword;
        emoPackages.clear();
        adapter.setAllLoaded(false);
        noNetView.startBadNetJudge();
        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoadingNext || adapter.isAllLoaded()) {
            return;
        }
        isLoadingNext = true;
        ArrayList<String> tags = new ArrayList<>();
        tags.add("二次元");
        FacehubApi.getApi().getPackagesByTags(tags, currentPage, LIMIT_PER_PAGE, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if (noNetView.isNetBad()) {
                    return;
                }
                noNetView.cancelBadNetJudge();
                ArrayList<EmoPackage> responseArray = (ArrayList<EmoPackage>) response;
                emoPackages.addAll(responseArray);
                if (responseArray.size() == 0 || responseArray.size() < LIMIT_PER_PAGE) {
                    adapter.setAllLoaded(true);
                } else {
                    adapter.setAllLoaded(false);
                }

                //如果没有搜索到结果
                if (currentPage == 0 && responseArray.size() == 0) {
                    noResult.setVisibility(View.VISIBLE);
                }
                isLoadingNext = false;
                currentPage++;
                //下载封面图
                for (int i = 0; i < responseArray.size(); i++) {
                    EmoPackage emoPackage = responseArray.get(i);
                    emoPackage.downloadCover(new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            refresh();
                        }

                        @Override
                        public void onError(Exception e) {
                            refresh();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                LogX.e("搜索出错 : " + e);
                noNetView.cancelBadNetJudge();
                if (currentPage == 0) {
                    noNetView.show();
                } else {
                    isLoadingNext = false;
                    adapter.setAllLoaded(true);
                }
            }
        });
    }

    public void refresh() {
        adapter.notifyDataSetChanged();
    }

    public void onEvent(DownloadProgressEvent event) {
        LogX.d(Constants.PROGRESS, "Search on event 进度 : " + event.percentage);
        for (int i = 0; i < emoPackages.size(); i++) {
            if (event.listId.equals(emoPackages.get(i).getId())) {
                adapter.notifyItemChanged(i);
            }
        }
    }

    public void onEvent(PackageCollectEvent event) {
        for (int i = 0; i < emoPackages.size(); i++) {
            if (event.emoPackageId.equals(emoPackages.get(i).getId())) {
                adapter.notifyItemChanged(i);
            }
        }
    }

    public void onEvent(CacheClearEvent event){
        adapter.clearLruCache();
    }

}

class ResultPackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private final int TYPE_NORMAL = 0;
    private final int TYPE_LOADING = 1;
    private boolean allLoaded = true;
//    private Drawable downloadBackDrawable;
    private LruCache<String, Bitmap> mLruCache;

    public ResultPackAdapter(Context context) {
        this.context = context;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            downloadBackDrawable = context.getDrawable(R.drawable.radius_rectangle_white_frame);
//        } else {
//            downloadBackDrawable = context.getResources().getDrawable(R.drawable.radius_rectangle_white_frame);
//        }
//        ViewUtilMethods.addColorFilter(downloadBackDrawable, FacehubApi.getApi().getThemeColor());

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

    public void clearLruCache(){
        mLruCache.evictAll();
        notifyDataSetChanged();
    }

    public void setEmoPackages(ArrayList<EmoPackage> emoPackages) {
        this.emoPackages = emoPackages;
        notifyDataSetChanged();
    }

    public void addEmoPackages(ArrayList<EmoPackage> emoPackages) {
        this.emoPackages.addAll(emoPackages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return emoPackages.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_LOADING;
        }
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View convertView;
        switch (viewType) {
            case TYPE_NORMAL:
                convertView = layoutInflater.inflate(R.layout.search_result_pack_item, parent, false);
                return new ResultPackHolder(convertView);
            case TYPE_LOADING:
                convertView = layoutInflater.inflate(R.layout.loading_footer, parent, false);
                return new LoadingHolder(convertView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_NORMAL:
                final ResultPackHolder holder = (ResultPackHolder) viewHolder;
                EmoPackage emoPackage = emoPackages.get(position);
                holder.listName.setText(emoPackage.getName());
                holder.listSubtitle.setText(emoPackage.getSubTitle());
                //封面
                if (emoPackage.getCover() == null || emoPackage.getCover().getDownloadStatus() == Image.DownloadStatus.fail) {
                    holder.coverView.setImageResource(R.drawable.load_fail);
                } else if (emoPackage.getCover().getThumbPath() != null) {
                    String path = emoPackages.get(position).getCover().getThumbPath();
                    Bitmap bitmap = mLruCache.get(path);
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeFile(path);
                        mLruCache.put(path, bitmap);
                    }
                    holder.coverView.setImageBitmap(bitmap);
                }

                if (emoPackages.get(position).isCollecting()) {
                    holder.showProgressBar(emoPackages.get(position).getPercent());
                } else {
                    if (emoPackages.get(position).isCollected()) {
                        holder.showDownloaded();
                    } else {
                        holder.showDownloadBtn();
                    }
                }

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("package_id", emoPackages.get(holder.getAdapterPosition()).getId());
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                };
                holder.left0.setOnClickListener(listener);
                holder.center0.setOnClickListener(listener);

                holder.downloadBtnArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        int position = holder.getAdapterPosition();
                        if (emoPackages.get(position).isCollecting() || emoPackages.get(position).isCollected()) {
                            return;
                        }
                        //emoPackage.setIsCollecting(true);
                        holder.showProgressBar(0f);
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
                break;
            case TYPE_LOADING:
                viewHolder.itemView.setVisibility(View.VISIBLE);
                if (isAllLoaded()) {
                    viewHolder.itemView.setVisibility(View.GONE);
                }
                break;
        }
    }

    public boolean isAllLoaded() {
        return allLoaded;
    }

    public void setAllLoaded(boolean allLoaded) {
        this.allLoaded = allLoaded;
        notifyDataSetChanged();
    }

    class ResultPackHolder extends RecyclerView.ViewHolder {
        SpImageView coverView;
        TextView listName, listSubtitle ; // , downloadText;
        View left0,center0 ; //,downloadBtnArea;
        DownloadFrameBtn downloadBtnArea;
//        CollectProgressBar progressBar;

        public ResultPackHolder(View itemView) {
            super(itemView);
            coverView = (SpImageView) itemView.findViewById(R.id.cover_image);
            listName = (TextView) itemView.findViewById(R.id.list_name);
            listSubtitle = (TextView) itemView.findViewById(R.id.list_subtitle);
            downloadBtnArea = (DownloadFrameBtn) itemView.findViewById(R.id.download_btn_area);
            left0 = itemView.findViewById(R.id.left0);
            center0 = itemView.findViewById(R.id.center0);
        }

        public void showDownloaded() {
            downloadBtnArea.showDownloaded();
        }

        public void showDownloadBtn() {
            downloadBtnArea.showDownloadBtn();
        }

        public void showProgressBar(final float percent) {
            downloadBtnArea.showProgressBar(percent);
            fastLog("Search 收藏更新进度 : " + percent);
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }
}
