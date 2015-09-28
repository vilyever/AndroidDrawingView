package com.vilyever.drawingview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

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

    /* #Delegates */

    /* #Private Methods */    
    
    /* #Public Methods */
    public void addPoint(VDDrawingPoint point) {
        switch (self.getBrush().getShape()) {
            case None:
            case Eraser:
            case LayerEraser: {
                if (self.getPoints().size() > 0 && self.getPoints().get(self.getPoints().size() - 1).isSamePoint(point)) {
                } else {
                    self.getPoints().add(point);
                }
                break;
            }
            case LayerEraserRectangle: {

                break;
            }
            case Polygon: {
                if (self.getPoints().size() == 2) {
                    self.getPoints().remove(1);
                }
                self.getPoints().add(point);
                break;
            }
            case Line:
            case Rectangle:
            case RoundedRetangle:
            case Circle:
            case Ellipse:
            case Triangle:
            case RightAngledRriangle:
            case IsoscelesTriangle:
            case Rhombus:
            case CenterSquare:
            case CenterCircle:
            case CenterEquilateralTrangle: {
                if (self.getPoints().size() == 2) {
                    self.getPoints().remove(1);
                }
                self.getPoints().add(point);
                break;
            }
        }
    }

    public void drawOnCanvas(Canvas canvas) {
        Paint paint = self.getPaint();
        switch (self.getBrush().getShape()) {
            case None:
            case Eraser: {
                if (self.getPoints().size() == 1) {
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(self.getPoints().get(0).x, self.getPoints().get(0).y, paint.getStrokeWidth() * 0.5f, paint);
                    paint.setStyle(Paint.Style.STROKE);
                }
                else if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    path.moveTo(self.getPoints().get(0).x, self.getPoints().get(0).y);
                    for (int i = 1; i < self.getPoints().size(); i++) {
                        path.quadTo(self.getPoints().get(i - 1).x, self.getPoints().get(i - 1).y,
                                (self.getPoints().get(i - 1).x + self.getPoints().get(i).x) / 2,
                                (self.getPoints().get(i - 1).y + self.getPoints().get(i).y) / 2);
                    }

                    canvas.drawPath(path, paint);
                }
                break;
            }
            case LayerEraserRectangle: {

            }
                break;
            case Polygon: {

            }
                break;
            case Line: {
                if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    path.moveTo(self.getPoints().get(0).x, self.getPoints().get(0).y);
                    path.lineTo(self.getPoints().get(1).x, self.getPoints().get(1).y);

                    canvas.drawPath(path, paint);
                }
                break;
            }
            case Rectangle: {
                if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    RectF rect = new RectF();
                    rect.left = Math.min(self.getPoints().get(0).x, self.getPoints().get(1).x);
                    rect.top = Math.min(self.getPoints().get(0).y, self.getPoints().get(1).y);
                    rect.right = Math.max(self.getPoints().get(0).x, self.getPoints().get(1).x);
                    rect.bottom = Math.max(self.getPoints().get(0).y, self.getPoints().get(1).y);
                    path.addRect(rect, Path.Direction.CW);

                    canvas.drawPath(path, paint);

                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(self.getBrush().getSolidColor());
                    canvas.drawRect(rect, paint);
                }
            }
                break;
            case RoundedRetangle: {
                if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    RectF rect = new RectF();
                    rect.left = Math.min(self.getPoints().get(0).x, self.getPoints().get(1).x);
                    rect.top = Math.min(self.getPoints().get(0).y, self.getPoints().get(1).y);
                    rect.right = Math.max(self.getPoints().get(0).x, self.getPoints().get(1).x);
                    rect.bottom = Math.max(self.getPoints().get(0).y, self.getPoints().get(1).y);
                    float round = Math.min(Math.abs(self.getPoints().get(0).x - self.getPoints().get(1).x), Math.abs(self.getPoints().get(0).y - self.getPoints().get(1).y)) / 10.0f;
                    round = Math.max(round, paint.getStrokeWidth());
                    path.addRoundRect(rect, round, round, Path.Direction.CW);

                    canvas.drawPath(path, paint);
                }
            }
                break;
            case Circle: {
                if (self.getPoints().size() > 1) {
                    Path path = new Path();
                    float centerX = (self.getPoints().get(0).x + self.getPoints().get(1).x) / 2.0f;
                    float centerY = (self.getPoints().get(0).y + self.getPoints().get(1).y) / 2.0f;
                    float radius = Math.min(Math.abs(self.getPoints().get(0).x - self.getPoints().get(1).x), Math.abs(self.getPoints().get(0).y - self.getPoints().get(1).y)) / 2.0f;
                    path.addCircle(centerX, centerY, radius, Path.Direction.CW);

                    canvas.drawPath(path, paint);
                }
            }
                break;
            case Ellipse: {

            }
                break;
            case Triangle: {

            }
                break;
            case RightAngledRriangle: {

            }
                break;
            case IsoscelesTriangle: {

            }
                break;
            case Rhombus: {

            }
                break;
            case CenterSquare: {

            }
                break;
            case CenterCircle: {

            }
                break;
            case CenterEquilateralTrangle: {

            }
                break;
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

            if (self.getBrush().getShape() == VDDrawingBrush.Shape.Eraser) {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            }
        }

        return paint;
    }

    public RectF getFrame() {
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

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}