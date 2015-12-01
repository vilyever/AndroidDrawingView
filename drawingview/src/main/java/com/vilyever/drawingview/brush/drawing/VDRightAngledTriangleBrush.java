package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDRightAngledTriangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRightAngledTriangleBrush extends VDShapeBrush {
    final VDRightAngledTriangleBrush self = this;


    /* #Constructors */
    public VDRightAngledTriangleBrush() {

    }

    public VDRightAngledTriangleBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public VDRightAngledTriangleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public VDRightAngledTriangleBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* #Overrides */
    @Override
    public Paint getPaint() {
        Paint paint = super.getPaint();
        paint.setStrokeMiter(Integer.MAX_VALUE);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        return paint;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.x, lastPoint.x);
            drawingRect.top = Math.min(beginPoint.y, lastPoint.y);
            drawingRect.right = Math.max(beginPoint.x, lastPoint.x);
            drawingRect.bottom = Math.max(beginPoint.y, lastPoint.y);

            if ((drawingRect.right - drawingRect.left) < self.getSize() * 2.0f
                    || (drawingRect.bottom - drawingRect.top) < self.getSize() * 2.0f) {
                return null;
            }

//            锐角距差计算，改用新算法
//            pathFrame = new RectF(drawingRect);
//
//            // 计算相似三角形比例
//            double x = pathFrame.right - pathFrame.left; // 底边
//            double y = pathFrame.bottom - pathFrame.top; // 左侧边
//            double z = Math.sqrt(x * x + y * y); // 斜边
//            double cos = y / z; // 左上角cos值
//            double tan = x / y; // 左上角tan值
//            double u = (self.getSize() / 2.0f) * (1 + 1 / cos); // 内外三角顶部间辅助相似三角形底边
//            double q = u / tan; // 内外三角顶部间辅助相似三角形左侧边
//            double factor = (y + (self.getSize() / 2.0f) + q) / y; // 相似比
//
//            pathFrame.top -= y * (factor - 1) - self.getSize() / 2.0f;
//            pathFrame.right += x * (factor - 1) - self.getSize() / 2.0f;
//
//            pathFrame.left -= self.getSize() / 2.0f;
//            pathFrame.bottom += self.getSize() / 2.0f;

            double w = self.getSize() / 2.0; // 内外间距
            double x = drawingRect.right - drawingRect.left; // 底边
            double y = drawingRect.bottom - drawingRect.top; // 左侧边
            double a = Math.atan(x / y); // 顶角
            double b = Math.PI / 2.0 - a; // 底角
            double dy = w / Math.tan(a / 2.0); // y差值
            double dx = w / Math.tan(b / 2.0); // x差值

            RectF outerRect = new RectF(drawingRect);
            outerRect.top -= dy;
            outerRect.right += dx;
            outerRect.left -= self.getSize() / 2.0f;
            outerRect.bottom += self.getSize() / 2.0f;

            RectF innerRect = new RectF(drawingRect);
            innerRect.top += dy;
            innerRect.right -= dx;
            innerRect.left += self.getSize() / 2.0f;
            innerRect.bottom -= self.getSize() / 2.0f;

            RectF pathFrame;
            if (!self.isEdgeRounded()) {
                pathFrame = new RectF(outerRect);
            }
            else {
                pathFrame = super.drawPath(canvas, drawingPath, state);
            }

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.moveTo(outerRect.left, outerRect.bottom);
            path.lineTo(outerRect.right, outerRect.bottom);
            path.lineTo(outerRect.left, outerRect.top);
            path.lineTo(outerRect.left, outerRect.bottom);

            if (self.getFillType() == FillType.Hollow) {
                path.lineTo(innerRect.left, innerRect.bottom);
                path.lineTo(innerRect.left, innerRect.top);
                path.lineTo(innerRect.right, innerRect.bottom);
                path.lineTo(innerRect.left, innerRect.bottom);

                path.lineTo(outerRect.left, outerRect.bottom);
            }

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
    public static VDRightAngledTriangleBrush defaultBrush() {
        return new VDRightAngledTriangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}