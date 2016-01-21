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
 */
public abstract class VDDrawingBrush extends VDBrush {
    final VDDrawingBrush self = this;

    protected float size;
    protected int color;
    protected boolean isEraser;
    protected boolean oneStrokeToLayer;

    protected Paint paint;

    /* #Constructors */
    public VDDrawingBrush() {
    }

    public VDDrawingBrush(float size, int color) {
        this.size = size;
        this.color = color;
    }

    /* #Overrides */
    @Override
    public boolean shouldDrawFromBegin() {
        return true;
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
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

    /* #Accessors */
    public float getSize() {
        return size * self.getDrawingRatio();
    }

    public <T extends VDDrawingBrush> T setSize(float size) {
        this.size = size;
        self.updatePaint();
        return (T) self;
    }

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

    public boolean isEraser() {
        return isEraser;
    }

    public <T extends VDDrawingBrush> T setIsEraser(boolean isEraser) {
        this.isEraser = isEraser;
        self.updatePaint();
        return (T) self;
    }

    public boolean isOneStrokeToLayer() {
        if (self.isEraser()) {
            return false;
        }
        return oneStrokeToLayer;
    }

    public <T extends VDDrawingBrush> T setOneStrokeToLayer(boolean oneStrokeToLayer) {
        this.oneStrokeToLayer = oneStrokeToLayer;
        return (T) self;
    }

    public Paint getPaint() {
        if (self.paint == null) {
            self.paint = new Paint();
            self.paint.setAntiAlias(true);
            self.paint.setDither(true);
            self.updatePaint();
        }

        return paint;
    }

    /* #Delegates */
     
    /* #Private Methods */
    protected void updatePaint() {
        self.getPaint().setStrokeWidth(self.getSize());
        self.getPaint().setColor(self.getColor());

        if (self.isEraser()) {
            self.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    /* #Protected Methods */
    protected Frame makeFrameWithBrushSpace(RectF drawingRect) {
        return new Frame(drawingRect.left - self.getSize() / 2.0f,
                            drawingRect.top - self.getSize() / 2.0f,
                            drawingRect.right + self.getSize() / 2.0f,
                            drawingRect.bottom + self.getSize() / 2.0f);
    }

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}