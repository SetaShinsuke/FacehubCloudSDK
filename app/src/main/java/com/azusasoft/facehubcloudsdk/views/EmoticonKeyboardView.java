package com.azusasoft.facehubcloudsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
import com.azusasoft.facehubcloudsdk.api.utils.UtilMethods;
import com.azusasoft.facehubcloudsdk.views.viewUtils.HorizontalListView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.views.EmoticonKeyboardView.NUM_ROWS;

/**
 * Created by SETA on 2016/3/16.
 */
public class EmoticonKeyboardView extends FrameLayout {
    private Context mContext;
    private View mainView;
    protected final static int NUM_ROWS = 2;

    private ViewPager emoticonPager;
    private KeyboardPageNav keyboardPageNav;
    private HorizontalListView listNavListView;
    //TODO:根据屏幕宽度计算每页显示几张表情
    //TODO:屏幕旋转时刷新键盘的接口

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

    private void constructView(Context context){
        mContext = context;
        this.mainView = LayoutInflater.from(context).inflate(R.layout.emoticon_keyboard,null);
        addView(mainView);

        View addListView = findViewById(R.id.add_list);
        ImageView addListBtn = (ImageView) addListView.findViewById(R.id.float_list_cover);
        addListBtn.setImageResource(R.drawable.emo_keyboard_add);

        this.emoticonPager = (ViewPager) mainView.findViewById(R.id.emoticon_pager);
        this.keyboardPageNav = (KeyboardPageNav) mainView.findViewById(R.id.keyboard_page_nav);
        this.listNavListView = (HorizontalListView) mainView.findViewById(R.id.list_nav);

        ListNavAdapter listNavAdapter = new ListNavAdapter(mContext);
        listNavListView.setAdapter(listNavAdapter);

        int numColumns = getNumColumns();
        final EmoticonPagerAdapter emoticonPagerAdapter = new EmoticonPagerAdapter(context, numColumns);
        this.emoticonPager.setAdapter(emoticonPagerAdapter);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) emoticonPager.getLayoutParams();
        layoutParams.height = NUM_ROWS*mContext.getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);

        ArrayList<UserList> userLists = new ArrayList<>(UserListDAO.findAll());
        emoticonPagerAdapter.setUserLists( userLists );
        listNavAdapter.setUserLists( userLists );

        keyboardPageNav.setCount(emoticonPagerAdapter.getCount(), 0);

        emoticonPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                keyboardPageNav.setCount(emoticonPagerAdapter.getCount(), position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void onScreenWidthChange(){
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) emoticonPager.getLayoutParams();
        layoutParams.height = NUM_ROWS*mContext.getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        ((EmoticonPagerAdapter)emoticonPager.getAdapter()).setNumColumns(getNumColumns());
    }
    private int getNumColumns(){
        int screenWith = UtilMethods.getScreenWidth(mContext);
        int itemWidth  = mContext.getResources().getDimensionPixelSize(R.dimen.keyboard_grid_item_width);
        return screenWith/itemWidth;
    }
}

/**
 * 显示表情的Pager
 *
 * 总页数 :         Total = 列表个数n * 每个列表占用页数p ;
 * 每个列表占用页数:     P = 表情数E / 每页表情数s (向上取整) ;
 * 每页表情数 :         s = 列数c * 2(行数);
 */
class EmoticonPagerAdapter extends PagerAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private int numColumns = 4;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private ArrayList<PageHolder> pageHolders = new ArrayList<>();

    public EmoticonPagerAdapter(Context context , int numColumns){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.numColumns = numColumns;
    }

    protected void setNumColumns(int numColumns){
        this.numColumns = numColumns;
        notifyDataSetChanged();
    }

    protected void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        pageHolders.clear();
        int s = NUM_ROWS * numColumns;
        for (UserList userList:userLists){ //每个列表
            int pagesOfThisList = (int)Math.ceil((userList.getEmoticons().size() / (float) s)); //这个列表所占的页数

            if(pagesOfThisList==0){ //空列表占位
                PageHolder pageHolder = new PageHolder();
                pageHolder.userList = userList;
                pageHolders.add(pageHolder);
            }

            for(int i=0;i<pagesOfThisList ;i++){ //每一页
                PageHolder pageHolder = new PageHolder();
                pageHolder.userList = userList;
                int start = s * i;
                int end = Math.min( userList.getEmoticons().size() , (i+1)*s );
                pageHolder.divide( start , end );
                pageHolders.add(pageHolder);
                fastLog("------------------------------");
                fastLog("页码 : " + pageHolders.indexOf(pageHolder));
                fastLog("start : " + start + " | end : " + end);
                fastLog("表情数 : " + pageHolder.emoticons.size());
                fastLog("------------------------------");
            }
        }
        fastLog("总页数 : " + pageHolders.size());
        notifyDataSetChanged();
    }

    protected int getPageCount(){
        return pageHolders.size();
    }

    @Override
    public int getCount() {
        return pageHolders.size();
    }

    /**
     * 方法2 :
     *    { page0 , page1 , page2 , ... }
     *    其中 :
     *      page = { emo0 , emo1 , ... }
     *
     *  返回: pages.get( pos );
     */
    private ArrayList<Emoticon> getEmoticonsByPagePos(int position){
        return pageHolders.get(position).emoticons;
    }


    //region 方法1
    /**
     * 查找 page 页的emoticons 是哪些
     * 步骤 :
     *   1.查出是哪个列表;
     *   2.查出列表内开始结束的下标;
     *   3.根据下标拿到emoticons
     */
//    private ArrayList<Emoticon> getEmoticonsByPagePos( int page ){
//        ArrayList<Emoticon> emoticons = new ArrayList<>();
//        if(userLists.isEmpty()){
//            return emoticons;
//        }
//        UserList destList = userLists.get(0);
//        int s = NUM_ROWS * numColumns;
//        int pageCursor = 0; //开始迭代页数
//        int pagesOfThisList = 0;
//        for(UserList userList:userLists){
//            pagesOfThisList = (int)Math.ceil((userList.getEmoticons().size() / (float) s)); //某个列表占用的页数
//            if( pageCursor + pagesOfThisList >= page){ //所需的emoticons就在这个列表.
//                destList = userList;
//                // pageCursor : 停在上个列表的尾端
//                // pageOnThisList : destList占用的总页数
//                break;
//            }
//        }
//        if(destList.getEmoticons().isEmpty()){
//            return emoticons;
//        }
//
//        int destPageInList = page - pageCursor;
//        int startIndex = 0;
//        int endIndex = 0;
//        for(int i=0;i<pagesOfThisList;i++){
//            if( i == destPageInList ){ //要查找的页
//                endIndex = Math.min( startIndex+s , destList.getEmoticons().size() );
//                break;
//            }
//            startIndex += s; //+1页
//        }
//
//        for(int i=0;i<destList.getEmoticons().size();i++){
//            if(i>=startIndex && i<=endIndex){
//                emoticons.add( destList.getEmoticons().get(i) );
//            }
//        }
//        return emoticons;
//    }
    //endregion

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.keyboard_pager_item, container, false);
        GridView keyboardGrid = (GridView) itemView.findViewById(R.id.grid_view);
        keyboardGrid.setNumColumns(numColumns);
        KeyboardEmoticonGridAdapter adapter = new KeyboardEmoticonGridAdapter(context,numColumns);
        keyboardGrid.setAdapter(adapter);
        adapter.setEmoticons( getEmoticonsByPagePos(position) );
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView( (View)object );
    }

    //用于记录每页的list & emoticons
    class PageHolder{
        UserList userList;
        ArrayList<Emoticon> emoticons = new ArrayList<>();
        void divide(int start,int end){
            emoticons.clear();
            for (int i=0;i<userList.getEmoticons().size();i++){
                if(i>=start && i<end){
                    emoticons.add( userList.getEmoticons().get(i) );
                }
            }
        }
    }
}

/**
 * 表情Grid的Adapter
 */
class KeyboardEmoticonGridAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private int numColumns = 4;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();

    public KeyboardEmoticonGridAdapter(Context context , int numColumns){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.numColumns = numColumns;
    }

    protected void setEmoticons(ArrayList<Emoticon> emoticons){
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.numColumns*NUM_ROWS;
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
        if(convertView==null){
            convertView = this.layoutInflater.inflate(R.layout.keyboard_grid_item,parent, false);
        }
        convertView.setVisibility(View.VISIBLE);
        TextView textView = (TextView) convertView.findViewById(R.id.text_view);
        textView.setText("");
        if(position>emoticons.size()-1){ //超出数据范围
            convertView.setVisibility(View.INVISIBLE);
        }else {
            textView.setText(""+emoticons.get(position).getId());
        }
        return convertView;
    }
}


/**
 * 页数指示 小点/滚动条
 *
 * 根据 ViewPager 来进行调整
 */
class KeyboardPageNav extends FrameLayout{
    private Context context;
    private View mainView;
    private HorizontalListView dotListView;
    private DotAdapter dotAdapter;

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

    private void constructView(Context context){
        this.context = context;
        mainView = LayoutInflater.from(context).inflate(R.layout.keyboard_nav_dots, null);
        addView(mainView);
        dotListView = (HorizontalListView) mainView.findViewById(R.id.nav_dots);
        dotAdapter = new DotAdapter(context);
        dotListView.setAdapter(dotAdapter);
    }

    int count = 0;
    int current = -1;

    public void setCount(int count , int current) {
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
    public void setCurrent(int current) {
        this.current = current;
        dotAdapter.notifyDataSetChanged();
    }

    class DotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Context context;
        private LayoutInflater layoutInflater;

        public DotAdapter(Context context){
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = layoutInflater.inflate(R.layout.float_nav_dot_item,parent,false);
            DotHolder holder = new DotHolder(convertView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return count;
        }

        class DotHolder extends RecyclerView.ViewHolder{
            public DotHolder(View itemView) {
                super(itemView);
            }
        }
    }
}

/**
 * 列表导航，显示封面
 */
class ListNavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private View.OnClickListener onListNavClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private UserList currentList;

    public ListNavAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.keyboard_list_nav_item,parent,false);
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
        ListNavHolder holder = (ListNavHolder)viewHolder;
        holder.cover.setVisibility(View.VISIBLE);
        holder.divider.setVisibility(View.VISIBLE);
        holder.backHole.setVisibility(View.VISIBLE);
        holder.favorIcon.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white, context.getTheme()));
        }else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        }

        if(position==0){ //默认收藏
            holder.cover.setVisibility(View.GONE);
            holder.backHole.setVisibility(View.GONE);
            holder.favorIcon.setVisibility(View.VISIBLE);
        }else if(position==getItemCount()-1){ //最后一个:设置
            holder.cover.setImageResource(R.drawable.emo_keyboard_setting);
            holder.divider.setVisibility(View.GONE);
        }else if( userLists.get(position).getCover()!=null
                && userLists.get(position).getCover().getFilePath(Image.Size.MEDIUM)!=null){
            //TODO:显示封面
            holder.cover.displayCircleImage(R.drawable.test);
        }else if(userLists.get(position).getEmoticons().size()>0
                && userLists.get(position).getEmoticons().get(0).getFilePath(Image.Size.MEDIUM)!=null){
                //TODO:第一张图当封面
            holder.cover.displayCircleImage(R.drawable.test);
        }else {
            //TODO:什么图都没有
            holder.cover.displayCircleImage(R.drawable.test);
        }

        if(this.currentList!=null
                && this.currentList.getId().equals(userLists.get(position).getId())){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.keyboard_background, context.getTheme()));
            }else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.keyboard_background));
            }
        }
    }

    @Override
    public int getItemCount() {
        return userLists.size()+1;
    }

    class ListNavHolder extends RecyclerView.ViewHolder{
        SpImageView cover;
        View divider,backHole, favorIcon;

        public ListNavHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListNavClickListener.onClick(v);
                    //TODO:改变列表样式?
                }
            });
        }
    }

}

