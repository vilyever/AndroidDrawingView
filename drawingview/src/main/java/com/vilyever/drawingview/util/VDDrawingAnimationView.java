package com.vilyever.drawingview.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.R;

/**
 * VDDrawingAnimationView
 * AndroidDrawingView <com.vilyever.drawingview.util>
 * Created by vilyever on 2015/12/1.
 * Feature:
 * 绘制忙碌时显示的动画视图
 */
public class VDDrawingAnimationView extends View {
    final VDDrawingAnimationView self = this;

    /* #Constructors */
    public VDDrawingAnimationView(Context context) {
        this(context, null);
    }

    public VDDrawingAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VDDrawingAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        self.initial();
    }


//    private boolean animated;
    private boolean animated;
    public VDDrawingAnimationView setAnimated(boolean animated) {
        this.animated = animated;
        self.pointPercent = 0;
        self.invalidate();
        return this;
    }
    public boolean isAnimated() {
        return animated;
    }

    private Drawable animitionDrawable;
    private Drawable getAnimitionDrawable() {
        if (animitionDrawable == null) {
            animitionDrawable = VDContextHolder.getContext().getResources().getDrawable(R.drawable.animation_drawing_brush);
        }
        return animitionDrawable;
    }

    private PathMeasure pathMeasure;
    private VDDrawingAnimationView setPathMeasure(PathMeasure pathMeasure) {
        this.pathMeasure = pathMeasure;
        return this;
    }
    private PathMeasure getPathMeasure() {
        if (pathMeasure == null) {
            int width = self.getWidth() / 12;
            int height = self.getHeight() / 12;

            int left = (self.getWidth() - width) / 2;
            int top = (self.getHeight() - height) / 2;

            Path path = new Path();

            int x = left;
            int y = top + height / 2;

            path.moveTo(x, y);
            path.lineTo(left + width / 2, top);
            path.lineTo(left + width / 6, top + height);
            path.lineTo(left + width, top + height / 6);

            pathMeasure = new PathMeasure(path, false);
        }
        return pathMeasure;
    }

    float[] points = new float[2];
    private float pointPercent = 0;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            // 视图尺寸变更时重置动画路径，置空后在下次getter时懒加载
            self.setPathMeasure(null);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制动画，即一张移动的笔状图片
        if (self.isAnimated()) {
            self.getPathMeasure().getPosTan(self.getPathMeasure().getLength() * (self.pointPercent / 100.0f), self.points, null);

            int centerX = (int) self.points[0];
            int centerY = (int) self.points[1];

            self.getAnimitionDrawable().setBounds(centerX - self.getAnimitionDrawable().getIntrinsicWidth() / 2,
                    centerY - self.getAnimitionDrawable().getIntrinsicHeight() / 2,
                    centerX + self.getAnimitionDrawable().getIntrinsicWidth() / 2,
                    centerY + self.getAnimitionDrawable().getIntrinsicHeight() / 2);
            self.getAnimitionDrawable().draw(canvas);

            self.pointPercent = (self.pointPercent + 2) % 100;

            self.invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 拦截touch事件
        return true;
    }

    /**
     * 初始化
     */
    private void initial() {
        self.setBackground(null);

        self.points = new float[2];
        self.pointPercent = 0;
    }
}