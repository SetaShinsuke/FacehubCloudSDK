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
import android.view.MotionEvent;
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
        final UserListsAdapter adapter = new UserListsAdapter(context);
        recyclerView.setAdapter(adapter);

    }

}

class UserListsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private LayoutInflater layoutInflater;

    public UserListsAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
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
        holder.coverImage = (SpImageView) convertView.findViewById(R.id.cover_image);
        holder.listName = (TextView) convertView.findViewById(R.id.list_name);
        holder.coverImage.setHeightRatio(1f);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        UserListHolder holder = (UserListHolder)viewHolder;
    }

    @Override
    public int getItemCount() {
        return 20;
    }

    class UserListHolder extends RecyclerView.ViewHolder{
        View deleteBtn,front,undo;
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
