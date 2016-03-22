package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/22.
 */
public class ListsManageActivity extends AppCompatActivity {
    private Context context;
    public static TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.list_manage_activity);
        //通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color,getTheme()));
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }
        logText = (TextView) findViewById(R.id.log);

        final FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar);
        actionbar.hideBtns();
        actionbar.setTitle("我的列表");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_lists);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        final UserListsAdapter adapter = new UserListsAdapter(context);
        recyclerView.setAdapter(adapter);
        final ArrayList<UserList> userLists = new ArrayList<>();
        for(int i=0;i<5;i++){
            UserList userList = new UserList();
            userList.setName("列表"+i);
            userLists.add(userList);
        }
        adapter.setUserLists(userLists);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if(viewHolder instanceof UserListsAdapter.UserListHolder){
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
                logText.setText("ClearView.");
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
                final UserList userList = holder.userList;
                final int index = userLists.indexOf(userList);
                userLists.remove( userList);
                adapter.notifyItemRemoved(index);
                Snackbar.make(recyclerView,"列表已删除",Snackbar.LENGTH_LONG).setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userLists.add(index,userList);
                        adapter.notifyItemInserted(index);
                    }
                }).show();
//                adapter.setUserLists(userLists);
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
                String text = "onChildDraw : \ndXOrg : " + dXOrg +
                        "\ndx : " + dX + "\ndy : " + dY + "actionState : " + actionState + "\nisCurrentlyActive : " + isCurrentlyActive;
                logText.setText(text);
            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder)viewHolder;
                getDefaultUIUtil().onDrawOver(c,recyclerView,holder.front,dX,dY,actionState,isCurrentlyActive);
            }

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
        holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
        holder.listName = (TextView) convertView.findViewById(R.id.list_name);
        holder.coverImage.setHeightRatio(1f);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        UserListHolder holder = (UserListHolder)viewHolder;
        holder.userList = userLists.get(position);
        holder.listName.setText(userLists.get(position).getName());
        holder.divider.setVisibility(View.VISIBLE);
        if(position==(getItemCount()-1) ){
            holder.divider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userLists.size();
    }

    class UserListHolder extends RecyclerView.ViewHolder{
        View deleteBtn,front,undo,divider;
        SpImageView coverImage;
        TextView listName;
        UserList userList;
//        float dx;

        public UserListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fastLog("On Item Click . ");
                }
            });
        }
    }
}
