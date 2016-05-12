package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/5/12.
 * 作者主页
 * todo:作者背景图，拉取包
 */
public class AuthorActivity extends AppCompatActivity {
    private String authorName;
    private ListView listView; //TODO:改用RecyclerView
    private AuthorListAdapter adapter;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        assert actionbar != null;
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getExtras() != null) {
            if(getIntent().getExtras().containsKey("author_name")){
                authorName = getIntent().getExtras().getString("author_name");
            }
        }
        actionbar.setTitle("["+authorName+"]的主页");

        View header = LayoutInflater.from(this).inflate(R.layout.author_header,null);
        ((TextView)header.findViewById(R.id.author_name)).setText(authorName);
        listView = (ListView) findViewById(R.id.list_view_author);
        assert listView != null;
        listView.addHeaderView(header);

        adapter = new AuthorListAdapter(this);
        listView.setAdapter(adapter);

        emoPackages = StoreDataContainer.getDataContainer().getEmoPackages();
        adapter.setEmoPackages(emoPackages);

        EventBus.getDefault().register(this);
    }

    public void onEvent(DownloadProgressEvent event){
        adapter.notifyDataSetChanged();

//        LogX.d(Constants.PROGRESS,"more on event 进度 : " + event.percentage);
//        for(int i=0;i<emoPackages.size();i++) {
//            if(event.emoPackageId.equals(emoPackages.get(i).getId())) {
//                adapter.notifyDataSetChanged();
////                fastLog("notify " + i + " changed.");
//            }
//        }
    }

    public void onEvent(PackageCollectEvent event){
        adapter.notifyDataSetChanged();
//        for(int i=0;i<emoPackages.size();i++) {
//            if(event.emoPackageId.equals(emoPackages.get(i).getId())) {
//                moreAdapter.notifyItemChanged(i);
//                fastLog("包收藏成功 : notify " + i + " changed.");
//            }
        }
}

class AuthorListAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<EmoPackage> emoPackages = new ArrayList<>();
    private Drawable downloadBtnDrawable;

    public AuthorListAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            downloadBtnDrawable = context.getDrawable(R.drawable.radius_rectangle_white_frame);
        }else {
            downloadBtnDrawable = context.getResources().getDrawable(R.drawable.radius_rectangle_white_frame);
        }
        downloadBtnDrawable.setColorFilter(new
                PorterDuffColorFilter( FacehubApi.getApi().getThemeColor() , PorterDuff.Mode.MULTIPLY));
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
            holder.downloadText = (TextView) convertView.findViewById(R.id.download_text);
            holder.downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            holder.progressBar = (CollectProgressBar) convertView.findViewById(R.id.progress_bar);
            holder.divider = convertView.findViewById(R.id.divider);
            holder.left0 = convertView.findViewById(R.id.left0);
            holder.right0 = convertView.findViewById(R.id.right0);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        holder.divider.setVisibility(View.GONE);
        if(position==getCount()-1){
            holder.divider.setVisibility(View.VISIBLE);
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
                        Snackbar.make(v, "网络连接失败，请稍后重试", Snackbar.LENGTH_SHORT).show();
//                                    notifyDataSetChanged();
                    }
                });

            }
        });

        holder.emoPackageName.setText(emoPackage.getName());
        if (emoPackage.getCover() != null && emoPackage.getCover().getFilePath(Image.Size.FULL) != null) {
            holder.coverImage.displayFile(emoPackage.getCover().getFilePath(Image.Size.FULL));
        } else {
            LogX.w("position " + position + "\n封面为空 , path: " + emoPackage.getCover().getFilePath(Image.Size.FULL));
            holder.coverImage.displayFile(null);
        }


        return convertView;
    }

    class Holder{
        SpImageView coverImage;
        TextView emoPackageName,downloadText;
        View divider,left0,right0;
        CollectProgressBar progressBar;
        public void showDownloaded(){
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("已下载");
            downloadText.setTextColor(Color.parseColor("#3fa142"));
            downloadText.setBackgroundColor(Color.parseColor("#00ffffff"));
            progressBar.setVisibility(View.GONE);
        }
        public void showDownloadBtn(){
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("下载");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                downloadText.setBackground(downloadBtnDrawable);
            }else {
                downloadText.setBackgroundDrawable(downloadBtnDrawable);
            }
            downloadText.setTextColor( FacehubApi.getApi().getThemeColor() );
            progressBar.setVisibility(View.GONE);
        }
        public void showProgressBar(final float percent){
            downloadText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor( FacehubApi.getApi().getThemeColor() );
            progressBar.setVisibility(View.VISIBLE);
            fastLog("Author页 收藏进度 : " + percent);
            progressBar.setPercentage(percent);
        }
    }
}
