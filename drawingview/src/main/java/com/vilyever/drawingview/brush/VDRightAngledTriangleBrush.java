package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDRightAngledTriangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRightAngledTriangleBrush extends VDShapeBrush {
    final VDRightAngledTriangleBrush self = this;


    /* #Constructors */
    public VDRightAngledTriangleBrush() {

    }

    public VDRightAngledTriangleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDRightAngledTriangleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDRightAngledTriangleBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */
    @Override
    public Paint getPaint() {
        Paint paint = super.getPaint();
        paint.setStrokeMiter(Integer.MAX_VALUE);
        return paint;
    }

    @Override
    public boolean drawPath(Canvas canvas, VDDrawingPath drawingPath, DrawingPointerState state) {
        if (canvas == null
                || drawingPath == null) {
            return true;
        }

        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF rect = new RectF();
            rect.left = Math.min(beginPoint.x, lastPoint.x);
            rect.top = Math.min(beginPoint.y, lastPoint.y);
            rect.right = Math.max(beginPoint.x, lastPoint.x);
            rect.bottom = Math.max(beginPoint.y, lastPoint.y);

            Path path = new Path();
            path.moveTo(rect.left, rect.bottom);
            path.lineTo(rect.right, rect.bottom);
            path.lineTo(rect.left, rect.top);
            path.lineTo(rect.left, rect.bottom);

            self.drawSolidShapePath(canvas, path);
        }

        if (state == DrawingPointerState.End) {
            return true;
        }
        return false;
    }

    @Override
    public RectF getDrawingFrame(VDDrawingPath drawingPath) {
        RectF rect = super.getDrawingFrame(drawingPath);
        if (self.isEdgeRounded()
            || rect == null) {
            return rect;
        }

        VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
        VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

        RectF pathRect = new RectF();
        pathRect.left = Math.min(beginPoint.x, lastPoint.x);
        pathRect.top = Math.min(beginPoint.y, lastPoint.y);
        pathRect.right = Math.max(beginPoint.x, lastPoint.x);
        pathRect.bottom = Math.max(beginPoint.y, lastPoint.y);

        float x = pathRect.right - pathRect.left;
        float y = pathRect.bottom - pathRect.top;
        double h = (x * y) / (Math.sqrt(x * x + y * y));
        double factor = (h + self.getSize() / 2 * Math.sqrt(2)) / h;

        rect.right += x * (factor - 1);
        rect.top -= y * (factor - 1);

        return rect;
    }

    /* #Accessors */
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDRightAngledTriangleBrush defaultBrush() {
        return new VDRightAngledTriangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}