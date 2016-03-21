package com.azusasoft.facehubcloudsdk.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;

import java.util.HashSet;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/21.
 */
public class ManageEmoticonsActivity extends AppCompatActivity {
    private boolean isOnEdit = false;
    private UserList userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_emoticons);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color,getTheme()));
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }

        GridView gridView = (GridView) findViewById(R.id.emoticon_manage_grid);
        EmoticonsManageAdapter adapter = new EmoticonsManageAdapter(this);
        gridView.setAdapter(adapter);

        FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar);
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void onClick(View view){
        fastLog("点击actionbar");
    }
}

class EmoticonsManageAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private HashSet selectedEmoticons = new HashSet();

    public EmoticonsManageAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public HashSet getSelectedEmoticons(){
        return this.selectedEmoticons;
    }

    @Override
    public int getCount() {
        return 48;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.emoticon_grid_item,parent,false);
        if(position%5==4){
            convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background_5);
        }else {
            convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background);
        }
        return convertView;
    }
}
