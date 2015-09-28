package com.vilyever.drawingview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.vilyever.jsonmodel.VDJsonModelDelegate;
import com.vilyever.jsonmodel.VDModel;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingPath
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingPath extends VDModel {
    private final VDDrawingPath self = this;

    private VDDrawingBrush brush;

    private List<VDDrawingPoint> points = new ArrayList<>();

    @VDJsonModelDelegate.VDJsonKeyIgnore
    private boolean canFinish = true;

    @VDJsonModelDelegate.VDJsonKeyIgnore
    private boolean canDraw = true;

    /* #Constructors */
    public VDDrawingPath() {
    }

    public VDDrawingPath(VDDrawingBrush brush) {
        self.setBrush(brush);
    }

    /* #Overrides */    
    
    /* #Accessors */
    public VDDrawingBrush getBrush() {
        return brush;
    }

    public void setBrush(VDDrawingBrush brush) {
        this.brush = VDDrawingBrush.copy(brush);
    }

    public List<VDDrawingPoint> getPoints() {
        return points;
    }

    public boolean isCanFinish() {
        return canFinish;
    }

    public boolean isCanDraw() {
        return canDraw;
    }

    /* #Delegates */

    /* #Private Methods */    
    
    /* #Public Methods */
    public void addPoint(VDDrawingPoint point) {
        if (self.getPoints().size() > 0
                && self.getPoints().get(self.getPoints().size() - 1).isSamePoint(point)) {
        }
        else {
            if (self.getBrush().getType() == VDDrawingBrush.Type.Shape
                    && self.getBrush().getShape() == VDDrawingBrush.Shape.Polygon) {
                if (!self.isCanFinish()) {
                    if (self.getPoints().size() > 1) {
                        self.getPoints().remove(self.getPoints().size() - 1);
                    }
                    self.getPoints().add(point);
                }
            }
            else {
                self.getPoints().add(point);
            }

            if (self.getBrush().getType() == VDDrawingBrush.Type.Shape) {
                self.canDraw = self.getPoints().size() >= 2;
            }
        }
    }

    public void addBeginPoint(VDDrawingPoint point) {
        if (self.getBrush().getType() == VDDrawingBrush.Type.Shape
                && self.getBrush().getShape() == VDDrawingBrush.Shape.Polygon) {
            self.canFinish = false;
            self.getPoints().add(point);
        }
    }

    public void addEndPoint(VDDrawingPoint point) {
        if (self.getBrush().getType() == VDDrawingBrush.Type.Shape
                && self.getBrush().getShape() == VDDrawingBrush.Shape.Polygon) {
            self.canFinish = false;
            VDDrawingPoint beginPoint = self.getPoints().get(0);
            if (point.isSamePoint(self.finishPathPoint())) {
                self.getPoints().add(point);
                self.canFinish = true;
            }
            else {
                if (self.getPoints().size() > 0) {
                    self.getPoints().remove(self.getPoints().size() - 1);
                }
                self.getPoints().add(point);
                if (Math.abs(beginPoint.x - point.x) < 10.0f + self.getPaint().getStrokeWidth()
                        && Math.abs(beginPoint.y - point.y) < 10.0f + self.getPaint().getStrokeWidth()) {
                    self.canFinish = true;
                }
            }
        }
    }

    public VDDrawingPoint finishPathPoint() {
        if (self.getBrush().getType() == VDDrawingBrush.Type.Shape
                && self.getBrush().getShape() == VDDrawingBrush.Shape.Polygon) {
            return VDDrawingPoint.copy(self.getPoints().get(0));
        }
        return null;
    }

    public void drawOnCanvas(Canvas canvas) {
        drawOnCanvas(canvas, false);
    }

    public void drawOnCanvas(Canvas canvas, boolean topLeftIsOrigin) {
        Paint paint = self.getPaint();

        float offsetX = 0.0f;
        float offsetY = 0.0f;

        if (topLeftIsOrigin) {
            offsetX = self.getPoints().get(0).x;
            offsetY = self.getPoints().get(0).y;
            for (int i = 1; i < self.getPoints().size(); i++) {
                offsetX = Math.min(offsetX, self.getPoints().get(i).x);
                offsetY = Math.min(offsetY, self.getPoints().get(i).y);
            }
            offsetX -=  paint.getStrokeWidth();
            offsetY -=  paint.getStrokeWidth();
        }

        VDDrawingPoint beginPoint = self.getPoints().get(0);
        VDDrawingPoint endPoint = self.getPoints().get(self.getPoints().size() - 1);

        RectF rect = new RectF();
        rect.left = Math.min(beginPoint.x - offsetX, endPoint.x - offsetX);
        rect.top = Math.min(beginPoint.y - offsetY, endPoint.y - offsetY);
        rect.right = Math.max(beginPoint.x - offsetX, endPoint.x - offsetX);
        rect.bottom = Math.max(beginPoint.y - offsetY, endPoint.y - offsetY);

        switch (self.getBrush().getType()) {
            case Pen: {
                if (self.getPoints().size() == 1) {
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(beginPoint.x - offsetX, beginPoint.y - offsetY, paint.getStrokeWidth() * 0.5f, paint);
                    paint.setStyle(Paint.Style.STROKE);
                }
                else if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    path.moveTo(beginPoint.x - offsetX, beginPoint.y - offsetY);
                    for (int i = 1; i < self.getPoints().size(); i++) {
                        path.quadTo(self.getPoints().get(i - 1).x - offsetX, self.getPoints().get(i - 1).y - offsetY,
                                    (self.getPoints().get(i - 1).x - offsetX + self.getPoints().get(i).x - offsetX) / 2,
                                    (self.getPoints().get(i - 1).y - offsetY + self.getPoints().get(i).y - offsetY) / 2);
                    }

                    canvas.drawPath(path, paint);
                }
                break;
            }
            case Eraser: {
                if (self.getPoints().size() == 1) {
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(beginPoint.x - offsetX, beginPoint.y - offsetY, paint.getStrokeWidth() * 0.5f, paint);
                    paint.setStyle(Paint.Style.STROKE);
                }
                else if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    path.moveTo(beginPoint.x - offsetX, beginPoint.y - offsetY);
                    for (int i = 1; i < self.getPoints().size(); i++) {
                        path.quadTo(self.getPoints().get(i - 1).x - offsetX, self.getPoints().get(i - 1).y - offsetY,
                                    (self.getPoints().get(i - 1).x - offsetX + self.getPoints().get(i).x - offsetX) / 2,
                                    (self.getPoints().get(i - 1).y - offsetY + self.getPoints().get(i).y - offsetY) / 2);
                    }

                    canvas.drawPath(path, paint);
                }
                break;
            }
            case LayerEraser: {

                break;
            }
            case Shape:
                switch (self.getBrush().getShape()) {
                    case Polygon: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.moveTo(beginPoint.x - offsetX, beginPoint.y - offsetY);
                            for (int i = 1; i < self.getPoints().size() - 1; i++) {
                                path.lineTo(self.getPoints().get(i).x - offsetX, self.getPoints().get(i).y - offsetY);
                            }

                            if (self.isCanFinish()) {
                                path.lineTo(beginPoint.x - offsetX, beginPoint.y - offsetY);
                            }
                            else {
                                path.lineTo(endPoint.x - offsetX, endPoint.y - offsetY);
                            }

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case Line: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.moveTo(beginPoint.x - offsetX, beginPoint.y - offsetY);
                            path.lineTo(endPoint.x - offsetX, endPoint.y - offsetY);

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case Rectangle: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.addRect(rect, Path.Direction.CW);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case RoundedRetangle: {
                        if (self.getPoints().size() > 1) {
                            float round = Math.min(Math.abs(beginPoint.x - endPoint.x), Math.abs(beginPoint.y - endPoint.y)) / 10.0f;
                            round = Math.max(round, paint.getStrokeWidth());

                            Path path = new Path();
                            path.addRoundRect(rect, round, round, Path.Direction.CW);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case Circle: {
                        if (self.getPoints().size() > 1) {
                            float centerX = (beginPoint.x - offsetX + endPoint.x - offsetX) / 2.0f;
                            float centerY = (beginPoint.y - offsetY + endPoint.y - offsetY) / 2.0f;
                            float radius = Math.min(Math.abs(beginPoint.x - endPoint.x), Math.abs(beginPoint.y - endPoint.y)) / 2.0f;

                            Path path = new Path();
                            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case Ellipse: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.addOval(rect, Path.Direction.CW);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case RightAngledRriangle: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.moveTo(rect.left, rect.bottom);
                            path.lineTo(rect.right, rect.bottom);
                            path.lineTo(rect.left, rect.top);
                            path.lineTo(rect.left, rect.bottom);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case IsoscelesTriangle: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.moveTo(rect.left, rect.bottom);
                            path.lineTo(rect.right, rect.bottom);
                            path.lineTo((rect.left + rect.right) / 2, rect.top);
                            path.lineTo(rect.left, rect.bottom);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case Rhombus: {
                        if (self.getPoints().size() > 1) {
                            Path path = new Path();
                            path.moveTo((rect.left + rect.right) / 2, rect.bottom);
                            path.lineTo(rect.right, (rect.top + rect.bottom) / 2);
                            path.lineTo((rect.left + rect.right) / 2, rect.top);
                            path.lineTo(rect.left, (rect.top + rect.bottom) / 2);
                            path.lineTo((rect.left + rect.right) / 2, rect.bottom);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                    case CenterCircle: {
                        if (self.getPoints().size() > 1) {
                            float centerX = beginPoint.x;
                            float centerY = beginPoint.y;
                            float radius = Math.min(Math.abs(beginPoint.x - endPoint.x), Math.abs(beginPoint.y - endPoint.y));

                            if (topLeftIsOrigin) {
                                centerX = radius + paint.getStrokeWidth();
                                centerY = radius + paint.getStrokeWidth();
                            }

                            Path path = new Path();
                            path.addCircle(centerX, centerY, radius, Path.Direction.CW);

                            canvas.drawPath(path, paint);

                            paint.setStyle(Paint.Style.FILL);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                            paint.setColor(self.getBrush().getSolidColor());

                            canvas.drawPath(path, paint);
                        }
                        break;
                    }
                }
                break;
            case Clip: {
                break;
            }
        }
    }

    public Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);

        if (self.getBrush().isRounded()) {
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        if (self.getBrush() != null) {
            paint.setStrokeWidth(self.getBrush().getSize());
            paint.setColor(self.getBrush().getColor());

            if (self.getBrush().getType() == VDDrawingBrush.Type.Eraser) {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            }
        }

        return paint;
    }

    public RectF getFrame() {
        VDDrawingPoint beginPoint = self.getPoints().get(0);
        VDDrawingPoint endPoint = self.getPoints().get(self.getPoints().size() - 1);

        switch (self.getBrush().getType()) {
            case Shape: {
                if (self.getBrush().getShape() == VDDrawingBrush.Shape.CenterCircle) {
                    if (self.getPoints().size() > 1) {
                        float centerX = beginPoint.x;
                        float centerY = beginPoint.y;
                        float radius = Math.min(Math.abs(beginPoint.x - endPoint.x), Math.abs(beginPoint.y - endPoint.y));

                        float leftest = centerX - radius;
                        float rightest = centerX + radius;
                        float topest = centerY - radius;
                        float bottomest = centerY + radius;

                        return new RectF(leftest - self.getBrush().getSize(),
                                            topest - self.getBrush().getSize(),
                                            rightest + self.getBrush().getSize(),
                                            bottomest + self.getBrush().getSize());
                    }
                }
            }
            default: {
                float leftest = self.getPoints().get(0).x;
                float rightest = self.getPoints().get(0).x;
                float topest = self.getPoints().get(0).y;
                float bottomest = self.getPoints().get(0).y;
                for (int i = 1; i < self.getPoints().size(); i++) {
                    VDDrawingPoint point = self.getPoints().get(i);
                    leftest = Math.min(point.x, leftest);
                    rightest = Math.max(point.x, rightest);
                    topest = Math.min(point.y, topest);
                    bottomest = Math.max(point.y, bottomest);
                }

                return new RectF(leftest - self.getBrush().getSize(),
                                    topest - self.getBrush().getSize(),
                                    rightest + self.getBrush().getSize(),
                                    bottomest + self.getBrush().getSize());
            }
        }
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}