package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDRoundedRectangleBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDRoundedRectangleBrush extends VDShapeBrush {
    final VDRoundedRectangleBrush self = this;

    protected float roundRadius;
    
    /* #Constructors */
    public VDRoundedRectangleBrush() {

    }

    public VDRoundedRectangleBrush(float size, int color) {
        this(size, color, FillType.Hollow);
    }

    public VDRoundedRectangleBrush(float size, int color, FillType fillType) {
        this(size, color, fillType, 20.0f);
    }

    public VDRoundedRectangleBrush(float size, int color, FillType fillType, float roundRadius) {
        this(size, color, fillType, false, roundRadius);
    }

    public VDRoundedRectangleBrush(float size, int color, FillType fillType, boolean edgeRounded, float roundRadius) {
        super(size, color, fillType, edgeRounded);
        this.roundRadius = roundRadius;
    }

    /* #Overrides */
    @Override
    public boolean isEdgeRounded() {
        return true;
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.x, lastPoint.x);
            drawingRect.top = Math.min(beginPoint.y, lastPoint.y);
            drawingRect.right = Math.max(beginPoint.x, lastPoint.x);
            drawingRect.bottom = Math.max(beginPoint.y, lastPoint.y);

            if ((drawingRect.right - drawingRect.left) < self.getSize()
                    || (drawingRect.bottom - drawingRect.top) < self.getSize()) {
                return Frame.EmptyFrame();
            }

            Frame pathFrame = self.makeFrameWithBrushSpace(drawingRect);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            float round = self.getRoundRadius() + self.getSize() / 2.0f;
            Path path = new Path();
            path.addRoundRect(drawingRect, round, round, Path.Direction.CW);

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, self.getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }
    
    /* #Accessors */
    public float getRoundRadius() {
        return roundRadius;
    }

    public <T extends VDShapeBrush> T setRoundRadius(float roundRadius) {
        this.roundRadius = roundRadius;
        return (T) self;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDRoundedRectangleBrush defaultBrush() {
        return new VDRoundedRectangleBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}