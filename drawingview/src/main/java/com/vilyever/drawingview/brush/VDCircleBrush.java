package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDCircleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDCircleBrush extends VDShapeBrush {
    final VDCircleBrush self = this;

    
    /* #Constructors */
    public VDCircleBrush() {

    }

    public VDCircleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDCircleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDCircleBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */
    @Override
    public boolean drawPath(Canvas canvas, VDDrawingPath drawingPath, DrawingPointerState state) {
        if (canvas == null
                || drawingPath == null) {
            return true;
        }

        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            float centerX = (beginPoint.x + lastPoint.x) / 2.0f;
            float centerY = (beginPoint.y + lastPoint.y) / 2.0f;
            float radius = Math.min(Math.abs(beginPoint.x - lastPoint.x), Math.abs(beginPoint.y - lastPoint.y)) / 2.0f;

            Path path = new Path();
            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

            self.drawSolidShapePath(canvas, path);
        }

        if (state == DrawingPointerState.End) {
            return true;
        }
        return false;
    }
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDCircleBrush defaultBrush() {
        return new VDCircleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}