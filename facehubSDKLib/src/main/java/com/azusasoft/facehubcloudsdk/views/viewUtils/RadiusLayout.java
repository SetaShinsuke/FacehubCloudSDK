package com.azusasoft.facehubcloudsdk.views.viewUtils;

/**
 * Created by SETA on 2015/10/17.
 * 注意：
 * 【1】.values/attrs.xml中添加
 *<pre>
 *  <declare-styleable name="RadiusLayout">
 *  <attr name="layout_radius" format="dimension" />
 *  </declare-styleable>
 *</pre>
 *
 * 【2】.styles.xml中添加
 * <pre>
 *      <style
 *          name="Widget.RadiusLayout" parent="@android:style/Widget">
 *      </style>
 * </pre>
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.azusasoft.facehubcloudsdk.R;

public class RadiusLayout extends FrameLayout {
    private int radiusPx = 0;

    private Bitmap maskBitmap;
    private Paint paint, maskPaint;
    private float cornerRadiusPx;

    public RadiusLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RadiusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RadiusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RadiusLayout, defStyle,
                R.style.Widget_RadiusLayout);
//        radiusPx = array.getInt(R.styleable.RadiusLayout_layout_radius,0);
        radiusPx = array.getDimensionPixelSize(R.styleable.RadiusLayout_layout_radius,0);
        array.recycle();

//        cornerRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusPx, metrics);
        cornerRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, radiusPx, metrics);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        setWillNotDraw(false);
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap offscreenBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas offscreenCanvas = new Canvas(offscreenBitmap);

        super.draw(offscreenCanvas);

        if (maskBitmap == null) {
            maskBitmap = createMask(canvas.getWidth(), canvas.getHeight());
        }

        offscreenCanvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint);
        canvas.drawBitmap(offscreenBitmap, 0f, 0f, paint);
    }

    private Bitmap createMask(int width, int height) {
        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(mask);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);

        canvas.drawRect(0, 0, width, height, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(new RectF(0, 0, width, height), cornerRadiusPx, cornerRadiusPx, paint);

        return mask;
    }
}