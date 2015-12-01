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

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() < 1) {
            return null;
        }

        VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);

        RectF drawingRect = new RectF();

        drawingRect.left = beginPoint.x;
        drawingRect.top = beginPoint.y;
        drawingRect.right = beginPoint.x;
        drawingRect.bottom = beginPoint.y;

        for (int i = 1; i < drawingPath.getPoints().size(); i++) {
            VDDrawingPoint point = drawingPath.getPoints().get(i);
            drawingRect.left = Math.min(point.x, drawingRect.left);
            drawingRect.top = Math.min(point.y, drawingRect.top);
            drawingRect.right = Math.max(point.x, drawingRect.right);
            drawingRect.bottom = Math.max(point.y, drawingRect.bottom);
        }

        return self.attachBrushSpace(drawingRect);
    }

    /* #Accessors */
    public float getSize() {
        return size;
    }

    public <T extends VDDrawingBrush> T setSize(float size) {
        this.size = size;
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
        return (T) self;
    }

    public boolean isEraser() {
        return isEraser;
    }

    public <T extends VDDrawingBrush> T setIsEraser(boolean isEraser) {
        this.isEraser = isEraser;
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

    /* #Delegates */
     
    /* #Private Methods */

    /* #Protected Methods */
    protected RectF attachBrushSpace(RectF drawingRect) {
        return new RectF(drawingRect.left - self.getSize() / 2.0f,
                            drawingRect.top - self.getSize() / 2.0f,
                            drawingRect.right + self.getSize() / 2.0f,
                            drawingRect.bottom + self.getSize() / 2.0f);
    }

    /* #Public Methods */
    public Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(self.getSize());
        paint.setColor(self.getColor());

        if (self.isEraser()) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        return paint;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}