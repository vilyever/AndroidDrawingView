package com.vilyever.drawingview.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vilyever.unitconversion.VDDimenConversion;

/**
 * VDDrawingAnimationView
 * AndroidDrawingView <com.vilyever.drawingview.util>
 * Created by vilyever on 2015/12/1.
 * Feature:
 * 绘制忙碌时显示的动画视图
 */
public class VDDrawingAnimationView extends View {
    final VDDrawingAnimationView self = this;

    private static final float AnimationRadius = VDDimenConversion.dpToPixel(25);

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

    /* Properties */
    private boolean animated;
    public VDDrawingAnimationView setAnimated(boolean animated) {
        this.animated = animated;
        self.setAnimationStartPointPercent(0);
        self.invalidate();
        return this;
    }
    public boolean isAnimated() {
        return animated;
    }

    /**
     * 动画绘制矩形
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private RectF animationRect;
    private RectF getAnimationRect() {
        if (animationRect == null) {
            animationRect = new RectF();
        }
        return animationRect;
    }

    private Path animationPath;
    private Path getAnimationPath() {
        if (animationPath == null) {
            animationPath = new Path();
        }
        return animationPath;
    }

    private Paint animationPaint;
    private Paint getAnimationPaint() {
        if (animationPaint == null) {
            animationPaint = new Paint();
            animationPaint.setAntiAlias(true);
            animationPaint.setDither(true);
            animationPaint.setStyle(Paint.Style.STROKE);
            animationPaint.setStrokeJoin(Paint.Join.ROUND);
            animationPaint.setStrokeCap(Paint.Cap.ROUND);
            animationPaint.setStrokeWidth(VDDimenConversion.dpToPixel(5));
            animationPaint.setColor(Color.parseColor("#43B8F7"));
        }
        return animationPaint;
    }

    private int animationStartPointPercent;
    private VDDrawingAnimationView setAnimationStartPointPercent(int animationStartPointPercent) {
        this.animationStartPointPercent = animationStartPointPercent % 101;
        return this;
    }
    private int getAnimationStartPointPercent() {
        return animationStartPointPercent;
    }

    private int animationSweepPercent;
    private VDDrawingAnimationView setAnimationSweepPercent(int animationSweepPercent) {
        this.animationSweepPercent = animationSweepPercent;
        if (this.animationSweepPercent == 120) {
            self.setSweepReverse(true);
        }
        else if (this.animationSweepPercent == -30) {
            self.setSweepReverse(false);
        }
        return this;
    }
    private int getAnimationSweepPercent() {
        int percent = Math.min(100, animationSweepPercent);
        percent = Math.max(0, animationSweepPercent);
        return percent;
    }

    private boolean sweepReverse;
    private VDDrawingAnimationView setSweepReverse(boolean sweepReverse) {
        this.sweepReverse = sweepReverse;
        return this;
    }
    private boolean isSweepReverse() {
        return sweepReverse;
    }

    /* Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制动画
        if (self.isAnimated()) {
            self.getAnimationRect().set(self.getWidth() / 2.0f - AnimationRadius,
                    self.getHeight() / 2.0f - AnimationRadius,
                    self.getWidth() / 2.0f + AnimationRadius,
                    self.getHeight() / 2.0f + AnimationRadius);

            float startAngle = 360.0f * self.getAnimationStartPointPercent() / 100.0f;
            float sweepAngle = -(360.0f * 0.65f * self.getAnimationSweepPercent() / 100.0f + 5.0f);

            self.getAnimationPath().reset();
            self.getAnimationPath().addArc(animationRect, startAngle, sweepAngle);

            canvas.drawPath(self.getAnimationPath(), self.getAnimationPaint());

            if (self.getAnimationSweepPercent() == 0) {
                self.setAnimationStartPointPercent(self.getAnimationStartPointPercent() + 1);
            }
            else {
                self.setAnimationStartPointPercent(self.getAnimationStartPointPercent() + 3);
            }

            if (!self.isSweepReverse()) {
                self.setAnimationSweepPercent(self.animationSweepPercent + 3);
            }
            else {
                self.setAnimationSweepPercent(self.animationSweepPercent - 3);
            }

            self.invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 拦截touch事件
        return true;
    }

    /* Private Methods */

    /**
     * 初始化
     */
    private void initial() {
        self.setBackground(null);
    }
}