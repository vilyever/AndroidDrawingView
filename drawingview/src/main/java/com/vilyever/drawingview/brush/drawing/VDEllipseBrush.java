package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDEllipseBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDEllipseBrush extends VDShapeBrush {
    final VDEllipseBrush self = this;

    
    /* #Constructors */
    public VDEllipseBrush() {

    }

    public VDEllipseBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public VDEllipseBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public VDEllipseBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* #Overrides */

    @Override
    public boolean isEdgeRounded() {
        return true;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 1) {
            RectF pathFrame = super.drawPath(canvas, drawingPath, state);

            if (state.isFetchFrame() || canvas == null) {
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
            path.addOval(drawingRect, Path.Direction.CW);

            RectF solidRect = new RectF(drawingRect);
            solidRect.left += self.getSize() / 2.0f;
            solidRect.top += self.getSize() / 2.0f;
            solidRect.right -= self.getSize() / 2.0f;
            solidRect.bottom -= self.getSize() / 2.0f;

            if (state.isCalibrateToOrigin()) {
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
    public static VDEllipseBrush defaultBrush() {
        return new VDEllipseBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}