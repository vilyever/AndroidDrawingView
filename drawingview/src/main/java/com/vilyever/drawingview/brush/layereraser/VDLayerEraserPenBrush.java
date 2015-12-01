package com.vilyever.drawingview.brush.layereraser;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.R;
import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;
import com.vilyever.drawingview.util.VDBezier;

/**
 * VDLayerEraserPenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/11/13.
 * Feature:
 */
public class VDLayerEraserPenBrush extends VDLayerEraserBrush {
    final VDLayerEraserPenBrush self = this;

    private static Drawable PenDrawable = VDContextHolder.getContext().getResources().getDrawable(R.drawable.animation_drawing_brush);

    /* #Constructors */
    public VDLayerEraserPenBrush() {
    }

    /* #Overrides */
    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (canvas != null && drawingPath.getPoints().size() > 0) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            if (drawingPath.getPoints().size() == 1) {
                PenDrawable.setBounds((int) beginPoint.x - PenDrawable.getIntrinsicWidth(),
                        (int) beginPoint.y - PenDrawable.getIntrinsicHeight(),
                        (int) beginPoint.x + PenDrawable.getIntrinsicWidth(),
                        (int) beginPoint.y + PenDrawable.getIntrinsicHeight());
                PenDrawable.draw(canvas);
            }
            else {
                for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                    VDDrawingPoint prePoint = drawingPath.getPoints().get(i - 1);
                    VDDrawingPoint currentPoint = drawingPath.getPoints().get(i);

//                    PenDrawable.setBounds((int) currentPoint.x - PenDrawable.getIntrinsicWidth(),
//                            (int) currentPoint.y - PenDrawable.getIntrinsicHeight(),
//                            (int) currentPoint.x + PenDrawable.getIntrinsicWidth(),
//                            (int) currentPoint.y + PenDrawable.getIntrinsicHeight());
//                    PenDrawable.draw(canvas);

                    VDDrawingPoint startPoint, secondPoint, endPoint;
                    if (i == 1) {
                        startPoint = prePoint;
                        secondPoint = prePoint;
                        endPoint = new VDDrawingPoint((prePoint.x + currentPoint.x) / 2.0f, (prePoint.y + currentPoint.y) / 2.0f);
                    } else {
                        VDDrawingPoint morePrePoint = drawingPath.getPoints().get(i - 2);
                        startPoint = new VDDrawingPoint((prePoint.x + morePrePoint.x) / 2.0f, (prePoint.y + morePrePoint.y) / 2.0f);
                        secondPoint = prePoint;
                        endPoint = new VDDrawingPoint((prePoint.x + currentPoint.x) / 2.0f, (prePoint.y + currentPoint.y) / 2.0f);
                    }

                    VDBezier bezier = new VDBezier(startPoint, secondPoint, endPoint);

                    float drawSteps =  (float) Math.sqrt(Math.floor(bezier.length()) / 10.0f);

                    for (int j = 0; j < drawSteps; j++) {
                        // Calculate the Bezier (x, y) coordinate for this step.
                        float t = ((float) j) / drawSteps;
                        float tt = t * t;
                        float u = 1 - t;
                        float uu = u * u;

                        float x = uu * bezier.startPoint.x;
                        x += 2 * u * t * bezier.secondPoint.x;
                        x += tt * bezier.endPoint.x;

                        float y = uu * bezier.startPoint.y;
                        y += 2 * u * t * bezier.secondPoint.y;
                        y += tt * bezier.endPoint.y;

                        PenDrawable.setBounds((int) x - PenDrawable.getIntrinsicWidth(),
                                (int) y - PenDrawable.getIntrinsicHeight(),
                                (int) x + PenDrawable.getIntrinsicWidth(),
                                (int) y + PenDrawable.getIntrinsicHeight());
                        PenDrawable.draw(canvas);
                    }

                }
            }
        }

        return null;
    }

    @Override
    public boolean shouldErase(@NonNull View layerView, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 0) {

        }
        return false;
    }

    /* #Accessors */

    /* #Delegates */

    /* #Private Methods */

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */

    /* #Annotations @interface */

    /* #Enums */
}