package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnStartDragListener;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/22.
 */
public class ListsManageActivity extends AppCompatActivity {
    private Context context;
//    public static TextView logText;
    private int swipedPosition = -1;
    private RecyclerView recyclerView;
    private UserListsAdapter adapter;
    private Runnable cancelDeleteTask;
    private View deleteBtnTop;
    ArrayList<UserList> userLists = new ArrayList<>();
    private boolean isOrdering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_list_manage);
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color,getTheme()));
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }
//        logText = (TextView) findViewById(R.id.log);

        final FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        actionbar.showEdit();
        actionbar.setTitle("我的列表");
        actionbar.setEditText("排序");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteBtnTop = findViewById(R.id.magic_top_delete_constantine);
        deleteBtnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDeleteList();
            }
        });
        deleteBtnTop.setVisibility(View.GONE);
        recyclerView = (RecyclerView) findViewById(R.id.user_lists_facehub);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UserListsAdapter(context);
        recyclerView.setAdapter(adapter);


        actionbar.setOnEditClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOrdering) {
                    actionbar.setEditText("排序");
                } else {
                    actionbar.setEditText("完成");
                }
                isOrdering = !isOrdering;
                adapter.setOrdering(isOrdering);
            }
        });

        userLists = new ArrayList<>(FacehubApi.getApi().getAllUserLists());
        adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !(v.getTag() instanceof UserListsAdapter.UserListHolder)
                        || isOrdering){ //排序时禁用点击跳转
                    return;
                }
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder) v.getTag();
                int index = userLists.indexOf(holder.userList);
                if(isOneSwiped()){
                    if(index== swipedPosition){ //点击了滑动出的列表
                        fastLog("删除列表。");
                    }else {
                        fastLog("取消滑动");
                        recyclerView.removeCallbacks(cancelDeleteTask);
                        adapter.notifyItemChanged(swipedPosition);
                    }
                    setSwipedPosition(-1); //不论点的哪个列表，都退出删除模式
                    return;
                }

                if(index==0){ //默认列表
                    //TODO:判断是否有已滑动的列表
                    Intent intent = new Intent(v.getContext(),ManageEmoticonsActivity.class);
                    v.getContext().startActivity(intent);
                    return;
                }


                if(holder.userList.getForkFromId()==null) {
                    return;
                }
                Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("package_id", holder.userList.getForkFromId());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
        adapter.setUserLists(userLists);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if(! (viewHolder instanceof UserListsAdapter.UserListHolder) ){
                    return 0;
                }

                if(isOrdering){
                    int dragFlags = ItemTouchHelper.UP   | ItemTouchHelper.DOWN ;
                    return makeMovementFlags(dragFlags,0);
                }

                if( !isOneSwiped()
                        && ((UserListsAdapter.UserListHolder)viewHolder).canSwipe){
                    int swipeFlags = ItemTouchHelper.START;
                    return makeMovementFlags(0,swipeFlags);
                }
                return 0;
            }

            @Override
            public boolean isLongPressDragEnabled() { //只允许点击拖动的按钮
                return false;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
                if(!isOrdering){
                    return false;
                }else {
                    int s = source.getAdapterPosition();
                    int t = target.getAdapterPosition();
                    adapter.notifyItemMoved(s,t);
                    fastLog("onMove. || From : " + s + " | to : " + t);
                    return true;
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if(isOrdering){
                    super.clearView(recyclerView,viewHolder);
                    return;
                }

                final UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
//                UserList userList = holder.userList;
//                final int index = userLists.indexOf(userList);
//                fastLog("ClearView position : " + index);
//                if(index==swipedPosition){
//                    return;
//                }
                getDefaultUIUtil().clearView(((UserListsAdapter.UserListHolder) viewHolder).front);
                fastLog("clearview . ");
                recyclerView.removeCallbacks(cancelDeleteTask);
                setSwipedPosition(-1);
                deleteBtnTop.setVisibility(View.GONE);
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                if(isOrdering){
                    return;
                }
                getDefaultUIUtil().clearView( ((UserListsAdapter.UserListHolder)viewHolder).itemView );
                final UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
                UserList userList = holder.userList;
                final int index = userLists.indexOf(userList);
                setSwipedPosition(index);
                recyclerView.removeCallbacks(cancelDeleteTask);
                cancelDeleteTask = new Runnable() {
                    @Override
                    public void run() {
                        deleteBtnTop.setVisibility(View.GONE);
                        adapter.notifyItemChanged(index);
                        setSwipedPosition(-1);
                    }
                };
                recyclerView.postDelayed(cancelDeleteTask,2000); //两秒后自动取消删除

                int top = ViewUtilMethods.getTopOnWindow(viewHolder.itemView)
                            - ViewUtilMethods.getTopOnWindow((View) deleteBtnTop.getParent());
                ViewUtilMethods.changeViewPosition(deleteBtnTop,0,top);
                deleteBtnTop.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(isOrdering){
                    super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
                    return;
                }
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
                getDefaultUIUtil().onDraw(c, recyclerView, holder.front, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if(isOrdering){
                    super.onSelectedChanged(viewHolder,actionState);
                    return;
                }
                if(viewHolder!=null){
                    getDefaultUIUtil().onSelected( ((UserListsAdapter.UserListHolder)viewHolder).front );
                    setSwipedPosition(-1);
                }
            }
        };

        final ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isOneSwiped()) {
                    recyclerView.removeCallbacks(cancelDeleteTask);
                    deleteBtnTop.setVisibility(View.GONE);
                    adapter.notifyItemChanged(swipedPosition);
                    setSwipedPosition(-1);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        adapter.setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                helper.startDrag(viewHolder);
            }
        });
    }

    private boolean isOneSwiped(){
        return swipedPosition >=0;
    }
    private void setSwipedPosition(int swipedPosition){
        this.swipedPosition = swipedPosition;
    }

    public void doDeleteList(){
        fastLog("删除表情---最上层");
        if(isOneSwiped()) {
            String listId = userLists.get(swipedPosition).getId();
            userLists.remove(swipedPosition);
            adapter.notifyItemRemoved(swipedPosition);
            FacehubApi.getApi().removeUserListById(listId);
            setSwipedPosition(-1);
        }
    }

}

/** ---------------------------------------------------------------------------------------- **/
class UserListsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;
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
    }

    public void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.lists_manage_item,parent,false);
        UserListHolder holder = new UserListHolder(convertView);
        holder.deleteBtn = convertView.findViewById(R.id.delete_back);
        holder.front = convertView.findViewById(R.id.front);
        holder.undo = convertView.findViewById(R.id.undo);
        holder.upDivider = convertView.findViewById(R.id.up_divider);
        holder.divider = convertView.findViewById(R.id.divider);
        holder.favorCover = convertView.findViewById(R.id.default_list_cover);
        holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
        holder.listName = (TextView) convertView.findViewById(R.id.list_name);
        holder.coverImage.setHeightRatio(1f);
        holder.touchView = convertView.findViewById(R.id.touch_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final UserListHolder holder = (UserListHolder)viewHolder;
        holder.userList = userLists.get(position);
        holder.listName.setText(userLists.get(position).getName());
        holder.upDivider.setVisibility(View.GONE);
        holder.divider.setVisibility(View.VISIBLE);
        holder.favorCover.setVisibility(View.GONE);
        holder.coverImage.setVisibility(View.VISIBLE);
        holder.touchView.setVisibility(View.GONE);
        if(position==(getItemCount()-1) && !isOrdering){ //编辑模式时都显示divider
            holder.divider.setVisibility(View.GONE);
        }
        if(isOrdering){
            holder.touchView.setVisibility(View.VISIBLE);
            holder.front.setBackgroundColor(Color.parseColor("#00ffffff"));
            holder.deleteBtn.setVisibility(View.GONE);
            holder.upDivider.setVisibility(View.VISIBLE);
            holder.touchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        fastLog("handle view touch down . ");
                        onStartDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }else {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.front.setBackgroundColor(Color.parseColor("#ffffff"));
        }
//        holder.coverImage.displayFile(null);
        if(position!=0 && holder.userList.getCover()!=null) {
            holder.coverImage.displayFile(holder.userList.getCover().getFilePath(Image.Size.FULL));
        }
        holder.canSwipe = true;
        if(position==0){
            holder.coverImage.setVisibility(View.GONE);
            holder.favorCover.setVisibility(View.VISIBLE);
            holder.canSwipe = false;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(v);
                }
            });
        }else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userLists.size();
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

    class UserListHolder extends RecyclerView.ViewHolder{
        View deleteBtn,front,undo,upDivider,divider,favorCover,touchView;
        SpImageView coverImage;
        TextView listName;
        UserList userList;
        boolean canSwipe = true;
//        float dx;

        public UserListHolder(View itemView) {
            super(itemView);
            itemView.setTag(this);
        }
    }
}
