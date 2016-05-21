package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.Preview;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.e;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.v;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.w;

/**
 * Created by SETA on 2016/3/28.
 * <p/>
 * 表情包详情页.
 */
public class EmoPackageDetailActivity extends BaseActivity {
    private Context context;
    private EmoPackage emoPackage;
    private Preview preview;
    GridViewWithHeaderAndFooter emoticonGrid;
    private DetailAdapter detailAdapter;
    private View headerWithBackground, headerNoBackground;
    private View header; //实际显示的那个header
    private View footer;
    private NoNetView noNetView;
    FacehubAlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        context = this;
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        alertDialog = (FacehubAlertDialog) findViewById(R.id.collect_dialog_facehub);
        preview = (Preview) findViewById(R.id.preview_facehub);
        emoticonGrid = (GridViewWithHeaderAndFooter) findViewById(R.id.emoticon_grid_facehub);
        headerWithBackground = LayoutInflater.from(context).inflate(R.layout.detail_header_background, null);
        headerNoBackground = LayoutInflater.from(context).inflate(R.layout.detail_header_no_background, null);
        headerWithBackground.setVisibility(View.GONE);
        headerNoBackground.setVisibility(View.GONE);

        footer = LayoutInflater.from(context).inflate(R.layout.detail_author_footer, null);

        View view = headerWithBackground.findViewById(R.id.background_image_holder);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = (int) (ViewUtilMethods.getScreenWidth(this) * 296f / 750);
        view.setLayoutParams(params);
        emoticonGrid.addHeaderView(headerWithBackground);
        emoticonGrid.addHeaderView(headerNoBackground);
        emoticonGrid.addFooterView(footer);
        headerNoBackground.setOnClickListener(null);
        headerWithBackground.setOnClickListener(null);

        detailAdapter = new DetailAdapter(context);
        detailAdapter.setPreview(preview);
        emoticonGrid.setAdapter(detailAdapter);

        noNetView = (NoNetView) findViewById(R.id.no_net);
        assert noNetView != null;
        noNetView.setOnReloadClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int netType = NetHelper.getNetworkType(context);
                        if (netType == NetHelper.NETTYPE_NONE) {
                            LogX.w("商店页 : 网络不可用!");
                            noNetView.show();
                        } else if (getIntent().getExtras() != null) {
                            String packId = getIntent().getExtras().getString("package_id");
                            initData(packId);
                        }
                    }
                }, 1000);
                noNetView.hide();
            }
        });

        loadData();
        if (getIntent().getExtras() != null) {
            String packId = getIntent().getExtras().getString("package_id");
            initData(packId);
        }

        preview.setCollectEmoticonInterface(new Preview.CollectEmoticonInterface() {
            @Override
            public void onStartCollect(Emoticon emoticon) {
                alertDialog.showCollecting();
            }

            @Override
            public void onCollected(Emoticon emoticon, boolean success) {
                if (success) {
                    alertDialog.showCollectSuccess();
                } else {
                    alertDialog.showCollectFail();
                }
            }
        });
        EventBus.getDefault().register(this);
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

    private void initData(String packId) {
        int netType = NetHelper.getNetworkType(this);
        if (netType == NetHelper.NETTYPE_NONE) {
            LogX.w("商店页 : 网络不可用!");
            noNetView.show();
            return;
        }
        FacehubApi.getApi().getPackageDetailById(packId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                emoPackage = (EmoPackage) response;
                loadData();
            }

            @Override
            public void onError(Exception e) {
                LogX.e("详情页拉取详情出错 : " + e);
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
        if (emoPackage.getBackground() == null) {
            header = headerNoBackground;
            setCover();
        } else {
            header = headerWithBackground;
            if(emoPackage.getAuthorName()!=null) {
                ((TextView) header.findViewById(R.id.author_name)).setText("作者: " + emoPackage.getAuthorName());
            }else {
                ((TextView) header.findViewById(R.id.author_name)).setText("");
            }
            emoticonGrid.setVisibility(View.GONE);
            setBackgroundImage();
        }
        header.setVisibility(View.VISIBLE);
        ((TextView) header.findViewById(R.id.pack_name)).setText(emoPackage.getName() + "");
        String description = emoPackage.getDescription();
        if (description == null || description.equals("null")) {
            description = "";
        }
        ((TextView) header.findViewById(R.id.pack_description)).setText(description + "");
        downloadBtn = header.findViewById(R.id.download_btn);
        downloadIcon = header.findViewById(R.id.download_icon);
        downloadText = (TextView) header.findViewById(R.id.download_text);
        progressBar = (CollectProgressBar) header.findViewById(R.id.progress);
        //根据下载状态设置按钮
        refreshDownloadBtn(header);

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emoPackage.isCollecting() || emoPackage.isCollected()) {
                    return;
                }
                fastLog("开始下载Detail表情");
                emoPackage.collect(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        fastLog("下载完成");
                        refreshDownloadBtn(header);
                    }

                    @Override
                    public void onError(Exception e) {
                        LogX.e("表情包下载失败 : " + e);
                    }
                });
                refreshDownloadBtn(header);
            }
        });
        detailAdapter.setEmoticons(emoPackage.getEmoticons());

        //下载表情
        for (int i = 0; i < emoPackage.getEmoticons().size(); i++) {
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
        //下载作者详情
        ((TextView) footer.findViewById(R.id.author_name)).setText(emoPackage.getAuthorName());
        String authorName = emoPackage.getAuthorName();
        if(authorName==null || authorName.equals("")){
            authorName = emoPackage.getName();
        }
        preview.setAuthor(null, authorName);
        final String finalAuthorName = authorName;
        fastLog("finalAuthorName : " + finalAuthorName);
        emoPackage.downloadAuthorAvatar(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                SpImageView avatarView = (SpImageView) footer.findViewById(R.id.author_head);
                String path = emoPackage.getAuthorAvatar().getFilePath(Image.Size.FULL);
                avatarView.displayCircleImage(path);
                preview.setAuthor(path, finalAuthorName);
            }

            @Override
            public void onError(Exception e) {
                LogX.e("详情页，作者头像下载失败 : " + e);
            }
        });

        View.OnClickListener onAuthorClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到作者个人页
                if (emoPackage.getAuthorName() == null) {
                    return;
                }
                Intent intent = new Intent(v.getContext(), AuthorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("author_name", emoPackage.getAuthorName());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        };
        footer.findViewById(R.id.author_detail).setOnClickListener(onAuthorClick);
        footer.findViewById(R.id.author_name).setOnClickListener(onAuthorClick);
        footer.findViewById(R.id.author_head).setOnClickListener(onAuthorClick);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到投诉页
                Resources resources = v.getResources();
                Intent intent = new Intent(v.getContext(), WebActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", resources.getString(R.string.agreement_complain));
                bundle.putString("web_url", resources.getString(R.string.agreement_url));
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        };
        footer.findViewById(R.id.agreement).setOnClickListener(onClickListener);
        footer.findViewById(R.id.complain).setOnClickListener(onClickListener);

    }

    private void refreshDownloadBtn(View header) {
        if (emoPackage == null) {
            return;
        }
        downloadBtn = header.findViewById(R.id.download_btn);
        downloadIcon = header.findViewById(R.id.download_icon);
        downloadText = (TextView) header.findViewById(R.id.download_text);
        progressBar = (CollectProgressBar) header.findViewById(R.id.progress);

        if (emoPackage.isCollecting()) { //下载中，显示进度条
            downloadBtn.setBackgroundColor(Color.parseColor("#eeeeee"));
            downloadIcon.setVisibility(View.GONE);
            downloadText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else if (emoPackage.isCollected()) { //已下载，显示已下载
            downloadBtn.setBackgroundColor(Color.parseColor("#d0d0d0"));
            downloadIcon.setVisibility(View.GONE);
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("已下载");
            progressBar.setVisibility(View.GONE);
        } else { //未下载，显示下载按钮
            downloadBtn.setBackgroundColor(FacehubApi.getApi().getThemeColor());
            downloadIcon.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            downloadText.setText("下载");
            progressBar.setVisibility(View.GONE);
        }
    }

    public void onEvent(DownloadProgressEvent event) {
        if (progressBar != null && emoPackage != null && event.listId.equals(emoPackage.getId())) {
            progressBar.setPercentage(event.percentage);
        }
    }

    public void onEvent(PackageCollectEvent event) {
        if (header != null && emoPackage != null && event.emoPackageId.equals(emoPackage.getId())) {
            refreshDownloadBtn(header);
        }
    }

    private void setCover() {
        if (emoPackage == null || emoPackage.getCover() == null) {
            LogX.i("Detail页面 : cover为空!");
            return;
        }
        emoPackage.downloadCover(Image.Size.FULL, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                SpImageView spImageView = (SpImageView) headerNoBackground.findViewById(R.id.cover_image);
                spImageView.setHeightRatio(1f);
                spImageView.displayFile(emoPackage.getCover().getFilePath(Image.Size.FULL));
            }

            @Override
            public void onError(Exception e) {
                LogX.e("详情页封面下载失败 : " + e);
            }
        });
    }

    private void setBackgroundImage() {
        if (emoPackage == null || emoPackage.getBackground() == null) {
            return;
        }
        final SpImageView backImage = (SpImageView) headerWithBackground.findViewById(R.id.background_image);
        //设置背景图
        emoPackage.downloadBackground(Image.Size.FULL, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                backImage.displayFile(emoPackage.getBackground().getFilePath(Image.Size.FULL));
                fastLog("background path : " + emoPackage.getBackground().getFilePath(Image.Size.FULL));
                detailAdapter.notifyDataSetChanged();
                emoticonGrid.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                LogX.e("下载包背景图出错 : " + e);
            }
        });
//        ((SpImageView) headerWithBackground.findViewById(R.id.background_image)).setImageResource(R.drawable.banner_demo);
    }

}

/**
 * 表情包详情页的adapter
 */
class DetailAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private Preview preview;
    private int width = 0;

    public DetailAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        Resources res = context.getResources();
        int w = (int) ((ViewUtilMethods.getScreenWidth(context)
                - res.getDimensionPixelSize(R.dimen.detail_grid_margin_sides) * 2) * 1f / 4
                - res.getDimensionPixelSize(R.dimen.detail_grid_item_margin) * 2);
        int w2 = ViewUtilMethods.dip2px(context, 80);
        width = Math.min(w, w2);
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
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.detail_grid_item, parent, false);
            holder = new Holder();
            holder.imageView = (SpImageView) convertView.findViewById(R.id.image_view_facehub);
            holder.leftMargin = convertView.findViewById(R.id.left_margin);
            holder.rightMargin = convertView.findViewById(R.id.right_margin);
            holder.content = convertView.findViewById(R.id.content);
//            holder.imageView.setHeightRatio(1f);
            holder.imageView.setDoResize(false);
            holder.radiusLayout = convertView.findViewById(R.id.radius_layout);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        final Emoticon emoticon = emoticons.get(position);

        ViewGroup.LayoutParams params = holder.content.getLayoutParams();
        params.width = width;
        params.height = width;
        ViewGroup.LayoutParams radiusParams = holder.radiusLayout.getLayoutParams();
//        params.width = width;
//        params.height = width;
        if (emoticon.getWidth() != 0 && emoticon.getHeight() != 0) {
            ViewGroup.LayoutParams imgParams = holder.imageView.getLayoutParams();
            if (emoticon.getWidth() > emoticon.getHeight()) { //宽度较长,已宽度为准
                double ratio = emoticon.getHeight() * 1f / emoticon.getWidth();
                imgParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                imgParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                holder.imageView.setHeightRatio(ratio);
//                radiusParams.height = (int) (width * ratio);
                imgParams.width = width;
                imgParams.height = (int) (width*ratio);
                radiusParams.width = imgParams.width;
                radiusParams.height = imgParams.height;
            } else {
                double ratio = emoticon.getWidth() * 1f / emoticon.getHeight();
                imgParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                imgParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.imageView.setWidthRatio(ratio);
                imgParams.height = width;
                imgParams.width = (int) (width*ratio);
                radiusParams.width = imgParams.width;
                radiusParams.height = imgParams.height;
//                radiusParams.width = (int) (width*ratio);
            }
        }

        holder.imageView.displayFile(emoticon.getFilePath(Image.Size.FULL));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fastLog("预览表情 : " + emoticon + "\nposition : " + position);
                preview.show(emoticon);
            }
        });
        holder.leftMargin.setVisibility(View.GONE);
        holder.rightMargin.setVisibility(View.GONE);
        if (position % 4 == 0) { //第一列
            holder.leftMargin.setVisibility(View.VISIBLE);
        }
        if (position % 4 == 3) { //最后一列
            holder.rightMargin.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    class Holder {
        SpImageView imageView; //,imageViewW,imageViewH;
        View leftMargin, rightMargin, content,radiusLayout;
    }
}