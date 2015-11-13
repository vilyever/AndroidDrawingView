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
import com.vilyever.drawingview.brush.VDBrush;

/**
 * VDLayerEraserPenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/11/13.
 * Feature:
 */
public class VDLayerEraserPenBrush extends VDLayerEraserBrush {
    final VDLayerEraserPenBrush self = this;


    /* #Constructors */
    public VDLayerEraserPenBrush() {
    }

    /* #Overrides */
    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, VDBrush.DrawingPointerState state) {
        if (canvas != null && drawingPath.getPoints().size() > 0) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            Drawable drawable = VDContextHolder.getContext().getResources().getDrawable(R.drawable.delete);

            drawable.setBounds((int) beginPoint.x - drawable.getIntrinsicWidth(),
                    (int) beginPoint.y - drawable.getIntrinsicHeight(),
                    (int) beginPoint.x + drawable.getIntrinsicWidth(),
                    (int) beginPoint.y + drawable.getIntrinsicHeight());

            for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                VDDrawingPoint prePoint = drawingPath.getPoints().get(i - 1);
                VDDrawingPoint point = drawingPath.getPoints().get(i);



                drawable.setBounds((int)point.x - drawable.getIntrinsicWidth(),
                        (int)point.y - drawable.getIntrinsicHeight(),
                        (int)point.x + drawable.getIntrinsicWidth(),
                        (int)point.y + drawable.getIntrinsicHeight());
                drawable.draw(canvas);
            }
        }

        return null;
    }

    @Override
    public boolean shouldErase(@NonNull View layerView, @NonNull VDDrawingPath drawingPath, VDBrush.DrawingPointerState state) {
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