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

import java.util.ArrayList;
import java.util.List;

/**
 * PolygonBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 * 多边形绘制，由多笔构成
 * 此brush展示如何使用多笔绘制图形
 */
public class PolygonBrush extends ShapeBrush {
    final PolygonBrush self = this;


    /* #Constructors */
    public PolygonBrush() {

    }

    public PolygonBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public PolygonBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public PolygonBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color, fillType, edgeRounded);
    }

    /* Public Methods */
    public static PolygonBrush defaultBrush() {
        return new PolygonBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewBrushDefaultSize), Color.BLACK);
    }

    /* #Overrides */
    @Override
    public FillType getFillType() {
        return FillType.Hollow;
    }

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
            List<DrawingPoint> endPoints = new ArrayList<>();

            int currentPointerID = beginPoint.pointerID;
            for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                DrawingPoint drawingPoint = drawingPath.getPoints().get(i);
                if (drawingPoint.pointerID != currentPointerID) {
                    endPoints.add(drawingPath.getPoints().get(i - 1));
                    currentPointerID = drawingPoint.pointerID;
                }
            }
            endPoints.add(lastPoint);

            boolean requireMoreDetail = true;
            if (beginPoint.pointerID != lastPoint.pointerID
                    && Math.abs(beginPoint.getX() - lastPoint.getX()) < (16.0f + getSize())
                    && Math.abs(beginPoint.getY() - lastPoint.getY()) < (16.0f + getSize())) {
                endPoints.remove(lastPoint);
                endPoints.add(beginPoint);
                requireMoreDetail = false;
            }
            else if (state.isForceFinish()) {
                endPoints.add(beginPoint);
                requireMoreDetail = false;
            }

            RectF drawingRect = new RectF();
            drawingRect.left = beginPoint.getX();
            drawingRect.top = beginPoint.getY();
            drawingRect.right = beginPoint.getX();
            drawingRect.bottom = beginPoint.getY();

            for (int i = 0; i < endPoints.size(); i++) {
                DrawingPoint point = endPoints.get(i);
                drawingRect.left = Math.min(point.getX(), drawingRect.left);
                drawingRect.top = Math.min(point.getY(), drawingRect.top);
                drawingRect.right = Math.max(point.getX(), drawingRect.right);
                drawingRect.bottom = Math.max(point.getY(), drawingRect.bottom);
            }

            Frame pathFrame = makeFrameWithBrushSpace(drawingRect);
            pathFrame.requireMoreDetail = requireMoreDetail;

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            Path path = new Path();
            path.moveTo(beginPoint.getX(), beginPoint.getY());
            for (int i = 0; i < endPoints.size(); i++) {
                path.lineTo(endPoints.get(i).getX(), endPoints.get(i).getY());
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