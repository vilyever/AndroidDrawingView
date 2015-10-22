package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;
import com.vilyever.jsonmodel.VDJson;
import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/20.
 * Feature:
 */
public abstract class VDDrawingBrush extends VDModel {
    final VDDrawingBrush self = this;

    protected float size;
    protected int color;
    private boolean isEraser;
    protected boolean oneStrokeToLayer;

    /* #Constructors */
    public VDDrawingBrush() {
    }

    public VDDrawingBrush(float size, int color) {
        this.size = size;
        this.color = color;
    }

    /* #Overrides */    
    
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
    
    /* #Public Methods */
    public static <T extends VDDrawingBrush> T copy(VDDrawingBrush brush) {
        return (T) new VDJson<>(brush.getClass()).modelFromJson(brush.toJson());
    }

    public boolean disableLayerTouch() {
        return false;
    }

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

    /**
     *
     * @param canvas the canvas in drawing
     * @param drawingPath the path will draw
     * @param state the finger pointer touching event state
     * @return is this path has enough points to finish a single draw with such brush
     */
    public abstract boolean drawPath(Canvas canvas, VDDrawingPath drawingPath, DrawingPointerState state);

    public RectF getDrawingFrame(VDDrawingPath drawingPath) {
        if (drawingPath == null
                || drawingPath.getPoints().size() < 1) {
            return null;
        }

        VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);

        float leftest = beginPoint.x;
        float rightest = beginPoint.x;
        float topest = beginPoint.y;
        float bottomest = beginPoint.y;

        for (int i = 1; i < drawingPath.getPoints().size(); i++) {
            VDDrawingPoint point = drawingPath.getPoints().get(i);
            leftest = Math.min(point.x, leftest);
            rightest = Math.max(point.x, rightest);
            topest = Math.min(point.y, topest);
            bottomest = Math.max(point.y, bottomest);
        }

        float offset = self.getSize() + 16;

        return new RectF(leftest - offset,
                            topest - offset,
                            rightest + offset,
                            bottomest + offset);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum DrawingPointerState {
        Begin, Drawing, End;
    }
}