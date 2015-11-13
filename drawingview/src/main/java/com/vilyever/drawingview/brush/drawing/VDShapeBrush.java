package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDShapeBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public abstract class VDShapeBrush extends VDDrawingBrush {
    final VDShapeBrush self = this;

    protected FillType fillType;
    protected boolean edgeRounded;

    /* #Constructors */
    public VDShapeBrush() {

    }

    public VDShapeBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public VDShapeBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public VDShapeBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color);
        this.fillType = fillType;
        this.edgeRounded = edgeRounded;
    }

    /* #Overrides */
    @Override
    public Paint getPaint() {
        Paint paint = super.getPaint();

        switch (self.getFillType()) {
            case Hollow:
                paint.setStyle(Paint.Style.STROKE);
                break;
            case Solid:
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                break;
        }

        if (self.isEdgeRounded()) {
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }
        else {
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStrokeJoin(Paint.Join.MITER);
        }

        return paint;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() < 2) {
            return null;
        }
        else {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF pathFrame = new RectF();

            pathFrame.left = Math.min(beginPoint.x, lastPoint.x);
            pathFrame.top = Math.min(beginPoint.y, lastPoint.y);
            pathFrame.right = Math.max(beginPoint.x, lastPoint.x);
            pathFrame.bottom = Math.max(beginPoint.y, lastPoint.y);

            return self.attachBrushSpace(pathFrame);
        }
    }

    /* #Accessors */
    public FillType getFillType() {
        if (self.isEraser()) {
            return FillType.Solid;
        }
        return fillType;
    }

    public <T extends VDShapeBrush> T setFillType(FillType fillType) {
        this.fillType = fillType;
        return (T) self;
    }

    public boolean isEdgeRounded() {
        return edgeRounded;
    }

    public <T extends VDShapeBrush> T setEdgeRounded(boolean edgeRounded) {
        this.edgeRounded = edgeRounded;
        return (T) self;
    }

    /* #Delegates */
     
    /* #Private Methods */

    /* #Protected Mothods */

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum FillType {
        Hollow, Solid;
    }
}