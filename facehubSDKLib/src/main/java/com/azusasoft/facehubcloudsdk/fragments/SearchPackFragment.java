package com.azusasoft.facehubcloudsdk.fragments;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/7/6.
 */
public class SearchPackFragment extends BaseFragment {
    private final int LIMIT_PER_PAGE = 10;

    private RecyclerViewEx recyclerView;
    private ResultPackAdapter adapter;
    private View noResult;
    ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private int currentPage = 1;
    private boolean allLoaded = true;

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_result,null);
        view.setBackgroundColor(0);
        noResult = view.findViewById(R.id.no_result);
        recyclerView = (RecyclerViewEx) view.findViewById(R.id.result_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        adapter = new ResultPackAdapter(getActivity());
        adapter.setEmoPackages(emoPackages);
        recyclerView.setAdapter(adapter);
        noResult.setVisibility(View.GONE);
        return view;
    }

    public void search(String keyword){
        emoPackages.clear();
        setAllLoaded(false);
        final ArrayList<String> tags = new ArrayList<>();
        tags.add("二次元");
        FacehubApi.getApi().getPackagesByTags(tags, currentPage , LIMIT_PER_PAGE, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                emoPackages.addAll((ArrayList<EmoPackage>)response);
                adapter.setAllLoaded(true);
            }

            @Override
            public void onError(Exception e) {
                LogX.e("搜索出错 : " + e);
            }
        });
    }

    public void refresh(){
        adapter.notifyDataSetChanged();
    }

    public void setAllLoaded(boolean allLoaded){
        adapter.setAllLoaded(allLoaded);
    }
}

class ResultPackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private final int TYPE_NORMAL = 0;
    private final int TYPE_LOADING = 1;
    private boolean allLoaded = true;

    public ResultPackAdapter(Context context){
        this.context = context;
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
        return emoPackages.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==getItemCount()-1){
            return TYPE_LOADING;
        }
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View convertView;
        switch (viewType){
            case TYPE_NORMAL:
                convertView = layoutInflater.inflate(R.layout.search_result_pack_item,parent,false);
                return new ResultPackHolder(convertView);
            case TYPE_LOADING:
                convertView = layoutInflater.inflate(R.layout.loading_footer,parent,false);
                return new LoadingHolder(convertView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)){
            case TYPE_NORMAL:
                ResultPackHolder holder = (ResultPackHolder) viewHolder;
                EmoPackage emoPackage = emoPackages.get(position);
                holder.listName.setText(emoPackage.getName());
                holder.listSubtitle.setText(emoPackage.getSubTitle());
                break;
            case TYPE_LOADING:
                viewHolder.itemView.setVisibility(View.VISIBLE);
                if(isAllLoaded()){
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

    class ResultPackHolder extends RecyclerView.ViewHolder{
        SpImageView coverView;
        TextView listName, listSubtitle;
        View right0;
        CollectProgressBar progressBar;
        public ResultPackHolder(View itemView) {
            super(itemView);
            coverView = (SpImageView) itemView.findViewById(R.id.cover_image);
            listName = (TextView) itemView.findViewById(R.id.list_name);
            listSubtitle = (TextView) itemView.findViewById(R.id.list_subtitle);
            right0 = itemView.findViewById(R.id.right0);
            progressBar = (CollectProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder{
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }
}
