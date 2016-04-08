package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/27.
 */
public class MorePackageActivity extends AppCompatActivity {
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
                if(layoutManager.findLastVisibleItemPosition()>=(moreAdapter.getItemCount()-1)){
                    loadNextPage();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        EventBus.getDefault().register(this);
    }

    public void onEvent(DownloadProgressEvent event){
        moreAdapter.notifyDataSetChanged();

//        fastLog("more on event 进度 : " + event.percentage);
//        for(int i=0;i<emoPackages.size();i++) {
//            if(event.emoPackageId.equals(emoPackages.get(i).getId())) {
//                moreAdapter.notifyItemChanged(i);
//                fastLog("notify " + i + " changed.");
//            }
//        }
    }

    public void onEvent(PackageCollectEvent event){
        moreAdapter.notifyDataSetChanged();
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
//        FacehubApi.getApi().getPackagesByTags(tags, currentPage , LIMIT_PER_PAGE, new ResultHandlerInterface() {
        FacehubApi.getApi().getPackagesByTags(tags, currentPage+1 , LIMIT_PER_PAGE, new ResultHandlerInterface() {
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

                //下载封面图
                for(int i=0;i<responseArray.size();i++){
                    EmoPackage emoPackage = responseArray.get(i);
                    emoPackage.downloadCover(Image.Size.FULL, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            moreAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
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
                moreHolder.left0 = view.findViewById(R.id.left0);
                moreHolder.center0 = view.findViewById(R.id.center0);
                moreHolder.progressBar = (CollectProgressBar) view.findViewById(R.id.progress_bar);
                moreHolder.setOnClick();
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
                moreHolder.listName.setText(emoPackage.getName() + "");
                String subTitle = emoPackage.getSubTitle();
                if(subTitle==null || subTitle.equals("null")) {
                    subTitle = "";
                }
                moreHolder.listSubtitle.setText(subTitle+"");
                if(emoPackage.isCollecting()){
//                    fastLog(position + " 收藏中");
                    moreHolder.showProgressBar(emoPackage.getPercent()*100);
                }else {
                    if(emoPackage.isCollected()){
//                    fastLog(position + "已收藏");
                        moreHolder.showDownloaded();
                    }else {
//                    fastLog(position + "无状态");
                        moreHolder.showDownloadBtn();
                    }
                }
                moreHolder.emoPackage = emoPackage;
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(),EmoPackageDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("package_id",emoPackage.getId());
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                };
                moreHolder.left0.setOnClickListener(listener);
                moreHolder.center0.setOnClickListener(listener);
                if(emoPackage.getCover()!=null && emoPackage.getCover().getFilePath(Image.Size.FULL)!=null){
                    moreHolder.coverImage.displayFile(emoPackage.getCover().getFilePath(Image.Size.FULL));
                }
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
        View left0,center0;
        CollectProgressBar progressBar;
        EmoPackage emoPackage;

        public MoreHolder(View itemView) {
            super(itemView);
        }

        public void setOnClick(){
            downloadBtnArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (emoPackage==null || emoPackage.isCollecting() || emoPackage.isCollected()) {
                        return;
                    }
                    FacehubApi.getApi().getPackageDetailById(emoPackage.getId(), new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            //TODO:显示进度条
                            fastLog("More 开始下载.");
                            emoPackage.collect(new ResultHandlerInterface() {
                                @Override
                                public void onResponse(Object response) {
                                    notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Exception e) {
                                    notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Snackbar.make(v, "网络连接失败，请稍后重试", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        public void showDownloaded(){
            downloadIcon.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            downloadIcon.setImageResource(R.drawable.downloaded_facehub);
            downloadText.setText("已下载");
            downloadText.setTextColor(Color.parseColor("#3fa142"));
            progressBar.setVisibility(View.GONE);
        }
        public void showDownloadBtn(){
            downloadIcon.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            downloadIcon.setImageResource(R.drawable.download_facehub);
            downloadText.setText("下载");
            downloadText.setTextColor(context.getResources().getColor(R.color.facehub_color));
            progressBar.setVisibility(View.GONE);
        }
        public void showProgressBar(final float percent){
            downloadIcon.setVisibility(View.GONE);
            downloadText.setVisibility(View.GONE);
            downloadText.setVisibility(View.GONE);
            downloadIcon.setImageResource(R.drawable.download_facehub);
            downloadText.setText("下载");
            downloadText.setTextColor(context.getResources().getColor(R.color.facehub_color));
            progressBar.setVisibility(View.VISIBLE);
//            progressBar.post(new Runnable() {
//                @Override
//                public void run() {
            fastLog("More 更新进度 : " + percent);
                    progressBar.setPercentage(percent);
//                }
//            });
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder{
        View mainView;
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }
}

