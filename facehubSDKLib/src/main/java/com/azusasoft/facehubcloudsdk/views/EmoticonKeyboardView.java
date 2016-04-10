package com.azusasoft.facehubcloudsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.activities.EmoStoreActivity;
import com.azusasoft.facehubcloudsdk.activities.ListsManageActivity;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonsRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListRemoveEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.GifViewFC;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HorizontalListView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView.LONG_CLICK_DURATION;
import static com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView.NUM_ROWS;

/**
 * Created by SETA on 2016/3/16.
 *
 * 注意！！
 *      1.请务必调用初始化函数 {@link #initKeyboard()} ,否则将无法显示预览;
 *      2.切换横/竖屏时请调用 {@link #onScreenWidthChange()} 更新键盘视图;
 *      3.请设置表情点击的回调 {@link #setEmoticonSendListener(EmoticonSendListener)};
 *
 * {@link ListNavAdapter}中保存当前列表{@link ListNavAdapter#currentList}
 * 数据改变的两种情况 :
 *      1.翻页 : 切换列表、改变导航点;
 *      2.点列表 : 翻页、改变导航点;
 *      注意!
 *          改变导航点时，判断是否要显示为滚动条
 *
 */
public class EmoticonKeyboardView extends FrameLayout {
    private Context mContext;
    private View mainView;
    protected final static int NUM_ROWS = 2;
    protected final static int LONG_CLICK_DURATION = 300;
    private ViewGroup previewContainer;
    private EmoticonSendListener emoticonSendListener = new EmoticonSendListener() {
        @Override
        public void onSend(Emoticon emoticon) {

        }
    };

    private ViewPager emoticonPager;
    private KeyboardPageNav keyboardPageNav;
    private HorizontalListView listNavListView;

    private EmoticonPagerAdapter emoticonPagerAdapter;
    private ListNavAdapter listNavAdapter;
    private ArrayList<UserList> userLists = new ArrayList<>();

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

    private void constructView(final Context context) {
        mContext = context;
        this.mainView = LayoutInflater.from(context).inflate(R.layout.emoticon_keyboard, null);
        addView(mainView);
        EventBus.getDefault().register(this);
        hide();

        View addListView = findViewById(R.id.add_list);
        ImageView addListBtn = (ImageView) addListView.findViewById(R.id.float_list_cover);
        addListBtn.setImageResource(R.drawable.emo_keyboard_add);
        addListView.findViewById(R.id.divider).setVisibility(GONE);
        addListView.setBackgroundColor(getResources().getColor(android.R.color.white));
        addListBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),EmoStoreActivity.class);
                v.getContext().startActivity(intent);
            }
        });

        this.emoticonPager = (ViewPager) mainView.findViewById(R.id.emoticon_pager);
        this.keyboardPageNav = (KeyboardPageNav) mainView.findViewById(R.id.keyboard_page_nav);
        this.listNavListView = (HorizontalListView) mainView.findViewById(R.id.list_nav);

        listNavAdapter = new ListNavAdapter(mContext);
        listNavListView.setAdapter(listNavAdapter);

        int numColumns = getNumColumns();
        emoticonPagerAdapter = new EmoticonPagerAdapter(context, numColumns);
        this.emoticonPager.setAdapter(emoticonPagerAdapter);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) emoticonPager.getLayoutParams();
        layoutParams.height = NUM_ROWS * mContext.getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);

        //// FIXME: 2016/3/30
        userLists = new ArrayList<>(FacehubApi.getApi().getAllUserLists());
        fastLog("userLists size : " + userLists.size());
        emoticonPagerAdapter.setUserLists(userLists);
        listNavAdapter.setUserLists(userLists);

        emoticonPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                UserList lastList = listNavAdapter.getCurrentList();
                if (lastList != emoticonPagerAdapter.getUerListByPage(position)) {
                    listNavAdapter.setCurrentList(emoticonPagerAdapter.getUerListByPage(position));
                }
                UserList currentList = listNavAdapter.getCurrentList();
                keyboardPageNav.setCount(emoticonPagerAdapter.getPageCount(currentList)
                        , emoticonPagerAdapter.getPageIndexInList(currentList, position));
                if (userLists.indexOf(currentList) == 0) {
                    keyboardPageNav.showScrollbar(true, positionOffset);
                } else {
                    keyboardPageNav.showScrollbar(false, 0);
                }

                LinearLayoutManager layoutManager = listNavListView.getLayoutManager();
                int index = userLists.indexOf(currentList);
                if (currentList != lastList
                        && (index < layoutManager.findFirstVisibleItemPosition()
                            || index > layoutManager.findLastVisibleItemPosition()) ) { //切换了列表，滚到相应位置
                    layoutManager.scrollToPositionWithOffset( index , 0 );
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        listNavAdapter.setListChangeListener(new KeyboardListChangeListener() {
            @Override
            public void onListChange(UserList lastList, UserList currentList) {
                fastLog("上一个列表 : " + lastList + "\n切换到列表 : " + currentList);
                int page = emoticonPagerAdapter.getFirstPageOfList(currentList);
                emoticonPager.setCurrentItem(page, false);
                keyboardPageNav.setCount(emoticonPagerAdapter.getPageCount(currentList)
                        , emoticonPagerAdapter.getPageIndexInList(currentList, page));
                if (userLists.indexOf(currentList) == 0) {
                    keyboardPageNav.showScrollbar(true, 0);
                } else {
                    keyboardPageNav.showScrollbar(false, 0);
                }
            }
        });

        emoticonPagerAdapter.setGridItemTouchListener(new GridItemTouchListener() {
            @Override
            public void onItemClick(View view, Object object) {
                if( ! (object instanceof Emoticon) ){
                    return;
                }
                Emoticon emoticon = (Emoticon) object;
                if(emoticon.getId()==null){
                    fastLog("点击 : 进入商店");
                    Intent intent = new Intent(context,EmoStoreActivity.class);
                    context.startActivity(intent);
                    return;
                }
                emoticonSendListener.onSend(emoticon);
            }

            @Override
            public void onItemLongClick(View view, final Emoticon emoticon) {
                if(emoticon.getId()==null){
                    return;
                }
                if(previewContainer!=null){
                    previewContainer.setVisibility(VISIBLE);
                    //TODO:预览表情
                    final GifViewFC gifView = (GifViewFC) previewContainer.findViewById(R.id.preview_image);
                    if(gifView == null){
                        return;
                    }
                    ImageView bubble = (ImageView) previewContainer.findViewById(R.id.preview_bubble);
                    gifView.setVisibility(GONE);
                    emoticon.download2File(Image.Size.FULL, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            gifView.setGifPath(emoticon.getFilePath(Image.Size.FULL));
                            gifView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    gifView.setVisibility(VISIBLE);
                                    fastLog("emoticon path : " + emoticon.getFilePath(Image.Size.FULL));
                                }
                            }, 200);
                        }

                        @Override
                        public void onError(Exception e) {
                            LogX.e("preview error : " + e);
                        }
                    });
                    int top = ViewUtilMethods.getTopOnWindow(view);
                    int left = ViewUtilMethods.getLeftOnWindow(view);
                    int center = left + (int)(view.getWidth()/2f);
                    int previewLeft = (int)(center - getResources().getDimensionPixelSize(R.dimen.keyboard_preview_frame_width)/2f);
                    TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[] {
                            android.R.attr.actionBarSize
                    });

                    float h = actionbarSizeTypedArray.getDimension(0, 0);
                    int previewTop  = top - getResources().getDimensionPixelSize(R.dimen.keyboard_preview_frame_height)
                            - view.getHeight() + getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_padding)
                            + (int)h;
                    int quarterScreen = (int)(ViewUtilMethods.getScreenWidth(context)/4f);
                    if(center<quarterScreen){
                        bubble.setImageResource(R.drawable.preview_frame_left);
                        previewLeft+=(int)(view.getWidth()/2f);
                    }else if (center<quarterScreen*2){
                        bubble.setImageResource(R.drawable.preview_frame_center);
                    }else if(center<quarterScreen*3){
                        bubble.setImageResource(R.drawable.preview_frame_center);
                    }else {
                        bubble.setImageResource(R.drawable.preview_frame_right);
                        previewLeft-=(int)(view.getWidth()/2f);
                    }
                    ViewUtilMethods.changeViewPosition(previewContainer,previewLeft,previewTop);
//                    fastLog("previewTop : " + top + "\npreviewLeft : " + left);
                }
            }

            @Override
            public void onItemOffTouch(View view, Object object) {
//                fastLog("松手 : " + object);
                if(previewContainer!=null){
                    previewContainer.setVisibility(GONE);
                }
            }
        });
    }

    public void setEmoticonSendListener(EmoticonSendListener emoticonSendListener){
        this.emoticonSendListener = emoticonSendListener;
    }

    public void onEvent(UserListRemoveEvent event){
        refresh();
    }
    public void onEvent(EmoticonsRemoveEvent event){
        refresh();
    }
    public void onEvent(PackageCollectEvent event){
        refresh();
    }

    public void refresh(){
        userLists = new ArrayList<>(FacehubApi.getApi().getAllUserLists());
        emoticonPagerAdapter.setUserLists(userLists);
        listNavAdapter.setUserLists(userLists);
    }

//    public void setPreviewContainer(ViewGroup previewContainer){
    public void initKeyboard(){
        //找到keyboard的爹,添加预览的container
        if(getParent()!=null && getParent() instanceof ViewGroup){
            ViewGroup parent = (ViewGroup)getParent();
            this.previewContainer = new FrameLayout(getContext());
            parent.addView(previewContainer);
            LayoutInflater.from(getContext()).inflate(R.layout.keyboard_preview, previewContainer);
            previewContainer.setVisibility(GONE);
        }
    }

    public void onScreenWidthChange() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) emoticonPager.getLayoutParams();
        layoutParams.height = NUM_ROWS * mContext.getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        ((EmoticonPagerAdapter) emoticonPager.getAdapter()).setNumColumns(getNumColumns());
        //保持列表，翻页到该列表第一页
        userLists = new ArrayList<>(FacehubApi.getApi().getAllUserLists());
        emoticonPagerAdapter.setUserLists(userLists);
        listNavAdapter.setUserLists(userLists);
        emoticonPager.setCurrentItem(emoticonPagerAdapter.getFirstPageOfList(listNavAdapter.getCurrentList()), false);
    }

    private int getNumColumns() {
        int screenWith = ViewUtilMethods.getScreenWidth(mContext);
        int itemWidth = mContext.getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        return screenWith / itemWidth;
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void hide(){
        setVisibility(GONE);
    }
}

/**
 * 显示表情的Pager
 * <p/>
 * 总页数 :         Total = 列表个数n * 每个列表占用页数p ;
 * 每个列表占用页数:     P = 表情数E / 每页表情数s (向上取整) ;
 * 每页表情数 :         s = 列数c * 2(行数);
 */
class EmoticonPagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private int numColumns = 4;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private ArrayList<PageHolder> pageHolders = new ArrayList<>();
    private GridItemTouchListener gridItemTouchListener = new GridItemTouchListener() {
        @Override
        public void onItemClick(View view, Object object) {

        }

        @Override
        public void onItemLongClick(View view, Emoticon emoticon) {

        }

        @Override
        public void onItemOffTouch(View view, Object object) {

        }
    };

    public EmoticonPagerAdapter(Context context, int numColumns) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.numColumns = numColumns;
    }

    protected void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        notifyDataSetChanged();
    }

    protected void setUserLists(ArrayList<UserList> userLists) {
        this.userLists = userLists;
        pageHolders.clear();
        int s = NUM_ROWS * numColumns;
        for (UserList userList : userLists) { //每个列表
            int indexOfList = userLists.indexOf(userList);
            ArrayList<Emoticon> emoticonsOfThisList = new ArrayList<>(userList.getEmoticons());
            if (indexOfList == 0) {
                emoticonsOfThisList.add(0, new Emoticon()); //空Emoticon用来显示 加号"+"
            }
            int pagesOfThisList = (int) Math.ceil((emoticonsOfThisList.size() / (float) s)); //这个列表所占的页数

            if (pagesOfThisList == 0) { //空列表占位
                PageHolder pageHolder = new PageHolder();
                pageHolder.userList = userList;
                pageHolders.add(pageHolder);
            }

            for (int i = 0; i < pagesOfThisList; i++) { //每一页
                PageHolder pageHolder = new PageHolder();
                pageHolder.userList = userList;
                int start = s * i;
                int end = Math.min(emoticonsOfThisList.size(), (i + 1) * s);
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

    protected UserList getUerListByPage(int page){
        if(page>pageHolders.size()-1 || pageHolders.get(page)==null){
            return null;
        }
        return pageHolders.get(page).userList;
    }

    protected int getPageIndexInList(UserList currentList , int pageGlobal) {
        int pageInside = pageGlobal - getFirstPageOfList(currentList);
        return pageInside;
    }

    protected int getFirstPageOfList(UserList userList) {
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
        GridView keyboardGrid = (GridView) itemView.findViewById(R.id.grid_view);
        keyboardGrid.setNumColumns(numColumns);
        KeyboardEmoticonGridAdapter adapter = new KeyboardEmoticonGridAdapter(context, numColumns);
        adapter.setGridItemTouchListener(this.gridItemTouchListener);
        keyboardGrid.setAdapter(adapter);
        adapter.setEmoticons(getEmoticonsByPagePos(position));
        container.addView(itemView);
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

    public void setGridItemTouchListener(GridItemTouchListener gridItemTouchListener) {
        this.gridItemTouchListener = gridItemTouchListener;
    }

    //用于记录每页的list & emoticons
    class PageHolder {
        UserList userList;
        ArrayList<Emoticon> emoticons = new ArrayList<>();

        void divide(ArrayList<Emoticon> emoticonsOfThisList, int start, int end) {
            emoticons.clear();
            for (int i = 0; i < emoticonsOfThisList.size(); i++) {
                if (i >= start && i < end) {
                    emoticons.add(emoticonsOfThisList.get(i));
                }
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
    private GridItemTouchListener gridItemTouchListener = new GridItemTouchListener() {
        @Override
        public void onItemClick(View view, Object object) {
        }

        @Override
        public void onItemLongClick(View view, Emoticon emoticon) {
        }

        @Override
        public void onItemOffTouch(View view, Object object) {
        }
    };

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
        return this.numColumns * NUM_ROWS;
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

        convertView.setClickable(false);
        convertView.setOnClickListener(null);
        if (position > emoticons.size() - 1) { //超出数据范围
            convertView.setVisibility(View.INVISIBLE);
            return convertView;
        }

        final Emoticon emoticon = emoticons.get(position);
        if (emoticon.getId() == null) {
            holder.addCross.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
        } else {
            holder.imageView.displayFile(emoticon.getFilePath(Image.Size.FULL));
        }

        final Holder finalHolder = holder;
        final View finalConvertView = convertView;
        holder.clickArea.setOnTouchListener(new View.OnTouchListener() {
            private boolean isTouchedOnce = false; //已经在点击中
            private boolean isLongPressed = false;
            private Handler handler = new Handler();
            Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    isLongPressed = true;
                    fastLog("进入长按状态.");
                    gridItemTouchListener.onItemLongClick(finalConvertView, emoticon);
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
//                boolean flag = false; //move & up 时返回true
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        fastLog("down.");
                        if (isTouchedOnce) {
                            break;
                        }
                        isTouchedOnce = true;
                        finalHolder.backFrame.setVisibility(View.VISIBLE);
                        isLongPressed = false;
                        handler.postDelayed(longPressRunnable, LONG_CLICK_DURATION);
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        flag = true;
                        if (!isLongPressed) { //没有长按
                            //隐藏预览
//                            gridItemTouchListener.onItemOffTouch(v,emoticon); //停止预览
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        fastLog("cancel.");
                        isTouchedOnce = false;
                        isLongPressed = false;
                        finalHolder.backFrame.setVisibility(View.GONE);
                        handler.removeCallbacks(longPressRunnable);
                        gridItemTouchListener.onItemOffTouch(v, emoticon); //停止预览
                        break;
                    case MotionEvent.ACTION_UP:
                        fastLog("up.");
//                        flag = true;
                        isTouchedOnce = false;
                        if (!isLongPressed) {
                            gridItemTouchListener.onItemClick(v, emoticon); //点击
                        } else {
                            gridItemTouchListener.onItemOffTouch(v, emoticon); //停止预览
                        }
                        isLongPressed = false;
                        finalHolder.backFrame.setVisibility(View.GONE);
                        handler.removeCallbacks(longPressRunnable);
                        break;
                    default:
                        break;
                }
//                return flag;
                return true;
            }
        });
        return convertView;
    }

    public void setGridItemTouchListener(GridItemTouchListener gridItemTouchListener) {
        this.gridItemTouchListener = gridItemTouchListener;
    }

    class Holder {
        SpImageView imageView;
        View backFrame;
        View addCross;
        View clickArea;
    }
}


/**
 * 页数指示 小点/滚动条
 * <p/>
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

    /**
     * 滚动条
     * 1.翻页过来的 : 给出左侧确切位置
     * 2.点击列表来的 : 一定是第一页
     */
    private Runnable scrollbarRunnable;
    protected void showScrollbar(boolean show , float start){
        if(show){
            scrollBar.setVisibility(VISIBLE);
            dotListView.setVisibility(INVISIBLE);
            View left = findViewById(R.id.left_margin);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollBar.getLayoutParams();
            LinearLayout.LayoutParams marginParams = (LinearLayout.LayoutParams) left.getLayoutParams();
            //单位长度
            int totalWidth = getWidth()-2*getResources().getDimensionPixelSize(R.dimen.keyboard_scrollbar_margin);
            final int w = (int) ( totalWidth / (float) dotAdapter.getItemCount());
            if(dotAdapter.getItemCount()<=0){
                params.width = 0;
            }else {
                params.width = w;
            }
            marginParams.width = w * current + (int)(start*w);
//            fastLog("left margin : " + (w * current + (int) (start * w)));
            left.setLayoutParams(marginParams);
            left.forceLayout();
            scrollBar.clearAnimation();
            final AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
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
            scrollBar.removeCallbacks( scrollbarRunnable );
            scrollbarRunnable = new Runnable() {
                @Override
                public void run() {
                    scrollBar.startAnimation(alphaAnimation);
                }
            };
            scrollBar.postDelayed(scrollbarRunnable,500);
        }else {
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

    public UserList getCurrentList() {
        return currentList;
    }
    public void setCurrentList(UserList currentList){
        this.currentList = currentList;
        notifyDataSetChanged();
    }

    private UserList currentList;

    public ListNavAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setUserLists(ArrayList<UserList> userLists) {
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
        holder.backHole = convertView.findViewById(R.id.back_hole);
        holder.favorIcon = convertView.findViewById(R.id.favor_icon);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ListNavHolder holder = (ListNavHolder) viewHolder;
        holder.cover.setVisibility(View.VISIBLE);
        holder.divider.setVisibility(View.VISIBLE);
        holder.backHole.setVisibility(View.VISIBLE);
        holder.favorIcon.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white, context.getTheme()));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        if (position == getItemCount() - 1) { //最后一个:设置
            holder.cover.setImageResource(R.drawable.emo_keyboard_setting);
            holder.divider.setVisibility(View.GONE);
            holder.userList = null;
            return;
        }

        holder.userList = userLists.get(position);
        holder.cover.displayFile(null);
        if (position == 0) { //默认收藏
            holder.cover.setVisibility(View.GONE);
            holder.backHole.setVisibility(View.GONE);
            holder.favorIcon.setVisibility(View.VISIBLE);
        } else if (userLists.get(position).getCover() != null
                && userLists.get(position).getCover().getFilePath(Image.Size.FULL) != null) {
            holder.cover.displayCircleImage( userLists.get(position).getCover().getFilePath(Image.Size.FULL));
        } else if (userLists.get(position).getEmoticons().size() > 0
                && userLists.get(position).getEmoticons().get(0).getFilePath(Image.Size.FULL) != null) {
            holder.cover.displayCircleImage(userLists.get(position).getEmoticons().get(0).getFilePath(Image.Size.FULL));
        } else {
            holder.cover.displayCircleImage(R.drawable.white_ball);
        }

        if (this.currentList != null
                && this.currentList.getId().equals(holder.userList.getId())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.facehub_background, context.getTheme()));
            } else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.facehub_background));
            }
        }

        holder.divider.setVisibility(View.VISIBLE);
        if (this.currentList != null
                && (userLists.indexOf(currentList)==position+1) ) {
            holder.divider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userLists.size() + 1;
    }

    public void setListChangeListener(KeyboardListChangeListener listChangeListener) {
        this.listChangeListener = listChangeListener;
    }

    class ListNavHolder extends RecyclerView.ViewHolder {
        SpImageView cover;
        View divider, backHole, favorIcon;
        UserList userList;

        public ListNavHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fastLog("点击列表 : " + userList);
                    if (userList == null) {
                        //TODO:进入个人列表编辑
                        Intent intent = new Intent(v.getContext(), ListsManageActivity.class);
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
    public void onListChange(UserList lastList , UserList currentList);
}

interface GridItemTouchListener{
    public void onItemClick(View view , Object object);
    public void onItemLongClick(View view , Emoticon emoticon );
    public void onItemOffTouch(View view , Object object);
}
