package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.EmoPackage;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.FacehubSDKException;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.api.utils.NetHelper;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.DataAvailable;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.GridItemSeTouchHelper;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.GridItemTouchListener;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.ScrollTrigger;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.TouchableGridHolder;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.DownloadSolidBtn;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.GifViewFC;
import com.azusasoft.facehubcloudsdk.views.viewUtils.NoNetView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.Preview;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.themeOptions;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

//import com.azusasoft.facehubcloudsdk.api.CollectProgressListener;

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
    private View unAvailableHint;
    private NoNetView noNetView;
    FacehubAlertDialog alertDialog;

    private ViewGroup rootViewGroup,previewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        context = this;
        //通知栏颜色
//        setStatusBarColor(FacehubApi.getApi().getActionbarColor());
        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        actionbar.hideBtns();
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionbar.setTitle(FacehubApi.getApi().getEmoStoreTitle());

        alertDialog = (FacehubAlertDialog) findViewById(R.id.collect_dialog_facehub);
        preview = (Preview) findViewById(R.id.preview_facehub);
        emoticonGrid = (GridViewWithHeaderAndFooter) findViewById(R.id.emoticon_grid_facehub);
        headerWithBackground = LayoutInflater.from(context).inflate(R.layout.detail_header_background, null);
        headerNoBackground = LayoutInflater.from(context).inflate(R.layout.detail_header_no_background, null);
        headerWithBackground.setVisibility(View.GONE);
        headerNoBackground.setVisibility(View.GONE);

        footer = LayoutInflater.from(context).inflate(R.layout.detail_author_footer, null);
        unAvailableHint = findViewById(R.id.unavailable_hint);

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
                if (getIntent().getExtras() != null) {
                    String packId = getIntent().getExtras().getString("package_id");
                    initData(packId);
                }
            }
        });

        loadData();
        if (getIntent().getExtras() != null) {
            String packId = getIntent().getExtras().getString("package_id");
            LogX.fastLog("详情页,package id : " + packId);
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

        initGridTouch();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            LogX.w(getClass().getName() + " || EventBus 反注册出错 : " + e);
        }
    }

    /** ===============================================================================
     * 初始化Grid的触摸
     */
    private void initGridTouch(){
        View activityView = findViewById(android.R.id.content);
        if (activityView instanceof ViewGroup) {
            rootViewGroup = (ViewGroup) activityView;
            this.previewContainer = new FrameLayout(context);
            rootViewGroup.addView(previewContainer);
            LayoutInflater.from(context).inflate(R.layout.keyboard_preview, previewContainer);
            previewContainer.setVisibility(View.GONE);
        }

        GridItemTouchListener gridItemTouchListener = new GridItemTouchListener() {
            @Override
            public void onItemClick(View view, DataAvailable object) {
//                LogX.fastLog("点击Data : " + object);
                Emoticon emoticon = (Emoticon)object;
                preview.show(emoticon);
            }

            @Override
            public void onItemLongClick(View view, DataAvailable data) {
//                LogX.fastLog("长按Data : " + data);
                final Emoticon emoticon = (Emoticon) data;
                if (previewContainer != null) {
                    previewContainer.setVisibility(View.VISIBLE);
                    //预览表情
                    final GifViewFC gifView = (GifViewFC) previewContainer.findViewById(R.id.preview_image);
                    if (gifView == null) {
                        return;
                    }
                    ImageView bubble = (ImageView) previewContainer.findViewById(R.id.preview_bubble);
                    gifView.setVisibility(View.GONE);
                    //显示表情
                    if(emoticon.getFullPath()==null) {
                        emoticon.downloadFull2File(true, new ResultHandlerInterface() {
                            @Override
                            public void onResponse(Object response) {
                                gifView.setGifPath(emoticon.getFullPath());
                                gifView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        gifView.setVisibility(View.VISIBLE);
//                                        fastLog("emoticon path : " + emoticon.getFilePath(Image.Size.FULL));
                                    }
                                }, 200);
                            }

                            @Override
                            public void onError(Exception e) {
                                LogX.e("preview error : " + e);
                            }
                        });
                    }else {
                        gifView.setGifPath(emoticon.getFullPath());
                        gifView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gifView.setVisibility(View.VISIBLE);
                            }
                        }, 200);
                    }

                    int top = ViewUtilMethods.getTopOnWindow(view);
                    int left = ViewUtilMethods.getLeftOnWindow(view);
                    int center = left + (int) (view.getWidth() / 2f);
                    int previewLeft = (int) (center - getResources().getDimensionPixelSize(R.dimen.keyboard_preview_frame_width) / 2f);
                    TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{
                            android.R.attr.actionBarSize
                    });

                    int rootTop = ViewUtilMethods.getTopOnWindow(rootViewGroup);

                    float h = actionbarSizeTypedArray.getDimension(0, 0);
//                    int previewTop  = top - getResources().getDimensionPixelSize(R.dimen.keyboard_preview_frame_height)
//                            - view.getHeight() + getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_padding)
//                            + (int)h;
                    int previewTop = top - getResources().getDimensionPixelSize(R.dimen.keyboard_preview_frame_height)
//                            - view.getHeight()
                            + getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_padding)
//                            + (int)h
                            - rootTop;
//
//                        fastLog("root top : " + rootTop + "\npreview top : " + previewTop);

                    int quarterScreen = (int) (ViewUtilMethods.getScreenWidth(context) / 4f);
                    if (center < quarterScreen) {
                        bubble.setImageResource(R.drawable.preview_frame_left);
                        previewLeft += (int) (view.getWidth() / 2f);
                    } else if (center < quarterScreen * 2) {
                        bubble.setImageResource(R.drawable.preview_frame_center);
                    } else if (center < quarterScreen * 3) {
                        bubble.setImageResource(R.drawable.preview_frame_center);
                    } else {
                        bubble.setImageResource(R.drawable.preview_frame_right);
                        previewLeft -= (int) (view.getWidth() / 2f);
                    }
                    if(previewTop<0){
                        bubble.setImageResource(R.drawable.preview_frame_over);
                        previewTop = 0;
                    }
                    ViewUtilMethods.changeViewPosition(previewContainer, previewLeft, previewTop);
                }
            }

            @Override
            public void onItemOffTouch(View view, DataAvailable object) {
                if (previewContainer != null) {
                    previewContainer.setVisibility(View.GONE);
                }
            }
        };
        ScrollTrigger scrollTrigger = new ScrollTrigger() {
            @Override
            public void setCanScroll(boolean canScroll) {

            }
        };
        GridItemSeTouchHelper gridItemSeTouchHelper = new GridItemSeTouchHelper(context
                ,gridItemTouchListener,scrollTrigger,true,300,0);
        gridItemSeTouchHelper.attachToGridView(emoticonGrid,null);
    }

    private void initData(String packId) {
        unAvailableHint.setVisibility(View.GONE);
        int netType = NetHelper.getNetworkType(this);
        if (netType == NetHelper.NETTYPE_NONE) {
            LogX.w("商店页 : 网络不可用!");
            noNetView.show();
            return;
        }
        LogX.d("DetailActivity init ID : " + packId);
        FacehubApi.getApi().getPackageDetailById(packId, new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                emoPackage = (EmoPackage) response;
                loadData();
            }

            @Override
            public void onError(Exception e) {
                if (e instanceof FacehubSDKException) {
                    if (((FacehubSDKException) e).getErrorType() == FacehubSDKException.ErrorType.emo_package_unavailable) {
                        unAvailableHint.setVisibility(View.VISIBLE);
                    }
                }
                LogX.e("详情页拉取详情出错 : " + e);
            }
        });
    }

//    private View downloadBtn, downloadIcon;
//    private TextView downloadText;
//    private CollectProgressBar progressBar;
    private DownloadSolidBtn downloadSolidBtn;

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
            if (emoPackage.getAuthorName() != null) {
                ((TextView) header.findViewById(R.id.author_name)).setText("作者: " + emoPackage.getAuthorName());
            } else {
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
//        downloadBtn = header.findViewById(R.id.download_btn);
//        downloadIcon = header.findViewById(R.id.download_icon);
//        downloadText = (TextView) header.findViewById(R.id.download_text);
//        progressBar = (CollectProgressBar) header.findViewById(R.id.progress);
        downloadSolidBtn = (DownloadSolidBtn) header.findViewById(R.id.download_solid_btn);

        //根据下载状态设置按钮
        refreshDownloadBtn(header);

        View.OnClickListener onDownloadClick = new View.OnClickListener() {
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
                        refreshDownloadBtn(header);
                    }
                });
                refreshDownloadBtn(header);
            }
        };
//        downloadBtn.setOnClickListener(onDownloadClick);
        downloadSolidBtn.setOnDownloadCLick(onDownloadClick);

        detailAdapter.setEmoticons(emoPackage.getEmoticons());

        //下载表情
        for (int i = 0; i < emoPackage.getEmoticons().size(); i++) {
            final Emoticon emoticon = emoPackage.getEmoticons().get(i);
            final int finalI = i;
            emoticon.downloadThumb2Cache(new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    detailAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    detailAdapter.notifyDataSetChanged();
                }
            });
        }
        //copyright
        String copyright = emoPackage.getCopyright();
        View copyrightView = footer.findViewById(R.id.copyright);
        TextView copyrightContent = (TextView) footer.findViewById(R.id.copyright_text_right);
        copyrightContent.setText(copyright);
        if (copyright == null) {
            copyrightView.setVisibility(View.GONE);
        } else {
            copyrightView.setVisibility(View.VISIBLE);
        }

        //下载作者详情
        ((TextView) footer.findViewById(R.id.author_name)).setText(emoPackage.getAuthorName());
        String authorName = emoPackage.getAuthorName();
        if (authorName == null || authorName.equals("")) {
            authorName = emoPackage.getName();
        }
        preview.setAuthor(null, authorName);
        final String finalAuthorName = authorName;
        fastLog("finalAuthorName : " + finalAuthorName);
        emoPackage.downloadAuthorAvatar(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                SpImageView avatarView = (SpImageView) footer.findViewById(R.id.author_head);
                String path = emoPackage.getAuthorAvatar().getFullPath();
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
        footer.findViewById(R.id.author_btn).setOnClickListener(onAuthorClick);
        if (emoPackage.getAuthorName() == null) {
            footer.findViewById(R.id.author_btn).setVisibility(View.GONE);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到投诉页
                Resources resources = v.getResources();
                Intent intent = new Intent(v.getContext(), WebActivity.class);
                Bundle bundle = new Bundle();
                if (v.getId() == R.id.agreement) {
                    bundle.putString("title", resources.getString(R.string.agreement));
                    bundle.putString("web_url", resources.getString(R.string.agreement_url));
                } else if (v.getId() == R.id.complaint) {
                    bundle.putString("title", resources.getString(R.string.complaint));
                    bundle.putString("web_url", resources.getString(R.string.complaint_url));
                } else {
                    return;
                }
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        };
        footer.findViewById(R.id.agreement).setOnClickListener(onClickListener);
        footer.findViewById(R.id.complaint).setOnClickListener(onClickListener);

        footer.removeCallbacks(getHeightRunnable);
        getHeightRunnable = new Runnable() {
            @Override
            public void run() {
                int bottom = footer.getBottom();
                LogX.fastLog("footer bottom : " + bottom);
                if (bottom > 0 && emoticonGrid.getChildCount() > 0) {
                    View lastChild = emoticonGrid.getChildAt(emoticonGrid.getChildCount() - 1);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) footer.getLayoutParams();
                    params.topMargin = emoticonGrid.getBottom() - lastChild.getBottom();
                    footer.setLayoutParams(params);
                }
            }
        };
        footer.post(getHeightRunnable);
    }

    Runnable getHeightRunnable;

    private void refreshDownloadBtn(View header) {
        if (emoPackage == null) {
            return;
        }
        downloadSolidBtn = (DownloadSolidBtn) header.findViewById(R.id.download_solid_btn);
//        downloadBtn = header.findViewById(R.id.download_btn);
//        downloadIcon = header.findViewById(R.id.download_icon);
//        downloadText = (TextView) header.findViewById(R.id.download_text);
//        progressBar = (CollectProgressBar) header.findViewById(R.id.progress);
//        downloadText.setTextColor(themeOptions.getDownloadSolidBtnTextColor());

        if (emoPackage.isCollecting()) { //下载中，显示进度条
            downloadSolidBtn.showProgress();
//            downloadBtn.setBackgroundColor(Color.parseColor("#eeeeee"));
//            downloadBtn.setBackgroundColor(themeOptions.getProgressBgColor());
//            downloadIcon.setVisibility(View.GONE);
//            downloadText.setVisibility(View.GONE);
//            progressBar.setVisibility(View.VISIBLE);
        } else if (emoPackage.isCollected()) { //已下载，显示已下载
            downloadSolidBtn.showDownloaded();
//            downloadBtn.setBackgroundColor(Color.parseColor("#d0d0d0"));
//            downloadBtn.setBackgroundColor(themeOptions.getDownloadBtnBgSolidFinColor());
//            downloadIcon.setVisibility(View.GONE);
//            downloadText.setVisibility(View.VISIBLE);
//            downloadText.setText("已下载");
//            progressBar.setVisibility(View.GONE);
        } else { //未下载，显示下载按钮
            downloadSolidBtn.showDownloadBtn();
//            downloadBtn.setBackgroundColor(FacehubApi.getApi().getThemeColor());
//            downloadBtn.setBackgroundColor(themeOptions.getDownloadBtnBgSolidColor());
//            downloadIcon.setVisibility(View.VISIBLE);
//            downloadText.setVisibility(View.VISIBLE);
//            downloadText.setText("下载");
//            progressBar.setVisibility(View.GONE);
        }
    }

    public void onEvent(DownloadProgressEvent event) {
        if(downloadSolidBtn!=null
                && emoPackage != null
                && event.listId.equals(emoPackage.getId())){
            downloadSolidBtn.setProgress(event.percentage);
        }
//        if (progressBar != null && emoPackage != null && event.listId.equals(emoPackage.getId())) {
//            progressBar.setPercentage(event.percentage);
//        }
    }

    public void onEvent(PackageCollectEvent event) {
        if (header != null && emoPackage != null && event.emoPackageId.equals(emoPackage.getId())) {
            refreshDownloadBtn(header);
        }
    }

    public void onEvent(ExitViewsEvent exitViewsEvent) {
        finish();
    }

    private void setCover() {
        if (emoPackage == null || emoPackage.getCover() == null) {
            LogX.i("Detail页面 : cover为空!");
            return;
        }
        emoPackage.downloadCover(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                SpImageView spImageView = (SpImageView) headerNoBackground.findViewById(R.id.cover_image);
                spImageView.setHeightRatio(1f);
                spImageView.displayFile(emoPackage.getCover().getThumbPath());
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
        emoPackage.downloadBackground(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                backImage.displayFile(emoPackage.getBackground().getFullPath());
                detailAdapter.notifyDataSetChanged();
                emoticonGrid.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                LogX.e("下载包背景图出错 : " + e);
            }
        });
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
            holder = new Holder(convertView);
        }
        holder = (Holder) convertView.getTag();
        final Emoticon emoticon = emoticons.get(position);
        holder.data = emoticon;

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
                imgParams.height = (int) (width * ratio);
                radiusParams.width = imgParams.width;
                radiusParams.height = imgParams.height;
            } else {
                double ratio = emoticon.getWidth() * 1f / emoticon.getHeight();
                imgParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                imgParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.imageView.setWidthRatio(ratio);
                imgParams.height = width;
                imgParams.width = (int) (width * ratio);
                radiusParams.width = imgParams.width;
                radiusParams.height = imgParams.height;
//                radiusParams.width = (int) (width*ratio);
            }
        }

        if (emoticon.getDownloadStatus() == Image.DownloadStatus.fail) {
            holder.imageView.setImageResource(R.drawable.load_fail);
        } else {
            holder.imageView.displayFile(emoticon.getThumbPath()); //显示缩略图
        }


//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fastLog("预览表情 : " + emoticon + "\nposition : " + position);
//                preview.show(emoticon);
//            }
//        });

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

    class Holder extends TouchableGridHolder {
        SpImageView imageView; //,imageViewW,imageViewH;
        View leftMargin, rightMargin, content, radiusLayout;
        ImageView frontFrame;

        public Holder(View itemView) {
            super(itemView);
            imageView = (SpImageView) itemView.findViewById(R.id.image_view_facehub);
            leftMargin = itemView.findViewById(R.id.left_margin);
            rightMargin = itemView.findViewById(R.id.right_margin);
            content = itemView.findViewById(R.id.content);
            imageView.setDoResize(false);
            radiusLayout = itemView.findViewById(R.id.radius_layout);
            frontFrame = (ImageView) itemView.findViewById(R.id.front_frame);
        }

        @Override
        public void offTouchEffect() {
            super.offTouchEffect();
//            frontFrame.setAlpha(1f);
        }

        @Override
        public void onTouchedEffect() {
            super.onTouchedEffect();
//            frontFrame.setAlpha(0.3f);
        }
    }
}