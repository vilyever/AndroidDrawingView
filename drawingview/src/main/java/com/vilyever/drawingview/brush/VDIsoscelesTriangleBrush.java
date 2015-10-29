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
 * VDIsoscelesTriangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDIsoscelesTriangleBrush extends VDShapeBrush {
    final VDIsoscelesTriangleBrush self = this;


    /* #Constructors */
    public VDIsoscelesTriangleBrush() {

    }

    public VDIsoscelesTriangleBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDIsoscelesTriangleBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDIsoscelesTriangleBrush(float size, int color, int solidColor, boolean edgeRounded) {
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
    public RectF drawPath(@NonNull Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
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
                    } else {
                        drawingRect.left = drawingRect.right - self.getSize() * 2.0f - 1;
                    }
                }
                if ((drawingRect.bottom - drawingRect.top) < (self.getSize() * 2.0f)) {
                    if (beginPoint.y <= lastPoint.y) {
                        drawingRect.bottom = drawingRect.top + self.getSize() * 2.0f + 1;
                    } else {
                        drawingRect.top = drawingRect.bottom - self.getSize() * 2.0f - 1;
                    }
                }

                pathFrame = new RectF(drawingRect);

                // 计算相似三角形比例
                double x = pathFrame.right - pathFrame.left; // 底边
                double h = pathFrame.bottom - pathFrame.top; // 高
                double y = Math.sqrt((x / 2.0f) * (x / 2.0f) + h * h); // 斜边
                double sin = (x / 2.0f) / y; // 顶角角度一半的sin值
                double factor = (h + (self.getSize() / 2.0f) * (1 / sin + 1)) / h; // 相似比

                pathFrame.left -= x * (factor - 1) / 2.0f;
                pathFrame.top -= h * (factor - 1) - self.getSize() / 2.0f;
                pathFrame.right += x * (factor - 1) / 2.0f;

                pathFrame.bottom += self.getSize() / 2.0f;
            }
            else {
                pathFrame = super.drawPath(canvas, drawingPath, state);
            }

            if (state == DrawingPointerState.FetchFrame) {
                return pathFrame;
            }

            Path path = new Path();
            path.moveTo((drawingRect.left + drawingRect.right) / 2, drawingRect.bottom);
            path.lineTo(drawingRect.right, drawingRect.bottom);
            path.lineTo((drawingRect.left + drawingRect.right) / 2, drawingRect.top);
            path.lineTo(drawingRect.left, drawingRect.bottom);
            path.lineTo((drawingRect.left + drawingRect.right) / 2, drawingRect.bottom);

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
    public static VDIsoscelesTriangleBrush defaultBrush() {
        return new VDIsoscelesTriangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}