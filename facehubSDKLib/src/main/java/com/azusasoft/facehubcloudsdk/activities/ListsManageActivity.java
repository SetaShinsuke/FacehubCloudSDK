package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.decoration.ItemShadowDecorator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.ADVRViewUtils;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.azusasoft.facehubcloudsdk.views.viewUtils.CollectProgressBar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnStartDragListener;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.DraggableItemAdapter;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.ItemDraggableRange;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/22.
 * 表情列表管理页
 */
public class ListsManageActivity extends BaseActivity {
    private final int  AUTO_CANCEL_DELAY = 1500;

    private Context context;
//    public static TextView logText;
    private int swipedIndex = -1;
    private RecyclerViewEx recyclerView;
    private UserListsAdapter originAdapter;
    private RecyclerView.Adapter adapter;
    private Runnable cancelDeleteTask;
    private View deleteBtnTop;
    ArrayList<UserList> userLists = new ArrayList<>();
    private boolean isOrdering = false;
//    private boolean isViewAnimating = false;

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
        deleteBtnTop = findViewById(R.id.magic_top_delete_constantine);
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

        deleteBtnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDeleteList(v);
            }
        });
        deleteBtnTop.setVisibility(View.GONE);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        originAdapter = new UserListsAdapter(context);

        //拖动manager
        RecyclerViewDragDropManager dragDropManager = new RecyclerViewDragDropManager();
        NinePatchDrawable drawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3);
        dragDropManager.setDraggingItemShadowDrawable( drawable );
        dragDropManager.setCheckCanDropEnabled(true); // !!! this method is required to use onCheckCanDrop()
        adapter = dragDropManager.createWrappedAdapter(originAdapter);

        recyclerView.setAdapter(adapter);

        actionbar.setOnEditClick(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(recyclerView.getItemAnimator().isRunning()){
                    fastLog("正在执行变换动画.");
                    return;
                }
                if(isOneSwiped()){ //取消正在删除的项目
                    recyclerView.removeCallbacks(cancelDeleteTask);
                        deleteBtnTop.setVisibility(View.GONE);
                        originAdapter.notifyItemChanged( originAdapter.getPositionByIndex(swipedIndex) );
                        setSwipedIndex(-1);
                    return;
                }

                /** 退出排序模式 */
                if (isOrdering) {
                    actionbar.setEditBtnText("排序");
                    //退出排序，提交更改
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

        userLists = FacehubApi.getApi().getUser().getUserLists();

        originAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !(v.getTag() instanceof UserListsAdapter.UserListHolder)
                        || isOrdering //排序时禁用点击跳转
                        || recyclerView.getItemAnimator().isRunning()){
                    return;
                }
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder) v.getTag();
                int index = userLists.indexOf(holder.userList);
                if(isOneSwiped()){
                    if(index== swipedIndex){ //点击了滑动出的列表
                        fastLog("删除列表。");
                    }else {
                        fastLog("取消滑动");
                        recyclerView.removeCallbacks(cancelDeleteTask);
                        originAdapter.notifyItemChanged( originAdapter.getPositionByIndex(swipedIndex) );
                    }
                    setSwipedIndex(-1); //不论点的哪个列表，都退出删除模式
                    return;
                }

                if(index==0){ //默认列表
                    //判断是否有已滑动的列表
                    Intent intent = new Intent(v.getContext(),ManageEmoticonsActivity.class);
                    v.getContext().startActivity(intent);
                    return;
                }


                if(holder.userList.isDefaultFavorList()) {
                    return;
                }
                Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("package_id", holder.userList.getForkFromId());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
        originAdapter.setUserLists(userLists);

        if( !FacehubApi.getApi().getUser().silentDownloadAll() ) { //如果没有自动静默下载，提示用户同步
            for (UserList userList : userLists) {
                userList.downloadCover(Image.Size.FULL, new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        originAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        LogX.e("表情管理页封面下载出错 : " + e);
                    }
                });
            }
        }

//        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
//            @Override
//            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                if(! (viewHolder instanceof UserListsAdapter.UserListHolder) ){
//                    return 0;
//                }
//
//                if(isOrdering){
//                    int dragFlags = ItemTouchHelper.UP   | ItemTouchHelper.DOWN ;
//                    return makeMovementFlags(dragFlags,0);
//                }
//
//                if( !isOneSwiped()
//                        && ((UserListsAdapter.UserListHolder)viewHolder).canSwipe){
//                    int swipeFlags = ItemTouchHelper.START;
//                    return makeMovementFlags(0,swipeFlags);
//                }
//                return 0;
//            }
//
//            @Override
//            public boolean isLongPressDragEnabled() { //只允许点击拖动的按钮
//                return false;
//            }
//
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
//                if(!isOrdering) {
//                    return false;
//                }else if(source instanceof UserListsAdapter.UserListHolder
//                        && target instanceof UserListsAdapter.UserListHolder
//                        && ((UserListsAdapter.UserListHolder) source).canMove
//                        && ((UserListsAdapter.UserListHolder) target).canMove){ //参加移位的holder是userList 且 canMove为true
//                    int s = source.getAdapterPosition();
//                    int t = target.getAdapterPosition();
//                    originAdapter.notifyItemMoved(s,t);
//                    FacehubApi.getApi().getUser().changeListPosition(originAdapter.getIndexByPosition(s),originAdapter.getIndexByPosition(t));
//                    fastLog("onMove. || From : " + s + " | to : " + t);
//                    return true;
//                }
//                return false;
//            }
//
//            @Override
//            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                if(isOrdering){
//                    super.clearView(recyclerView,viewHolder);
//                    return;
//                }
//
//                final UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
////                UserList userList = holder.userList;
////                final int index = userLists.indexOf(userList);
////                fastLog("ClearView position : " + index);
////                if(index==swipedIndex){
////                    return;
////                }
//                getDefaultUIUtil().clearView(((UserListsAdapter.UserListHolder) viewHolder).front);
//                fastLog("clearview . ");
//                recyclerView.removeCallbacks(cancelDeleteTask);
//                setSwipedIndex(-1);
//                deleteBtnTop.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
//                if(isOrdering){
//                    return;
//                }
//                getDefaultUIUtil().clearView( ((UserListsAdapter.UserListHolder)viewHolder).itemView );
//                final UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
//                UserList userList = holder.userList;
//                final int index = userLists.indexOf(userList);
//                setSwipedIndex(index);
//                fastLog("===Item " + index + " Swiped . ");
//                recyclerView.removeCallbacks(cancelDeleteTask);
//                cancelDeleteTask = new Runnable() {
//                    @Override
//                    public void run() {
//                        deleteBtnTop.setVisibility(View.GONE);
//                        originAdapter.notifyItemChanged( originAdapter.getPositionByIndex(index) );
//                        setSwipedIndex(-1);
//                    }
//                };
//                recyclerView.postDelayed(cancelDeleteTask,1250); //两秒后自动取消删除
//
//                int top = ViewUtilMethods.getTopOnWindow(viewHolder.itemView)
//                            - ViewUtilMethods.getTopOnWindow((View) deleteBtnTop.getParent());
//                ViewUtilMethods.changeViewPosition(deleteBtnTop,0,top);
//                deleteBtnTop.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                if(isOrdering){
//                    super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
//                    return;
//                }
//                if(viewHolder instanceof UserListsAdapter.UserListHolder) {
//                    UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder) viewHolder;
//                    getDefaultUIUtil().onDraw(c, recyclerView, holder.front, dX, dY, actionState, isCurrentlyActive);
//                }else {
//                    super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
//                }
//            }
//
//            @Override
//            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//                if(isOrdering){
//                    super.onSelectedChanged(viewHolder,actionState);
//                    return;
//                }
//                if(viewHolder!=null){
//                    getDefaultUIUtil().onSelected( ((UserListsAdapter.UserListHolder)viewHolder).front );
//                    setSwipedIndex(-1);
//                }
//            }
//        };
//
//        final ItemTouchHelper helper = new ItemTouchHelper(callback);
//        helper.attachToRecyclerView(recyclerView);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isOneSwiped()) {
                    recyclerView.removeCallbacks(cancelDeleteTask);
                    deleteBtnTop.setVisibility(View.GONE);
                    originAdapter.notifyItemChanged( originAdapter.getPositionByIndex(swipedIndex) );
                    setSwipedIndex(-1);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        originAdapter.setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
//                helper.startDrag(viewHolder);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(context, R.drawable.material_shadow_z1)));
        }
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider_h), true));
        dragDropManager.attachRecyclerView(recyclerView);

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

    public void onEvent(DownloadProgressEvent event){
        for (int i = 0; i < userLists.size(); i++) {
            if (event.listId.equals(userLists.get(i).getId())) {
                LogX.d(Constants.PROGRESS, "编辑列表下载 on event 进度 : " + event.percentage);
                originAdapter.notifyItemChanged(originAdapter.getPositionByIndex(i));
            }
        }
    }

    public void onEvent(UserListPrepareEvent event){
        if(event.listId==null){
            return;
        }
        for (int i = 0; i < userLists.size(); i++) {
            if (event.listId.equals(userLists.get(i).getId())) {
                originAdapter.notifyItemChanged(originAdapter.getPositionByIndex(i));
            }
        }
    }

    private boolean isOneSwiped(){
        return swipedIndex >=0;
    }
    private void setSwipedIndex(int swipedPosition){
        this.swipedIndex = swipedPosition;
    }
    private void clearSwipedView(){
        if( isOneSwiped() ) {
            deleteBtnTop.setVisibility(View.GONE);
            originAdapter.notifyItemChanged(originAdapter.getPositionByIndex(swipedIndex));
            setSwipedIndex(-1);
        }
    }

    public void doDeleteList(View view){
        fastLog("删除表情---最上层");
        if(isOneSwiped()) {
            String listId = userLists.get(swipedIndex).getId();
            fastLog("删除列表 " + swipedIndex);
            userLists.remove(swipedIndex);
            originAdapter.notifyItemRemoved(originAdapter.getPositionByIndex(swipedIndex));
            FacehubApi.getApi().removeUserListById(listId);
            setSwipedIndex(-1);
        }
    }

}

/**
 * 列表编辑页Adapter
 */
class UserListsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements DraggableItemAdapter<RecyclerView.ViewHolder>{
    private final int TYPE_SUBTITLE = 0;
    private final int TYPE_NORMAL = 1;

    private Context context;
    private LayoutInflater layoutInflater;
    private long removeAnimationDuration = 500;
    private boolean isAnimating = false;
    private ArrayList<UserList> userLists = new ArrayList<>();
    //某个列表被滑动后，点击的操作交给上一级处理
    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private boolean isOrdering = false;
    private OnStartDragListener onStartDragListener = new OnStartDragListener() {
        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {

        }
    };

    public UserListsAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_SUBTITLE){
            View convertView = layoutInflater.inflate(R.layout.list_manage_subtitle_item,parent,false);
            SubtitleHolder holder = new SubtitleHolder(convertView);
            holder.subtitleTextView = (TextView) convertView.findViewById(R.id.subtitle);
            return holder;
        }else if(viewType==TYPE_NORMAL) {
            View convertView = layoutInflater.inflate(R.layout.lists_manage_item, parent, false);
            UserListHolder holder = new UserListHolder(convertView);
            holder.container = convertView.findViewById(R.id.container);
            holder.deleteBtn = convertView.findViewById(R.id.delete_back);
            holder.deleteBtn21 = (TextView) convertView.findViewById(R.id.delete_btn_21);
            holder.front = convertView.findViewById(R.id.front);
            holder.upDivider = convertView.findViewById(R.id.up_divider);
            holder.divider = convertView.findViewById(R.id.divider);
            holder.favorCover = convertView.findViewById(R.id.default_list_cover);
            holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
            holder.listName = (TextView) convertView.findViewById(R.id.list_name);
            holder.coverImage.setHeightRatio(1f);
            holder.touchView = convertView.findViewById(R.id.touch_view);

            holder.right0 = convertView.findViewById(R.id.right0);
            holder.downloadText = (TextView) convertView.findViewById(R.id.download_text);
            holder.syncText = (TextView) convertView.findViewById(R.id.sync_text);
            holder.progressBar = (CollectProgressBar) convertView.findViewById(R.id.progress_bar);
            ViewUtilMethods.addColorFilter(holder.downloadText.getBackground(),FacehubApi.getApi().getThemeColor());
            ViewUtilMethods.addColorFilter(holder.syncText.getBackground(),FacehubApi.getApi().getThemeColor());
            ViewUtilMethods.addColorFilter(holder.deleteBtn21.getBackground(),FacehubApi.getApi().getThemeColor());
            return holder;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0||position==2){
            return TYPE_SUBTITLE;
        }else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if(getItemViewType(position)==TYPE_SUBTITLE){ //显示标题
            SubtitleHolder holder = (SubtitleHolder)viewHolder;
            if(position==0){
                holder.setText("默认列表");
            }else if(position==2){
                holder.setText("收藏的表情包");
            }

            holder.showSelf();
            if(isOrdering){
                if(position<2){
                    holder.hideSelf();
                }
            }

        }else if(getItemViewType(position)==TYPE_NORMAL) { //显示列表
            int listIndex = getIndexByPosition(position);

            final UserListHolder holder = (UserListHolder) viewHolder;
            holder.front.setVisibility(View.VISIBLE);
            holder.userList = userLists.get(listIndex);
            holder.listName.setText(holder.userList.getName());
            holder.upDivider.setVisibility(View.GONE);
            holder.divider.setVisibility(View.VISIBLE);
            holder.favorCover.setVisibility(View.GONE);
            holder.coverImage.setVisibility(View.VISIBLE);
            holder.touchView.setVisibility(View.GONE);

            holder.showSelf();
            if(isOrdering){
                if(position<2){
                    holder.hideSelf();
                }
            }

            if (position == (getItemCount() - 1) && !isOrdering) { //编辑模式时都显示divider
                holder.divider.setVisibility(View.GONE);
            }
            if (isOrdering && listIndex!=0) { //第一个列表不显示拖动按钮
                holder.front.setVisibility(View.VISIBLE);
                holder.touchView.setVisibility(View.VISIBLE);
                holder.front.setBackgroundColor(Color.parseColor("#00ffffff"));
                holder.deleteBtn.setVisibility(View.GONE);
                holder.upDivider.setVisibility(View.VISIBLE);
//                holder.touchView.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        if (MotionEventCompat.getActionMasked(event) ==
//                                MotionEvent.ACTION_DOWN) {
//                            fastLog("handle view touch down . ");
//                            onStartDragListener.onStartDrag(holder);
//                        }
//                        return false;
//                    }
//                });
            } else {
                holder.deleteBtn.setVisibility(View.VISIBLE);
                holder.front.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            if (listIndex != 0 && holder.userList.getCover() != null) {
                holder.coverImage.displayFile(holder.userList.getCover().getFilePath(Image.Size.FULL));
            }
            holder.canSwipe = true;
            holder.canMove  = true;
            if (listIndex == 0) {
                holder.coverImage.setVisibility(View.GONE);
                holder.favorCover.setVisibility(View.VISIBLE);
                holder.divider.setVisibility(View.GONE);
                holder.canSwipe = false;
                holder.canMove  = false;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(v);
                    }
                });
            } else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onClick(v);
                    }
                });
            }

            //下载按钮设置
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
            holder.deleteBtn21.setOnClickListener(new View.OnClickListener() {
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
                    fastLog("删除位置 : " + pos);
                    if(pos<0){
                        return;
                    }
                    notifyItemRemoved(pos);
                    FacehubApi.getApi().removeUserListById(holder.userList.getId());
                }
            });
        }
    }

    Handler handler = new Handler();
    Runnable task = new Runnable() {
        @Override
        public void run() {
            isAnimating = false;
        }
    };

    public int getIndexByPosition(int position){
        int listIndex = position;
        if(position<1){
            listIndex = position;
        }else if(position==1){
            listIndex = 0;
        }else if(position>2){
            listIndex = position-2;
        }
        return listIndex;
    }

    public int getPositionByIndex(int index){
        int position = index;
        if(index==0){
            position = index+1;
        }else if(index>0){
            position = index + 2;
        }
        return position;
    }

    @Override
    public int getItemCount() {
        if(userLists.size()==0){
            return 0;
        }
        return userLists.size() + 2;
    }

    @Override
    public long getItemId(int position) {
        if(getItemViewType(position)==TYPE_NORMAL){
            int index = getIndexByPosition(position);
            UserList userList = userLists.get(index);
            return userList.getDbId();
        }
        return super.getItemId(position);
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener){
        this.onStartDragListener = onStartDragListener;
    }

    public void setOrdering(boolean ordering){
        this.isOrdering = ordering;
        notifyDataSetChanged();
    }

    public void setRemoveAnimationDuration(long removeAnimationDuration) {
        this.removeAnimationDuration = removeAnimationDuration;
    }

    //region 拖动Adapter继承
    @Override
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder viewHolder, int position, int x, int y) {
        fastLog("check can start drag , position : " + position);
        if(getItemViewType(position)==TYPE_SUBTITLE){
            fastLog("标题 不可拖动 " + position);
            return false;
        }
        // x, y --- relative from the itemView's top-left
        UserListHolder holder = (UserListHolder)viewHolder;
        if(holder.userList.isDefaultFavorList()){
            fastLog("默认列表,不可拖动 " + position);
            return false;
        }
        final View containerView = holder.container;
        final View dragHandleView = holder.touchView;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        boolean flag = ADVRViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
        fastLog("Postion : " + position + " 是否可拖动 ? " + flag);
        return flag;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerView.ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        int fromIndex = getIndexByPosition(fromPosition);
        int toIndex = getIndexByPosition(toPosition);
        FacehubApi.getApi().getUser().changeListPosition(fromIndex,toIndex);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        if(draggingPosition<3 || draggingPosition<3){ //前3个不可拖动
            return false;
        }
        return true;
    }
    //endregion

    class UserListHolder extends AbstractDraggableItemViewHolder {
        View container;
        View deleteBtn,front,upDivider,divider,favorCover,touchView;
        TextView deleteBtn21;
        SpImageView coverImage;
        TextView listName;
        UserList userList;
        boolean canSwipe = true;
        boolean canMove = true;
        View right0;
        TextView downloadText,syncText;
        CollectProgressBar progressBar;

//        float dx;

        public UserListHolder(View itemView) {
            super(itemView);
            itemView.setTag(this);
        }

        public void hideSelf(){
            ViewGroup.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            itemView.setLayoutParams(layoutParams);
        }

        public void showSelf(){
            ViewGroup.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
        }

        public void autoShowDownloadBtn(){
            if (userList.isDownloading()) {
                showProgressBar(userList.getPercent());
            } else if(isOrdering) {
                clearDownloadBtn();
                deleteBtn21.setVisibility(View.GONE);
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
            deleteBtn21.setVisibility(View.VISIBLE);
            deleteBtn21.setTextColor(FacehubApi.getApi().getThemeColor());
            if(userList!=null && userList.isDefaultFavorList()){
                deleteBtn21.setVisibility(View.GONE);
            }
        }

        private void showDownloadBtn() {
            right0.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.VISIBLE);
            syncText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.GONE);
            deleteBtn21.setVisibility(View.GONE);
        }

        private void showSyncBtn() {
            right0.setVisibility(View.VISIBLE);
            syncText.setVisibility(View.VISIBLE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.GONE);
            deleteBtn21.setVisibility(View.GONE);
        }

        private void showProgressBar(final float percent) {
            right0.setVisibility(View.VISIBLE);
            downloadText.setVisibility(View.GONE);
            syncText.setVisibility(View.GONE);
            downloadText.setText("下载");
            downloadText.setTextColor(FacehubApi.getApi().getThemeColor());
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setPercentage(percent);
            deleteBtn21.setVisibility(View.GONE);
        }
    }

    class SubtitleHolder extends RecyclerView.ViewHolder{
        TextView subtitleTextView;
        public SubtitleHolder(View itemView) {
            super(itemView);
        }
        public void setText(String text){
            if(subtitleTextView!=null){
                subtitleTextView.setText(text);
            }
        }


        public void hideSelf(){
            ViewGroup.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , 0);
            itemView.setLayoutParams(layoutParams);
        }

        public void showSelf(){
            ViewGroup.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
        }
    }
}
