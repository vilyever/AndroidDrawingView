package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.R;
import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.drawingview.model.DrawingPoint;
import com.vilyever.resource.Resource;

/**
 * RightAngledTriangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 * 直角三角形绘制，直角位于左下角
 */
public class RightAngledTriangleBrush extends ShapeBrush {
    final RightAngledTriangleBrush self = this;


    /* #Constructors */
    public RightAngledTriangleBrush() {

    }

    public RightAngledTriangleBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public RightAngledTriangleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public RightAngledTriangleBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* Public Methods */
    public static RightAngledTriangleBrush defaultBrush() {
        return new RightAngledTriangleBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewBrushDefaultSize), Color.BLACK);
    }

    /* #Overrides */
    @Override
    protected void updatePaint() {
        super.updatePaint();

        if (!isEdgeRounded()) {
            getPaint().setStrokeMiter(Integer.MAX_VALUE);
            getPaint().setStrokeWidth(0);
            getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
        }
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        updatePaint();
        if (drawingPath.getPoints().size() > 1) {
            DrawingPoint beginPoint = drawingPath.getPoints().get(0);
            DrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.getX(), lastPoint.getX());
            drawingRect.top = Math.min(beginPoint.getY(), lastPoint.getY());
            drawingRect.right = Math.max(beginPoint.getX(), lastPoint.getX());
            drawingRect.bottom = Math.max(beginPoint.getY(), lastPoint.getY());

            if ((drawingRect.right - drawingRect.left) < getSize() * 2.0f
                    || (drawingRect.bottom - drawingRect.top) < getSize() * 2.0f) {
                return Frame.EmptyFrame();
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
//            double u = (getSize() / 2.0f) * (1 + 1 / cos); // 内外三角顶部间辅助相似三角形底边
//            double q = u / tan; // 内外三角顶部间辅助相似三角形左侧边
//            double factor = (y + (getSize() / 2.0f) + q) / y; // 相似比
//
//            pathFrame.top -= y * (factor - 1) - getSize() / 2.0f;
//            pathFrame.right += x * (factor - 1) - getSize() / 2.0f;
//
//            pathFrame.left -= getSize() / 2.0f;
//            pathFrame.bottom += getSize() / 2.0f;

            Path path = new Path();
            Frame pathFrame;

            if (isEdgeRounded()) {
                pathFrame = super.drawPath(canvas, drawingPath, state);
                if (state.isFetchFrame() || canvas == null) {
                    return pathFrame;
                }

                path.moveTo(drawingRect.left, drawingRect.bottom);
                path.lineTo(drawingRect.right, drawingRect.bottom);
                path.lineTo(drawingRect.left, drawingRect.top);
                path.lineTo(drawingRect.left, drawingRect.bottom);
            }
            else {
                double w = getSize() / 2.0; // 内外间距
                double x = drawingRect.right - drawingRect.left; // 底边
                double y = drawingRect.bottom - drawingRect.top; // 左侧边
                double a = Math.atan(x / y); // 顶角
                double b = Math.PI / 2.0 - a; // 底角
                double dy = w / Math.tan(a / 2.0); // y差值
                double dx = w / Math.tan(b / 2.0); // x差值

                RectF outerRect = new RectF(drawingRect);
                outerRect.top -= dy;
                outerRect.right += dx;
                outerRect.left -= getSize() / 2.0f;
                outerRect.bottom += getSize() / 2.0f;

                RectF innerRect = new RectF(drawingRect);
                innerRect.top += dy;
                innerRect.right -= dx;
                innerRect.left += getSize() / 2.0f;
                innerRect.bottom -= getSize() / 2.0f;

                pathFrame = new Frame(outerRect);
                if (state.isFetchFrame() || canvas == null) {
                    return pathFrame;
                }

                path.moveTo(outerRect.left, outerRect.bottom);
                path.lineTo(outerRect.right, outerRect.bottom);
                path.lineTo(outerRect.left, outerRect.top);
                path.lineTo(outerRect.left, outerRect.bottom);

                if (getFillType() == FillType.Hollow) {
                    path.lineTo(innerRect.left, innerRect.bottom);
                    path.lineTo(innerRect.left, innerRect.top);
                    path.lineTo(innerRect.right, innerRect.bottom);
                    path.lineTo(innerRect.left, innerRect.bottom);

                    path.lineTo(outerRect.left, outerRect.bottom);
                }
            }

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }

}