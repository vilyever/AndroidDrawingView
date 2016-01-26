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
 * 矩形绘制
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

    /* Public Methods */
    public static VDRectangleBrush defaultBrush() {
        return new VDRectangleBrush(5, Color.BLACK);
    }

    /* #Overrides */
    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        self.updatePaint();
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
            path.addRect(drawingRect, Path.Direction.CW);

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, self.getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }

}