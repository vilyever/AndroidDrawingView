package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

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
    public boolean isEdgeRounded() {
        return true;
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            float centerX = (beginPoint.getX() + lastPoint.getX()) / 2.0f;
            float centerY = (beginPoint.getY() + lastPoint.getY()) / 2.0f;
            float radius = Math.min(Math.abs(beginPoint.getX() - lastPoint.getX()), Math.abs(beginPoint.getY() - lastPoint.getY())) / 2.0f;

            RectF drawingRect = new RectF();
            drawingRect.left = centerX - radius;
            drawingRect.top = centerY - radius;
            drawingRect.right = centerX + radius;
            drawingRect.bottom = centerY + radius;

            if ((drawingRect.right - drawingRect.left) < self.getSize()
                    || (drawingRect.bottom - drawingRect.top) < self.getSize()) {
                return Frame.EmptyFrame();
            }

            Frame pathFrame = self.makeFrameWithBrushSpace(drawingRect);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, self.getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
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