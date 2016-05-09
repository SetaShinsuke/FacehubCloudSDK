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
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/21.
 * 默认列表表情管理页
 */
public class ManageEmoticonsActivity extends AppCompatActivity {
    private boolean isOnEdit = false;
    private UserList userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_emoticons);
        //设置通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(FacehubApi.getApi().getThemeColor());
        }

        if(UserListDAO.findAll().size()<=0){
            return;
        }
        userList = UserListDAO.findAll().get(0);
        final TextView emoticonsCount = (TextView) findViewById(R.id.emoticons_count_facehub);
        final TextView selectedDeleteBtn = (TextView)findViewById(R.id.selected_count_facehub);
        emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");

        GridView gridView = (GridView) findViewById(R.id.emoticon_manage_grid_facehub);
        final EmoticonsManageAdapter adapter = new EmoticonsManageAdapter(this);
        adapter.setEmoticons(userList.getEmoticons());
        gridView.setAdapter(adapter);

        final FacehubActionbar actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        actionbar.setTitle(userList.getName()+"");
        actionbar.setOnBackBtnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionbar.showEdit();
        actionbar.setOnEditClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnEdit) {
                    findViewById(R.id.bottom_bar_facehub).setVisibility(View.GONE);
                    actionbar.setEditText("编辑");
                    selectedDeleteBtn.setText("删除(0)");
                    adapter.clearSelected();
                } else {
                    findViewById(R.id.bottom_bar_facehub).setVisibility(View.VISIBLE);
                    actionbar.setEditText("完成");
                    selectedDeleteBtn.setText("删除(0)");
                }
                isOnEdit = !isOnEdit;
                adapter.setOnEdit(isOnEdit);
            }
        });
        findViewById(R.id.bottom_bar_facehub).setBackgroundColor(FacehubApi.getApi().getThemeColor());

        adapter.setSelectChangeListener(new SelectChangeListener() {
            @Override
            public void onSelectChange(ArrayList<Emoticon> selectedEmoticons) {
                selectedDeleteBtn.setText("删除(" + selectedEmoticons.size() + ")");
            }
        });
        selectedDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !adapter.getSelectedEmoticons().isEmpty() ){
                    //删除表情
                    ArrayList<String> ids = new ArrayList<>();
                    for(Emoticon emoticon:adapter.getSelectedEmoticons()){
                        ids.add(emoticon.getId());
                    }

                    userList.removeEmoticons(adapter.getSelectedEmoticons());
                    adapter.setEmoticons(userList.getEmoticons());
                    adapter.clearSelected();
                    actionbar.setEditText("编辑");
                    adapter.setOnEdit(false);
                    selectedDeleteBtn.setText("删除(0)");
                    findViewById(R.id.bottom_bar_facehub).setVisibility(View.GONE);
                    isOnEdit = false;
                    emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");
                }
            }
        });
    }

    public void onClick(View view){
        fastLog("点击actionbar");
    }
}

/**
 * 表情选中的回调
 */
interface SelectChangeListener{
    public void onSelectChange(ArrayList<Emoticon> selectedEmoticons);
}

/**
 * 表情管理页Adapter
 */
class EmoticonsManageAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private ArrayList<Emoticon> selectedEmoticons = new ArrayList<>();
    private boolean isOnEdit = false;
    private SelectChangeListener selectChangeListener = new SelectChangeListener() {
        @Override
        public void onSelectChange(ArrayList<Emoticon> selectedEmoticons) {

        }
    };

    public EmoticonsManageAdapter(Context context){
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons){
        this.emoticons = new ArrayList<>(emoticons);
        notifyDataSetChanged();
    }

    public void setOnEdit(boolean isOnEdit){
        this.isOnEdit = isOnEdit;
        notifyDataSetChanged();
    }

    public ArrayList<Emoticon> getSelectedEmoticons(){
        return this.selectedEmoticons;
    }
    public void clearSelected(){
        this.selectedEmoticons.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return emoticons.size();
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
        Holder holder = new Holder();
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.emoticon_grid_item,parent,false);
            holder.imageView = (SpImageView) convertView.findViewById(R.id.grid_image);
            holder.shade = (SpImageView) convertView.findViewById(R.id.shade);
            holder.checkIcon = convertView.findViewById(R.id.select_check);
            holder.imageView.setHeightRatio(1f);
            holder.shade.setHeightRatio(1f);
            convertView.setMinimumHeight((int) (ViewUtilMethods.getScreenWidth(context)/5f));
            convertView.setTag(holder);
            convertView.setOnClickListener(null);
            convertView.setClickable(false);
        }
        holder = (Holder)convertView.getTag();

        if(position%5==4){
            convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background_5);
        }else {
            convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background);
        }

        final Emoticon emoticon = emoticons.get(position);
        holder.shade.setVisibility(View.GONE);
        holder.checkIcon.setVisibility(View.GONE);
        if(isOnEdit){
            if(selectedEmoticons.contains(emoticon)){
                holder.shade.setVisibility(View.VISIBLE);
                holder.checkIcon.setVisibility(View.VISIBLE);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedEmoticons.contains(emoticon)){ //取消选择
                        selectedEmoticons.remove(emoticon);
                        notifyDataSetChanged();
                    }else {
                        selectedEmoticons.add(emoticon);
                        notifyDataSetChanged();
                    }
                    selectChangeListener.onSelectChange(selectedEmoticons);
                }
            });
        }

        holder.imageView.displayFile( emoticon.getFilePath(Image.Size.FULL));
        return convertView;
    }

    public void setSelectChangeListener(SelectChangeListener selectChangeListener) {
        this.selectChangeListener = selectChangeListener;
    }

    class Holder{
        SpImageView imageView,shade;
        View checkIcon;
    }
}
