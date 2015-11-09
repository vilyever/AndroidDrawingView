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
    public Paint getPaint() {
        Paint paint = super.getPaint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(0);
        return paint;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 0) {
            RectF pathFrame = super.drawPath(canvas, drawingPath, state);

            if (state == DrawingPointerState.ForceFinishFetchFrame) {
                return pathFrame;
            }
            else if (state == DrawingPointerState.FetchFrame || canvas == null) {
                return pathFrame;
            }

            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            Paint paint = self.getPaint();
            Path path = new Path();
            if (drawingPath.getPoints().size() == 1) {
                paint.setStyle(Paint.Style.FILL);
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

            if (state == DrawingPointerState.CalibrateToOrigin) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, paint);

            return pathFrame;
        }

        return null;
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