package com.azusasoft.facehubcloudsdk.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.Image;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.UserListDAO;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnStartDragListener;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/21.
 * 点击编辑按钮 : none/非none
 * 点击mode弹窗 : 排序/编辑
 */
public class ManageEmoticonsActivity extends AppCompatActivity {

    public enum ManageMode {
        none, editMode, orderMode
    }

    //    private boolean isOnEdit = false;
    private ManageMode currentMode = ManageMode.none;
    private UserList userList;
    private EmoticonsManageAdapter adapter;
    private FacehubActionbar actionbar;
    private View dialogContainer,dialog;
    private TextView emoticonsCount,selectedDeleteBtn;
    private boolean isViewAnimating = false;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_emoticons);
        //设置通知栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color, getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.facehub_color));
        }


        actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        dialogContainer = findViewById(R.id.mode_dialog_container);
        if (UserListDAO.findAll().size() <= 0) {
            return;
        }
        userList = UserListDAO.findAll().get(0);
        emoticonsCount = (TextView) findViewById(R.id.emoticons_count_facehub);
        selectedDeleteBtn = (TextView) findViewById(R.id.selected_count_facehub);
        emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.emoticon_manage_grid_facehub);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        adapter = new EmoticonsManageAdapter(this);
        adapter.setEmoticons(userList.getEmoticons());
        recyclerView.setAdapter(adapter);

        dialog = findViewById(R.id.mode_dialog);
        dialogContainer.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogContainer.getVisibility() == View.VISIBLE && !isViewAnimating) {
                    hideDialog();
                }
            }
        });
        dialogContainer.findViewById(R.id.edit_btn).setOnClickListener(new DialogBtnClickListener());
        dialogContainer.findViewById(R.id.order_btn).setOnClickListener(new DialogBtnClickListener());
        dialogContainer.findViewById(R.id.cancel_btn).setOnClickListener(new DialogBtnClickListener());

        actionbar.setTitle(userList.getName() + "");
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
                if (isViewAnimating) {
                    return;
                }
                if (getCurrentMode() == ManageMode.none) {
                    showDialog();
                } else {
                    setCurrentMode(ManageMode.none);
                }
            }
        });

        adapter.setSelectChangeListener(new SelectChangeListener() {
            @Override
            public void onSelectChange(ArrayList<Emoticon> selectedEmoticons) {
                selectedDeleteBtn.setText("删除(" + selectedEmoticons.size() + ")");
            }
        });
        selectedDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!adapter.getSelectedEmoticons().isEmpty()) {
                    //TODO:删除表情
                    ArrayList<String> ids = new ArrayList<>();
                    for (Emoticon emoticon : adapter.getSelectedEmoticons()) {
                        ids.add(emoticon.getId());
                    }

                    userList.removeEmoticons(adapter.getSelectedEmoticons());
                    adapter.setEmoticons(userList.getEmoticons());
                    adapter.clearSelected();
                    setCurrentMode(ManageMode.none);
                }
            }
        });

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
                if(getCurrentMode()==ManageMode.orderMode){
                    int dragFlags = ItemTouchHelper.UP   | ItemTouchHelper.DOWN |
                            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    return makeMovementFlags(dragFlags,0);
                }
                return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, ViewHolder source, ViewHolder target) {
                int s = source.getAdapterPosition();
                int t = target.getAdapterPosition();
                adapter.notifyItemMoved(s,t);
                fastLog("onMove. || From : " + s + " | to : " + t);
                userList.changeEmoticonPosition(s, t);
                return true;
            }

            @Override
            public void onSwiped(ViewHolder viewHolder, int direction) {

            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });
    }

    private ManageMode getCurrentMode() {
        return this.currentMode;
    }

    private void setCurrentMode(ManageMode mode){
        boolean doSave = (mode!=ManageMode.none);
        currentMode = mode;
        adapter.setManageMode(mode);
        switch (currentMode){
            case none: //切换到查看模式
                findViewById(R.id.bottom_bar_facehub).setVisibility(View.GONE);
                currentMode = ManageMode.none;
                actionbar.setEditText("编辑");
                emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");
                adapter.clearSelected();
                if(doSave){
                    userList.save2DB();
                }
                adapter.setEmoticons(userList.getEmoticons());
                break;

            case editMode: //切换到编辑模式
                currentMode = ManageMode.editMode;
                actionbar.setEditText("完成");
                findViewById(R.id.bottom_bar_facehub).setVisibility(View.VISIBLE);
                emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");
                selectedDeleteBtn.setText("删除(0)");
                break;

            case orderMode: //切换到排序模式
                findViewById(R.id.bottom_bar_facehub).setVisibility(View.GONE);
                currentMode = ManageMode.orderMode;
                actionbar.setEditText("完成");
                break;

            default:
                break;
        }
    }

    private void showDialog() {
        dialogContainer.setVisibility(View.VISIBLE);
        dialog.setVisibility(View.GONE);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 0f);
        translateAnimation.setDuration(150);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isViewAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialog.setVisibility(View.VISIBLE);
                isViewAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dialog.startAnimation(translateAnimation);
    }

    private void hideDialog() {
        dialogContainer.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 1f);
        translateAnimation.setDuration(150);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isViewAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialog.setVisibility(View.GONE);
                dialogContainer.setVisibility(View.GONE);
                isViewAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.mode_dialog).startAnimation(translateAnimation);
    }

    class DialogBtnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(isViewAnimating){
                return;
            }
            int i = v.getId();
            if (i == R.id.order_btn) {
                setCurrentMode(ManageMode.orderMode);
            } else if (i == R.id.edit_btn) {
                setCurrentMode(ManageMode.editMode);
            } else if (i == R.id.cancel_btn) {
                setCurrentMode(ManageMode.none);
            }
            hideDialog();
        }
    }

}

interface SelectChangeListener {
    public void onSelectChange(ArrayList<Emoticon> selectedEmoticons);
}

class EmoticonsManageAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private ArrayList<Emoticon> selectedEmoticons = new ArrayList<>();
    private ManageEmoticonsActivity.ManageMode manageMode = ManageEmoticonsActivity.ManageMode.none;
    private SelectChangeListener selectChangeListener = new SelectChangeListener() {
        @Override
        public void onSelectChange(ArrayList<Emoticon> selectedEmoticons) {

        }
    };
    private OnStartDragListener onStartDragListener = new OnStartDragListener() {
        @Override
        public void onStartDrag(ViewHolder viewHolder) {

        }
    };

    public EmoticonsManageAdapter(Context context ) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons) {
//        this.emoticons = new ArrayList<>(emoticons);
        this.emoticons = emoticons;
        notifyDataSetChanged();
    }

    public void setManageMode(ManageEmoticonsActivity.ManageMode mode) {
        this.manageMode = mode;
        notifyDataSetChanged();
    }

    public ArrayList<Emoticon> getSelectedEmoticons() {
        return this.selectedEmoticons;
    }

    public void clearSelected() {
        this.selectedEmoticons.clear();
        notifyDataSetChanged();
    }

    public void setSelectChangeListener(SelectChangeListener selectChangeListener) {
        this.selectChangeListener = selectChangeListener;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener){
        this.onStartDragListener = onStartDragListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.emoticon_grid_item, parent, false);
        Holder holder = new Holder(convertView);
//        holder.handleView = convertView.findViewById(R.id.handle_view);
        holder.imageView = (SpImageView) convertView.findViewById(R.id.grid_image);
        holder.shade = (SpImageView) convertView.findViewById(R.id.shade);
        holder.checkIcon = convertView.findViewById(R.id.select_check);
        holder.imageView.setHeightRatio(1f);
        holder.shade.setHeightRatio(1f);
        convertView.setMinimumHeight((int) (ViewUtilMethods.getScreenWidth(context) / 5f));
        convertView.setOnClickListener(null);
        convertView.setClickable(false);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Holder holder = (Holder) viewHolder;
        View convertView = holder.itemView;
        if(manageMode== ManageEmoticonsActivity.ManageMode.orderMode){ //排序模式时都用同一个边框
            convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background_full);
        }else {
            if (position % 5 == 4) {
                convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background_5);
            } else {
                convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background);
            }
        }

        final Emoticon emoticon = emoticons.get(position);
        holder.shade.setVisibility(View.GONE);
        holder.checkIcon.setVisibility(View.GONE);
        if (manageMode== ManageEmoticonsActivity.ManageMode.editMode) { //编辑模式
            if (selectedEmoticons.contains(emoticon)) {
                holder.shade.setVisibility(View.VISIBLE);
                holder.checkIcon.setVisibility(View.VISIBLE);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedEmoticons.contains(emoticon)) { //取消选择
                        selectedEmoticons.remove(emoticon);
                        notifyDataSetChanged();
                    } else {
                        selectedEmoticons.add(emoticon);
                        notifyDataSetChanged();
                    }
                    selectChangeListener.onSelectChange(selectedEmoticons);
                }
            });
            holder.itemView.setOnTouchListener(null);
        }else if(manageMode== ManageEmoticonsActivity.ManageMode.orderMode){ //排序模式
//            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (MotionEventCompat.getActionMasked(event) ==
//                            MotionEvent.ACTION_DOWN) {
//                        fastLog("handle view touch down . ");
//                        onStartDragListener.onStartDrag(holder);
//                    }
//                    return false;
//                }
//            });
            holder.itemView.setOnTouchListener(null);
        }else {
            holder.itemView.setOnTouchListener(null);
        }

        holder.imageView.displayFile(emoticon.getFilePath(Image.Size.FULL));
    }

    @Override
    public int getItemCount() {
        return emoticons.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        SpImageView imageView, shade;
        View checkIcon;
//        handleView;

        public Holder(View itemView) {
            super(itemView);
        }
    }
}