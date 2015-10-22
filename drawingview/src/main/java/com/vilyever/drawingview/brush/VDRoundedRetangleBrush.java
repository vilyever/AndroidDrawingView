package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDRoundedRetangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRoundedRetangleBrush extends VDShapeBrush {
    final VDRoundedRetangleBrush self = this;

    
    /* #Constructors */
    public VDRoundedRetangleBrush() {

    }

    public VDRoundedRetangleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDRoundedRetangleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDRoundedRetangleBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */
    @Override
    public boolean isEdgeRounded() {
        return true;
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

            float round = Math.min(Math.abs(beginPoint.x - lastPoint.x), Math.abs(beginPoint.y - lastPoint.y)) / 10.0f;
            round = Math.max(round, self.getSize());

            Path path = new Path();
            path.addRoundRect(rect, round, round, Path.Direction.CW);

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
    public static VDRoundedRetangleBrush defaultBrush() {
        return new VDRoundedRetangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}