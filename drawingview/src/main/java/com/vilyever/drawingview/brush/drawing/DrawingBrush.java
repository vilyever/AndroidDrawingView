package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.brush.Brush;
import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.drawingview.model.DrawingPoint;

/**
 * DrawingBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/20.
 * Feature:
 * 绘制图形类brush
 *
 * Known Direct Subclasses:
 * {@link PenBrush}
 * {@link ShapeBrush}
 */
public abstract class DrawingBrush extends Brush {
    final DrawingBrush self = this;

    /* #Constructors */
    public DrawingBrush() {
    }

    public DrawingBrush(float size, int color) {
        this.size = size;
        this.color = color;
    }

    /* Properties */
    /**
     * 笔刷大小
     */
    protected float size;
    public float getSize() {
        return this.size * getDrawingRatio();
    }
    public <T extends DrawingBrush> T setSize(float size) {
        this.size = size;
        updatePaint();
        return (T) this;
    }

    /**
     * 笔刷颜色
     */
    protected int color;
    public int getColor() {
        if (isEraser()) {
            return Color.TRANSPARENT;
        }
        return this.color;
    }
    public <T extends DrawingBrush> T setColor(int color) {
        this.color = color;
        updatePaint();
        return (T) this;
    }

    /**
     * 是否是橡皮擦
     */
    protected boolean isEraser;
    public boolean isEraser() {
        return this.isEraser;
    }
    public <T extends DrawingBrush> T setIsEraser(boolean isEraser) {
        this.isEraser = isEraser;
        updatePaint();
        return (T) this;
    }

    /**
     * paint
     * 临时存储以免每次绘制都生成
     */
    @JsonKeyIgnore
    protected Paint paint;
    public Paint getPaint() {
        if (this.paint == null) {
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            this.paint.setDither(true);
            updatePaint();
        }

        return this.paint;
    }

    /* Overrides */
    @Override
    public boolean isOneStrokeToLayer() {
        if (isEraser()) {
            return false;
        }
        return super.isOneStrokeToLayer();
    }

    @Override
    public boolean shouldDrawFromBegin() {
        return true;
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        updatePaint();
        if (drawingPath.getPoints().size() < 1) {
            return Frame.EmptyFrame();
        }

        DrawingPoint beginPoint = drawingPath.getPoints().get(0);

        RectF drawingRect = new RectF();

        drawingRect.left = beginPoint.getX();
        drawingRect.top = beginPoint.getY();
        drawingRect.right = beginPoint.getX();
        drawingRect.bottom = beginPoint.getY();

        for (int i = 1; i < drawingPath.getPoints().size(); i++) {
            DrawingPoint point = drawingPath.getPoints().get(i);
            drawingRect.left = Math.min(point.getX(), drawingRect.left);
            drawingRect.top = Math.min(point.getY(), drawingRect.top);
            drawingRect.right = Math.max(point.getX(), drawingRect.right);
            drawingRect.bottom = Math.max(point.getY(), drawingRect.bottom);
        }

        return makeFrameWithBrushSpace(drawingRect);
    }

    /* Protected Methods */
    /**
     * 更新paint
     */
    protected void updatePaint() {
        getPaint().setStrokeWidth(getSize());
        getPaint().setColor(getColor());

        if (isEraser()) {
            getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    /**
     * 修正绘制边界
     * @param drawingRect 原绘制边界
     * @return 包含笔刷尺寸的边界
     */
    protected Frame makeFrameWithBrushSpace(RectF drawingRect) {
        return new Frame(drawingRect.left - getSize() / 2.0f,
                            drawingRect.top - getSize() / 2.0f,
                            drawingRect.right + getSize() / 2.0f,
                            drawingRect.bottom + getSize() / 2.0f);
    }
}