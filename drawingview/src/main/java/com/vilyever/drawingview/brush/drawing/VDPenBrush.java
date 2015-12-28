package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDPenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/20.
 * Feature:
 */
public class VDPenBrush extends VDDrawingBrush {
    final VDPenBrush self = this;

    
    /* #Constructors */
    public VDPenBrush() {

    }

    public VDPenBrush(float size, int color) {
        super(size, color);
    }

    /* #Overrides */
    @Override
    protected void updatePaint() {
        super.updatePaint();

        self.getPaint().setStyle(Paint.Style.STROKE);
        self.getPaint().setStrokeCap(Paint.Cap.ROUND);
        self.getPaint().setStrokeJoin(Paint.Join.ROUND);
        self.getPaint().setStrokeMiter(0);
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 0) {
            Frame pathFrame = super.drawPath(canvas, drawingPath, state);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            self.updatePaint();
            Path path = new Path();
            if (drawingPath.getPoints().size() == 1) {
                self.getPaint().setStyle(Paint.Style.FILL);
                path.addCircle(beginPoint.x, beginPoint.y, self.getSize() / 2.0f, Path.Direction.CW);
            }
            else if (drawingPath.getPoints().size() > 1) {
                path.moveTo(beginPoint.x, beginPoint.y);
                for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                    VDDrawingPoint prePoint = drawingPath.getPoints().get(i - 1);
                    VDDrawingPoint currentPoint = drawingPath.getPoints().get(i);

                    double s = Math.sqrt(Math.pow(currentPoint.x - prePoint.x, 2) + Math.pow(currentPoint.y - prePoint.y, 2));

                    if (s < 2) { // 往复颤抖间距估值，高分辨率上使用quadTo会出现异常绘画，反复在一点来回抖动会出现毛刺
                        path.lineTo(currentPoint.x, currentPoint.y);
                    }
                    else {
                        path.quadTo(prePoint.x, prePoint.y,
                                     (prePoint.x + currentPoint.x) / 2.0f, (prePoint.y + currentPoint.y) / 2.0f);
                    }

                    if (state.shouldEnd()) {
                        if (i == drawingPath.getPoints().size() - 1) {
                            path.quadTo((prePoint.x + currentPoint.x) / 2.0f, (prePoint.y + currentPoint.y) / 2.0f,
                                    currentPoint.x, currentPoint.y);
                        }
                    }
                }
            }

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, self.getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }

    @Override
    public boolean shouldDrawFromBegin() {
        return false;
    }

    /* #Accessors */
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDPenBrush defaultBrush() {
        return new VDPenBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}