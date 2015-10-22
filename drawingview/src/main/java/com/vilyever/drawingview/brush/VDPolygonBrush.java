package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

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
    public boolean drawPath(Canvas canvas, VDDrawingPath drawingPath, DrawingPointerState state) {
        if (canvas == null
                || drawingPath == null) {
            return true;
        }

        if (drawingPath.getPoints().size() > 1) {
            Paint paint = self.getPaint();

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
            if (state == DrawingPointerState.End
                    && beginPoint.pointerID != lastPoint.pointerID
                    && Math.abs(beginPoint.x - lastPoint.x) < (10.0f + self.getSize())
                    && Math.abs(beginPoint.y - lastPoint.y) < (10.0f + self.getSize())) {
                drawOver = true;
            }

            Path path = new Path();
            path.moveTo(beginPoint.x, beginPoint.y);
            for (int i = 0; i < endPoints.size() - 1; i++) {
                path.lineTo(endPoints.get(i).x, endPoints.get(i).y);
            }

            if (!drawOver) {
                path.lineTo(lastPoint.x, lastPoint.y);
                canvas.drawPath(path, paint);
            }
            else {
                path.lineTo(beginPoint.x, beginPoint.y);
                self.drawSolidShapePath(canvas, path);
            }

            return drawOver;
        }

        return false;
    }

    @Override
    public RectF getDrawingFrame(VDDrawingPath drawingPath) {
        if (drawingPath == null
                || drawingPath.getPoints().size() < 2) {
            return null;
        }
        else {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);
            List<VDDrawingPoint> endPoints = new ArrayList<>();

            int currentPointerID = beginPoint.pointerID;
            for (int i = 1; i < drawingPath.getPoints().size() - 1; i++) {
                VDDrawingPoint drawingPoint = drawingPath.getPoints().get(i);
                if (drawingPoint.pointerID != currentPointerID) {
                    endPoints.add(drawingPath.getPoints().get(i - 1));
                    currentPointerID = drawingPoint.pointerID;
                }
            }
            endPoints.add(lastPoint);

            float leftest = beginPoint.x;
            float rightest = beginPoint.x;
            float topest = beginPoint.y;
            float bottomest = beginPoint.y;

            for (int i = 0; i < endPoints.size(); i++) {
                VDDrawingPoint point = endPoints.get(i);
                leftest = Math.min(point.x, leftest);
                rightest = Math.max(point.x, rightest);
                topest = Math.min(point.y, topest);
                bottomest = Math.max(point.y, bottomest);
            }

            return new RectF(leftest - self.getSize(),
                    topest - self.getSize(),
                    rightest + self.getSize(),
                    bottomest + self.getSize());
        }
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