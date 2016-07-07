package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.fragments.BaseFragment;
import com.azusasoft.facehubcloudsdk.fragments.SearchEmoFragment;
import com.azusasoft.facehubcloudsdk.fragments.SearchPackFragment;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FlowLayout;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnTouchEffect;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ResizablePager;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SearchIndicator;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnTabClickListener;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;
import java.util.List;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }
        findViewById(R.id.search_title_bar).setBackgroundColor(FacehubApi.getApi().getThemeColor());
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

//        hotHistoryRecyclerView.setVisibility(View.VISIBLE);
//        resultArea.setVisibility(View.GONE);
        searchIndicator.setColor(FacehubApi.getApi().getThemeColor());

        initData();
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.setFocusableInTouchMode(true);
            }
        });
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

        final List<BaseFragment> fragments = new ArrayList<>();
        searchEmoFragment = new SearchEmoFragment();
        searchPackFragment = new SearchPackFragment();
        fragments.add(searchEmoFragment);
        fragments.add(searchPackFragment);
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

        resultPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                fastLog("翻页offset : " + positionOffset);
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
                hotTagsHolder.setOnItemClickListener(new OnHotTagItemClickListener() {
                    @Override
                    public void onItemClick(String tag) {
//                        mOnClickTagListener.onClickTag(tag);
                        fastLog("点击热门标签 : " + tag);
                    }
                });
                if (hotTags.size() > 0) {
                    isHotLoaded = true;
                }
                break;
            case TYPE_HISTORY:
                position = position - 3;
                Log.d("asdaferw", " posistion:" + position);
                HistoryHolder historyViewHolder = (HistoryHolder) holder;
                final String tag = histories.get(position);
                historyViewHolder.loadData(tag);
                historyViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        mOnClickTagListener.onClickTag(tag);
                        fastLog("点击搜索记录 : " + tag);
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
            hotView.setTextColor(FacehubApi.getApi().getThemeColor());
            ViewUtilMethods.addColorFilter(hotView.getBackground(),FacehubApi.getApi().getThemeColor());
            hotView.setText(tag);
            hotView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onHotTagItemClickListener.onItemClick(tag);
                }
            });
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(ViewUtilMethods.dip2px(context,3), 0, ViewUtilMethods.dip2px(context,3), 0);
            //// TODO: 2016/7/5 标签颜色——主题色
            flowLayout.addView(hotView, lp);
        }

        private OnHotTagItemClickListener onHotTagItemClickListener;

        public void setOnItemClickListener(OnHotTagItemClickListener listener) {
            onHotTagItemClickListener = listener;
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

    public interface OnHotTagItemClickListener {
        void onItemClick(String tag);
    }
}

class ResultPagerAdapter extends PagerAdapter{
    private Context context;

    public ResultPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
