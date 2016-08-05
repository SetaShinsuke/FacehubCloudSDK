package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.StoreDataContainer;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.PackageCollectEvent;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.fragments.BaseFragment;
import com.azusasoft.facehubcloudsdk.fragments.SearchEmoFragment;
import com.azusasoft.facehubcloudsdk.fragments.SearchPackFragment;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FlowLayout;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnTabClickListener;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnTouchEffect;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ResizablePager;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SearchIndicator;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.FacehubApi.themeOptions;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/6/21.
 * 搜索页
 */
public class SearchActivity extends BaseActivity {
    private Context context;
    private EditText editText;
    private RecyclerViewEx hotHistoryRecyclerView;
    private HotHistoryAdapter hotHistoryAdapter;

    private View resultArea;
    private SearchIndicator searchIndicator;
    private ResizablePager resultPager;
    private SearchEmoFragment searchEmoFragment;
    private SearchPackFragment searchPackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fastLog("Search Page On create .");
        setContentView(R.layout.activity_search);
        this.context = this;
        //通知栏颜色
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
//        }
//        findViewById(R.id.search_title_bar).setBackgroundColor(themeOptions.getTitleBgColor());
        View title = findViewById(R.id.search_title_bar);
        ViewUtilMethods.setBackgroundForView(title,themeOptions.getTitleBgDrawable());

        resultArea = findViewById(R.id.search_result);
        searchIndicator = (SearchIndicator) findViewById(R.id.search_indicator);
        resultPager = (ResizablePager) findViewById(R.id.result_pager);

        editText = (EditText) findViewById(R.id.edit_text_search);
        hotHistoryRecyclerView = (RecyclerViewEx) findViewById(R.id.search_hot_tag_history);
        editText.setFocusableInTouchMode(false);

        View cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnTouchListener(new OnTouchEffect());
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        hotHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false) );
        hotHistoryAdapter = new HotHistoryAdapter(this);
        hotHistoryRecyclerView.setAdapter(hotHistoryAdapter);

        hotHistoryRecyclerView.setVisibility(View.VISIBLE);
        resultArea.setVisibility(View.GONE);

        searchIndicator.setColor(themeOptions.getThemeColor());

        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.setFocusableInTouchMode(true);
            }
        });
        initData();
        initListeners();
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

    private void initData(){
        //热门标签
        hotHistoryAdapter.setHotTags(FacehubApi.getApi().getHotTags(new ResultHandlerInterface() {
            @Override
            public void onResponse(Object response) {
                hotHistoryAdapter.setHotTags( ((ArrayList<String>)response) );
            }

            @Override
            public void onError(Exception e) {
                LogX.e("加载热门标签出错 : " + e);
            }
        }));

        //搜索记录
        hotHistoryAdapter.setHistories(FacehubApi.getApi().getSearchHistories());

        //搜索结果
        final List<BaseFragment> fragments = new ArrayList<>();
        searchPackFragment = new SearchPackFragment();
        searchEmoFragment = new SearchEmoFragment();
        fragments.add(searchPackFragment);
        fragments.add(searchEmoFragment);
        resultPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return position == 0 ? "表情包" : "表情单品";
            }
        });
    }

    private void initListeners(){
        resultPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                searchIndicator.scroll2Page(position,positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        searchIndicator.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabClick(int page) {
                if(page <= resultPager.getAdapter().getCount()-1) {
                    fastLog("点击滚动到page : " + page);
                    resultPager.setCurrentItem(page,true);
                }
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {
                    search();
                    return true;
                }
                return false;
            }
        });
        hotHistoryAdapter.setOnTagItemClickListener(new HotHistoryAdapter.OnTagItemClickListener() {
            @Override
            public void onItemClick(String tag) {
                editText.setText(tag+"");
                search();
            }
        });
    }

    private void search(){
        final String keyword = editText.getText()+"";
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(this, "请输入关键词!", Toast.LENGTH_SHORT).show();
            return;
        }
        //把搜索的记录添加到历史记录的数据库中
        StoreDataContainer.getDataContainer().addSearchHistoriy(this,keyword);
        hotHistoryAdapter.notifyDataSetChanged();
        resultArea.setVisibility(View.VISIBLE);
        resultArea.post(new Runnable() {
            @Override
            public void run() {
                searchPackFragment.search(keyword);
                searchEmoFragment.search(keyword);
            }
        });
    }

    public void onEvent(DownloadProgressEvent event) {
        searchPackFragment.onEvent(event);
    }

    public void onEvent(PackageCollectEvent event) {
        searchPackFragment.onEvent(event);
    }

    public void onEvent(ExitViewsEvent exitViewsEvent) {
        finish();
    }

}

class HotHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;
    private final int TYPE_TITLE = 0;
    private final int TYPE_HOT= 1;
    private final int TYPE_HISTORY = 2;
    private final int TYPE_CLEAR = 3;
    private String[] titles = new String[]{"热门搜索","搜索历史"};
    private ArrayList<String> hotTags = new ArrayList<>();
    private ArrayList<String> histories = new ArrayList<>();
    private boolean isHotLoaded = false;
    private OnTagItemClickListener onTagItemClickListener = new OnTagItemClickListener() {
        @Override
        public void onItemClick(String tag) {

        }
    };

    public HotHistoryAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setHotTags(ArrayList<String> hotTags) {
        this.hotTags = hotTags;
        isHotLoaded = false;
        notifyDataSetChanged();
    }

    public void setHistories(ArrayList<String> histories) {
        this.histories = histories;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(histories.size()==0){
            return 2;
        }
        return histories.size() + titles.length + 2; //历史记录 + 标题x2 + 热门标签 + 清除记录
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
            case 2:
                return TYPE_TITLE;
            case 1:
                return TYPE_HOT;
            default:
                if (getItemCount() - 1 == position) {
                    return TYPE_CLEAR;
                }
                return TYPE_HISTORY;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_TITLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hottag_history_title, parent, false);
                return new TitleHolder(view);
            case TYPE_HOT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hottag_history_hottags, parent, false);
                return new HotTagHolder(view);
            case TYPE_HISTORY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hottag_history_historys, parent, false);
                return new HistoryHolder(view);
            case TYPE_CLEAR:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hottag_history_clear, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int itemCount = getItemCount();
                        FacehubApi.getApi().clearSearchHistory();
                        notifyItemRangeRemoved(2, itemCount + 2);
                    }
                });
                return new ClearHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_TITLE:
                TitleHolder titleHolder = (TitleHolder) holder;
                if (position == 0) {
                    titleHolder.loadData(titles[0]);
                } else if (position == 2) {
                    titleHolder.loadData(titles[1]);
                }
                break;
            case TYPE_HOT:
                if (isHotLoaded) {
                    return;
                }
                HotTagHolder hotTagsHolder = (HotTagHolder) holder;
                hotTagsHolder.flowLayout.removeAllViews();
                for (String tag : hotTags) {
                    hotTagsHolder.loadData(tag);
                }
                hotTagsHolder.setOnItemClickListener(onTagItemClickListener);
                if (hotTags.size() > 0) {
                    isHotLoaded = true;
                }
                break;
            case TYPE_HISTORY:
                position = position - 3;
                HistoryHolder historyViewHolder = (HistoryHolder) holder;
                final String tag = histories.get(position);
                historyViewHolder.loadData(tag);
                historyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onTagItemClickListener.onItemClick(tag);
                    }
                });
                historyViewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove(tag);
                    }
                });
                break;
        }
    }

    private void remove(String history) {
        int index = histories.indexOf(history);
        histories.remove(history);
        if (histories.size() > 0) {
            notifyItemRemoved(index + 3);
        } else {
            notifyItemRangeRemoved(2, getItemCount() + 2);
        }
    }

    public void setOnTagItemClickListener(OnTagItemClickListener onTagItemClickListener) {
        this.onTagItemClickListener = onTagItemClickListener;
    }

    class TitleHolder extends RecyclerView.ViewHolder{
        private TextView textView;

        public TitleHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_hottag_title);
        }

        public void loadData(String title) {
            textView.setText(title);
        }
    }

    class HotTagHolder extends RecyclerView.ViewHolder{

        private final FlowLayout flowLayout;

        public HotTagHolder(View itemView) {
            super(itemView);
            flowLayout = (FlowLayout) itemView;
        }

        public void loadData(final String tag) {
            TextView hotView = (TextView) View.inflate(flowLayout.getContext(), R.layout.hot_tag, null);
            hotView.setTextColor(themeOptions.getThemeColor());
            ViewUtilMethods.addColorFilter(hotView.getBackground(),themeOptions.getThemeColor());
            hotView.setText(tag);
            hotView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTagItemClickListener.onItemClick(tag);
                }
            });
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(ViewUtilMethods.dip2px(context,3), 0, ViewUtilMethods.dip2px(context,3), 0);
            flowLayout.addView(hotView, lp);
        }

        private OnTagItemClickListener onTagItemClickListener;

        public void setOnItemClickListener(OnTagItemClickListener listener) {
            onTagItemClickListener = listener;
        }
    }

    class HistoryHolder extends RecyclerView.ViewHolder{
        public TextView mHistory;
        public View mDelete;

        public HistoryHolder(View itemView) {
            super(itemView);
            mHistory = (TextView) itemView.findViewById(R.id.item_history_name);
            mDelete = itemView.findViewById(R.id.item_history_delete);
        }

        public void loadData(final String history) {
            mHistory.setText(history);
        }
    }

    class ClearHolder extends RecyclerView.ViewHolder{

        public ClearHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnTagItemClickListener {
        void onItemClick(String tag);
    }
}
