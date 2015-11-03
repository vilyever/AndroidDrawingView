package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

/**
 * VDRoundedRectangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRoundedRectangleBrush extends VDShapeBrush {
    final VDRoundedRectangleBrush self = this;

    
    /* #Constructors */
    public VDRoundedRectangleBrush() {

    }

    public VDRoundedRectangleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDRoundedRectangleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDRoundedRectangleBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */
    @Override
    public boolean isEdgeRounded() {
        return true;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 1) {
            RectF pathFrame = super.drawPath(canvas, drawingPath, state);

            if (state == DrawingPointerState.FetchFrame || canvas == null) {
                return pathFrame;
            }

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

            if (state == DrawingPointerState.CalibrateToOrigin) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            self.drawSolidShapePath(canvas, path);

            return pathFrame;
        }

        return null;
    }
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDRoundedRectangleBrush defaultBrush() {
        return new VDRoundedRectangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}