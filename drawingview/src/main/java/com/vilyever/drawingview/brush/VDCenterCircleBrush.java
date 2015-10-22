package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDCenterCircleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDCenterCircleBrush extends VDShapeBrush {
    final VDCenterCircleBrush self = this;


    /* #Constructors */
    public VDCenterCircleBrush() {
    }

    public VDCenterCircleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDCenterCircleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDCenterCircleBrush(float size, int color, int solidColor, boolean edgeRounded) {
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

            float centerX = beginPoint.x;
            float centerY = beginPoint.y;
            float radius = Math.min(Math.abs(beginPoint.x - lastPoint.x), Math.abs(beginPoint.y - lastPoint.y));

            Path path = new Path();
            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

            self.drawSolidShapePath(canvas, path);
        }

        if (state == DrawingPointerState.End) {
            return true;
        }
        return false;
    }

    @Override
    public RectF getDrawingFrame(VDDrawingPath drawingPath) {
        if (drawingPath != null
                && drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            float centerX = beginPoint.x;
            float centerY = beginPoint.y;
            float radius = Math.min(Math.abs(beginPoint.x - lastPoint.x), Math.abs(beginPoint.y - lastPoint.y));

            float leftest = centerX - radius;
            float rightest = centerX + radius;
            float topest = centerY - radius;
            float bottomest = centerY + radius;

            return new RectF(leftest - self.getSize(),
                    topest - self.getSize(),
                    rightest + self.getSize(),
                    bottomest + self.getSize());
        }

        return super.getDrawingFrame(drawingPath);
    }

    /* #Accessors */
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDCenterCircleBrush defaultBrush() {
        return new VDCenterCircleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}