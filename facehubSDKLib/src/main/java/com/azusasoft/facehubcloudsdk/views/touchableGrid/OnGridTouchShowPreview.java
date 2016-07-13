package com.azusasoft.facehubcloudsdk.views.touchableGrid;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

/**
 * Created by SETA on 2016/7/13.
 * 长按预览+点击 监听器封装.
 *
 * 使用说明:
 * 1.触摸过程中传递的Data请实现{@link DataAvailable}接口，对于{@link DataAvailable#isAvailable()}为false的条目，忽略长按效果;
 * 2.根据参数构造{@link OnGridTouchShowPreview};
 *      构造{@link OnGridTouchShowPreview}
 *      context 上下文;
 *      gridItemTouchListener 处理 点击+长按+脱手 的回调;
 *      scrollTrigger 长按时用来阻断外部的滚动(如viewPager等);
 *      longClickDuration 长按判定时长;
 *      itemPadding 触摸判定的拓展大小;
 */
class OnGridTouchShowPreview implements View.OnTouchListener {
    private Context context;
    private GridItemTouchListener gridItemTouchListener; //处理 点击+长按+脱手 的回调;
    private ScrollTrigger scrollTrigger; //长按时用来阻断滚动
//    private LONG_CLICK_DURATION
    private int longClickDuration = 300;
    private int itemZonePadding = 0;

    private boolean isTouchedOnce = false; //已经在点击中(down时true , up&cancel时false )
    private boolean isLongPressed = false; //已在长按中(task中true, up&cancel时false)
    //pagerTrigger : 与isLongPressed保持相反
    private Handler handler = new Handler();
    private TouchableGridHolder lastTouchedHolder = null;

    /**
     * 构造{@link OnGridTouchShowPreview}
     * @param context 上下文
     * @param gridItemTouchListener 处理 点击+长按+脱手 的回调;
     * @param scrollTrigger 长按时用来阻断外部的滚动(如viewPager等);
     * @param longClickDuration 长按判定时长;
     * @param itemPadding 触摸判定的拓展大小
     */
    public OnGridTouchShowPreview(Context context
            ,GridItemTouchListener gridItemTouchListener //处理 点击+长按+脱手 的回调;
            ,ScrollTrigger scrollTrigger //长按时用来阻断外部的滚动(如viewPager等)
            ,int longClickDuration
            ,int itemPadding){ //长按判定时长
        this.context = context;
        this.gridItemTouchListener = gridItemTouchListener;
        this.scrollTrigger = scrollTrigger;
        this.longClickDuration = longClickDuration;
        this.itemZonePadding = itemPadding;
    }

    public void attatchToGridView(GridView gridView){

    }

    class Task implements Runnable {
        public View touchedView; //触摸的view
        public Object touchedData; //触摸的要预览的数据(eg.一个emoticon)

        @Override
        public void run() {
            isLongPressed = true;
            gridItemTouchListener.onItemLongClick(touchedView, touchedData);
            scrollTrigger.setCanScroll(false);
        }
    }

    Task confirmLongPressTask = new Task();

    //处理触摸
    @Override
    public boolean onTouch(View gridView, MotionEvent event) {
        if(! (gridView instanceof GridView) ){
            Log.e(STGVUtilMethods.TAG,"OnGridTouchShowPreview interface attached to an object which is not a GridView!!");
            return false;
        }

        //GridView是否消耗事件?
        //      : 如果[点击]或者[长按]了，应当消耗
        boolean flag = false; //move & ups时返回true(翻页会进入cancel)

        int action = event.getAction();
        //拿到相应位置的itemView ()
        TouchableGridHolder touchableGridHolder = null;
        int[] location = new int[2];
        gridView.getLocationInWindow(location);
        View itemView = getViewByPosition(
                (GridView) gridView
                , event.getX() + location[0]
                , event.getY() + location[1]);

        if ((itemView != null)  && (itemView.getTag() instanceof TouchableGridHolder)) {
            touchableGridHolder = (TouchableGridHolder) itemView.getTag();

        } else { /** itemView空/holder空,取消预览 **/
            handler.removeCallbacks(confirmLongPressTask);
            gridItemTouchListener.onItemOffTouch(null, null);
            scrollTrigger.setCanScroll(true);
            isLongPressed = false;
            isTouchedOnce = false;
            if(lastTouchedHolder!=null){
                lastTouchedHolder.offTouchEffect();
            }
            return false;
        }
        if (touchableGridHolder.data == null) { //emoticon空，不作处理
            if(lastTouchedHolder!=null){
                lastTouchedHolder.offTouchEffect();
            }
            return false;
        }
        confirmLongPressTask.touchedView = itemView;
        confirmLongPressTask.touchedData = touchableGridHolder.data;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchedOnce) {
                    break;
                }
                lastTouchedHolder = null;
                isTouchedOnce = true;
                isLongPressed = false;
                if (!touchableGridHolder.data.isAvailable()) { //id空表示为"+"加号，不产生点击效果
                    touchableGridHolder.onTouchedEffect();
                }
                handler.postDelayed(confirmLongPressTask, longClickDuration);
                break;
            case MotionEvent.ACTION_MOVE:
                flag = true;

                if (lastTouchedHolder != null && lastTouchedHolder != touchableGridHolder) { //触摸的holder变了
//                            fastLog("触摸的holder变了");
                    lastTouchedHolder.offTouchEffect();
                }
                lastTouchedHolder = touchableGridHolder;

                if (isLongPressed) { //长按+移动时，切换预览图
                    handler.removeCallbacks(confirmLongPressTask);
                    gridItemTouchListener.onItemLongClick(itemView, touchableGridHolder.data);
                    scrollTrigger.setCanScroll(false);
                }
                break;
            case MotionEvent.ACTION_UP:
//                        fastLog("up.");
                handler.removeCallbacks(confirmLongPressTask);
                isTouchedOnce = false;
                if (isLongPressed) { //长按时松手,调用offTouch,取消预览
                    gridItemTouchListener.onItemOffTouch(itemView, touchableGridHolder.data);
                    touchableGridHolder.offTouchEffect();
                } else { //非长按松手,认为做了点击
                    gridItemTouchListener.onItemClick(itemView, touchableGridHolder.data);
                    touchableGridHolder.offTouchEffect();
                }
                scrollTrigger.setCanScroll(true);
                isLongPressed = false;
                flag = true;
                lastTouchedHolder = null;
                break;
            case MotionEvent.ACTION_CANCEL:
//                        fastLog("cancel.");
                handler.removeCallbacks(confirmLongPressTask);
                gridItemTouchListener.onItemOffTouch(itemView, touchableGridHolder.data);
                touchableGridHolder.offTouchEffect();
                scrollTrigger.setCanScroll(true);
                isLongPressed = false;
                isTouchedOnce = false;
                lastTouchedHolder = null;
//                        flag = true;
                break;
        }
        return flag;
    }

    //拿到itemView
    private View getViewByPosition(GridView grid, float x, float y) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            // do stuff with child view
            if (STGVUtilMethods.isInZoneOf(context, child, x, y, itemZonePadding) )
                return child;
        }
        return null;
    }
}