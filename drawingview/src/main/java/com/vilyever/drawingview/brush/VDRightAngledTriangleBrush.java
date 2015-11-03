package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

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
        this(size, color, Color.TRANSPARENT);
    }

    public VDRightAngledTriangleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDRightAngledTriangleBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */
    @Override
    public Paint getPaint() {
        Paint paint = super.getPaint();
        paint.setStrokeMiter(Integer.MAX_VALUE);
        return paint;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.x, lastPoint.x);
            drawingRect.top = Math.min(beginPoint.y, lastPoint.y);
            drawingRect.right = Math.max(beginPoint.x, lastPoint.x);
            drawingRect.bottom = Math.max(beginPoint.y, lastPoint.y);

            RectF pathFrame;
            if (!self.isEdgeRounded()) {
                if ((drawingRect.right - drawingRect.left) < (self.getSize() * 2.0f)) {
                    if (beginPoint.x <= lastPoint.x) {
                        drawingRect.right = drawingRect.left + self.getSize() * 2.0f + 1;
                    }
                    else {
                        drawingRect.left = drawingRect.right - self.getSize() * 2.0f - 1;
                    }
                }
                if ((drawingRect.bottom - drawingRect.top) < (self.getSize() * 2.0f)) {
                    if (beginPoint.y <= lastPoint.y) {
                        drawingRect.bottom = drawingRect.top + self.getSize() * 2.0f + 1;
                    }
                    else {
                        drawingRect.top = drawingRect.bottom - self.getSize() * 2.0f - 1;
                    }
                }

                pathFrame = new RectF(drawingRect);

                // 计算相似三角形比例
                double x = pathFrame.right - pathFrame.left; // 底边
                double y = pathFrame.bottom - pathFrame.top; // 左侧边
                double z = Math.sqrt(x * x + y * y); // 斜边
                double cos = y / z; // 左上角cos值
                double tan = x / y; // 左上角tan值
                double u = (self.getSize() / 2.0f) * (1 + 1 / cos); // 内外三角顶部间辅助相似三角形底边
                double q = u / tan; // 内外三角顶部间辅助相似三角形左侧边
                double factor = (y + (self.getSize() / 2.0f) + q) / y; // 相似比

                pathFrame.top -= y * (factor - 1) - self.getSize() / 2.0f;
                pathFrame.right += x * (factor - 1) - self.getSize() / 2.0f;

                pathFrame.left -= self.getSize() / 2.0f;
                pathFrame.bottom += self.getSize() / 2.0f;
            }
            else {
                pathFrame = super.drawPath(canvas, drawingPath, state);
            }

            if (state == DrawingPointerState.FetchFrame || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.moveTo(drawingRect.left, drawingRect.bottom);
            path.lineTo(drawingRect.right, drawingRect.bottom);
            path.lineTo(drawingRect.left, drawingRect.top);
            path.lineTo(drawingRect.left, drawingRect.bottom);

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
    public static VDRightAngledTriangleBrush defaultBrush() {
        return new VDRightAngledTriangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}