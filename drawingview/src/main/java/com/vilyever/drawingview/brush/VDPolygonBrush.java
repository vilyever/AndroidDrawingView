package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * VDPolygonBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/21.
 * Feature:
 */
public class VDPolygonBrush extends VDShapeBrush {
    final VDPolygonBrush self = this;


    /* #Constructors */
    public VDPolygonBrush() {

    }

    public VDPolygonBrush(float size, int color) {
        this(size, color, Color.TRANSPARENT);
    }

    public VDPolygonBrush(float size, int color, int solidColor) {
        this(size, color, solidColor, true);
    }

    public VDPolygonBrush(float size, int color, int solidColor, boolean edgeRounded) {
        super(size, color, solidColor, edgeRounded);
    }

    /* #Overrides */

    @Override
    public boolean isEdgeRounded() {
        return true;
    }

    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 1) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);
            List<VDDrawingPoint> endPoints = new ArrayList<>();

            int currentPointerID = beginPoint.pointerID;
            for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                VDDrawingPoint drawingPoint = drawingPath.getPoints().get(i);
                if (drawingPoint.pointerID != currentPointerID) {
                    endPoints.add(drawingPath.getPoints().get(i - 1));
                    currentPointerID = drawingPoint.pointerID;
                }
            }
            endPoints.add(lastPoint);

            boolean drawOver = false;
            if (beginPoint.pointerID != lastPoint.pointerID
                    && Math.abs(beginPoint.x - lastPoint.x) < (16.0f + self.getSize())
                    && Math.abs(beginPoint.y - lastPoint.y) < (16.0f + self.getSize())) {
                endPoints.remove(lastPoint);
                endPoints.add(beginPoint);
                drawOver = true;
            }
            else if (state.shouldForceFinish()) {
                endPoints.add(beginPoint);
                drawOver = true;
            }

            RectF drawingRect = new RectF();
            drawingRect.left = beginPoint.x;
            drawingRect.top = beginPoint.y;
            drawingRect.right = beginPoint.x;
            drawingRect.bottom = beginPoint.y;

            for (int i = 0; i < endPoints.size(); i++) {
                VDDrawingPoint point = endPoints.get(i);
                drawingRect.left = Math.min(point.x, drawingRect.left);
                drawingRect.top = Math.min(point.y, drawingRect.top);
                drawingRect.right = Math.max(point.x, drawingRect.right);
                drawingRect.bottom = Math.max(point.y, drawingRect.bottom);
            }

            RectF pathFrame = self.attachBrushSpace(drawingRect);;

            if (state == DrawingPointerState.FetchFrame || canvas == null) {
                return pathFrame;
            }

            Paint paint = self.getPaint();

            if (!drawOver) {
                Path path = new Path();
                path.moveTo(beginPoint.x, beginPoint.y);
                for (int i = 0; i < endPoints.size(); i++) {
                    path.lineTo(endPoints.get(i).x, endPoints.get(i).y);
                }

                canvas.drawPath(path, paint);
            }
            else {
                Path path = new Path();
                path.moveTo((beginPoint.x + endPoints.get(0).x) / 2.0f, (beginPoint.y + endPoints.get(0).y) / 2.0f);
                for (int i = 0; i < endPoints.size(); i++) {
                    path.lineTo(endPoints.get(i).x, endPoints.get(i).y);
                }
                path.lineTo((beginPoint.x + endPoints.get(0).x) / 2.0f, (beginPoint.y + endPoints.get(0).y) / 2.0f);

                if (state == DrawingPointerState.CalibrateToOrigin) {
                    path.offset(-pathFrame.left, -pathFrame.top);
                }

                self.drawSolidShapePath(canvas, path);
            }

            return drawOver ? pathFrame : UnfinishFrame;
        }

        return null;
    }

    /* #Accessors */
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDPolygonBrush defaultBrush() {
        return new VDPolygonBrush(5, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}