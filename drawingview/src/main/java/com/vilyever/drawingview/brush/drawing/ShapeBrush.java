package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.drawingview.model.DrawingPoint;

/**
 * ShapeBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 * 形状绘制brush
 *
 * Known Direct Subclasses:
 * {@link LineBrush}
 * {@link RectangleBrush}
 * {@link RoundedRectangleBrush}
 * {@link EllipseBrush}
 * {@link CircleBrush}
 * {@link CenterCircleBrush}
 * {@link RightAngledTriangleBrush}
 * {@link IsoscelesTriangleBrush}
 * {@link RhombusBrush}
 * {@link PolygonBrush}
 */
public abstract class ShapeBrush extends DrawingBrush {
    final ShapeBrush self = this;

    /* #Constructors */
    public ShapeBrush() {

    }

    public ShapeBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public ShapeBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, false);
    }

    public ShapeBrush(float size, int color, FillType fillType, boolean edgeRounded) {
        super(size, color);
        this.fillType = fillType;
        this.edgeRounded = edgeRounded;
    }

    /* Properties */
    /**
     * 填充样式
     * 镂空，铺满
     */
    public enum FillType {
        Hollow, Solid;
    }
    protected FillType fillType;
    public FillType getFillType() {
        if (isEraser()) {
            return FillType.Solid;
        }

        if (this.fillType == null) {
            return FillType.Hollow;
        }
        return this.fillType;
    }
    public <T extends ShapeBrush> T setFillType(FillType fillType) {
        this.fillType = fillType;
        updatePaint();
        return (T) this;
    }

    /**
     * 边缘交点是否圆角
     */
    protected boolean edgeRounded;
    public boolean isEdgeRounded() {
        return this.edgeRounded;
    }
    public <T extends ShapeBrush> T setEdgeRounded(boolean edgeRounded) {
        this.edgeRounded = edgeRounded;
        updatePaint();
        return (T) this;
    }

    /* #Overrides */
    @Override
    protected void updatePaint() {
        super.updatePaint();

        switch (getFillType()) {
            case Hollow:
                getPaint().setStyle(Paint.Style.STROKE);
                break;
            case Solid:
                getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
                break;
        }

        if (isEdgeRounded()) {
            getPaint().setStrokeCap(Paint.Cap.ROUND);
            getPaint().setStrokeJoin(Paint.Join.ROUND);
        }
        else {
            getPaint().setStrokeCap(Paint.Cap.SQUARE);
            getPaint().setStrokeJoin(Paint.Join.MITER);
        }
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        updatePaint();
        if (drawingPath.getPoints().size() < 2) {
            return Frame.EmptyFrame();
        }
        else {
            DrawingPoint beginPoint = drawingPath.getPoints().get(0);
            DrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();

            drawingRect.left = Math.min(beginPoint.getX(), lastPoint.getX());
            drawingRect.top = Math.min(beginPoint.getY(), lastPoint.getY());
            drawingRect.right = Math.max(beginPoint.getX(), lastPoint.getX());
            drawingRect.bottom = Math.max(beginPoint.getY(), lastPoint.getY());

            return makeFrameWithBrushSpace(drawingRect);
        }
    }

}