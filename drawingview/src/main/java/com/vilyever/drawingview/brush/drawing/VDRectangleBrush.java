package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDRectangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRectangleBrush extends VDShapeBrush {
    final VDRectangleBrush self = this;

    
    /* #Constructors */
    public VDRectangleBrush() {

    }

    public VDRectangleBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public VDRectangleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public VDRectangleBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* #Overrides */
    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 1) {
            RectF pathFrame = super.drawPath(canvas, drawingPath, state);

            if (state == DrawingPointerState.ForceFinishFetchFrame) {
                return pathFrame;
            }
            else if (state == DrawingPointerState.FetchFrame || canvas == null) {
                return pathFrame;
            }

            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.x, lastPoint.x);
            drawingRect.top = Math.min(beginPoint.y, lastPoint.y);
            drawingRect.right = Math.max(beginPoint.x, lastPoint.x);
            drawingRect.bottom = Math.max(beginPoint.y, lastPoint.y);

            if ((drawingRect.right - drawingRect.left) < self.getSize()
                    || (drawingRect.bottom - drawingRect.top) < self.getSize()) {
                return null;
            }

            Path path = new Path();
            path.addRect(drawingRect, Path.Direction.CW);

            if (state == DrawingPointerState.CalibrateToOrigin
                    || state == DrawingPointerState.ForceCalibrateToOrigin) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, self.getPaint());

            return pathFrame;
        }

        return null;
    }
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDRectangleBrush defaultBrush() {
        return new VDRectangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}