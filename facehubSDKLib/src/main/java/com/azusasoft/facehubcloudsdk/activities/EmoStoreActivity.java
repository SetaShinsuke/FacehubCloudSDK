package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Banner;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.BannerView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HorizontalListView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/23.
 * 此处的分页加载是指 {@link Section} 的分页
 */
public class EmoStoreActivity extends AppCompatActivity {
    private static final int LIMIT_PER_PAGE = 8; //每次拉取的分区个数
    private static final int LIMIT_PER_SECTION = 8; //每个分区显示的包的个数

    private Context context;
    private RecyclerView recyclerView;
    private SectionAdapter sectionAdapter;
    private int currentPage = 0; //已加载的tags的页数
    private boolean isAllLoaded = false;
    private boolean isLoadingNext = false;
    private ArrayList<Section> sections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoticon_store);
        context = this;
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }

        this.sections = StoreDataContainer.getDataContainer().getSections();
        sections.clear();

        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        actionbar.showSettings();
        actionbar.setTitle("面馆表情");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionbar.setOnSettingsClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListsManageActivity.class);
                context.startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_facehub);
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        sectionAdapter = new SectionAdapter(context);
        sectionAdapter.setSections(sections);
        recyclerView.setAdapter(sectionAdapter);
        //滚动加载
        isLoadingNext = true;
        FacehubApi.getApi().getPackageTagsByParam("tag_type=custom",new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                ArrayList responseArray = (ArrayList) response;
                for (Object obj : responseArray) {
                    if (obj instanceof String) {
                        Section section = new Section();
                        section.setTagName((String) obj);
                        sections.add(section);
//                        tags.add( (String)obj );
                    }
                }
//                sectionAdapter.notifyDataSetChanged();
                sectionAdapter.smartNotify();
                loadNextPage();
            }

            @Override
            public void onError(Exception e) {
                LogX.e("Error gettingTags : " + e);
            }
        });

        //Banner
        View bView = LayoutInflater.from(context).inflate(R.layout.banner_layout, recyclerView, false);
        final BannerView bannerView = (BannerView) bView.findViewById(R.id.banner_view_facehub);
        sectionAdapter.setBannerView(bView);
        FacehubApi.getApi().getBanners(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                ArrayList<Banner> banners = (ArrayList<Banner>) response;
                bannerView.setBanners(banners);
            }

            @Override
            public void onError(Exception e) {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                if(layoutManager.findLastVisibleItemPosition()>=(sectionAdapter.getItemCount()-1)){
                    if(!isLoadingNext && !isAllLoaded) {
                        loadNextPage();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
        sectionAdapter.setAllLoaded(isAllLoaded);
    }

    //继续拉取section
    private void loadNextPage() {
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
                    ArrayList responseArray = (ArrayList) response;
                    section.getEmoPackages().clear();
                    for (Object obj : responseArray) {
                        if (obj instanceof EmoPackage) {
                            EmoPackage emoPackage = (EmoPackage) obj;
                            section.getEmoPackages().add(emoPackage);
                        }
                    }
//                    sectionAdapter.notifyDataSetChanged();
                    sectionAdapter.smartNotify();
//                    currentPage++;
                    isLoadingNext = false;
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
        currentPage++;
    }

}

/**
 * ------------------------------------------------------------------
 **/
class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_BANNER = 0;
    private static final int TYPE_SECTION = 1;
    private static final int TYPE_FOOTER = 2;

    private Context context;
    private LayoutInflater layoutInflater;
    ArrayList<Section> sections = new ArrayList<>();
    private boolean isAllLoaded = false;
    private View bannerView;

    public SectionAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setSections(ArrayList<Section> sections) {
        this.sections = sections;
//        notifyDataSetChanged();
        smartNotify();
    }

    public void setAllLoaded(boolean isAllLoaded) {
        this.isAllLoaded = isAllLoaded;
//        notifyDataSetChanged();
        smartNotify();
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
                drawable.setColorFilter(new
                        PorterDuffColorFilter( FacehubApi.getApi().getThemeColor() , PorterDuff.Mode.MULTIPLY));

                sectionHolder.indexListView = (HorizontalListView) convertView.findViewById(R.id.section_index);
                sectionHolder.indexListView.setHasFixedSize(true);
                ((SimpleItemAnimator)sectionHolder.indexListView.getItemAnimator()).setSupportsChangeAnimations(false);
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
        } else if (position > sections.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_SECTION;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BANNER:

                break;
            case TYPE_SECTION:
                final Section section = sections.get(position-1);
                SectionHolder sectionHolder = (SectionHolder)viewHolder;
                sectionHolder.tagName.setText(section.getTagName());
                SectionIndexAdapter adapter = sectionHolder.indexAdapter;
                adapter.setEmoPackages(section.getEmoPackages());
                //sectionHolder.indexListView.setAdapter(adapter);
                sectionHolder.indexAdapter.notifyDataSetChanged();
                sectionHolder.section = section;
                break;
            case TYPE_FOOTER:
                LoadingHolder loadingHolder = (LoadingHolder)viewHolder;
                loadingHolder.itemView.setVisibility(View.VISIBLE);
                if(isAllLoaded){
                    loadingHolder.itemView.setVisibility(View.GONE);
                }
                break;
        }
    }

    private Handler handler = new Handler();
    private Runnable notifyTask;
    public void smartNotify(){
        handler.removeCallbacks(notifyTask);
        notifyTask = new Runnable() {
            @Override
            public void run() {
//                LogX.fastLog("@notify smartNotify . ");
                notifyDataSetChanged();
            }
        };
        handler.postDelayed(notifyTask,100);
    }

    @Override
    public int getItemCount() {
        return sections.size() + 2;
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
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    class BannerHolder extends RecyclerView.ViewHolder{

        public BannerHolder(View itemView) {
            super(itemView);
        }
    }
}

class SectionIndexAdapter extends RecyclerView.Adapter<SectionIndexAdapter.SectionIndexHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();


    public SectionIndexAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    Handler handler = new Handler();
    Runnable notifyTask;
    public void setEmoPackages(ArrayList<EmoPackage> emoPackagesParam) {
        this.emoPackages = emoPackagesParam;
        smartNotify();
        for(int i=0;i<emoPackages.size();i++){
            EmoPackage emoPackage = emoPackages.get(i);
//                    final int finalI = i;
            emoPackage.downloadCover(Image.Size.FULL, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    smartNotify();
                }

                @Override
                public void onError(Exception e) {
                    LogX.e("商店页封面下载失败 : " + e);
                }
            });
        }
    }


    private void smartNotify(){
        handler.removeCallbacks(notifyTask);
        notifyTask = new Runnable() {
            @Override
            public void run() {
//                LogX.fastLog("@notify smartNotify . ");
                notifyDataSetChanged();
            }
        };
        handler.postDelayed(notifyTask, 100);
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

        if (emoPackage.getCover() != null && emoPackage.getCover().getFilePath(Image.Size.FULL) != null) {
            holder.coverImage.displayFile(emoPackage.getCover().getFilePath(Image.Size.FULL));
        }else {
            holder.coverImage.displayFile(null);
        }
        holder.emoPackage = emoPackage;
    }

    @Override
    public int getItemCount() {
        return emoPackages.size();
//        if(emoPackages.size()<=3){
//            return emoPackages.size();
//        }
//        return 3;
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


