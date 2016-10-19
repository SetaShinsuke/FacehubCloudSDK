package com.azusasoft.facehubcloudsdk.fragments;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/7/6.
 */
public class SearchEmoFragment extends BaseFragment {
    private final int LIMIT_PER_PAGE = 32;

    private Context context;
    private RecyclerViewEx recyclerView;
    private ResultEmoAdapter adapter;
    private NoNetView noNetView;
    private View noResult;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private String keyword;
    private int currentPage = 1;
    private boolean isLoadingNext = false;
    private boolean allLoaded = false;


    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_result,null);
        context = view.getContext();
        noResult = view.findViewById(R.id.no_result);
        noNetView = (NoNetView) view.findViewById(R.id.no_net);
        recyclerView = (RecyclerViewEx) view.findViewById(R.id.result_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(context,4));
        adapter = new ResultEmoAdapter(context);
        adapter.setEmoticons(emoticons);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new OnScroll());
        recyclerView.disableItemAnimation();
        noResult.setVisibility(View.GONE);
        initNoNetView();
        return view;
    }

    @Override
    protected void finishView() {

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
                emoticons.clear();
                adapter.setEmoticons(emoticons);
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
        emoticons.clear();
        adapter.setAllLoaded(false);
        noNetView.startBadNetJudge();
        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoadingNext || adapter.isAllLoaded()) {
            return;
        }
        isLoadingNext = true;
        //TODO:搜索
        LogX.fastLog("搜索表情");
        String[] ids = new String[]{"d68eb925-25fc-46e3-8290-8af9b5d850d6","aad67fb7-4c3b-48ae-a95d-6b5fde2c28c2","5748f2eb-5a7c-4f8d-a509-1f5516ac9a42"};
        String packId = ids[currentPage-1];
        FacehubApi.getApi().getPackageDetailById(packId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                if (noNetView.isNetBad()) {
                    return;
                }
                noNetView.cancelBadNetJudge();
                EmoPackage emoPackage = (EmoPackage) response;
                ArrayList<Emoticon> responseArray = emoPackage.getEmoticons();
                emoticons.addAll(responseArray);
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
                    Emoticon emoticon = responseArray.get(i);
                    if(emoticon.getThumbPath()!=null){
                        continue;
                    }
                    emoticon.downloadThumb2Cache(new ResultHandlerInterface() {
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

}

class ResultEmoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private boolean allLoaded = false;

    public ResultEmoAdapter(Context context){
        this.context = context;
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons){
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.search_result_emo_item,parent,false);
        return new ResultEmoHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ResultEmoHolder holder = (ResultEmoHolder)viewHolder;
        Emoticon emoticon = emoticons.get(position);
        if(emoticon.getDownloadStatus() == Image.DownloadStatus.fail){
            holder.imageView.setImageResource(R.drawable.load_fail);
        }else if(emoticon.getThumbPath()!=null){
            holder.imageView.displayFile(emoticon.getThumbPath());
        }
        if (position % 4 == 3) {
            holder.mainBack.setBackgroundResource(R.drawable.emoticon_grid_item_background_5);
        } else {
            holder.mainBack.setBackgroundResource(R.drawable.emoticon_grid_item_background);
        }
    }

    @Override
    public int getItemCount() {
        return emoticons.size();
    }

    public boolean isAllLoaded() {
        return allLoaded;
    }

    public void setAllLoaded(boolean allLoaded) {
        this.allLoaded = allLoaded;
    }

    class ResultEmoHolder extends RecyclerView.ViewHolder{
        SpImageView imageView;
        View mainBack;
        public ResultEmoHolder(View itemView) {
            super(itemView);
            mainBack = itemView.findViewById(R.id.main_back);
            imageView = (SpImageView) itemView.findViewById(R.id.result_emo);
            imageView.setHeightRatio(1f);
        }
    }
}