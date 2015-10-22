package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDLineBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDLineBrush extends VDShapeBrush {
    final VDLineBrush self = this;


    /* #Constructors */
    public VDLineBrush() {

    }

    public VDLineBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDLineBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDLineBrush(float size, int color, int solidColor, boolean edgeRounded) {
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
            Paint paint = self.getPaint();

            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            Path path = new Path();
            path.moveTo(beginPoint.x, beginPoint.y);
            path.lineTo(lastPoint.x, lastPoint.y);

            canvas.drawPath(path, paint);
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
    public static VDLineBrush defaultBrush() {
        return new VDLineBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}