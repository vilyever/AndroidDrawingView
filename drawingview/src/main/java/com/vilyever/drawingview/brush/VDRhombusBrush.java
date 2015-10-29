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
 * VDRhombusBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRhombusBrush extends VDShapeBrush {
    final VDRhombusBrush self = this;


    /* #Constructors */
    public VDRhombusBrush() {

    }

    public VDRhombusBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDRhombusBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, false);
    }

    public VDRhombusBrush(float size, int color, int solidColor, boolean edgeRounded) {
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

                // 计算相似菱形上半三角形比例
                double x = pathFrame.right - pathFrame.left; // 底边
                double h = (pathFrame.bottom - pathFrame.top) / 2.0f; // 高
                double y = Math.sqrt((x / 2.0f) * (x / 2.0f) + h * h); // 斜边
                double sin = (x / 2.0f) / y; // 顶角角度一半的sin值
                double factor = (h + (self.getSize() / 2.0f) * (1 / sin)) / h; // 相似比

                pathFrame.left -= x * (factor - 1) / 2.0f;
                pathFrame.top -= h * (factor - 1);
                pathFrame.right += x * (factor - 1) / 2.0f;
                pathFrame.bottom += h * (factor - 1);
            }
            else {
                pathFrame = super.drawPath(canvas, drawingPath, state);
            }

            if (state == DrawingPointerState.FetchFrame) {
                return pathFrame;
            }

            Path path = new Path();
            path.moveTo(drawingRect.left + (drawingRect.right - drawingRect.left) / 4.0f, drawingRect.top + (drawingRect.bottom - drawingRect.top) / 4.0f);
            path.lineTo((drawingRect.left + drawingRect.right) / 2.0f, drawingRect.top);
            path.lineTo(drawingRect.right, (drawingRect.top + drawingRect.bottom) / 2.0f);
            path.lineTo((drawingRect.left + drawingRect.right) / 2.0f, drawingRect.bottom);
            path.lineTo(drawingRect.left, (drawingRect.top + drawingRect.bottom) / 2.0f);
            path.lineTo(drawingRect.left + (drawingRect.right - drawingRect.left) / 4.0f, drawingRect.top + (drawingRect.bottom - drawingRect.top) / 4.0f);

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
    public static VDRhombusBrush defaultBrush() {
        return new VDRhombusBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}