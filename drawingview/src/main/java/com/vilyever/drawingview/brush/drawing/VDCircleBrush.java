package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;
import com.vilyever.drawingview.brush.VDBrush;

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
        this(size, color, FillType.Hollow);
    }

    public VDCircleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public VDCircleBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* #Overrides */
    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, VDBrush.DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            float centerX = (beginPoint.x + lastPoint.x) / 2.0f;
            float centerY = (beginPoint.y + lastPoint.y) / 2.0f;
            float radius = Math.min(Math.abs(beginPoint.x - lastPoint.x), Math.abs(beginPoint.y - lastPoint.y)) / 2.0f;

            RectF drawingRect = new RectF();
            drawingRect.left = centerX - radius;
            drawingRect.top = centerY - radius;
            drawingRect.right = centerX + radius;
            drawingRect.bottom = centerY + radius;

            if ((drawingRect.right - drawingRect.left) < self.getSize()
                    || (drawingRect.bottom - drawingRect.top) < self.getSize()) {
                return null;
            }

            RectF pathFrame = self.attachBrushSpace(drawingRect);

            if (state == VDBrush.DrawingPointerState.ForceFinishFetchFrame) {
                return pathFrame;
            }
            else if (state == VDBrush.DrawingPointerState.FetchFrame || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

            if (state == VDBrush.DrawingPointerState.CalibrateToOrigin
                    || state == VDBrush.DrawingPointerState.ForceCalibrateToOrigin) {
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
    public static VDCircleBrush defaultBrush() {
        return new VDCircleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}