package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;

import java.util.ArrayList;

/**
 * Created by SETA on 2016/3/22.
 */
public class ListsManageActivity extends AppCompatActivity {
    private Context context;
//    public static TextView logText;
    private boolean isOneSwiped = false;

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
        actionbar.hideBtns();
        actionbar.setTitle("我的列表");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_lists_facehub);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        final UserListsAdapter adapter = new UserListsAdapter(context);
        recyclerView.setAdapter(adapter);
        final ArrayList<UserList> userLists = new ArrayList<>(FacehubApi.getApi().getAllUserLists());
//        for(int i=0;i<20;i++){
//            UserList userList = new UserList();
//            userList.setName("列表"+i);
//            userLists.add(userList);
//        }
        adapter.setUserLists(userLists);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if( !isOneSwiped
                        && viewHolder instanceof UserListsAdapter.UserListHolder
                        && ((UserListsAdapter.UserListHolder)viewHolder).canSwipe){
                    int swipeFlags = ItemTouchHelper.START;
                    return makeMovementFlags(0,swipeFlags);
                }
                return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                getDefaultUIUtil().clearView( ((UserListsAdapter.UserListHolder)viewHolder).front );
//                logText.setText("ClearView.");
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                isOneSwiped = true;
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
                final UserList userList = holder.userList;
                final int index = userLists.indexOf(userList);
                Snackbar snackbar = Snackbar.make(recyclerView, "确认删除?", Snackbar.LENGTH_LONG).setAction("删除", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userLists.remove( userList);
                        FacehubApi.getApi().removeUserListById(userList.getId());
                        adapter.notifyItemRemoved(index);
                    }
                });
                snackbar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
//                        super.onDismissed(snackbar, event);
//                        FacehubApi.getApi().removeUserListById(userList.getId(), new ResultHandlerInterface() {
//                            @Override
//                            public void onResponse(Object response) {
                        isOneSwiped = false;
                        adapter.notifyItemChanged(index);
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//
//                            }
//                        });
                    }
                });
                snackbar.show();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                float dXOrg = dX;
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
//                if(dX<-130){
//                    dX = -130;
//                }else if(dX>0){
//                    dX = 0;
//                }
                getDefaultUIUtil().onDraw(c, recyclerView, holder.front, dX, dY, actionState, isCurrentlyActive);
//                String text = "onChildDraw : \ndXOrg : " + dXOrg +
//                        "\ndx : " + dX + "\ndy : " + dY + "actionState : " + actionState + "\nisCurrentlyActive : " + isCurrentlyActive;
//                logText.setText(text);
            }

//            @Override
//            public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
//                getDefaultUIUtil().onDrawOver(c,recyclerView,holder.front,dX,dY,actionState,isCurrentlyActive);
//            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//                super.onSelectedChanged(viewHolder, actionState);
                if(viewHolder!=null){
                    getDefaultUIUtil().onSelected( ((UserListsAdapter.UserListHolder)viewHolder).front );
                }
            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

}

/** ---------------------------------------------------------------------------------------- **/
class UserListsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<UserList> userLists = new ArrayList<>();

    public UserListsAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setUserLists(ArrayList<UserList> userLists){
        this.userLists = userLists;
        notifyDataSetChanged();
    }

//    public boolean isOnEdit(){
//        return edittingPos>=0;
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.lists_manage_item,parent,false);
        UserListHolder holder = new UserListHolder(convertView);
        holder.deleteBtn = convertView.findViewById(R.id.delete_back);
        holder.front = convertView.findViewById(R.id.front);
        holder.undo = convertView.findViewById(R.id.undo);
        holder.divider = convertView.findViewById(R.id.divider);
        holder.favorCover = convertView.findViewById(R.id.default_list_cover);
        holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
        holder.listName = (TextView) convertView.findViewById(R.id.list_name);
        holder.coverImage.setHeightRatio(1f);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final UserListHolder holder = (UserListHolder)viewHolder;
        holder.userList = userLists.get(position);
        holder.listName.setText(userLists.get(position).getName());
        holder.divider.setVisibility(View.VISIBLE);
        holder.favorCover.setVisibility(View.GONE);
        if(position==(getItemCount()-1) ){
            holder.divider.setVisibility(View.GONE);
        }
        holder.coverImage.displayFile(null);
        if(position!=0 && holder.userList.getCover()!=null) {
            holder.coverImage.displayFile(holder.userList.getCover().getFilePath(Image.Size.FULL));
        }
        holder.canSwipe = true;
        if(position==0){
            holder.favorCover.setVisibility(View.VISIBLE);
            holder.canSwipe = false;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),ManageEmoticonsActivity.class);
                    v.getContext().startActivity(intent);
                }
            });
        }else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
        }
    }

    @Override
    public int getItemCount() {
        return userLists.size();
    }

    class UserListHolder extends RecyclerView.ViewHolder{
        View deleteBtn,front,undo,divider,favorCover;
        SpImageView coverImage;
        TextView listName;
        UserList userList;
        boolean canSwipe = true;
//        float dx;

        public UserListHolder(View itemView) {
            super(itemView);
        }
    }
}
