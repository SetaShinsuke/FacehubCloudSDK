package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.views.uiModels.Section;
import com.azusasoft.facehubcloudsdk.views.uiModels.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/27.
 */
public class MorePackageActivity extends AppCompatActivity {
    protected static TextView logText;
    private static final int LIMIT_PER_PAGE = 10; //每次拉取的分区个数
    private Context context;
    private MoreAdapter moreAdapter;
    private FacehubAlertDialog dialog;
    private int currentPage = 0; //已加载的tags的页数
    private boolean isAllLoaded = false;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private String sectionName;
    private boolean isLoadingNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_more_package);
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color, getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }
        logText = (TextView) findViewById(R.id.log);

        dialog = (FacehubAlertDialog) findViewById(R.id.alert_dialog);
        dialog.hide();
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar);
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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        moreAdapter = new MoreAdapter(context);
        recyclerView.setAdapter(moreAdapter);

        sectionName = getIntent().getExtras().getString("section_name");
        actionbar.setTitle(sectionName);
        emoPackages = StoreDataContainer.getDataContainer().getEmoPackagesOfSection(sectionName);
        emoPackages.clear();
        moreAdapter.setEmoPackages(emoPackages);
        loadNextPage();
//        FacehubApi.getApi().getPackagesByTags();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                String s = "first : " + layoutManager.findFirstVisibleItemPosition()
                        + "\nlast : " + layoutManager.findLastVisibleItemPosition()
                        +"\ntotal : " + moreAdapter.getItemCount();
                logText.setText(s);
                if(layoutManager.findLastVisibleItemPosition()>=(moreAdapter.getItemCount()-1)){
                    loadNextPage();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

    }

    private void setAllLoaded(boolean isAllLoaded){
        this.isAllLoaded = isAllLoaded;
        moreAdapter.setAllLoaded(isAllLoaded);
    }

    public void loadNextPage(){ //拉取package
        if(isAllLoaded || isLoadingNext){
            return;
        }
        isLoadingNext = true;
        ArrayList<String> tags = new ArrayList<>();
        tags.add(sectionName);
        FacehubApi.getApi().getPackagesByTags(tags, currentPage, LIMIT_PER_PAGE, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                ArrayList<EmoPackage> responseArray = (ArrayList<EmoPackage>)response;
                if(responseArray.size()==0 || responseArray.size()<LIMIT_PER_PAGE){
                    setAllLoaded(true);
                }else {
                    setAllLoaded(false);
                }
                emoPackages.addAll(responseArray);
                moreAdapter.notifyDataSetChanged();
                currentPage++;
                isLoadingNext = false;
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}

class MoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private final static int TYPE_NORMAL=0;
    private final static int TYPE_LOADING=1;
    private boolean isAllLoaded = false;

    public MoreAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setEmoPackages(ArrayList<EmoPackage> emoPackages){
        this.emoPackages = emoPackages;
        notifyDataSetChanged();
    }

    public void setAllLoaded(boolean isAllLoaded){
        this.isAllLoaded = isAllLoaded;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case TYPE_NORMAL:
                view = layoutInflater.inflate(R.layout.more_item,parent,false);
                MoreHolder moreHolder = new MoreHolder(view);
                moreHolder.coverImage = (SpImageView) view.findViewById(R.id.cover_image);
                moreHolder.listName = (TextView) view.findViewById(R.id.list_name);
                moreHolder.downloadBtnArea = view.findViewById(R.id.download_btn_area);
                moreHolder.listSubtitle = (TextView) view.findViewById(R.id.list_subtitle);
                moreHolder.downloadIcon = (ImageView) view.findViewById(R.id.download_icon);
                moreHolder.downloadText = (TextView) view.findViewById(R.id.download_text);
                moreHolder.coverImage.setHeightRatio(1f);
                return moreHolder;
            case TYPE_LOADING:
                view = layoutInflater.inflate(R.layout.loading_footer,parent,false);
                LoadingHolder loadingHolder = new LoadingHolder(view);
                loadingHolder.mainView = view.findViewById(R.id.main_view);
                return loadingHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case TYPE_NORMAL:
                final MoreHolder moreHolder = (MoreHolder)holder;
                final EmoPackage emoPackage = emoPackages.get(position);
                moreHolder.listName.setText(emoPackage.getName()+"");
                moreHolder.listSubtitle.setText(emoPackage.getSubTitle()+"");
                moreHolder.downloadBtnArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(emoPackage.getDownloadStatus()== EmoPackage.DownloadStatus.NONE){
                            //TODO:开始下载
                            moreHolder.downloadIcon.setImageResource(R.drawable.downloaded_facehub);
                            moreHolder.downloadText.setText("已下载");
                            moreHolder.downloadText.setTextColor(Color.parseColor("#3fa142"));
                        }else if(emoPackage.getDownloadStatus()== EmoPackage.DownloadStatus.SUCCESS){

                        }
                    }
                });
                break;
            case TYPE_LOADING:
                LoadingHolder loadingHolder = (LoadingHolder)holder;
                loadingHolder.mainView.setVisibility(View.VISIBLE);
                if(isAllLoaded){
                    loadingHolder.mainView.setVisibility(View.GONE);
                }
                break;
        }
        //TODO:下载状态&进度条
    }

    @Override
    public int getItemViewType(int position) {
        if(position>emoPackages.size()-1){
            return TYPE_LOADING;
        }else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return emoPackages.size()+1;
    }

    class MoreHolder extends RecyclerView.ViewHolder{
        SpImageView coverImage;
        TextView listName,listSubtitle;
        View downloadBtnArea;
        ImageView downloadIcon;
        TextView downloadText;

        public MoreHolder(View itemView) {
            super(itemView);
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder{
        View mainView;
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }
}
