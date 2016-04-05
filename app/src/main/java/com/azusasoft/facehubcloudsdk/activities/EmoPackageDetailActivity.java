package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
//import com.azusasoft.facehubcloudsdk.api.CollectProgressListener;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HeaderGridView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.Preview;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/28.
 */
public class EmoPackageDetailActivity extends AppCompatActivity {
    private Context context;
    private EmoPackage emoPackage;
    private Preview preview;
    HeaderGridView emoticonGrid;
    private DetailAdapter detailAdapter;
    private View headerWithBackground, headerNoBackground;
    FacehubAlertDialog alertDialog;

    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        context = this;
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color, getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar);
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logText = (TextView) findViewById(R.id.log_text);

        alertDialog = (FacehubAlertDialog) findViewById(R.id.collect_dialog);
        preview = (Preview) findViewById(R.id.preview);
        emoticonGrid = (HeaderGridView) findViewById(R.id.emoticon_grid);
        headerWithBackground = LayoutInflater.from(context).inflate(R.layout.detail_header_background, null);
        headerNoBackground = LayoutInflater.from(context).inflate(R.layout.detail_header_no_background, null);
        headerWithBackground.setVisibility(View.GONE);
        headerNoBackground.setVisibility(View.GONE);
        emoticonGrid.addHeaderView(headerWithBackground);
        emoticonGrid.addHeaderView(headerNoBackground);
        headerNoBackground.setOnClickListener(null);
        headerWithBackground.setOnClickListener(null);

        detailAdapter = new DetailAdapter(context);
        detailAdapter.setPreview(preview);
        emoticonGrid.setAdapter(detailAdapter);

        loadData();
        if (getIntent().getExtras() != null) {
            String packId = getIntent().getExtras().getString("package_id");
            FacehubApi.getApi().getPackageDetailById(packId, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    emoPackage = (EmoPackage) response;
                    loadData();
                    //TODO:下载作者头像
//                    emoPackage.setCollectProgressListener(new CollectProgressListener() {
//                        @Override
//                        public void onProgressChange(float percent) {
//                            logText.setText("下载进度 : " + (percent*100) + " %" );
//                        }
//                    });
                }

                @Override
                public void onError(Exception e) {

                }
            });
            EventBus.getDefault().register(this);
        }

        preview.setCollectEmoticonInterface(new Preview.CollectEmoticonInterface() {
            @Override
            public void onStartCollect(Emoticon emoticon) {
                alertDialog.showCollecting();
            }

            @Override
            public void onCollected(Emoticon emoticon, boolean success) {
                if(success) {
                    alertDialog.showCollectSuccess();
                }else {
                    alertDialog.showCollectFail();
                }
            }
        });
    }

    private View downloadBtn, downloadIcon;
    private TextView downloadText;
    private CollectProgressBar progressBar;

    private void loadData() {
        if (emoPackage == null) {
            emoticonGrid.setVisibility(View.GONE);
            return;
        }
        emoticonGrid.setVisibility(View.VISIBLE);
        final View header;
        if (emoPackage.getBackground() == null) {
            header = headerNoBackground;
            setCover();
        } else {
            header = headerWithBackground;
            ((TextView)header.findViewById(R.id.author_name)).setText("作者: " + emoPackage.getAuthorName());
            setBackgroundImage();
        }
        header.setVisibility(View.VISIBLE);
        ((TextView) header.findViewById(R.id.pack_name)).setText(emoPackage.getName() + "");
        ((TextView) header.findViewById(R.id.pack_description)).setText(emoPackage.getDescription() + "");
        downloadBtn = header.findViewById(R.id.download_btn);
        downloadIcon = header.findViewById(R.id.download_icon);
        downloadText = (TextView) header.findViewById(R.id.download_text);
        progressBar = (CollectProgressBar) header.findViewById(R.id.progress);
        //TODO:根据下载状态设置按钮
        refreshDownloadBtn(header);

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emoPackage.isCollecting() || emoPackage.isCollected()){
                    return;
                }
                fastLog("开始下载");
                //TODO:开始下载
                emoPackage.collect(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        logText.setText("下载完成.");
                        fastLog("下载完成");
                        refreshDownloadBtn(header);
                    }

                    @Override
                    public void onError(Exception e) {
                        fastLog("下载失败 : " + e);
                    }
                });
                refreshDownloadBtn(header);
            }
        });
        detailAdapter.setEmoticons(emoPackage.getEmoticons());

        //下载表情
        for(int i=0;i<emoPackage.getEmoticons().size();i++){
            Emoticon emoticon = emoPackage.getEmoticons().get(i);
            emoticon.download2Cache(Image.Size.FULL, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    detailAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
        //todo:下载作者详情
        preview.setAuthor(null,emoPackage.getAuthorName());
    }

    private void refreshDownloadBtn(View header){
        if(emoPackage==null){
            return;
        }
        downloadBtn = header.findViewById(R.id.download_btn);
        downloadIcon = header.findViewById(R.id.download_icon);
        downloadText = (TextView) header.findViewById(R.id.download_text);
        progressBar = (CollectProgressBar) header.findViewById(R.id.progress);

        if(emoPackage.isCollecting()){ //下载中，显示进度条
            downloadBtn.setBackgroundColor(Color.parseColor("#eeeeee"));
            downloadIcon.setVisibility(View.GONE);
            downloadText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }else if(emoPackage.isCollected()){ //已下载，显示已下载
            downloadBtn.setBackgroundColor(Color.parseColor("#d0d0d0"));
            downloadIcon.setVisibility(View.GONE);
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("已下载");
            progressBar.setVisibility(View.GONE);
        }else{ //未下载，显示下载按钮
            downloadBtn.setBackgroundColor( getResources().getColor(R.color.facehub_color) );
            downloadIcon.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("下载中");
            progressBar.setVisibility(View.GONE);
        }
    }

    public void onEvent(DownloadProgressEvent event){
        if(progressBar!=null){
            progressBar.setPercentage(event.percentage);
        }
    }

    private void setCover(){
        if(emoPackage==null || emoPackage.getCover()==null){
            return;
        }
        emoPackage.downloadCover(Image.Size.FULL, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                ((SpImageView)headerNoBackground.findViewById(R.id.cover_image))
                        .displayFile(emoPackage.getCover().getFilePath(Image.Size.FULL));
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void setBackgroundImage(){
        if(emoPackage==null || emoPackage.getBackground()==null){
            return;
        }
        //todo:设置背景图
        emoPackage.downloadBackground(Image.Size.FULL, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                ((SpImageView) headerWithBackground.findViewById(R.id.background_image))
                        .displayFile(emoPackage.getBackground().getFilePath(Image.Size.FULL));
            }

            @Override
            public void onError(Exception e) {

            }
        });
//        ((SpImageView) headerWithBackground.findViewById(R.id.background_image)).setImageResource(R.drawable.banner_demo);
    }

}

class DetailAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private Preview preview;

    public DetailAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return emoticons.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.detail_grid_item,parent,false);
            holder = new Holder();
            holder.imageView = (SpImageView) convertView.findViewById(R.id.image_view);
            holder.imageView.setHeightRatio(1f);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        final Emoticon emoticon = emoticons.get(position);
        holder.imageView.displayFile(emoticon.getFilePath(Image.Size.FULL));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fastLog("预览表情 : " + emoticon + "\nposition : " + position);
                preview.show(emoticon);
            }
        });
        return convertView;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    class Holder{
        SpImageView imageView;
    }
}