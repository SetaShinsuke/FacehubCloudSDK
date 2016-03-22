package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/22.
 */
public class ListsManageActivity extends AppCompatActivity {
    private Context context;

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
        final TextView logText = (TextView) findViewById(R.id.log);

        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar);
        actionbar.hideBtns();
        actionbar.setTitle("我的列表");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.user_lists);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        UserListsAdapter adapter = new UserListsAdapter(context);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                fastLog("move.");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                fastLog("swipe : " + swipeDir);
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder) viewHolder;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                UserListsAdapter.UserListHolder holder = (UserListsAdapter.UserListHolder) viewHolder;
//                fastLog("actionState : " + actionState);
                if(actionState!=ItemTouchHelper.ACTION_STATE_SWIPE){
                    return;
                }
                if (dX < -130) {
                    dX = -130;
                } else if (dX > 0) {
                    dX = 0;
                }
                String text = "onChildDraw : \ndx : " + dX + "\ndy : " + dY + "\nactionState : "
                        + actionState + "\nisCurrentlyActive : " + isCurrentlyActive;
                logText.setText(text);
                getDefaultUIUtil().onDraw(c, recyclerView
                        , holder.front
                        , dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                if (dX < -130) {
//                    dX = -130;
//                } else if (dX > 0) {
//                    dX = 0;
//                }
                super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//                fastLog("onChildDrawOver : \ndx : " + dX + "\ndy : " + dY + "\nactionState : "
//                        + actionState + "\nisCurrentlyActive : " + isCurrentlyActive );
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
//                fastLog("onMoved : \nFromPos : " + fromPos + "\ntoPos : " + toPos + "\nx : "
//                        + x + "\ny : " + y);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
//                fastLog("onSelectedChanged :\nactionState : "
//                        + actionState );
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }
}

class UserListsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;

    public UserListsAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.lists_manage_item,parent,false);
        UserListHolder holder = new UserListHolder(convertView);
        holder.deleteBtn = convertView.findViewById(R.id.delete_back);
        holder.front = convertView.findViewById(R.id.front);
        holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
        holder.listName = (TextView) convertView.findViewById(R.id.list_name);
        holder.coverImage.setHeightRatio(1f);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        UserListHolder holder = (UserListHolder)viewHolder;
    }

    @Override
    public int getItemCount() {
        return 20;
    }

    class UserListHolder extends RecyclerView.ViewHolder{
        View deleteBtn,front;
        SpImageView coverImage;
        TextView listName;
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
