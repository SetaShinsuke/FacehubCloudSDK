package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
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
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.decoration.ItemShadowDecorator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.DraggableItemAdapter;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.ItemDraggableRange;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.ADVRViewUtils;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

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
        originAdapter = new UserListAdapterNew(context);

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
//        originAdapter.setOnItemClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if( !(v.getTag() instanceof UserListsAdapter.UserListHolder)
//                        || isOrdering //排序时禁用点击跳转
//                        || recyclerView.getItemAnimator().isRunning()){
//                    return;
//                }
//                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder) v.getTag();
//                int index = userLists.indexOf(holder.userList);
//
//                if(index==0){ //默认列表
//                    //判断是否有已滑动的列表
//                    Intent intent = new Intent(v.getContext(),ManageEmoticonsActivity.class);
//                    v.getContext().startActivity(intent);
//                    return;
//                }
//
//
//                if(holder.userList.isDefaultFavorList()) {
//                    return;
//                }
//                Intent intent = new Intent(v.getContext(), EmoPackageDetailActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("package_id", holder.userList.getForkFromId());
//                intent.putExtras(bundle);
//                v.getContext().startActivity(intent);
//            }
//        });
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(context, R.drawable.material_shadow_z1)));
        }
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider_h), true));
        dragDropManager.attachRecyclerView(recyclerView);
    }
}


/** ========================================== Adapter =========================================== */
class UserListAdapterNew extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                        implements DraggableItemAdapter<RecyclerView.ViewHolder>{
    private Context context;
    private boolean ordering;
    private ArrayList<UserList> userLists = new ArrayList<>();

    public UserListAdapterNew(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        notifyDataSetChanged();
    }

    public void setOrdering(boolean ordering) {
        this.ordering = ordering;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userLists.size();
    }

    @Override
    public long getItemId(int position) {
        return userLists.get(position).getDbId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.lists_manage_item,parent,false);
        return new UserListHolderNew(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        UserListHolderNew holder = (UserListHolderNew)viewHolder;
        holder.listName.setText(userLists.get(position).getName());

    }

    //region 拖动Adapter
    @Override
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder viewHolder, int position, int x, int y) {
        fastLog("check can start drag , position : " + position);
        // x, y --- relative from the itemView's top-left
        UserListHolderNew holder = (UserListHolderNew)viewHolder;
        if(userLists.get(position).isDefaultFavorList()){
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

    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        fastLog("onCheckCanDrop , dragging : " + draggingPosition + " || drop : " + draggingPosition);
        return false;
    }
    //endregion

    class UserListHolderNew extends AbstractDraggableItemViewHolder{
        View container,touchView;
        TextView listName;
        public UserListHolderNew(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            touchView = itemView.findViewById(R.id.touch_view);
            listName = (TextView) itemView.findViewById(R.id.list_name);
        }
    }
}
