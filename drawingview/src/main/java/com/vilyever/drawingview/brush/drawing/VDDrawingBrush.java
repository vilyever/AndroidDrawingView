package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDDrawingBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/20.
 * Feature:
 * 绘制图形类brush
 *
 * Known Direct Subclasses:
 * {@link VDPenBrush}
 * {@link VDShapeBrush}
 */
public abstract class VDDrawingBrush extends VDBrush {
    final VDDrawingBrush self = this;

    /* #Constructors */
    public VDDrawingBrush() {
    }

    public VDDrawingBrush(float size, int color) {
        this.size = size;
        this.color = color;
    }

    /* Properties */
    /**
     * 笔刷大小
     */
    protected float size;
    public float getSize() {
        return size * self.getDrawingRatio();
    }
    public <T extends VDDrawingBrush> T setSize(float size) {
        this.size = size;
        self.updatePaint();
        return (T) self;
    }

    /**
     * 笔刷颜色
     */
    protected int color;
    public int getColor() {
        if (isEraser) {
            return Color.TRANSPARENT;
        }
        return color;
    }
    public <T extends VDDrawingBrush> T setColor(int color) {
        this.color = color;
        self.updatePaint();
        return (T) self;
    }

    /**
     * 是否是橡皮擦
     */
    protected boolean isEraser;
    public boolean isEraser() {
        return isEraser;
    }
    public <T extends VDDrawingBrush> T setIsEraser(boolean isEraser) {
        this.isEraser = isEraser;
        self.updatePaint();
        return (T) self;
    }

    /**
     * paint
     * 临时存储以免每次绘制都生成
     */
    @VDJsonKeyIgnore
    protected Paint paint;
    public Paint getPaint() {
        if (self.paint == null) {
            self.paint = new Paint();
            self.paint.setAntiAlias(true);
            self.paint.setDither(true);
            self.updatePaint();
        }

        return paint;
    }

    /* #Overrides */
    @Override
    public boolean isOneStrokeToLayer() {
        if (self.isEraser()) {
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
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        self.updatePaint();
        if (drawingPath.getPoints().size() < 1) {
            return Frame.EmptyFrame();
        }

        VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);

        RectF drawingRect = new RectF();

        drawingRect.left = beginPoint.getX();
        drawingRect.top = beginPoint.getY();
        drawingRect.right = beginPoint.getX();
        drawingRect.bottom = beginPoint.getY();

        for (int i = 1; i < drawingPath.getPoints().size(); i++) {
            VDDrawingPoint point = drawingPath.getPoints().get(i);
            drawingRect.left = Math.min(point.getX(), drawingRect.left);
            drawingRect.top = Math.min(point.getY(), drawingRect.top);
            drawingRect.right = Math.max(point.getX(), drawingRect.right);
            drawingRect.bottom = Math.max(point.getY(), drawingRect.bottom);
        }

        return self.makeFrameWithBrushSpace(drawingRect);
    }

    /* Protected Methods */
    /**
     * 更新paint
     */
    protected void updatePaint() {
        self.getPaint().setStrokeWidth(self.getSize());
        self.getPaint().setColor(self.getColor());

        if (self.isEraser()) {
            self.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    /**
     * 修正绘制边界
     * @param drawingRect 原绘制边界
     * @return 包含笔刷尺寸的边界
     */
    protected Frame makeFrameWithBrushSpace(RectF drawingRect) {
        return new Frame(drawingRect.left - self.getSize() / 2.0f,
                            drawingRect.top - self.getSize() / 2.0f,
                            drawingRect.right + self.getSize() / 2.0f,
                            drawingRect.bottom + self.getSize() / 2.0f);
    }
}