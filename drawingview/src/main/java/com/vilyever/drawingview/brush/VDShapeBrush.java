package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDShapeBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public abstract class VDShapeBrush extends VDDrawingBrush {
    final VDShapeBrush self = this;

    protected int solidColor;
    protected boolean edgeRounded;

    /* #Constructors */
    public VDShapeBrush() {

    }

    public VDShapeBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDShapeBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDShapeBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color);
        this.solidColor = solidColor;
        this.edgeRounded = edgeRounded;
    }

    /* #Overrides */
    @Override
    public Paint getPaint() {
        Paint paint = super.getPaint();

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
    public RectF drawPath(@NonNull Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
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
    public int getSolidColor() {
        if (self.isEraser()) {
            return Color.TRANSPARENT;
        }
        return solidColor;
    }

    public <T extends VDShapeBrush> T setSolidColor(int solidColor) {
        this.solidColor = solidColor;
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
    protected void drawSolidShapePath(@NonNull Canvas canvas, @NonNull Path path) {
        Paint paint = self.getPaint();

        // draw solid color
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(self.getSolidColor());
        canvas.drawPath(path, paint);

        // erase the intersection of solid and border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(self.getColor());
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPath(path, paint);

        // draw border color
        paint.setXfermode(null);
        canvas.drawPath(path, paint);
    }
    
    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}