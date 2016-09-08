package com.azusasoft.facehubcloudsdk.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
//import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.api.FacehubApi;
import com.azusasoft.facehubcloudsdk.api.ProgressInterface;
import com.azusasoft.facehubcloudsdk.api.ResultHandlerInterface;
import com.azusasoft.facehubcloudsdk.api.models.Emoticon;
import com.azusasoft.facehubcloudsdk.api.models.UserList;
import com.azusasoft.facehubcloudsdk.api.models.events.DownloadProgressEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.EmoticonCollectEvent;
import com.azusasoft.facehubcloudsdk.api.models.events.ExitViewsEvent;
import com.azusasoft.facehubcloudsdk.api.utils.Constants;
import com.azusasoft.facehubcloudsdk.api.utils.LogX;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.animator.GeneralItemAnimator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.DraggableItemAdapter;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.ItemDraggableRange;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.azusasoft.facehubcloudsdk.views.uploacModule.presenters.UploadEmoPresenter;
import com.azusasoft.facehubcloudsdk.views.uploacModule.interfaces.UploadView;
import com.azusasoft.facehubcloudsdk.views.uploacModule.presenters.UploadEmoPresenterImpl;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubActionbar;
import com.azusasoft.facehubcloudsdk.views.viewUtils.FacehubAlertDialog;
import com.azusasoft.facehubcloudsdk.views.viewUtils.OnStartDragListener;
import com.azusasoft.facehubcloudsdk.views.viewUtils.SpImageView;
import com.azusasoft.facehubcloudsdk.views.viewUtils.ViewUtilMethods;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.azusasoft.facehubcloudsdk.activities.ManageEmoticonsActivity.ManageMode.editMode;
import static com.azusasoft.facehubcloudsdk.activities.ManageEmoticonsActivity.ManageMode.none;
import static com.azusasoft.facehubcloudsdk.api.FacehubApi.themeOptions;
import static com.azusasoft.facehubcloudsdk.api.utils.LogX.fastLog;

/**
 * Created by SETA on 2016/3/21.
 * 默认列表表情管理页
 */
public class ManageEmoticonsActivity extends BaseActivity implements UploadView {
    UploadEmoPresenter uploadEmoPresenter;

    public enum ManageMode {
        none, editMode, orderMode
    }

    //    private boolean isOnEdit = false;
    private ManageMode currentMode = none;
    private UserList userList;
    private EmoticonsManageAdapter originAdapter;
    private RecyclerView.Adapter adapter;
    private FacehubActionbar actionbar;
    private View dialogContainer, dialog;
    private TextView emoticonsCount, selectedDeleteBtn;
    private boolean isViewAnimating = false;
    //    private ItemTouchHelper itemTouchHelper;
    private FacehubAlertDialog syncAlertDialog;
    private ProgressDialog uploadingDialog;
    View bottomEditBar, bottomSyncBar;

    RecyclerViewDragDropManager recyclerViewDragDropManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_emoticons);
        //设置通知栏颜色
//        setStatusBarColor(FacehubApi.getApi().getActionbarColor());

        actionbar = (FacehubActionbar) findViewById(R.id.actionbar_facehub);
        dialogContainer = findViewById(R.id.mode_dialog_container);
        syncAlertDialog = (FacehubAlertDialog) findViewById(R.id.alert_dialog);
        uploadingDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
        final ArrayList<UserList> userLists = FacehubApi.getApi().getUser().getUserLists();
        if (userLists.size() <= 0) {
            return;
        }
        for (UserList list : userLists) {
            if (list.isDefaultFavorList()) {
                userList = list;
            }
        }
        emoticonsCount = (TextView) findViewById(R.id.emoticons_count_facehub);
        //删除按钮
        selectedDeleteBtn = (TextView) findViewById(R.id.selected_count_facehub);

        final TextView emoticonsCount = (TextView) findViewById(R.id.emoticons_count_facehub);
        final TextView selectedDeleteBtn = (TextView) findViewById(R.id.selected_count_facehub);

        emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");

        RecyclerViewEx recyclerView = (RecyclerViewEx) findViewById(R.id.emoticon_manage_grid_facehub);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);

        // drag & drop manager
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        recyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));
        // Start dragging after long press
        recyclerViewDragDropManager.setInitiateOnLongPress(true);
        recyclerViewDragDropManager.setInitiateOnMove(false);
        recyclerViewDragDropManager.setLongPressTimeout(150);

        //adapter
        originAdapter = new EmoticonsManageAdapter(this);
        originAdapter.setEmoticons(userList.getEmoticons());
        adapter = recyclerViewDragDropManager.createWrappedAdapter(originAdapter);      // wrap for dragging

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);  // requires *wrapped* adapter
        recyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
//            recyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
        }

        recyclerViewDragDropManager.attachRecyclerView(recyclerView);

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
                if (getCurrentMode() == none) {
                    showDialog();
//                    setCurrentMode(ManageMode.editMode);
                } else {
                    setCurrentMode(none);
                }
            }
        });
        bottomEditBar = findViewById(R.id.bottom_bar_facehub);
        bottomSyncBar = findViewById(R.id.bottom_bar_sync);
        bottomEditBar.setBackgroundColor(themeOptions.getThemeColor());
        bottomSyncBar.setBackgroundColor(themeOptions.getThemeColor());

        originAdapter.setSelectChangeListener(new SelectChangeListener() {
            @Override
            public void onSelectChange(ArrayList<Emoticon> selectedEmoticons) {
                selectedDeleteBtn.setText("删除(" + selectedEmoticons.size() + ")");
            }
        });
        assert selectedDeleteBtn != null;
        selectedDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!originAdapter.getSelectedEmoticons().isEmpty()) {
                    //删除表情
                    ArrayList<String> ids = new ArrayList<>();
                    for (Emoticon emoticon : originAdapter.getSelectedEmoticons()) {
                        ids.add(emoticon.getId());
                    }
                    userList.removeEmoticons(ids);
                    FacehubApi.getApi().removeEmoticonsByIds(ids, userList.getId());
                    originAdapter.setEmoticons(userList.getEmoticons());
                    originAdapter.clearSelected();
                    setCurrentMode(none);
                }
            }
        });

        //上传功能
        this.uploadEmoPresenter = new UploadEmoPresenterImpl(this);
        originAdapter.setOnUploadClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadEmoPresenter.startUpload();
            }
        });


        originAdapter.setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(ViewHolder viewHolder) {
//                itemTouchHelper.startDrag(viewHolder);
            }
        });

        if (!userList.isPrepared()) {
            syncAlertDialog.showSyncHint();
            bottomSyncBar.setVisibility(View.VISIBLE);
        }
        bottomSyncBar.findViewById(R.id.sync_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.isDownloading()) {
                    return;
                }
                bottomSyncBar.setVisibility(View.GONE);
                syncAlertDialog.showSyncing();
                userList.prepare(new ResultHandlerInterface() {
                    @Override
                    public void onResponse(Object response) {
                        adapter.notifyDataSetChanged();
                        syncAlertDialog.showSyncSuccess();
                    }

                    @Override
                    public void onError(Exception e) {
                        adapter.notifyDataSetChanged();
                        syncAlertDialog.showSyncFail();
                        bottomSyncBar.setVisibility(View.VISIBLE);
                        LogX.e("表情管理页同步表情失败 : " + e);
                    }
                }, new ProgressInterface() {
                    @Override
                    public void onProgress(double process) {

                    }
                });
            }
        });

        EventBus.getDefault().register(this);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            LogX.w(getClass().getName() + " || EventBus 反注册出错 : " + e);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(uploadEmoPresenter.handleUploadIntent(this,requestCode, resultCode, data)){
            return;
        }
        LogX.v(getClass().getName() + " onActivityResult() . ");
    }

    //开始上传表情
    @Override
    public void onUploadStart() {
//        Toast.makeText(ManageEmoticonsActivity.this, "开始上传", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constants.RESULT_LOAD_IMAGE);
    }

    //选好图片，开始上传文件
    @Override
    public void onPicSelected() {
//        syncAlertDialog.showUploadAlert(UPLOADING);
        uploadingDialog.setIndeterminate(true);
        uploadingDialog.setMessage("正在上传...");
        uploadingDialog.setCancelable(false);
        uploadingDialog.show();
    }

    //表情上传完成
    @Override
    public void onUploadFinish(String resultType) {
//        Toast.makeText(ManageEmoticonsActivity.this, "上传完成,是否成功 : " + resultType, Toast.LENGTH_SHORT).show();
        uploadingDialog.cancel();
        syncAlertDialog.showUploadAlert(resultType);
    }

    public void onEvent(DownloadProgressEvent event) {
        if (event.listId.equals(userList.getId())) {
            originAdapter.setEmoticons(userList.getEmoticons());
        }
    }

    public void onEvent(EmoticonCollectEvent event){
        LogX.fastLog(getClass().getName() + "接收到表情收藏的事件");
        originAdapter.setEmoticons(userList.getEmoticons());
    }

    public void onEvent(ExitViewsEvent exitViewsEvent) {
        finish();
    }

    private ManageMode getCurrentMode() {
        return this.currentMode;
    }


    //点击编辑按钮 : none/非none
    //点击mode弹窗 : 排序/编辑

    private void setCurrentMode(ManageMode mode) {
        boolean doSave = (currentMode == ManageMode.orderMode);
        switch (mode) {
            case none: //切换到查看模式
                if (!userList.isPrepared()) {
                    bottomSyncBar.setVisibility(View.VISIBLE);
                }
                bottomEditBar.setVisibility(View.GONE);
                currentMode = none;
                actionbar.setEditBtnText("编辑");
                emoticonsCount.setText("共有" + originAdapter.getEmoticons().size() + "个表情");
                fastLog("需要替换列表? : " + doSave);
                if (doSave) {
                    ArrayList<String> emoIds = new ArrayList<>();
                    for (Emoticon emoticon : originAdapter.getEmoticons()) {
                        emoIds.add(emoticon.getId());
                    }
//                    userList.setEmoticons(originAdapter.getEmoticons());
                    FacehubApi.getApi().replaceEmoticonsByIds(FacehubApi.getApi().getUser(), emoIds, userList.getId(), new ResultHandlerInterface() {
                        @Override
                        public void onResponse(Object response) {
                            LogX.i("列表表情替换成功!");
                            originAdapter.setEmoticons(userList.getEmoticons());
                        }

                        @Override
                        public void onError(Exception e) {
                            LogX.e("列表表情替换出错 : " + e);
                        }
                    });
                }
                break;

            case editMode: //切换到编辑模式
                bottomSyncBar.setVisibility(View.GONE);
                currentMode = ManageMode.editMode;
                actionbar.setEditBtnText("完成");
                bottomEditBar.setVisibility(View.VISIBLE);
                emoticonsCount.setText("共有" + userList.getEmoticons().size() + "个表情");
                selectedDeleteBtn.setText("删除(0)");
                break;

            case orderMode: //切换到排序模式
                if (userList.isPrepared()) {
                    bottomEditBar.setVisibility(View.GONE);
                    currentMode = ManageMode.orderMode;
                    actionbar.setEditBtnText("完成");
                } else {
                    bottomSyncBar.setVisibility(View.VISIBLE);
                    syncAlertDialog.showSyncHint();
                }
                break;

            default:
                break;
        }
        currentMode = mode;
        originAdapter.setManageMode(mode);
        originAdapter.clearSelected();
        originAdapter.notifyDataSetChanged();
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

    class DialogBtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isViewAnimating) {
                return;
            }
            int i = v.getId();
            if (i == R.id.order_btn) {
                setCurrentMode(ManageMode.orderMode);
            } else if (i == R.id.edit_btn) {
                setCurrentMode(ManageMode.editMode);
            } else if (i == R.id.cancel_btn) {
                setCurrentMode(none);
            }
            hideDialog();
        }
    }

}

/**
 * 表情选中的回调
 */
interface SelectChangeListener {
    public void onSelectChange(ArrayList<Emoticon> selectedEmoticons);
}

/**
 * 表情管理页Adapter
 */
class EmoticonsManageAdapter extends RecyclerView.Adapter<ViewHolder>
        implements DraggableItemAdapter<ViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Emoticon> emoticons = new ArrayList<>();
    private ArrayList<Emoticon> selectedEmoticons = new ArrayList<>();
    private ManageEmoticonsActivity.ManageMode manageMode = none;
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
    public View.OnClickListener onUploadClick;

    public EmoticonsManageAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void setEmoticons(ArrayList<Emoticon> emoticons) {
        this.emoticons = new ArrayList<>(emoticons);
        notifyDataSetChanged();
    }

    public ArrayList<Emoticon> getEmoticons() {
        return emoticons;
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

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnUploadClick(View.OnClickListener onUploadClick) {
        this.onUploadClick = onUploadClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.emoticon_grid_item, parent, false);
        Holder holder = new Holder(convertView);
//        holder.handleView = convertView.findViewById(R.id.handle_view);
        holder.imageView = (SpImageView) convertView.findViewById(R.id.grid_image);
        holder.shade = (SpImageView) convertView.findViewById(R.id.shade);
        holder.addCross = convertView.findViewById(R.id.add_cross);
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
        if (manageMode == ManageEmoticonsActivity.ManageMode.orderMode) { //排序模式时都用同一个边框
            convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background_full);
        } else {
            if (position % 5 == 4) {
                convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background_5);
            } else {
                convertView.setBackgroundResource(R.drawable.emoticon_grid_item_background);
            }
        }

        holder.itemView.setOnTouchListener(null);

        Emoticon emoticon = null;
        //视图区别
        holder.shade.setVisibility(View.GONE);
        holder.checkIcon.setVisibility(View.GONE);
        if (manageMode == none) { //普通模式,显示"+"
            if (position == 0) {
                convertView.setOnClickListener(onUploadClick);
            } else {
                emoticon = emoticons.get(position - 1);
            }
        } else if (manageMode == editMode) { //编辑模式,增加点击监听
            emoticon = emoticons.get(position);
            if (selectedEmoticons.contains(emoticon)) {
                holder.shade.setVisibility(View.VISIBLE);
                holder.checkIcon.setVisibility(View.VISIBLE);
            }
            final Emoticon finalEmoticon = emoticon;
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedEmoticons.contains(finalEmoticon)) { //取消选择
                        selectedEmoticons.remove(finalEmoticon);
                        notifyDataSetChanged();
                    } else {
                        selectedEmoticons.add(finalEmoticon);
                        notifyDataSetChanged();
                    }
                    selectChangeListener.onSelectChange(selectedEmoticons);
                }
            });
        } else { //排序模式
            emoticon = emoticons.get(position);
        }

        //显示图片
        holder.addCross.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.VISIBLE);
        if (emoticon == null) {
            holder.addCross.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.imageView.setImageResource(R.drawable.add_emo_manage);
        } else {
            holder.imageView.displayFile(emoticon.getThumbPath());
        }
    }

    @Override
    public long getItemId(int position) {
        if (position <= emoticons.size() - 1) {
            return emoticons.get(position).getDbId();
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if (manageMode == none) {
            return emoticons.size() + 1;
        }
        return emoticons.size();
    }

    //region 继承拖动Adapter
    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        if (manageMode == ManageEmoticonsActivity.ManageMode.orderMode) {
            return true;
        }
        return false;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        Emoticon emoticon = emoticons.remove(fromPosition);
        emoticons.add(toPosition, emoticon);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        if (manageMode == ManageEmoticonsActivity.ManageMode.orderMode) {
            return true;
        }
        return false;
    }
    //endregion

    class Holder extends AbstractDraggableItemViewHolder {
        SpImageView imageView, shade;
        View checkIcon, addCross;
//        handleView;

        public Holder(View itemView) {
            super(itemView);
        }
    }
}
