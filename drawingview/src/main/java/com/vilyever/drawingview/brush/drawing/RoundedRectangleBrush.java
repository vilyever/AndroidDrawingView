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
 * RoundedRectangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 * 圆角矩形绘制
 */
public class RoundedRectangleBrush extends ShapeBrush {
    final RoundedRectangleBrush self = this;

    protected float roundRadius;

    /* Constructors */
    public RoundedRectangleBrush() {

    }

    public RoundedRectangleBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public RoundedRectangleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, Resource.getDimensionPixelSize(R.dimen.drawingViewRoundedRectangleBrushDefaultRoundedRadius));
    }

    public RoundedRectangleBrush(float size, int color, FillType fillType, float roundRadius) {
        this(size, color, fillType, false, roundRadius);
    }

    public RoundedRectangleBrush(float size, int color, FillType fillType, boolean edgeRounded, float roundRadius) {
        super(size, color, fillType, edgeRounded);
        this.roundRadius = roundRadius;
    }

    /* Public Methods */
    public static RoundedRectangleBrush defaultBrush() {
        return new RoundedRectangleBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewBrushDefaultSize), Color.BLACK);
    }

    /* Properties */
    public float getRoundRadius() {
        return roundRadius;
    }

    public <T extends ShapeBrush> T setRoundRadius(float roundRadius) {
        this.roundRadius = roundRadius;
        return (T) this;
    }

    /* Overrides */
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

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.getX(), lastPoint.getX());
            drawingRect.top = Math.min(beginPoint.getY(), lastPoint.getY());
            drawingRect.right = Math.max(beginPoint.getX(), lastPoint.getX());
            drawingRect.bottom = Math.max(beginPoint.getY(), lastPoint.getY());

            if ((drawingRect.right - drawingRect.left) < getSize()
                    || (drawingRect.bottom - drawingRect.top) < getSize()) {
                return Frame.EmptyFrame();
            }

            Frame pathFrame = makeFrameWithBrushSpace(drawingRect);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            float round = getRoundRadius() + getSize() / 2.0f;
            Path path = new Path();
            path.addRoundRect(drawingRect, round, round, Path.Direction.CW);

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }
    
}