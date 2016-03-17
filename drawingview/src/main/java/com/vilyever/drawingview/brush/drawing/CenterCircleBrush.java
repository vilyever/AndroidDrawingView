package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.R;
import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.drawingview.model.DrawingPoint;
import com.vilyever.resource.Resource;

/**
 * CenterCircleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 * 中心圆绘制，即以起点为圆心，终点到起点距离为半径
 */
public class CenterCircleBrush extends ShapeBrush {
    final CenterCircleBrush self = this;


    /* #Constructors */
    public CenterCircleBrush() {
    }

    public CenterCircleBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public CenterCircleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public CenterCircleBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* Public Methods */
    public static CenterCircleBrush defaultBrush() {
        return new CenterCircleBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewBrushDefaultSize), Color.BLACK);
    }

    /* #Overrides */
    @Override
    public boolean isEdgeRounded() {
        return true;
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        updatePaint();
        if (drawingPath.getPoints().size() > 1) {
            DrawingPoint beginPoint = drawingPath.getPoints().get(0);
            DrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            float centerX = beginPoint.getX();
            float centerY = beginPoint.getY();
            float radius = Math.min(Math.abs(beginPoint.getX() - lastPoint.getX()), Math.abs(beginPoint.getY() - lastPoint.getY()));

            RectF drawingRect = new RectF();
            drawingRect.left = centerX - radius;
            drawingRect.top = centerY - radius;
            drawingRect.right = centerX + radius;
            drawingRect.bottom = centerY + radius;

            if ((drawingRect.right - drawingRect.left) < getSize()
                    || (drawingRect.bottom - drawingRect.top) < getSize()) {
                return Frame.EmptyFrame();
            }

            Frame pathFrame = makeFrameWithBrushSpace(drawingRect);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }
}