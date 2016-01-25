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
 * 椭圆绘制
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

    /* Public Methods */
    public static VDEllipseBrush defaultBrush() {
        return new VDEllipseBrush(5, Color.BLACK);
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

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.getX(), lastPoint.getX());
            drawingRect.top = Math.min(beginPoint.getY(), lastPoint.getY());
            drawingRect.right = Math.max(beginPoint.getX(), lastPoint.getX());
            drawingRect.bottom = Math.max(beginPoint.getY(), lastPoint.getY());

            if ((drawingRect.right - drawingRect.left) < self.getSize()
                    || (drawingRect.bottom - drawingRect.top) < self.getSize()) {
                return Frame.EmptyFrame();
            }

            Frame pathFrame = self.makeFrameWithBrushSpace(drawingRect);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
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

        return Frame.EmptyFrame();
    }
}