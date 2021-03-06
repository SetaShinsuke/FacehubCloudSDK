package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Author;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.DownloadFrameBtn;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.getApi;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/5/12.
 * 作者主页
 */
public class AuthorActivity extends BaseActivity {
    //    private String authorName;
    private ListView listView; //TODO:改用RecyclerView
    private View header;
    private AuthorListAdapter adapter;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private final int LIMIT_PER_PAGE = 15;
    Author author;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

//        setStatusBarColor(getApi().getActionbarColor());
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try {
            String authorName = getIntent().getExtras().getString("author_name");
            author = getApi().getAuthorContainer().getUniqueAuthorByName(authorName);
        }catch (Exception e){
            LogX.e("启动作者页出错 : " + e);
            finish();
            return;
        }
        if(author==null){
            finish();
            return;
        }

        if(author.getName()==null || author.getName().equals("")) {
            actionbar.setTitle("作者主页");
        }else {
            actionbar.setTitle("" + author.getName());
        }

        header = LayoutInflater.from(this).inflate(R.layout.author_header,null);
        ((TextView)header.findViewById(R.id.author_name)).setText(author.getName());
        ((TextView)header.findViewById(R.id.author_description)).setText(author.getDescription());
        fastLog("Author page , author banner : " + author.getAuthorBanner());
        listView = (ListView) findViewById(R.id.list_view_author);
        assert listView != null;
        listView.addHeaderView(header);

        adapter = new AuthorListAdapter(this);
        listView.setAdapter(adapter);
        loadNextPage();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(isAllLoaded||isLoadingNext){
                    return;
                }
                fastLog("最后一个可见位置 : " + (firstVisibleItem+visibleItemCount) );
                if(firstVisibleItem+visibleItemCount>=(adapter.getCount()-1)){
                    LogX.w("加载下一页");
                    loadNextPage();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        downloadAuthorBanner();

        EventBus.getDefault().register(this);
    }

    private void downloadAuthorBanner(){
        if(author.getAuthorBanner()==null ){
            return;
        }
        LogX.d("下载author banner : " + author.getAuthorBanner());
        author.downloadAuthorBanner(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                LogX.d("Author banner下载完毕 : " + author.getAuthorBanner());
//                ((SpImageView)header.findViewById(R.id.background_image)).displayFile(author.getAuthorBanner().getFullPath());
                ((SpImageView)header.findViewById(R.id.background_image))
                        .displayFile(((File)response).getAbsolutePath());
                listView.forceLayout();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                LogX.e("作者页下载banner出错 : " + e);
            }
        });
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

    private int currentPage = 0;
    private boolean isLoadingNext = false;
    private boolean isAllLoaded = false;

    private void loadNextPage(){
        if(isAllLoaded){
            LogX.w("全部加载完了.");
        }
        if(isAllLoaded || isLoadingNext){
            return;
        }
        isLoadingNext = true;
        ArrayList<String> tags = new ArrayList<>();
//        tags.add(authorName); //接真实作者数据
//        tags.add("热门");
        tags.add(author.getName());
        getApi().getPackagesByTags(tags, currentPage+1, LIMIT_PER_PAGE, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                LogX.fastLog("author init data : " + response);
                ArrayList<EmoPackage> result = (ArrayList<EmoPackage>) response;
                if(result.size()==0 || result.size()<LIMIT_PER_PAGE){
                    setAllLoaded(true);
                }else {
                    setAllLoaded(false);
                }
                emoPackages.addAll(result);

                adapter.setEmoPackages(emoPackages);
                currentPage++;
                isLoadingNext = false;

                //下载封面图
                for(int i=0;i<result.size();i++){
                    final EmoPackage emoPackage = result.get(i);
                    emoPackage.downloadCover(new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            adapter.notifyDataSetChanged();
//                            for(int i=0;i<emoPackages.size();i++) {
//                                if(emoPackage.getId().equals(emoPackages.get(i).getId())) {
//                                    moreAdapter.notifyItemChanged(i);
//                                }
//                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            LogX.e("作者页封面下载失败 : " + e);
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                LogX.e("作者个人页拉取包失败 : " + e);
                isLoadingNext = false;
            }
        });
        adapter.setEmoPackages(emoPackages);
    }

    private void setAllLoaded(boolean isAllLoaded){
        this.isAllLoaded = isAllLoaded;
//        adapter.setAllLoaded(isAllLoaded);
    }

    public void onEvent(DownloadProgressEvent event){
        adapter.notifyDataSetChanged();

//        LogX.d(Constants.PROGRESS,"more on event 进度 : " + event.percentage);
//        for(int i=0;i<emoPackages.size();i++) {
//            if(event.listId.equals(emoPackages.get(i).getId())) {
//                adapter.notifyDataSetChanged();
////                fastLog("notify " + i + " changed.");
//            }
//        }
    }

    public void onEvent(PackageCollectEvent event){
        adapter.notifyDataSetChanged();
//        for(int i=0;i<emoPackages.size();i++) {
//            if(event.listId.equals(emoPackages.get(i).getId())) {
//                moreAdapter.notifyItemChanged(i);
//                fastLog("包收藏成功 : notify " + i + " changed.");
//            }
    }

    public void onEvent(ExitViewsEvent exitViewsEvent){
        finish();
    }
}

class AuthorListAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
//    private Drawable downloadBtnDrawable;

    public AuthorListAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setEmoPackages(ArrayList<EmoPackage> emoPackages){
        this.emoPackages = emoPackages;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
//        return 20;
        return emoPackages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView==null){
            holder = new Holder();
            convertView = layoutInflater.inflate(R.layout.author_list_item,parent,false);
            holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
            holder.emoPackageName = (TextView) convertView.findViewById(R.id.emo_package_name);
            holder.downloadFrameBtn = (DownloadFrameBtn) convertView.findViewById(R.id.download_btn_area);
            holder.divider = convertView.findViewById(R.id.divider);
            holder.left0 = convertView.findViewById(R.id.left0);
            holder.right0 = convertView.findViewById(R.id.right0);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        holder.divider.setVisibility(View.VISIBLE);
        if(position==getCount()-1){
            holder.divider.setVisibility(View.GONE);
        }

        final EmoPackage emoPackage = emoPackages.get(position);
        if (emoPackage.isCollecting()) {
            holder.showProgressBar(emoPackage.getPercent());
        } else {
            if (emoPackage.isCollected()) {
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
                bundle.putString("package_id", emoPackage.getId());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        };
        holder.left0.setOnClickListener(listener);

        final Holder finalHolder = holder;
        holder.right0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (emoPackage.isCollecting() || emoPackage.isCollected()) {
                    return;
                }
                //emoPackage.setIsCollecting(true);
                finalHolder.showProgressBar(0f);
                emoPackage.collect(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
//                                    notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText( v.getContext() , "网络连接失败，请稍后重试", Toast.LENGTH_SHORT).show();
//                                    notifyDataSetChanged();
                    }
                });

            }
        });

        holder.emoPackageName.setText(emoPackage.getName());
        if(emoPackage.getCover()!=null && emoPackage.getCover().getDownloadStatus()== Image.DownloadStatus.fail){
            holder.coverImage.setImageResource(R.drawable.load_fail);
        }else {
            if (emoPackage.getCover() != null && emoPackage.getCover().getThumbPath() != null) {
                holder.coverImage.displayFile(emoPackage.getCover().getThumbPath());
            } else {
                LogX.w("position " + position + "\n封面为空 , path: " + emoPackage.getCover().getThumbPath());
                holder.coverImage.displayFile(null);
            }
        }


        return convertView;
    }

    class Holder{
        SpImageView coverImage;
        TextView emoPackageName ; //,downloadText;
        View divider,left0,right0;
        private DownloadFrameBtn downloadFrameBtn;

        public void showDownloaded(){
            downloadFrameBtn.showDownloaded();
        }
        public void showDownloadBtn(){
            downloadFrameBtn.showDownloadBtn();
        }
        public void showProgressBar(final float percent){
            downloadFrameBtn.showProgressBar(percent);
        }
    }
}
