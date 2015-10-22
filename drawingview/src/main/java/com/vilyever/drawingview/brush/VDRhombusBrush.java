package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDRhombusBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRhombusBrush extends VDShapeBrush {
    final VDRhombusBrush self = this;


    /* #Constructors */
    public VDRhombusBrush() {

    }

    public VDRhombusBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDRhombusBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDRhombusBrush(float size, int color, int solidColor, boolean edgeRounded) {
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

            RectF rect = new RectF();
            rect.left = Math.min(beginPoint.x, lastPoint.x);
            rect.top = Math.min(beginPoint.y, lastPoint.y);
            rect.right = Math.max(beginPoint.x, lastPoint.x);
            rect.bottom = Math.max(beginPoint.y, lastPoint.y);

            Path path = new Path();
            path.moveTo((rect.left + rect.right) / 2.0f, rect.bottom);
            path.lineTo(rect.right, (rect.top + rect.bottom) / 2.0f);
            path.lineTo((rect.left + rect.right) / 2.0f, rect.top);
            path.lineTo(rect.left, (rect.top + rect.bottom) / 2.0f);
            path.lineTo((rect.left + rect.right) / 2.0f, rect.bottom);

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
    public static VDRhombusBrush defaultBrush() {
        return new VDRhombusBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}