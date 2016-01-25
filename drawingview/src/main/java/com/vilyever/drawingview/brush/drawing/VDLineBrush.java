package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDLineBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 * 直线绘制
 */
public class VDLineBrush extends VDShapeBrush {
    final VDLineBrush self = this;


    /* #Constructors */
    public VDLineBrush() {

    }

    public VDLineBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public VDLineBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public VDLineBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* Public Methods */
    public static VDLineBrush defaultBrush() {
        return new VDLineBrush(5, Color.BLACK);
    }

    /* #Overrides */
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

            Frame pathFrame;
            if (!self.isEdgeRounded()) {
                pathFrame = new Frame(drawingRect);

                // 计算画笔延展宽高
                double x = drawingRect.right - drawingRect.left;
                double y = drawingRect.bottom - drawingRect.top;
                double z = Math.sqrt(x * x + y * y);
                double sina = y / z; // 水平线与直线夹角sin值
                double sinb = x / z; // 竖直线与直线夹角sin值
                double ta = Math.asin(sina); // 水平线与直线夹角
                double tb = Math.asin(sinb); // 竖直线与直线夹角
                double tdx = Math.abs(ta - Math.PI / 4);
                double tdy = Math.abs(tb - Math.PI / 4);
                double dx = Math.cos(tdx) * (self.getSize() / 2.0f) * Math.sqrt(2);
                double dy = Math.cos(tdy) * (self.getSize() / 2.0f) * Math.sqrt(2);

                pathFrame.left -= dx;
                pathFrame.top -= dy;
                pathFrame.right += dx;
                pathFrame.bottom += dy;
            }
            else {
                pathFrame = super.drawPath(canvas, drawingPath, state);
            }

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.moveTo(beginPoint.getX(), beginPoint.getY());
            path.lineTo(lastPoint.getX(), lastPoint.getY());

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, self.getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }

}