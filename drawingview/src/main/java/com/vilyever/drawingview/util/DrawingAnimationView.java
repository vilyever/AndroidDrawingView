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

import com.vilyever.unitconversion.DimenConverter;

/**
 * VDDrawingAnimationView
 * AndroidDrawingView <com.vilyever.drawingview.util>
 * Created by vilyever on 2015/12/1.
 * Feature:
 * 绘制忙碌时显示的动画视图
 */
public class DrawingAnimationView extends View {
    final DrawingAnimationView self = this;

    private static final float AnimationRadius = DimenConverter.dpToPixel(25);

    /* #Constructors */
    public DrawingAnimationView(Context context) {
        this(context, null);
    }

    public DrawingAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /* Properties */
    private boolean animated;
    public DrawingAnimationView setAnimated(boolean animated) {
        this.animated = animated;
        setAnimationStartPointPercent(0);
        invalidate();
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
            animationPaint.setStrokeWidth(DimenConverter.dpToPixel(5));
            animationPaint.setColor(Color.parseColor("#43B8F7"));
        }
        return animationPaint;
    }

    private int animationStartPointPercent;
    private DrawingAnimationView setAnimationStartPointPercent(int animationStartPointPercent) {
        this.animationStartPointPercent = animationStartPointPercent % 101;
        return this;
    }
    private int getAnimationStartPointPercent() {
        return animationStartPointPercent;
    }

    private int animationSweepPercent;
    private DrawingAnimationView setAnimationSweepPercent(int animationSweepPercent) {
        this.animationSweepPercent = animationSweepPercent;
        if (this.animationSweepPercent == 120) {
            setSweepReverse(true);
        }
        else if (this.animationSweepPercent == -30) {
            setSweepReverse(false);
        }
        return this;
    }
    private int getAnimationSweepPercent() {
        int percent = Math.min(100, animationSweepPercent);
        percent = Math.max(0, animationSweepPercent);
        return percent;
    }

    private boolean sweepReverse;
    private DrawingAnimationView setSweepReverse(boolean sweepReverse) {
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
        if (isAnimated()) {
            getAnimationRect().set(getWidth() / 2.0f - AnimationRadius,
                    getHeight() / 2.0f - AnimationRadius,
                    getWidth() / 2.0f + AnimationRadius,
                    getHeight() / 2.0f + AnimationRadius);

            float startAngle = 360.0f * getAnimationStartPointPercent() / 100.0f;
            float sweepAngle = -(360.0f * 0.65f * getAnimationSweepPercent() / 100.0f + 5.0f);

            getAnimationPath().reset();
            getAnimationPath().addArc(animationRect, startAngle, sweepAngle);

            canvas.drawPath(getAnimationPath(), getAnimationPaint());

            if (getAnimationSweepPercent() == 0) {
                setAnimationStartPointPercent(getAnimationStartPointPercent() + 1);
            }
            else {
                setAnimationStartPointPercent(getAnimationStartPointPercent() + 3);
            }

            if (!isSweepReverse()) {
                setAnimationSweepPercent(animationSweepPercent + 3);
            }
            else {
                setAnimationSweepPercent(animationSweepPercent - 3);
            }

            invalidate();
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
    private void init() {
        setBackground(null);
    }
}