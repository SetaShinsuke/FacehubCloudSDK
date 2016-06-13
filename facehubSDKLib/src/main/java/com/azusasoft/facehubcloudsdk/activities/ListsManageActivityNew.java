package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.UserListPrepareEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.animator.GeneralItemAnimator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.decoration.ItemShadowDecorator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.DraggableItemAdapter;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.ItemDraggableRange;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.ADVRViewUtils;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.logLevel;

/**
 * Created by SETA on 2016/6/3.
 */
public class ListsManageActivityNew extends BaseActivity {
    private Context context;

    private RecyclerView recyclerView;
    private UserListAdapterNew originAdapter;
    private RecyclerView.Adapter adapter;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private boolean isOrdering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_list_manage);
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }

        final FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        recyclerView = (RecyclerViewEx) findViewById(R.id.user_lists_facehub);

        actionbar.showEdit();
        actionbar.setTitle("我的列表");
        actionbar.setEditBtnText("排序");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView.getItemAnimator().isRunning()) {
                    return;
                }
                finish();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        View footer = getLayoutInflater().inflate(R.layout.list_manage_footer,recyclerView,false);
        originAdapter = new UserListAdapterNew(context,footer);

        //拖动manager
        RecyclerViewDragDropManager dragDropManager = new RecyclerViewDragDropManager();
        NinePatchDrawable drawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3);
        dragDropManager.setDraggingItemShadowDrawable( drawable );
        dragDropManager.setCheckCanDropEnabled(true); // !!! this method is required to use onCheckCanDrop()
        adapter = dragDropManager.createWrappedAdapter(originAdapter);

        recyclerView.setAdapter(adapter);

        GeneralItemAnimator itemAnimator = new RefactoredDefaultItemAnimator();
//        ItemNoneChangeAnimator itemAnimator = new ItemNoneChangeAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        actionbar.setOnEditClick(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(recyclerView.getItemAnimator().isRunning()){
                    fastLog("正在执行变换动画.");
                    return;
                }

                /** 退出排序模式 */
                if (isOrdering) {
                    actionbar.setEditBtnText("排序");
                    //退出排序，提交更改
                    FacehubApi.getApi().getUser().setUserLists(userLists);
                    FacehubApi.getApi().getUser().updateLists();
                    ArrayList<String> listIds = new ArrayList<>();
                    for(UserList userList : userLists){
                        listIds.add(userList.getId());
                    }
                    FacehubApi.getApi().reorderUserLists(listIds, new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            LogX.d("排序同步成功 : " + response);
                        }

                        @Override
                        public void onError(Exception e) {
                            LogX.e("排序同步失败 : " + e);
                        }
                    });
                } else { /** 开始排序 */
                    actionbar.setEditBtnText("完成");
                }
                isOrdering = !isOrdering;
                originAdapter.setOrdering(isOrdering);
            }
        });

        originAdapter.setOnFooterLayoutChangeListener(new OnFooterLayoutChangeListener() {
            Runnable footerLayoutTask;

            @Override
            public void onFooterLayoutChange(final View footer) {
                if(footer==null){
                    fastLog("footer null.");
                    return;
                }
                footer.removeCallbacks(footerLayoutTask);
                footerLayoutTask = new Runnable() {
                    @Override
                    public void run() {
                        int bottom = footer.getBottom();
                        fastLog("footer bottom : " + bottom);
                        fastLog("recyclerView bottom : " + recyclerView.getBottom());
                        fastLog("recyclerView height : " + recyclerView.getHeight());

                        int lastVisible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        fastLog("last visible : " + lastVisible);

                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) footer.getLayoutParams();
                        if(lastVisible == recyclerView.getAdapter().getItemCount()-1){
                            Resources resources = context.getResources();
                            int measuredHeight = resources.getDimensionPixelSize(R.dimen.list_manage_subtitle_height)*3
                                       + resources.getDimensionPixelSize(R.dimen.list_manage_item_height)
                                            *(recyclerView.getAdapter().getItemCount()-3);
//                            params.topMargin = recyclerView.getBottom() - bottom;
                            int top = recyclerView.getHeight() - measuredHeight;
                            top = top>0?top:0;
                            params.topMargin = top;
                            fastLog("1");
                        }else {
                            params.topMargin = 0;
                            fastLog("2");
                        }
                        fastLog("topMargin : " + params.topMargin);
                        footer.setLayoutParams(params);
//                        if(bottom > 0 && recyclerView.getChildCount()>0){
//                            View lastChild = recyclerView.getChildAt(recyclerView.getChildCount()-1);
//                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) footer.getLayoutParams();
//                            params.topMargin = recyclerView.getBottom() - lastChild.getBottom();
//                            params.topMargin = recyclerView.getBottom() - bottom - footer.getHeight();
//                            footer.setLayoutParams(params);
//                        }
                    }
                };
                footer.post(footerLayoutTask);
            }
        });

        userLists = new ArrayList<>( FacehubApi.getApi().getUser().getUserLists() );
        originAdapter.setUserLists(userLists);

        if( !FacehubApi.getApi().getUser().silentDownloadAll() ) { //如果没有自动静默下载，提示用户同步
            for (UserList userList : userLists) {
                userList.downloadCover( new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        LogX.e("表情管理页封面下载出错 : " + e);
                    }
                });
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(context, R.drawable.material_shadow_z1)));
        }
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider_h), true));
        dragDropManager.attachRecyclerView(recyclerView);

        EventBus.getDefault().register(this);
    }

    public void onEvent(DownloadProgressEvent event){
        for (int i = 0; i < userLists.size(); i++) {
            if (event.listId.equals(userLists.get(i).getId())) {
                LogX.d(Constants.PROGRESS, "编辑列表下载 on event 进度 : " + event.percentage);
                adapter.notifyItemChanged(originAdapter.getPositionByIndex(i));
            }
        }
    }

    public void onEvent(UserListPrepareEvent event){
        if(event.listId==null){
            return;
        }
        for (int i = 0; i < userLists.size(); i++) {
            if (event.listId.equals(userLists.get(i).getId())) {
                adapter.notifyItemChanged(originAdapter.getPositionByIndex(i));
            }
        }
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
}


/** ========================================== Adapter =========================================== */
class UserListAdapterNew extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                        implements DraggableItemAdapter<RecyclerView.ViewHolder>{
    private Context context;
    private boolean ordering = false;
    private boolean isAnimating = false;
    private long removeAnimationDuration = 500;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private View footer;

    //1.setLists时; 2.删除列表时; 3.模式切换时
    private OnFooterLayoutChangeListener onFooterLayoutChangeListener = new OnFooterLayoutChangeListener() {
        @Override
        public void onFooterLayoutChange(View footer) {
        }
    };

    private final int TYPE_SUBTITLE = 0;
    private final int TYPE_NORMAL = 1;
    private final int TYPE_FOOTER = 2;

    public UserListAdapterNew(Context context,View footer) {
        this.context = context;
        this.footer = footer;
        setHasStableIds(true);
    }

    public void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        notifyDataSetChanged();
        onFooterLayoutChangeListener.onFooterLayoutChange(footer);
    }

    public void setOrdering(boolean ordering) {
        this.ordering = ordering;
        notifyDataSetChanged();
        if(!ordering) {
            onFooterLayoutChangeListener.onFooterLayoutChange(footer);
        }
    }

    protected int getIndexByPosition(int position){
        if(ordering){
            return position;
        }
        if(position==getItemCount()-1){
            return -1;
        }
        if(position==0 || position==2){
            return -1;
        }
        if(position==1){
            return 0;
        }
        return position-2;
    }

    protected int getPositionByIndex(int index){
        if(ordering){
            return index;
        }
        if(index==0){
            return 1;
        }
        return index+2;
    }

    public void setOnFooterLayoutChangeListener(OnFooterLayoutChangeListener onFooterLayoutChangeListener) {
        this.onFooterLayoutChangeListener = onFooterLayoutChangeListener;
    }

    @Override
    public int getItemCount() {
        if(userLists.size()==0){
            return 0;
        }
        if(ordering){
            return userLists.size();
        }
        return userLists.size() + 2 + 1 ; //副标题x2 + footer
    }

    @Override
    public int getItemViewType(int position) {
        if(ordering){
            return TYPE_NORMAL;
        }
        if(position==getItemCount()-1){
            return TYPE_FOOTER;
        }
        if(position==0 || position==2){
            return TYPE_SUBTITLE;
        }
        return TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        if(getItemViewType(position) != TYPE_NORMAL){
            return ViewGroup.NO_ID;
        }
        return userLists.get( getIndexByPosition(position) ).getDbId();
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType) {
            case TYPE_NORMAL:
                v = inflater.inflate(R.layout.lists_manage_item_21, parent, false);
                return new UserListHolderNew(v);
            case TYPE_SUBTITLE:
                v = inflater.inflate(R.layout.list_manage_subtitle_item, parent, false);
                return new SubtitleHolder(v);
            case TYPE_FOOTER:
//                if(footer==null) {
//                    footer = inflater.inflate(R.layout.list_manage_footer, parent, false);
//                }
                return new FooterHolder(footer);
            default:
                throw new IllegalStateException("Unexpected viewType (= " + viewType + ")");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)){
            case TYPE_SUBTITLE:
                SubtitleHolder subtitleHolder = (SubtitleHolder)viewHolder;
                if(position==0){
                    subtitleHolder.setText("默认列表");
                }else if(position==2){
                    subtitleHolder.setText("收藏的表情包");
                }
                break;
            case TYPE_NORMAL:
                final UserListHolderNew holder = (UserListHolderNew)viewHolder;
                int index = getIndexByPosition(position);
                holder.userList = userLists.get( index );
                holder.listName.setText(holder.userList.getName());

                //封面设置
                holder.favorCover.setVisibility(View.GONE);
                holder.coverImage.setVisibility(View.VISIBLE);
                if ( !holder.userList.isDefaultFavorList() && holder.userList.getCover() != null) {
                    holder.coverImage.displayFile(holder.userList.getCover().getThumbPath());
                }
                if (holder.userList.isDefaultFavorList()) {
                    holder.coverImage.setVisibility(View.GONE);
                    holder.favorCover.setVisibility(View.VISIBLE);
                }

                //拖动按钮
                holder.hideTouchView();
                if(ordering && !holder.userList.isDefaultFavorList()){
                    holder.showTouchView();
                }
                //下载按钮
                holder.autoShowDownloadBtn();
                holder.right0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(holder.userList.isDownloading() || holder.userList.isPrepared()){
                            return;
                        }
                        holder.showProgressBar(0f);
                        holder.userList.prepare(new ResultHandlerInterface() {
                            @Override
                            public void onResponse(Object response) {
                                holder.autoShowDownloadBtn();
                            }

                            @Override
                            public void onError(Exception e) {
                                holder.autoShowDownloadBtn();
                                LogX.e("列表管理页,列表下载出错 : " + e);
                            }
                        }, new ProgressInterface() {
                            @Override
                            public void onProgress(double process) {

                            }
                        });
                    }
                });

                //删除按钮
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isAnimating){
                            return;
                        }
                        isAnimating = true;
                        handler.removeCallbacks(task);
                        handler.postDelayed(task,removeAnimationDuration+100);
                        int pos = getPositionByIndex(userLists.indexOf(holder.userList));
                        userLists.remove(holder.userList);
                        FacehubApi.getApi().getUser().getUserLists().remove(holder.userList);
                        fastLog("删除位置 : " + pos);
                        if(pos<0){
                            return;
                        }
                        notifyItemRemoved(pos);
                        onFooterLayoutChangeListener.onFooterLayoutChange(footer);
                        FacehubApi.getApi().removeUserListById(holder.userList.getId());
                    }
                });
                break;

            case TYPE_FOOTER:
                break;
        }
    }

    Handler handler = new Handler();
    Runnable task = new Runnable() {
        @Override
        public void run() {
            isAnimating = false;
        }
    };

    //region 拖动Adapter
    @Override
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder viewHolder, int position, int x, int y) {
        if(!ordering){
            return false;
        }
        if(getItemViewType(position) != TYPE_NORMAL){
            return false;
        }
        UserListHolderNew holder = (UserListHolderNew)viewHolder;
        int index = getIndexByPosition(position);
        if(userLists.get(index).isDefaultFavorList()){
            return false;
        }
        final View containerView = holder.container;
        final View dragHandleView = holder.touchView;

        // x, y --- relative from the itemView's top-left
        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        boolean flag = ADVRViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
        return flag;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerView.ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if(fromPosition==toPosition){
            return;
        }
        int fromIndex = getIndexByPosition(fromPosition);
        int toIndex   = getIndexByPosition(toPosition);
        UserList userList = userLists.remove(fromIndex);
        userLists.add(toIndex,userList);
        notifyItemMoved(fromIndex,toIndex);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        if(!ordering || getItemViewType(draggingPosition)!=TYPE_NORMAL
                || getItemViewType(draggingPosition)!=TYPE_NORMAL){
            return false;
        }
        UserList userListDragging = userLists.get(getIndexByPosition(draggingPosition));
        UserList userListDrop     = userLists.get(getIndexByPosition(dropPosition));
        if(userListDragging.isDefaultFavorList() || userListDrop.isDefaultFavorList()){
            return false;
        }
        return true;
    }
    //endregion

    //列表Holder
    class UserListHolderNew extends AbstractDraggableItemViewHolder{
        View container,touchView,touchViewFake,front,favorCover,right0;
        SpImageView coverImage;
        TextView listName;
        UserList userList;
        TextView downloadText,syncText,deleteBtn;
        CollectProgressBar progressBar;


        public UserListHolderNew(View itemView) {
            super(itemView);
            container    = itemView.findViewById(R.id.container);
            coverImage   = (SpImageView) itemView.findViewById(R.id.cover_image);
            coverImage   .setHeightRatio(1f);
            favorCover   = itemView.findViewById(R.id.default_list_cover);
            listName     = (TextView) itemView.findViewById(R.id.list_name);
            front        = itemView.findViewById(R.id.front);
            right0       = itemView.findViewById(R.id.right0);
            touchView    = itemView.findViewById(R.id.touch_view);
            touchViewFake= itemView.findViewById(R.id.touch_view_fake);
            downloadText = (TextView) itemView.findViewById(R.id.download_text);
            syncText     = (TextView) itemView.findViewById(R.id.sync_text);
            deleteBtn    = (TextView) itemView.findViewById(R.id.delete_btn_21);
            progressBar  = (CollectProgressBar) itemView.findViewById(R.id.progress_bar);

            ViewUtilMethods.addColorFilter(downloadText.getBackground(),FacehubApi.getApi().getThemeColor());
            ViewUtilMethods.addColorFilter(syncText.getBackground(),FacehubApi.getApi().getThemeColor());
            ViewUtilMethods.addColorFilter(deleteBtn.getBackground(),FacehubApi.getApi().getThemeColor());

            front.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ordering){
                        return;
                    }
                    if(userList!=null){
                        if(userList.isDefaultFavorList()){
                            Intent intent = new Intent(v.getContext(),ManageEmoticonsActivity.class);
                            v.getContext().startActivity(intent);
                        }else {
                            Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("package_id", userList.getForkFromId());
                            intent.putExtras(bundle);
                            v.getContext().startActivity(intent);
                        }
                    }
                }
            });
        }

        public void showTouchView(){
            touchView.setVisibility(View.VISIBLE);
            touchViewFake.setVisibility(View.VISIBLE);
        }
        public void hideTouchView(){
            touchView.setVisibility(View.GONE);
            touchViewFake.setVisibility(View.GONE);
        }

        public void autoShowDownloadBtn(){
            if (userList.isDownloading()) { //下载中，显示进度条
                showProgressBar(userList.getPercent());
            } else if(ordering) { //不是下载中，但是排序中
                clearDownloadBtn(); //隐藏 下载/同步/删除
                deleteBtn.setVisibility(View.GONE);
            }else {
                if (userList.isPrepared()) {
                    clearDownloadBtn();
                } else if(userList.isDefaultFavorList()){ //为下载好的默认列表
                    showSyncBtn();
                }else {
                    showDownloadBtn();
                }
            }
        }

        private void clearDownloadBtn() {
            downloadText.setVisibility(View.GONE);
            syncText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            right0.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setTextColor(FacehubApi.getApi().getThemeColor());
            if(userList!=null && userList.isDefaultFavorList()){
                deleteBtn.setVisibility(View.GONE);
            }
        }

        private void showDownloadBtn() {
            right0.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            syncText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }

        private void showSyncBtn() {
            right0.setVisibility(View.VISIBLE);
            syncText.setVisibility(View.VISIBLE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }

        private void showProgressBar(final float percent) {
            right0.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.GONE);
            syncText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setPercentage(percent);
            deleteBtn.setVisibility(View.GONE);
        }

    }

    //标题Holder
    class SubtitleHolder extends AbstractDraggableItemViewHolder{
        TextView subtitle;
        public SubtitleHolder(View itemView) {
            super(itemView);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
        }
        public void setText(String title){
            subtitle.setText(title);
        }
    }

    //Footer Holder
    class FooterHolder extends AbstractDraggableItemViewHolder{
        public FooterHolder(View itemView) {
            super(itemView);
        }
    }
}

interface OnFooterLayoutChangeListener {
    public void onFooterLayoutChange(View footer);
}
