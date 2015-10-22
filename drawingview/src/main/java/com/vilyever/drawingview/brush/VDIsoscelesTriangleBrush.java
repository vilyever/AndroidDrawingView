package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDIsoscelesTriangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDIsoscelesTriangleBrush extends VDShapeBrush {
    final VDIsoscelesTriangleBrush self = this;


    /* #Constructors */
    public VDIsoscelesTriangleBrush() {

    }

    public VDIsoscelesTriangleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDIsoscelesTriangleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDIsoscelesTriangleBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */
    @Override
    public boolean drawPath(Canvas canvas, VDDrawingPath drawingPath, VDDrawingBrush.DrawingPointerState state) {
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
            path.lineTo((rect.left + rect.right) / 2, rect.top);
            path.lineTo(rect.left, rect.bottom);

            self.drawSolidShapePath(canvas, path);
        }

        if (state == VDDrawingBrush.DrawingPointerState.End) {
            return true;
        }
        return false;
    }
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDIsoscelesTriangleBrush defaultBrush() {
        return new VDIsoscelesTriangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}