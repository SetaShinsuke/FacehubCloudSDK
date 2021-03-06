package com.azusasoft.facehubcloudsdk.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.activities.EmoStoreActivity;
import com.azusasoft.facehubcloudsdk.activities.EmoStoreActivityWC;
import com.azusasoft.facehubcloudsdk.activities.ListsManageActivityNew;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.LocalList;
import com.azusasoft.facehubcloudsdk.api.models.SendRecordDAO;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonCollectEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonsRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.LoginEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ReorderEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListPrepareEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.FHanlder;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.DataAvailable;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.GridItemTouchListener;
import com.azusasoft.facehubcloudsdk.views.touchableGrid.ScrollTrigger;
import com.azusasoft.facehubcloudsdk.views.viewUtils.GifViewFC;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HorizontalListView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ResizablePager;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.Constants.KAOMOJI_COLUMN;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.KAOMOJI_ROW;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LOCAL_EMO_CUSTOM;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LOCAL_EMO_EMOJI;
import static com.azusasoft.facehubcloudsdk.api.utils.Constants.LOCAL_EMO_VOICE;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView.LONG_CLICK_DURATION;
import static com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView.NUM_ROWS_MORE;
import static com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView.NUM_ROWS_NORMAL;
import static com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods.isInZoneOf;

/**
 * Created by SETA on 2016/3/16.
 *
 * 注意！！
 * 1.请务必调用初始化函数 {@link #initKeyboard(boolean, String, OnClickListener)}   ,否则将无法显示预览;
 * 2.切换横/竖屏时请调用 {@link #onScreenWidthChange()} 更新键盘视图;
 * 3.请设置表情点击的回调 {@link #setEmoticonSendListener(EmoticonSendListener)};
 *
 * 另外：目前表情键盘仅支持宽度为全屏宽度，如有其它需求或问题，请联系我们。
 *
 */
public class EmoticonKeyboardView extends FrameLayout {
//    private Context mContext;
    private View mainView;
    protected final static int NUM_ROWS_NORMAL = 2;
    protected final static int NUM_ROWS_MORE = 3;
    protected final static int LONG_CLICK_DURATION = 300;
    private int totalHeight = 0;
    private ViewGroup previewContainer;
    private EmoticonSendListener emoticonSendListener = new EmoticonSendListener() {
        @Override
        public void onSend(Emoticon emoticon) {

        }
    };

    private boolean hasInit = false;
    private boolean localEmoticonEnabled = true;
//    private boolean mixLayoutEnabled = false; //是否显示发送/删除按钮
    private boolean sendButtonEnabled = false; //发送按钮是否可点击
    private String sendBtnColorString = "#467fff";

    ViewGroup rootViewGroup;
    private ResizablePager emoticonPager;
    private KeyboardPageNav keyboardPageNav;
    private HorizontalListView listNavListView;
    private View sendBtn,addListView;

    private EmoticonPagerAdapter emoticonPagerAdapter;
    private ListNavAdapter listNavAdapter;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private GridItemTouchListener gridItemTouchListener;
    private GifViewFC gifView;

    public boolean isPreviewShowing = false;

    private Runnable previewRunnable;

    public EmoticonKeyboardView(Context context) {
        super(context);
        constructView(context);
    }

    public EmoticonKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public EmoticonKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmoticonKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

//
//    {@link ListNavAdapter}中保存当前列表{@link ListNavAdapter#currentList}
//    数据改变的两种情况 :
//    1.翻页 : 切换列表、改变导航点;
//    2.点列表 : 翻页、改变导航点;
//    注意!
//    改变导航点时，判断是否要显示为滚动条
//


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            EventBus.getDefault().unregister(this);
        }catch (Exception e){
            LogX.e("表情键盘 EventBus 反注册出错 : " + e);
        }
    }

    private void constructView(final Context context) {
//        mContext = context;
        this.mainView = LayoutInflater.from(context).inflate(R.layout.emoticon_keyboard, null);
        addView(mainView);
//        EventBus.getDefault().register(this);
        hide();

        addListView = findViewById(R.id.add_list);
        ImageView addListBtn = (ImageView) addListView.findViewById(R.id.float_list_cover);
//        addListBtn.setImageResource(R.drawable.emo_keyboard_add);
        addListBtn.setImageResource(R.drawable.emoji_shop);
        addListView.findViewById(R.id.back_hole).setVisibility(GONE);
        addListView.findViewById(R.id.content).setBackgroundColor(getResources().getColor(android.R.color.white));
//        addListView.setBackgroundColor(getResources().getColor(android.R.color.white));
        addListView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FacehubApi.getApi().getViewStyle()== Constants.VIEW_STYLE_WECHAT){
                    Intent intent = new Intent(v.getContext(), EmoStoreActivityWC.class);
                    v.getContext().startActivity(intent);
                }else {
                    Intent intent = new Intent(v.getContext(), EmoStoreActivity.class);
                    v.getContext().startActivity(intent);
                }
            }
        });
        sendBtn = findViewById(R.id.send_btn);

        this.emoticonPager = (ResizablePager) mainView.findViewById(R.id.emoticon_pager);
        this.keyboardPageNav = (KeyboardPageNav) mainView.findViewById(R.id.keyboard_page_nav);
        this.listNavListView = (HorizontalListView) mainView.findViewById(R.id.list_nav);

        listNavAdapter = new ListNavAdapter(getContext());
        listNavListView.setAdapter(listNavAdapter);

        int numColumnsNormal = getNumColumnsNormal();
        int numColumnsMore = getNumColumnsMore();
        emoticonPagerAdapter = new EmoticonPagerAdapter(context, numColumnsNormal,numColumnsMore);
        this.emoticonPager.setAdapter(emoticonPagerAdapter);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) emoticonPager.getLayoutParams();
        totalHeight = NUM_ROWS_NORMAL * getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        layoutParams.height = totalHeight;
        emoticonPagerAdapter.setKeyboardHeight(totalHeight);

        if (!isInEditMode()) {
            emoticonPagerAdapter.setUserLists(userLists);
            listNavAdapter.setUserLists(userLists);
            refresh();

            //翻页
            emoticonPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    UserList lastList = listNavAdapter.getCurrentList();
                    if (lastList != emoticonPagerAdapter.getUerListByPage(position)) {
                        listNavAdapter.setCurrentList(emoticonPagerAdapter.getUerListByPage(position));
                    }
                    UserList currentList = listNavAdapter.getCurrentList();
                    if(currentList==null){
                        return;
                    }
                    keyboardPageNav.setCount(emoticonPagerAdapter.getPageCount(currentList)
                            , emoticonPagerAdapter.getPageIndexInList(currentList, position));
                    if (currentList.isDefaultFavorList()) { //默认收藏列表,显示进度条
                        keyboardPageNav.showScrollbar(true, positionOffset);
                    } else {
                        keyboardPageNav.showScrollbar(false, 0);
                    }

                    autoShowSendBtn(currentList);

                    LinearLayoutManager layoutManager = listNavListView.getLayoutManager();
                    int index = userLists.indexOf(currentList);
                    if (currentList != lastList
                            && (index < layoutManager.findFirstVisibleItemPosition()
                            || index > layoutManager.findLastVisibleItemPosition())) { //切换了列表，滚到相应位置
                        int offset = (int) (getResources().getDimensionPixelSize(R.dimen.keyboard_list_nav_width)*1f/2);
                        layoutManager.scrollToPositionWithOffset(index, -offset);
                    }
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            //切换列表监听
            listNavAdapter.setListChangeListener(new KeyboardListChangeListener() {
                @Override
                public void onListChange(UserList lastList, UserList currentList) {
                    LogX.d("上一个列表 : " + lastList + "\n切换到列表 : " + currentList);
                    int page = emoticonPagerAdapter.getFirstPageOfList(currentList);
                    emoticonPager.setCurrentItem(page, false);
                    keyboardPageNav.setCount(emoticonPagerAdapter.getPageCount(currentList)
                            , emoticonPagerAdapter.getPageIndexInList(currentList, page));
                    if(currentList==null){
                        return;
                    }
                    if (currentList.isDefaultFavorList()) {
                        keyboardPageNav.showScrollbar(true, 0);
                    } else {
                        keyboardPageNav.showScrollbar(false, 0);
                    }

                    autoShowSendBtn(currentList);
                }
            });

            /**================================================================================**/
            /** ============================= 发送/预览 核心处理代码 =========================== **/
            gridItemTouchListener = new GridItemTouchListener() {
                private View touchedView;
                private Emoticon touchedEmoticon;

                @Override
                public void onItemClick(View view, DataAvailable object) {
                    if (!(object instanceof Emoticon)) {
                        return;
                    }
                    clearTouchEffect();
                    Emoticon emoticon = (Emoticon) object;
                    if (emoticon.getId() == null) {
                        LogX.i("点击 : 进入商店");
                        if(FacehubApi.getApi().getViewStyle()==Constants.VIEW_STYLE_WECHAT){
                            Intent intent = new Intent(context, EmoStoreActivityWC.class);
                            context.startActivity(intent);
                        }else {
                            Intent intent = new Intent(context, EmoStoreActivity.class);
                            context.startActivity(intent);
                        }
                        return;
                    }

                    /** 记录发送表情 */
                    if( !emoticon.isLocal() ) {
                        SendRecordDAO.recordEvent(emoticon.getId());
//                        String dateStr = UtilMethods.getDateString();
//                        SendRecord sendRecord = SendRecordDAO.getUniqueSendRecord(dateStr
//                                , emoticon.getId()
//                                , FacehubApi.getApi().getUser().getUserId() );
//                        sendRecord.count++; //表情发送+1
//                        sendRecord.save();
                    }

                    emoticonSendListener.onSend(emoticon);
                    LogX.i("发送表情 : " + emoticon.getId()
                            + "\npath : " + emoticon.getFullPath());
                }

                @Override
                public void onItemLongClick(View view, DataAvailable emoticonObj) {
                    final Emoticon emoticon = (Emoticon) emoticonObj;
                    if (view == null || emoticon == null || emoticon.getId() == null) {
                        clearTouchEffect();
                        return;
                    }
                    if (view == touchedView || emoticon == touchedEmoticon) {
                        //预览的表情没有变
                        return;
                    }
                    clearTouchEffect();
                    isPreviewShowing = true;
                    this.touchedView = view;
                    this.touchedEmoticon = emoticon;
                    showTouchEffect();

                    if (previewContainer != null) {
                        previewContainer.setVisibility(VISIBLE);
                        //预览表情
                        if (gifView == null) {
                            return;
                        }
                        ImageView bubble = (ImageView) previewContainer.findViewById(R.id.preview_bubble);
                        gifView.setVisibility(GONE);
                        emoticon.downloadFull2File( true, new ResultHandlerInterface() {
                            @Override
                            public void onResponse(Object response) {
                                gifView.setGifPath(emoticon.getFullPath());
                                gifView.removeCallbacks(previewRunnable);
                                previewRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        gifView.setVisibility(VISIBLE);
//                                        fastLog("emoticon path : " + emoticon.getFilePath(Image.Size.FULL));
                                    }
                                };
                                postDelayed(previewRunnable,200);
                            }

                            @Override
                            public void onError(Exception e) {
                                LogX.e("preview error : " + e);
                            }
                        });

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
                        ViewUtilMethods.changeViewPosition(previewContainer, previewLeft, previewTop);
//                    fastLog("previewTop : " + top + "\npreviewLeft : " + left);
                    }
                }

                @Override
                public void onItemOffTouch(View view, DataAvailable object) {
                    clearTouchEffect();
                    isPreviewShowing = false;
                    this.touchedView = null;
                    this.touchedEmoticon = null;
//                    clearTouchEffect();
                    if (previewContainer != null) {
                        previewContainer.setVisibility(GONE);
                    }
                }

                private void clearTouchEffect() {
                    if (touchedView != null && touchedView.getTag() != null
                            && touchedView.getTag() instanceof KeyboardEmoticonGridAdapter.Holder) {
                        ((KeyboardEmoticonGridAdapter.Holder) touchedView.getTag()).showFrame(false);
                    }
                }

                private void showTouchEffect() {
                    View view = touchedView;
                    if (view != null && view.getTag() != null && view.getTag() instanceof KeyboardEmoticonGridAdapter.Holder) {
                        ((KeyboardEmoticonGridAdapter.Holder) view.getTag()).showFrame(true);
                    }
                }
            };
            emoticonPagerAdapter.setScrollTrigger(new ScrollTrigger() {
                @Override
                public void setCanScroll(boolean canScroll) {
                    emoticonPager.setPagingEnabled(canScroll);
                }
            });
            /**=================================== 处理结束 ====================================**/
            /**================================================================================**/

            emoticonPagerAdapter.setGridItemTouchListener(gridItemTouchListener);
        }
    }

    public void setEmoticonSendListener(EmoticonSendListener emoticonSendListener) {
        this.emoticonSendListener = emoticonSendListener;
        if(emoticonPagerAdapter!=null){
            emoticonPagerAdapter.setEmoticonSendListener(emoticonSendListener);
        }
    }

    private void autoShowSendBtn(UserList currentList){
        if(currentList==null){
            sendBtn.setVisibility(GONE);
        }else if(currentList.isEmojiList() || currentList.isKaomojiList()) {
            sendBtn.setVisibility(VISIBLE);
        }else if(currentList instanceof LocalList
                && ((LocalList)currentList).isNeedMixLayout()){
            sendBtn.setVisibility(VISIBLE);
        }else {
            sendBtn.setVisibility(GONE);
        }
    }

    public void onEvent(UserListRemoveEvent event) {
        refresh();
    }
    public void onEvent(EmoticonsRemoveEvent event) {
        refresh();
    }
    public void onEvent(EmoticonCollectEvent event) {
        refresh();
    }
    public void onEvent(PackageCollectEvent event) {
        refresh();
    }
    public void onEvent(ReorderEvent event) {
        refresh();
    }
    public void onEvent(UserListPrepareEvent event){
        refresh();
    }
    public void onEvent(LoginEvent event){
        refresh();
        FacehubApi.getApi().getUser().silentDownloadAll();
    }
    public void onEvent(ExitViewsEvent event){
        hide();
        refresh();
    }

    public void refresh() {
        if(isPreviewShowing){
            return;
        }
        userLists = new ArrayList<>(FacehubApi.getApi().getUser().getAvailableUserLists());
        boolean isLogin = FacehubApi.getApi().getUser().isLogin();
        if(localEmoticonEnabled){
            if(hasInit || !isLogin) {
                ArrayList<LocalList> localLists = FacehubApi.getApi().getUser().getLocalLists();
                userLists.addAll(0,localLists);
            }
        }

        //判断颜文字
        if(FacehubApi.getApi().isKaomojiEnabled()){
            if (hasInit || !isLogin) {
                userLists.add(0, FacehubApi.getApi().getUser().getKaomojiList(getContext()));
            }
        }

        //判断emoji是否打开
        if(FacehubApi.getApi().isEmojiEnabled()) {
            if (hasInit || !isLogin) {
                userLists.add(0, FacehubApi.getApi().getUser().getEmojiList(getContext()));
            }
        }


        if( !isLogin ){
            addListView.setVisibility(GONE);
        }else {
            addListView.setVisibility(VISIBLE);
        }

        fastLog("Keyboard refresh - userLists size : " + userLists.size());
        emoticonPagerAdapter.setUserLists(userLists);
        listNavAdapter.setUserLists(userLists);
    }

    /**
     * 初始化键盘
     * @param localEmoticonEnabled 是否有默认表情
     * @param sendBtnColorString 键盘内发送按钮的颜色，可为空
     * @param onSendButtonClickListener 键盘内发送按钮点击事件监听器
     */
    public void initKeyboard(boolean localEmoticonEnabled
            , @Nullable String sendBtnColorString
            ,@Nullable OnClickListener onSendButtonClickListener) {
        //找到keyboard的爹,添加预览的container
//        if(getParent()!=null && getParent() instanceof ViewGroup){
//        if(FacehubApi.getApi().getUser().isLogin()) {
            hasInit = true;
//        }
        if (getContext() instanceof Activity) {
            View activityView = ((Activity) getContext()).findViewById(android.R.id.content);
            if (activityView instanceof ViewGroup) {
                rootViewGroup = (ViewGroup) activityView;
                this.previewContainer = new FrameLayout(getContext());
                rootViewGroup.addView(previewContainer);
                LayoutInflater.from(getContext()).inflate(R.layout.keyboard_preview, previewContainer);
                previewContainer.setVisibility(GONE);
                gifView = (GifViewFC) previewContainer.findViewById(R.id.preview_image);
            }
        }
        setLocalEmoticonEnabled(localEmoticonEnabled);
        if(sendBtnColorString!=null){
            setSendBtnColorString(sendBtnColorString);
        }
        setOnSendButtonClickListener(onSendButtonClickListener);

//        setMixLayoutEnabled(FacehubApi.getApi().isMixLayoutEnabled());
        FacehubApi.getApi().getUser().silentDownloadAll();
        refresh();
    }

    public void destroyKeyboard(){
        //清理runnable
        removeCallbacks(previewRunnable);
        previewRunnable = null;
        keyboardPageNav.onDestroy();

        if(gifView!=null) {
            gifView.onDestroy();
        }

        rootViewGroup.removeView(previewContainer);
        this.previewContainer = null;
        rootViewGroup = null;

        System.gc();
    }

    public void onScreenWidthChange() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) emoticonPager.getLayoutParams();
        totalHeight = NUM_ROWS_NORMAL * getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        layoutParams.height = totalHeight;
        emoticonPagerAdapter.setKeyboardHeight(totalHeight);
        ((EmoticonPagerAdapter) emoticonPager.getAdapter()).setNumColumns(getNumColumnsNormal(),getNumColumnsMore());
        //保持列表，翻页到该列表第一页
        refresh();
        emoticonPager.setCurrentItem(emoticonPagerAdapter.getFirstPageOfList(listNavAdapter.getCurrentList()), false);
    }

    private int getNumColumnsNormal() {
        int screenWith = ViewUtilMethods.getScreenWidth(getContext());
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        return screenWith / itemWidth;
    }

    private int getNumColumnsMore() {
        int screenWith = ViewUtilMethods.getScreenWidth(getContext());
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width_mini);
        return screenWith / itemWidth;
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility==VISIBLE){
            fastLog("显示键盘，检查用户是否登录 : " + FacehubApi.getApi().getUser().isLogin());
            SendRecordDAO.recordEvent(Constants.RECORD_SHOW_KEYBOARD);
        }
        if(visibility==VISIBLE && !FacehubApi.getApi().getUser().isLogin()){
            LogX.i("显示键盘，用户未登录，正在检查是否需要自动重试登录……");
            FacehubApi.getApi().retryLogin(new ResultHandlerInterface() {
                @Override
                public void onResponse(Object response) {
                    LogX.i("重试登录成功!");
                }

                @Override
                public void onError(Exception e) {
                    LogX.e("重试登录出错!");
                }
            });
        }
    }

    //region 预置表情设置

//    /**
//     * 从文件读取默认表情配置
//     * @param version 版本号
//     * @param configJsonAssetsPath 配置文件，在assets文件夹内的具体路径
//     * @param mixLayoutEnabled 是否允许图文混排;
//     * @throws LocalEmoPackageParseException 配置JSON解析出错时抛出异常
//     */
//    public void loadEmoticonFromLocal(int version, @NonNull String configJsonAssetsPath, boolean mixLayoutEnabled) throws LocalEmoPackageParseException{
//        setMixLayoutEnabled(mixLayoutEnabled);
//        try {
//            CodeTimer codeTimer = new CodeTimer();
//            codeTimer.start("开始解析JSON");
//            FacehubApi.getApi().getUser().restoreLocalEmoticons(getContext(),version,configJsonAssetsPath);
//            codeTimer.end("解析JSON完成");
//        }catch (Exception e){
//            throw new LocalEmoPackageParseException("解析本地表情配置出错" + e);
//        }
//        refresh();
//    }

    /**
     * 设置是否有预置表情
     * @param localEmoticonEnabled 是否有预置表情
     */
    private void setLocalEmoticonEnabled(boolean localEmoticonEnabled){
        this.localEmoticonEnabled = localEmoticonEnabled; /** 注意！预置表情变动时，清除已选中的列表*/
        if(listNavAdapter!=null) {
            listNavAdapter.setCurrentList(null);
            listNavAdapter.setLocalEmoticonEnabled(localEmoticonEnabled);
        }
        if(!localEmoticonEnabled){
            sendBtn.setVisibility(GONE);
        }else {
            sendBtn.setVisibility(VISIBLE);
        }
    }

    /**
     * 发送按钮可点击
     * @param sendButtonEnabled 发送按钮是否可以点击
     */
    public void setSendButtonEnabled(boolean sendButtonEnabled){
        this.sendButtonEnabled = sendButtonEnabled;
        if(sendButtonEnabled){
            sendBtn.setBackgroundColor(Color.parseColor(sendBtnColorString));
        }else {
            int color;
//            if (Build.VERSION.SDK_INT >= 23) {
//                color = getResources().getColor(R.color.send_btn_color_disabled,getContext().getTheme());
//            }else {
                color = getResources().getColor(R.color.send_btn_color_disabled);
//            }
            sendBtn.setBackgroundColor(color);
        }
    }

//    private void setMixLayoutEnabled(boolean mixLayoutEnabled){
//        this.mixLayoutEnabled = mixLayoutEnabled;
//        if(!localEmoticonEnabled || !mixLayoutEnabled){
//            sendBtn.setVisibility(GONE);
//        }else {
//            sendBtn.setVisibility(VISIBLE);
//        }
//    }

    private void setSendBtnColorString(String colorString){
        this.sendBtnColorString = colorString;
    }

    private void setOnSendButtonClickListener(final OnClickListener onSendButtonClickListener){
        sendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendButtonEnabled){
                    LogX.i("点击键盘发送按钮.");
                    if(onSendButtonClickListener!=null) {
                        onSendButtonClickListener.onClick(v);
                    }
                }
            }
        });
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
//        this.onDeleteListener = onDeleteListener;
        emoticonPagerAdapter.setOnDeleteListener(onDeleteListener);
    }

    //endregion 预置表情设置

}

/**
 * 显示表情的Pager
 */
class EmoticonPagerAdapter extends PagerAdapter {
    final private Context context;
    private LayoutInflater layoutInflater;
    private int numColumnsNormal = 4;
    private int numColumnsEmoji = 7;
    private int keyboardHeight = 0;
    private int screenWidth = 0;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private ArrayList<PageHolder> pageHolders = new ArrayList<>();
    private GridItemTouchListener gridItemTouchListener = new GridItemTouchListener() {
        @Override
        public void onItemClick(View view, DataAvailable object) {

        }

        @Override
        public void onItemLongClick(View view, DataAvailable emoticon) {

        }

        @Override
        public void onItemOffTouch(View view, DataAvailable object) {

        }
    };
    private EmoticonSendListener emoticonSendListener = new EmoticonSendListener() {
        @Override
        public void onSend(Emoticon emoticon) {

        }
    };
    private OnDeleteListener onDeleteListener = new OnDeleteListener() {
        @Override
        public void onDelete() {

        }
    };
    private ScrollTrigger scrollTrigger = new ScrollTrigger() {
        @Override
        public void setCanScroll(boolean canScroll) {

        }
    };

    /**
     * 总页数 :         Total = 列表个数n * 每个列表占用页数p ;
     * 每个列表占用页数:     P = 表情数E / 每页表情数s (向上取整) ;
     * 每页表情数 :         s = 列数c * 2(行数);
     */

    /**
     * PagerAdapter记录
     *          普通表情的 行列数{@link #numColumnsNormal} & {@link }
     */

    public EmoticonPagerAdapter(Context context, int numColumnsNormal,int numColumnsEmoji) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.numColumnsNormal = numColumnsNormal;
        this.numColumnsEmoji = numColumnsEmoji;
        this.screenWidth = ViewUtilMethods.getScreenWidth(context);
    }

    protected void setNumColumns(int numColumnsNormal,int numColumnsMore) {
        this.numColumnsNormal = numColumnsNormal;
        this.numColumnsEmoji = numColumnsMore;
        notifyDataSetChanged();
    }

    public void setKeyboardHeight(int keyboardHeight){
        this.keyboardHeight = keyboardHeight;
    }

    /**
     * 设置进来的数据是{@link UserList}的合集，但实际用到的是{@link PageHolder}的合集;
     * 转化为holder
     * @param userLists 用户列表的集合
     */
    protected void setUserLists(ArrayList<UserList> userLists) {
        this.userLists = userLists;
        pageHolders.clear();
        for (UserList userList : userLists) { //每个列表
            ArrayList<Emoticon> emoticonsOfThisList = userList.getAvailableEmoticons();
            if (userList.isDefaultFavorList()) { //默认列表，显示"+"
//            if (userList.getForkFromId()==null
//                    && !userList.isLocal()
//                    && !userList.isEmojiList()) { //野生列表
                emoticonsOfThisList.add(0, new Emoticon()); //空Emoticon用来显示 加号"+"
            }

            //1.每页最多显示的表情数
            int s = NUM_ROWS_NORMAL * numColumnsNormal; //每页表情数(最多)
            //1.1 本地表情
            if(userList instanceof LocalList){
                LocalList localList = (LocalList)userList;
                if(localList.getLocalType().equals(LOCAL_EMO_EMOJI)) {
                    s = NUM_ROWS_MORE * numColumnsEmoji; //本地列表行数增加
                }else {
                    s = localList.getRowNum() * localList.getColumnNum(); //自定义/语音表情，根据配置设置行列数
                }

                //如果不允许图文混排，则没有删除按钮
                if( localList.isNeedMixLayout() ) {
                    s -= 1; //本地表情每页最多显示的数目，因为多一个删除按钮，所以-1;
                }
            }
            //1.2 emoji表情
            else if(userList.isEmojiList()){
                s = NUM_ROWS_MORE * numColumnsEmoji - 1 ;//删除按钮
                if(userList.isKaomojiList()){
                    s = KAOMOJI_COLUMN * KAOMOJI_ROW;
                }
            }


            //2.某个列表所占的页数
            int pagesOfThisList = (int) Math.ceil((emoticonsOfThisList.size() / (float) s)); //这个列表所占的页数

            //3.新建PageHolder并添加
            if (pagesOfThisList == 0) { //空列表占位
                PageHolder pageHolder = new PageHolder(userList,NUM_ROWS_NORMAL,numColumnsNormal);
                pageHolders.add(pageHolder);
            }

            for (int i = 0; i < pagesOfThisList; i++) { //每一页
                PageHolder pageHolder;
                int start = s * i;
                int end = Math.min(emoticonsOfThisList.size(), (i + 1) * s); //此页最后一个表情的下标
                if(userList instanceof LocalList){ /** 本地表情 */
                    LocalList localList = (LocalList)userList;
                    if(localList.getLocalType().equals(LOCAL_EMO_EMOJI)){
                        pageHolder = new PageHolder(userList,NUM_ROWS_MORE,numColumnsEmoji);
                    }else {
                        pageHolder = new PageHolder(userList, localList.getRowNum(), localList.getColumnNum());
                    }
                }else if(userList.isEmojiList()) {
                    if(userList.isKaomojiList()) {
                        pageHolder = new PageHolder(userList, KAOMOJI_ROW, KAOMOJI_COLUMN);
                    }else {
                        pageHolder = new PageHolder(userList, NUM_ROWS_MORE, numColumnsEmoji);
                    }
                }else {
                    pageHolder = new PageHolder(userList,NUM_ROWS_NORMAL,numColumnsNormal);
                }
                pageHolder.divide(emoticonsOfThisList, start, end);
                pageHolders.add(pageHolder);
//                fastLog("------------------------------");
//                fastLog("页码 : " + pageHolders.indexOf(pageHolder));
//                fastLog("start : " + start + " | end : " + end);
//                fastLog("表情数 : " + pageHolder.emoticons.size());
//                fastLog("------------------------------");
            }
        }
        notifyDataSetChanged();
    }

    //获取列表占用的页数
    protected int getPageCount(UserList userList) {
        int count = 0;
        for (PageHolder pageHolder : pageHolders) {
            if (pageHolder.userList.getId().equals(userList.getId())) {
                count++;
            }
        }
        return count;
    }

    protected UserList getUerListByPage(int page) {
        if (page > pageHolders.size() - 1 || pageHolders.get(page) == null) {
            return null;
        }
        return pageHolders.get(page).userList;
    }

    protected int getPageIndexInList(UserList currentList, int pageGlobal) {
        int pageInside = pageGlobal - getFirstPageOfList(currentList);
        return pageInside;
    }

    protected int getFirstPageOfList(UserList userList) {
        if(userList==null){
            return 0;
        }
        for (int i = 0; i < pageHolders.size(); i++) {
            if (pageHolders.get(i).userList.getId().equals(userList.getId())) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getCount() {
        return pageHolders.size();
    }

    public void setEmoticonSendListener(EmoticonSendListener emoticonSendListener){
        this.emoticonSendListener = emoticonSendListener;
    }

    /**
     * 方法2 :
     * { page0 , page1 , page2 , ... }
     * 其中 :
     * page = { emo0 , emo1 , ... }
     * <p/>
     * 返回: pages.get( pos );
     */
    private ArrayList<Emoticon> getEmoticonsByPagePos(int position) {
        return pageHolders.get(position).emoticons;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.keyboard_pager_item, container, false);
        final PageHolder pageHolder = pageHolders.get(position);
        GridView keyboardGrid = (GridView) itemView.findViewById(R.id.grid_view);
        keyboardGrid.setNumColumns(pageHolder.numColumns);


        //TODO:根据本地列表type
        if(pageHolder.userList.isEmojiList()){
            boolean isText = false;
            if(pageHolder.userList.isKaomojiList()){
                isText = true;
            }
            KeyboardEmojiGridAdapter emojiAdapter
                    = new KeyboardEmojiGridAdapter(context
                    ,pageHolder.numColumns
                    ,pageHolder.numRows
                    ,pageHolder.customItemWidth
                    ,pageHolder.customItemHeight
                    ,isText);
            keyboardGrid.setAdapter(emojiAdapter);
            emojiAdapter.setEmoticons(pageHolder.emoticons);
            emojiAdapter.setEmoticonSendListener(this.emoticonSendListener);
            emojiAdapter.setOnDeleteListener(this.onDeleteListener);
            container.addView(itemView);
        }else if(pageHolder.isLocal()){ //本地表情，特别处理
            KeyboardLocalEmoGridAdapter adapter
                    = new KeyboardLocalEmoGridAdapter(context
                        ,pageHolder.numColumns
                        ,pageHolder.numRows
                        ,pageHolder.customItemWidth
                        ,pageHolder.customItemHeight
                        ,pageHolder.getLocalType());
            keyboardGrid.setAdapter(adapter);
            adapter.setEmoticons(pageHolder.emoticons);
            adapter.setEmoticonSendListener(this.emoticonSendListener);
            adapter.setOnDeleteListener(this.onDeleteListener);
            container.addView(itemView);

        }else { //面馆表情，正常显示
            /**================================================================================**/
            /** ========================== region : 触摸事件:核心代码 ========================== **/
            keyboardGrid.setOnTouchListener(new View.OnTouchListener() {
                private boolean isTouchedOnce = false; //已经在点击中(down时true , up&cancel时false )
                private boolean isLongPressed = false; //已在长按中(task中true, up&cancel时false)
                //pagerTrigger : 与isLongPressed保持相反
                private FHanlder handler = new FHanlder();
                private KeyboardEmoticonGridAdapter.Holder lastTouchedHolder = null;

                class Task implements Runnable {
                    public View touchedView; //触摸的view
                    public Emoticon touchedEmoticon; //触摸的要预览的emoticon

                    @Override
                    public void run() {
                        isLongPressed = true;
//                    fastLog("进入长按状态.");
                        gridItemTouchListener.onItemLongClick(touchedView, touchedEmoticon);
                        scrollTrigger.setCanScroll(false);
                    }
                }

                Task confirmLongPressTask = new Task();

                //处理触摸
                @Override
                public boolean onTouch(View gridView, MotionEvent event) {
                    //GridView是否消耗事件?
                    //      : 如果[点击]或者[长按]了，应当消耗
                    boolean flag = false; //move & ups时返回true(翻页会进入cancel)

                    int action = event.getAction();
                    //拿到相应位置的itemView ()
                    KeyboardEmoticonGridAdapter.Holder gridItemHolder = null;
                    int[] location = new int[2];
                    gridView.getLocationInWindow(location);
                    View itemView = getViewByPosition(
                            (GridView) gridView
                            , event.getX() + location[0]
                            , event.getY() + location[1]);
                    if ((itemView != null)
                            && (itemView.getTag() instanceof KeyboardEmoticonGridAdapter.Holder)) {
                        gridItemHolder =
                                (KeyboardEmoticonGridAdapter.Holder) itemView.getTag();
                    } else { /** itemView空/holder空,取消预览 **/
                        handler.removeCallbacks(confirmLongPressTask);
                        gridItemTouchListener.onItemOffTouch(null, null);
                        scrollTrigger.setCanScroll(true);
                        isLongPressed = false;
                        isTouchedOnce = false;
                        if(lastTouchedHolder!=null){
                            lastTouchedHolder.showFrame(false);
                        }
                        return false;
                    }
                    if (gridItemHolder.emoticon == null) { //emoticon空，不作处理
                        if(lastTouchedHolder!=null){
                            lastTouchedHolder.showFrame(false);
                        }
                        return false;
                    }
                    confirmLongPressTask.touchedView = itemView;
                    confirmLongPressTask.touchedEmoticon = gridItemHolder.emoticon;

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            if (isTouchedOnce) {
                                break;
                            }
                            lastTouchedHolder = null;
                            isTouchedOnce = true;
                            isLongPressed = false;
                            if (gridItemHolder.emoticon.getId() != null) { //id空表示为"+"加号，不产生点击效果
                                gridItemHolder.showFrame(true);
                            }
                            handler.postDelayed(confirmLongPressTask, LONG_CLICK_DURATION);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            flag = true;

                            if (lastTouchedHolder != null && lastTouchedHolder != gridItemHolder) { //触摸的holder变了
//                            fastLog("触摸的holder变了");
                                lastTouchedHolder.showFrame(false);
                            }
                            lastTouchedHolder = gridItemHolder;

                            if (isLongPressed) { //长按+移动时，切换预览图
                                handler.removeCallbacks(confirmLongPressTask);
                                gridItemTouchListener.onItemLongClick(itemView, gridItemHolder.emoticon);
                                scrollTrigger.setCanScroll(false);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
//                        fastLog("up.");
                            handler.removeCallbacks(confirmLongPressTask);
                            isTouchedOnce = false;
                            if (isLongPressed) { //长按时松手,调用offTouch,取消预览
                                gridItemTouchListener.onItemOffTouch(itemView, gridItemHolder.emoticon);
                                gridItemHolder.showFrame(false);
                            } else { //非长按松手,认为做了点击
                                gridItemTouchListener.onItemClick(itemView, gridItemHolder.emoticon);
                                gridItemHolder.showFrame(false);
                            }
                            scrollTrigger.setCanScroll(true);
                            isLongPressed = false;
                            flag = true;
                            lastTouchedHolder = null;
                            break;
                        case MotionEvent.ACTION_CANCEL:
//                        fastLog("cancel.");
                            handler.removeCallbacks(confirmLongPressTask);
                            gridItemTouchListener.onItemOffTouch(itemView, gridItemHolder.emoticon);
                            gridItemHolder.showFrame(false);
                            scrollTrigger.setCanScroll(true);
                            isLongPressed = false;
                            isTouchedOnce = false;
                            lastTouchedHolder = null;
//                        flag = true;
                            break;
                    }
                    return flag;
                }

                //拿到itemView
                private View getViewByPosition(GridView grid, float x, float y) {
                    for (int i = 0; i < grid.getChildCount(); i++) {
                        View child = grid.getChildAt(i);
                        // do stuff with child view
                        if (isInZoneOf(context, child, x, y,
                                context.getResources().getDimension(R.dimen.keyboard_grid_item_padding)))
                            return child;
                    }
                    return null;
                }
            });
            /** ==========================          endregion        ========================== **/
            /**================================================================================**/

            KeyboardEmoticonGridAdapter adapter = new KeyboardEmoticonGridAdapter(context, numColumnsNormal);
            keyboardGrid.setAdapter(adapter);
            adapter.setEmoticons(getEmoticonsByPagePos(position));
            container.addView(itemView);
        }
        return itemView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    void setGridItemTouchListener(GridItemTouchListener gridItemTouchListener) {
        this.gridItemTouchListener = gridItemTouchListener;
    }

    void setScrollTrigger(ScrollTrigger scrollTrigger) {
        this.scrollTrigger = scrollTrigger;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }


    //用于记录每页的list & emoticons
    class PageHolder {
        UserList userList;
        ArrayList<Emoticon> emoticons = new ArrayList<>(); //本页包含的表情
        int numRows = 2;
        int numColumns = 4;
        int customItemWidth = 0;
        int customItemHeight = 0;

        PageHolder(UserList userList,int numRows,int numColumns){
            this.userList = userList;
            this.numRows = numRows;
            this.numColumns = numColumns;

            if(numColumns>0 && numColumns>0) {
                float w = screenWidth * 1f / numColumns;
                float h =  keyboardHeight*1f/numRows;
                this.customItemWidth = (int) Math.min(w,h);
            }
            if(userList instanceof LocalList
                    && ((LocalList)userList).getRowNum()>0) { //根据设置的行数设置行高
                int rowOfList = ((LocalList)userList).getRowNum();
                this.customItemHeight = (int) (keyboardHeight * 1f / rowOfList);
            }

            if(userList.isKaomojiList()) { //根据设置的行数设置行高
                this.customItemWidth = (int) (screenWidth*1f/Constants.KAOMOJI_COLUMN);
                this.customItemHeight = (int) (keyboardHeight * 1f / Constants.KAOMOJI_ROW);
            }
        }

        boolean isLocal(){
            return userList.isLocal();
        }

        String getLocalType(){
            if(userList.isLocal()){
                return ((LocalList)userList).getLocalType();
            }
            return null;
        }

        void divide(ArrayList<Emoticon> emoticonsOfThisList, int start, int end) { //从所有表情中取出start -> end的表情
            emoticons.clear();
            for (int i = 0; i < emoticonsOfThisList.size(); i++) {
                if (i >= start && i < end) {
                    emoticons.add(emoticonsOfThisList.get(i));
                }
            }
            if(userList instanceof LocalList
                    && ((LocalList)userList).isNeedMixLayout() ){ //本地表情，每页最后添加空表情，用来显示删除按钮
                emoticons.add(new Emoticon());
            }
            if(userList.isEmojiList()){
                emoticons.add(new Emoticon());
            }
        }
    }
}

/**
 * 表情Grid的Adapter
 */
class KeyboardEmoticonGridAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private int numColumns = 4;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();

    public KeyboardEmoticonGridAdapter(Context context, int numColumns) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.numColumns = numColumns;
    }

    protected void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.numColumns * NUM_ROWS_NORMAL;
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
        Holder holder = new Holder();
        if (convertView == null) {
            convertView = this.layoutInflater.inflate(R.layout.keyboard_grid_item, parent, false);
            holder.backFrame = convertView.findViewById(R.id.back_frame);
            holder.imageView = (SpImageView) convertView.findViewById(R.id.grid_image);
            holder.imageView.setHeightRatio(1);
            holder.addCross = convertView.findViewById(R.id.add_cross);
            holder.clickArea = convertView.findViewById(R.id.click_area);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        convertView.setVisibility(View.VISIBLE);
        holder.imageView.setVisibility(View.VISIBLE);
        holder.addCross.setVisibility(View.GONE);

        if (position > emoticons.size() - 1) { //超出数据范围
            convertView.setVisibility(View.INVISIBLE);
            return convertView;
        }

        Emoticon emoticon = emoticons.get(position);
        holder.emoticon = emoticon;
        holder.imageView.setTag(emoticon);
        if (emoticon.getId() == null) {
            holder.addCross.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.imageView.displayFile(emoticon.getThumbPath()); //键盘显示缩略图
        }
        return convertView;
    }

    class Holder {
        SpImageView imageView;
        View backFrame;
        View addCross;
        View clickArea;
        Emoticon emoticon;

        public void showFrame(boolean doShow) { //点击效果
            if (backFrame != null) {
                if (doShow) {
                    backFrame.setVisibility(View.VISIBLE);
                } else {
                    backFrame.setVisibility(View.GONE);
                }
            }
        }
    }
}

/**
 * Emoji表情的Adapter
 */
class KeyboardEmojiGridAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private int numColumns = 7;
    private int numRows = 3;
    private int customHeight = 0;
    private int customWidth = 0;
    private boolean isKaomoji = false;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private EmoticonSendListener emoticonSendListener = new EmoticonSendListener() {
        @Override
        public void onSend(Emoticon emoticon) {

        }
    };
    private OnDeleteListener onDeleteListener = new OnDeleteListener() {
        @Override
        public void onDelete() {

        }
    };

    public KeyboardEmojiGridAdapter(Context context
            ,int numColumns
            ,int numRows
            ,int customWidth
            ,int customHeight
            ,boolean isKaomoji){
        this.context = context;
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.customWidth = customWidth;
        this.customHeight = customHeight;
        this.layoutInflater = LayoutInflater.from(context);
        this.isKaomoji = isKaomoji;
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons){
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(isKaomoji){
            return KAOMOJI_COLUMN * KAOMOJI_ROW;
        }
        return numColumns*numRows;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EmojiHolder holder;
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.keyboard_grid_item_emoji,parent,false);
            holder = new EmojiHolder();
            holder.mainView = convertView.findViewById(R.id.main_view);
            holder.textView = (TextView) convertView.findViewById(R.id.text_view);
            holder.textView.setBackgroundResource(0);
            holder.imageView = (SpImageView) convertView.findViewById(R.id.del_img);
            convertView.setBackgroundResource(R.drawable.local_emo_background);
            convertView.setTag(holder);
        }
        holder = (EmojiHolder) convertView.getTag();
        if(isKaomoji){
            holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.textView.setTextColor(context.getResources().getColor(R.color.kaomoji_text));
            ViewGroup.LayoutParams params = holder.mainView.getLayoutParams();
            params.height = customHeight;
            params.width = customWidth;
            holder.mainView.setLayoutParams(params);
        }
        convertView.setVisibility(View.VISIBLE);
        if (position > emoticons.size() - 1) { //超出数据范围
            convertView.setVisibility(View.INVISIBLE);
            return convertView;
        }
        final Emoticon emoticon = emoticons.get(position);
        holder.textView.setVisibility(View.VISIBLE);
        holder.imageView.setVisibility(View.GONE);
        if(emoticon.getId()==null) { //空Emoticon,显示删除按键
            holder.imageView.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.GONE);
            holder.imageView.setImageResource(R.drawable.del);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fastLog("点击删除按钮.");
                    onDeleteListener.onDelete();
                }
            });
        }else { //正常显示emoticon
            final String emojiString = emoticon.getDescription();
            holder.textView.setText(emojiString);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emoticonSendListener.onSend(emoticon);
                }
            });

        }
        return convertView;
    }

    public void setEmoticonSendListener(EmoticonSendListener emoticonSendListener) {
        this.emoticonSendListener = emoticonSendListener;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    class EmojiHolder{
        View mainView;
        TextView textView;
        SpImageView imageView;
    }
}

/**
 * 本地表情Grid的Adapter
 */
class KeyboardLocalEmoGridAdapter extends BaseAdapter{

    private Context context;
    private LayoutInflater layoutInflater;
    private int numColumns = 7;
    private int numRows = 3;
    private int customItemWidth = 0;
    private int customItemHeight = 0;
    private String localType;

    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private EmoticonSendListener emoticonSendListener = new EmoticonSendListener() {
        @Override
        public void onSend(Emoticon emoticon) {

        }
    };
    private OnDeleteListener onDeleteListener = new OnDeleteListener() {
        @Override
        public void onDelete() {

        }
    };

    private final int TYPE_EMOJI = 0;
    private final int TYPE_CUSTOM = 1;
    private final int TYPE_VOICE = 2;

    public KeyboardLocalEmoGridAdapter(Context context, int numColumns ,int numRows
            , int customItemWidth , int customItemHeight
            , String localType) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.customItemWidth = customItemWidth;
        this.customItemHeight = customItemHeight;
        this.localType = localType;
    }

    protected void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.numColumns * numRows;
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
    public int getItemViewType(int position) {
        if(localType==null){
            return TYPE_EMOJI;
        }
        int type = TYPE_EMOJI;
        switch (localType){
            case LOCAL_EMO_EMOJI:
                type = TYPE_EMOJI;
                break;
            case LOCAL_EMO_CUSTOM:
                type = TYPE_CUSTOM;
                break;
            case LOCAL_EMO_VOICE:
                type = TYPE_VOICE;
                break;
        }
        return type;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Emoticon emoticon;
        switch (getItemViewType(position)){
            case TYPE_EMOJI:
                LocalEmoHolder emojiHolder;
                if(convertView==null){
                    convertView = layoutInflater.inflate(R.layout.keyboard_grid_local_item,parent,false);
                    emojiHolder = new LocalEmoHolder();
                    emojiHolder.imageView = (SpImageView) convertView.findViewById(R.id.local_emo_img);
                    emojiHolder.imageView.setHeightRatio(1f);
                    convertView.setBackgroundResource(R.drawable.local_emo_background);
                    convertView.setTag(emojiHolder);
                }
                emojiHolder = (LocalEmoHolder) convertView.getTag();
                convertView.setVisibility(View.VISIBLE);
                if (position > emoticons.size() - 1) { //超出数据范围
                    convertView.setVisibility(View.INVISIBLE);
                    return convertView;
                }
                emoticon = emoticons.get(position);
                if(emoticon.getId()==null) { //空Emoticon,显示删除按键
                    emojiHolder.imageView.setImageResource(R.drawable.del);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fastLog("点击删除按钮.");
                            onDeleteListener.onDelete();
                        }
                    });
                }else { //正常显示emoticon
                    final String localEmoPath = "assets://" + emoticon.getFullPath();
                    emojiHolder.imageView.displayImage(localEmoPath);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emoticonSendListener.onSend(emoticon);
                            fastLog("点击发送本地表情 : " + emoticon.getId() + "\npath : " + localEmoPath);
                        }
                    });

                }
                return convertView;

            case TYPE_CUSTOM:
                LocalEmoHolder customHolder;
                if(convertView==null){
                    convertView = layoutInflater.inflate(R.layout.keyboard_grid_local_item_custom,parent,false);
                    customHolder = new LocalEmoHolder();
                    customHolder.imageView = (SpImageView) convertView.findViewById(R.id.image);
                    customHolder.imageView.setHeightRatio(1f);
                    customHolder.mainView = convertView.findViewById(R.id.main_view);
                    customHolder.mainView.setBackgroundResource(R.drawable.local_emo_background);
                    convertView.setTag(customHolder);
                }
                customHolder = (LocalEmoHolder) convertView.getTag();
                convertView.setVisibility(View.VISIBLE);
                //调整条目宽度
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.width = customItemWidth;
                convertView.setLayoutParams(layoutParams);
                if (position > emoticons.size() - 1) { //超出数据范围
                    convertView.setVisibility(View.INVISIBLE);
                    return convertView;
                }
                emoticon = emoticons.get(position);

                if(emoticon.getId()==null) { //空Emoticon,显示删除按键
                    customHolder.imageView.setImageResource(R.drawable.del);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fastLog("点击删除按钮.");
                            onDeleteListener.onDelete();
                        }
                    });
                }else {
                    final String localEmoPath = "assets://" + emoticon.getFullPath();
                    customHolder.imageView.displayImage(localEmoPath);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emoticonSendListener.onSend(emoticon);
                            fastLog("点击发送本地表情 : " + emoticon.getId() + "\npath : " + localEmoPath);
                        }
                    });
                }
                return convertView;

            case TYPE_VOICE:
                LocalEmoTextHolder emoTextHolder;
                if(convertView==null){
                    convertView = layoutInflater.inflate(R.layout.keyboard_grid_local_item_text,parent,false);
                    emoTextHolder = new LocalEmoTextHolder();
                    emoTextHolder.textView = (TextView) convertView.findViewById(R.id.text_view);
                    convertView.setTag(emoTextHolder);
                }
                emoTextHolder = (LocalEmoTextHolder) convertView.getTag();
                convertView.setVisibility(View.VISIBLE);
                //调整行高
                ViewGroup.LayoutParams voiceParams = convertView.getLayoutParams();
                voiceParams.height = customItemHeight;
                convertView.setLayoutParams(voiceParams);

                if (position > emoticons.size() - 1) { //超出数据范围
                    convertView.setVisibility(View.INVISIBLE);
                    return convertView;
                }
                emoticon = emoticons.get(position);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emoticonSendListener.onSend(emoticon);
                    }
                });
                String content = emoticon.getDescription();
                emoTextHolder.textView.setText(content);
                return convertView;
        }
        return convertView;
    }

    public void setEmoticonSendListener(EmoticonSendListener emoticonSendListener) {
        this.emoticonSendListener = emoticonSendListener;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    class LocalEmoHolder{
        SpImageView imageView;
        View mainView;
    }

    class LocalEmoTextHolder{
        TextView textView;
    }
}


/**
 * 页数指示 小点/滚动条
 *
 * 根据 ViewPager 来进行调整
 */
class KeyboardPageNav extends FrameLayout {
    private Context context;
    private View mainView;
    private HorizontalListView dotListView;
    private DotAdapter dotAdapter;
    private ImageView scrollBar;

    public KeyboardPageNav(Context context) {
        super(context);
        constructView(context);
    }

    public KeyboardPageNav(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public KeyboardPageNav(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KeyboardPageNav(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context) {
        this.context = context;
        mainView = LayoutInflater.from(context).inflate(R.layout.keyboard_nav_dots, null);
        addView(mainView);
        scrollBar = (ImageView) mainView.findViewById(R.id.scroll_bar);
        dotListView = (HorizontalListView) mainView.findViewById(R.id.nav_dots);
        dotAdapter = new DotAdapter(context);
        dotListView.setAdapter(dotAdapter);
    }

    int count = 0;
    int current = -1;

    public void setCount(int count, int current) {
        this.count = count;
        //计算navDots宽度
        final Resources resources = context.getResources();
        int navWidth = count *
                (resources.getDimensionPixelSize(R.dimen.keyboard_dot_height)
                        + 2 * resources.getDimensionPixelSize(R.dimen.keyboard_dot_margin));
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(navWidth, dotListView.getLayoutParams().height);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dotListView.setLayoutParams(params);
        this.current = current;
        dotAdapter.notifyDataSetChanged();
    }

    public void onDestroy(){
        removeCallbacks(scrollbarRunnable);
        scrollbarRunnable = null;
    }

    /**
     * 滚动条
     * 1.翻页过来的 : 给出左侧确切位置
     * 2.点击列表来的 : 一定是第一页
     */
    private Runnable scrollbarRunnable;

    protected void showScrollbar(boolean show, float start) {
        if (show) {
            scrollBar.setVisibility(VISIBLE);
            dotListView.setVisibility(INVISIBLE);
            View left = findViewById(R.id.left_margin);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollBar.getLayoutParams();
            LinearLayout.LayoutParams marginParams = (LinearLayout.LayoutParams) left.getLayoutParams();
            //单位长度
            int totalWidth = getWidth() - 2 * getResources().getDimensionPixelSize(R.dimen.keyboard_scrollbar_margin);
            final int w = (int) (totalWidth / (float) dotAdapter.getItemCount());
            if (dotAdapter.getItemCount() <= 0) {
                params.width = 0;
            } else {
                params.width = w;
            }
            marginParams.width = w * current + (int) (start * w);
//            fastLog("left margin : " + (w * current + (int) (start * w)));
            left.setLayoutParams(marginParams);
            left.forceLayout();
            scrollBar.clearAnimation();
            final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(150);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    scrollBar.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            removeCallbacks(scrollbarRunnable);
            scrollbarRunnable = new Runnable() {
                @Override
                public void run() {
                    scrollBar.startAnimation(alphaAnimation);
                }
            };
            postDelayed(scrollbarRunnable, 500);
        } else {
            scrollBar.setVisibility(GONE);
            dotListView.setVisibility(VISIBLE);
        }
    }

    class DotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private LayoutInflater layoutInflater;

        public DotAdapter(Context context) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = layoutInflater.inflate(R.layout.float_nav_dot_item, parent, false);
            DotHolder holder = new DotHolder(convertView);
            holder.selectedDot = convertView.findViewById(R.id.dot_img_selected);
            holder.unselectedDot = convertView.findViewById(R.id.dot_img_unselected);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            DotHolder holder = (DotHolder) viewHolder;
            holder.unselectedDot.setVisibility(VISIBLE);
            holder.selectedDot.setVisibility(GONE);
            if (position == current) {
                holder.selectedDot.setVisibility(VISIBLE);
                holder.unselectedDot.setVisibility(GONE);
            }
        }

        @Override
        public int getItemCount() {
            return count;
        }

        class DotHolder extends RecyclerView.ViewHolder {
            View selectedDot, unselectedDot;

            public DotHolder(View itemView) {
                super(itemView);
            }
        }
    }
}

/**
 * 列表导航，显示封面
 */
class ListNavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private KeyboardListChangeListener listChangeListener = new KeyboardListChangeListener() {
        @Override
        public void onListChange(UserList lastList, UserList currentList) {
        }
    };
    private boolean localEmoticonEnabled = false;

    UserList getCurrentList() {
        return currentList;
    }

    void setCurrentList(UserList currentList) {
        this.currentList = currentList;
        notifyDataSetChanged();
    }

    public void setLocalEmoticonEnabled(boolean localEmoticonEnabled){
        this.localEmoticonEnabled = localEmoticonEnabled;
        notifyDataSetChanged();
    }

    private UserList currentList;

    public ListNavAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    void setUserLists(ArrayList<UserList> userLists) {
        this.userLists = userLists;
        if (currentList == null && userLists.size() > 0) {
            currentList = userLists.get(0);
            listChangeListener.onListChange(null, currentList);
        }
        notifyDataSetChanged();
    }

//    public void setOnListNavClickListener(View.OnClickListener onListNavClickListener){
//        this.onListNavClickListener = onListNavClickListener;
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.keyboard_list_nav_item, parent, false);
        //横向显示五个列表
        ListNavHolder holder = new ListNavHolder(convertView);
        holder.cover = (SpImageView) convertView.findViewById(R.id.float_list_cover);
        holder.divider = convertView.findViewById(R.id.divider);
        holder.topdivider = convertView.findViewById(R.id.top_divider);
        holder.backHole = (ImageView) convertView.findViewById(R.id.back_hole);
        holder.favorIcon = convertView.findViewById(R.id.favor_icon);
        holder.localEmoIcon = convertView.findViewById(R.id.local_emo_icon);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ListNavHolder holder = (ListNavHolder) viewHolder;
        holder.cover.setVisibility(View.VISIBLE);
        holder.divider.setVisibility(View.VISIBLE);
        holder.topdivider.setVisibility(View.VISIBLE);
        holder.backHole.setVisibility(View.VISIBLE);
        holder.favorIcon.setVisibility(View.GONE);
        holder.localEmoIcon.setVisibility(View.GONE);
        holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));

        if (FacehubApi.getApi().getUser().isLogin() && position == getItemCount() - 1) { //最后一个:设置
            holder.cover.setImageResource(R.drawable.emo_keyboard_setting);
            holder.backHole.setImageResource(R.drawable.white_ball);
            holder.divider.setVisibility(View.GONE);
            holder.userList = null;
            return;
        }

        holder.backHole.setImageResource(R.drawable.white_ball_with_frame);
        holder.userList = userLists.get(position);
        holder.cover.displayFile(null);
        if(localEmoticonEnabled){ //有默认列表
            if(holder.userList.isEmojiList()){
                if(holder.userList.isKaomojiList()) {
                    holder.backHole.setVisibility(View.GONE);
                    holder.cover.displayImage(holder.userList.getCover().getThumbPath());
                }else{
                    holder.cover.displayCircleUri(holder.userList.getCover().getThumbPath());
                }
            }else if(holder.userList instanceof LocalList
                    && ((LocalList)holder.userList).getLocalType().equals(LOCAL_EMO_EMOJI)){
//            if(position==0){
                holder.cover.setVisibility(View.GONE);
                holder.backHole.setVisibility(View.GONE);
                holder.favorIcon.setVisibility(View.GONE);
                holder.localEmoIcon.setVisibility(View.VISIBLE);
            }else if(holder.userList instanceof LocalList
                    && !((LocalList)holder.userList).getLocalType().equals(LOCAL_EMO_EMOJI)){ //自定义列表，显示封面
                String coverPath = "assets://"+ userLists.get(position).getCover().getThumbPath();
                holder.cover.displayCircleAssets(coverPath);
            }else if(holder.userList.isDefaultFavorList()){
                holder.cover.setVisibility(View.GONE);
                holder.backHole.setVisibility(View.GONE);
                holder.favorIcon.setVisibility(View.VISIBLE);
            }else if (userLists.get(position).getCover() != null
                    && userLists.get(position).getCover().getThumbPath() != null) {
                holder.cover.displayCircleImage(userLists.get(position).getCover().getThumbPath());
            } else if (userLists.get(position).getAvailableEmoticons().size() > 0
                    && userLists.get(position).getAvailableEmoticons().get(0).getThumbPath() != null) {
                holder.cover.displayCircleImage(userLists.get(position).getAvailableEmoticons().get(0).getThumbPath());
            } else {
                holder.cover.displayCircleImage(R.drawable.white_ball);
            }
        }else {
            if(holder.userList.isEmojiList()){
                if(holder.userList.isKaomojiList()) {
                    holder.backHole.setVisibility(View.GONE);
                    holder.cover.displayImage(holder.userList.getCover().getThumbPath());
                }else{
                    holder.cover.displayCircleUri(holder.userList.getCover().getThumbPath());
                }
            }else if(holder.userList.isDefaultFavorList()){
//            if (position == 0) { //默认收藏
                holder.cover.setVisibility(View.GONE);
                holder.backHole.setVisibility(View.GONE);
                holder.favorIcon.setVisibility(View.VISIBLE);
            } else if (userLists.get(position).getCover() != null
                    && userLists.get(position).getCover().getThumbPath() != null) {
                holder.cover.displayCircleImage(userLists.get(position).getCover().getThumbPath());
            } else if (userLists.get(position).getAvailableEmoticons().size() > 0
                    && userLists.get(position).getAvailableEmoticons().get(0).getThumbPath() != null) {
                holder.cover.displayCircleImage(userLists.get(position).getAvailableEmoticons().get(0).getThumbPath());
            } else {
                holder.cover.displayCircleImage(R.drawable.white_ball);
            }
        }

        if (this.currentList != null
                && this.currentList.getId().equals(holder.userList.getId())) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.facehub_background, context.getTheme()));
//            } else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.facehub_background));
                holder.topdivider.setVisibility(View.GONE);
//            }
        }

        if (this.currentList != null
                && (userLists.indexOf(currentList) == position + 1)) {
            holder.divider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
//        if(localEmoticonEnabled) {
//            return userLists.size() + 2; //默认列表+编辑按钮
//        }else {
        if(FacehubApi.getApi().getUser().isLogin()) {
            return userLists.size() + 1;
        }else {
            return userLists.size();
        }
//        }
    }

    void setListChangeListener(KeyboardListChangeListener listChangeListener) {
        this.listChangeListener = listChangeListener;
    }

    class ListNavHolder extends RecyclerView.ViewHolder {
        SpImageView cover;
        ImageView backHole;
        View divider,topdivider, favorIcon,localEmoIcon;
        UserList userList;

        public ListNavHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogX.i("点击列表 : " + userList);
                    if (userList == null) {
                        //TODO:进入个人列表编辑
//                        Intent intent = new Intent(v.getContext(), ListsManageActivity.class);
                        Intent intent = new Intent(v.getContext(), ListsManageActivityNew.class);
                        v.getContext().startActivity(intent);
                    } else if (userList != currentList) { //切换列表
                        listChangeListener.onListChange(currentList, userList);
                        currentList = userList;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

}

/**
 * Created by SETA on 2016/3/18.
 * 用于监听表情键盘的列表切换事件
 */
interface KeyboardListChangeListener {
    void onListChange(UserList lastList, UserList currentList);
}




