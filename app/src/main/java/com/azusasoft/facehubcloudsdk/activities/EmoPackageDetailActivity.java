package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HeaderGridView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.models.EmoPackage.DownloadStatus.DOWNLOADING;
import static com.azusasoft.facehubcloudsdk.api.models.EmoPackage.DownloadStatus.NONE;
import static com.azusasoft.facehubcloudsdk.api.models.EmoPackage.DownloadStatus.SUCCESS;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/28.
 */
public class EmoPackageDetailActivity extends AppCompatActivity {
    private Context context;
    private EmoPackage emoPackage;
    HeaderGridView emoticonGrid;
    private DetailAdapter detailAdapter;
    private View headerWithBackground, headerNoBackground;
    FacehubAlertDialog alertDialog;

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

        alertDialog = (FacehubAlertDialog) findViewById(R.id.alert_dialog);
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
        emoticonGrid.setAdapter(detailAdapter);

        loadData();
        if (getIntent().getExtras() != null) {
            String packId = getIntent().getExtras().getString("package_id");
            FacehubApi.getApi().getPackageDetailById(packId, new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    emoPackage = (EmoPackage) response;
                    loadData();
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }

    }

    private View downloadBtn, downloadIcon;
    private TextView downloadText;
    private View progressBar;

    private void loadData() {
        if (emoPackage == null) {
            emoticonGrid.setVisibility(View.GONE);
            return;
        }
        emoticonGrid.setVisibility(View.VISIBLE);
        View header;
//        if (emoPackage.getBackground() == null) {
        if (emoPackage.getAuthorName() == null || emoPackage.getAuthorName().equals("")) {
            header = headerNoBackground;
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
        progressBar = header.findViewById(R.id.progress);
        //TODO:根据下载状态设置按钮
        downloadBtn.setBackgroundColor(getResources().getColor(R.color.facehub_color));
        downloadIcon.setVisibility(View.VISIBLE);
        downloadText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        downloadText.setText("下载");

        final int grey = Color.parseColor("#d0d0d0");
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emoPackage.getDownloadStatus() == NONE) {
                    fastLog("开始下载");
                    //TODO:开始下载
                    downloadBtn.setBackgroundColor(grey);
                    downloadIcon.setVisibility(View.GONE);
                    downloadText.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    emoPackage.setDownloadStatus(DOWNLOADING);
                } else if (emoPackage.getDownloadStatus() == DOWNLOADING) {
                    //结束下载
                    fastLog("结束下载");
                    downloadBtn.setBackgroundColor(grey);
                    downloadIcon.setVisibility(View.GONE);
                    downloadText.setVisibility(View.VISIBLE);
                    downloadText.setText("已下载");
                    progressBar.setVisibility(View.GONE);
                    emoPackage.setDownloadStatus(SUCCESS);
                }
            }
        });
        detailAdapter.setEmoticons(emoPackage.getEmoticons());

        emoticonGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position>emoPackage.getEmoticons().size()-1) {
                    return;
                }
                Emoticon emoticon = emoPackage.getEmoticons().get(position);
                fastLog("预览表情 : " + emoticon);
                //TODO:预览表情
            }
        });
    }

    private void setBackgroundImage(){
//        if(emoPackage==null || emoPackage.getBackground()==null){
//            return;
//        }
        //todo:设置背景图
        ((SpImageView) headerWithBackground.findViewById(R.id.background_image)).setImageResource(R.drawable.banner_demo);
    }

}

class DetailAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();

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
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.detail_grid_item,parent,false);
            holder = new Holder();
            holder.imageView = (SpImageView) convertView.findViewById(R.id.image_view);
            holder.imageView.setHeightRatio(1f);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        Emoticon emoticon = emoticons.get(position);
        convertView.setClickable(false);
        return convertView;
    }

    class Holder{
        SpImageView imageView;
    }
}
