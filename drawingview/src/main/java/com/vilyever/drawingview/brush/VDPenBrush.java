package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDPenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/20.
 * Feature:
 */
public class VDPenBrush extends VDDrawingBrush {
    final VDPenBrush self = this;

    
    /* #Constructors */
    public VDPenBrush() {

    }

    public VDPenBrush(float size, int color) {
        super(size, color);
    }

    /* #Overrides */
    @Override
    public boolean drawPath(Canvas canvas, VDDrawingPath drawingPath, DrawingPointerState state) {
        if (canvas == null
                || drawingPath == null) {
            return true;
        }

        if (drawingPath.getPoints().size() > 0) {
            Paint paint = self.getPaint();

            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint endPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            if (drawingPath.getPoints().size() == 1) {
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(beginPoint.x, beginPoint.y, paint.getStrokeWidth() * 0.5f, paint);
                paint.setStyle(Paint.Style.STROKE);
            } else if (drawingPath.getPoints().size() > 1) {
                Path path = new Path();
                path.moveTo(beginPoint.x, beginPoint.y);
                for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                    path.quadTo(drawingPath.getPoints().get(i - 1).x, drawingPath.getPoints().get(i - 1).y,
                            (drawingPath.getPoints().get(i - 1).x + drawingPath.getPoints().get(i).x) / 2.0f,
                            (drawingPath.getPoints().get(i - 1).y + drawingPath.getPoints().get(i).y) / 2.0f);
                }

                canvas.drawPath(path, paint);
            }
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
    public static VDPenBrush defaultBrush() {
        return new VDPenBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}