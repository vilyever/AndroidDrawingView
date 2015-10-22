//package com.vilyever.drawingview.brush;
//
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffXfermode;
//
//import com.vilyever.drawingview.VDDrawingPath;
//import com.vilyever.drawingview.VDDrawingPoint;
//
///**
// * VDEraserBrush
// * AndroidDrawingView <com.vilyever.drawingview.brush>
// * Created by vilyever on 2015/10/21.
// * Feature:
// */
//public class VDEraserBrush extends VDDrawingBrush {
//    final VDEraserBrush self = this;
//
//
//    /* #Constructors */
//    public VDEraserBrush() {
//
//    }
//
//    public VDEraserBrush(float size) {
//        super(size, Color.TRANSPARENT);
//    }
//
//    /* #Overrides */
//    @Override
//    public int getColor() {
//        return Color.TRANSPARENT;
//    }
//
//    @Override
//    public boolean isOneStrokeToLayer() {
//        return false;
//    }
//
//    @Override
//    public Paint getPaint() {
//        Paint paint = super.getPaint();
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//
//        return paint;
//    }
//
//    @Override
//    public boolean drawSolidShapePath(Canvas canvas, VDDrawingPath drawingPath, DrawingPointerState state) {
//        if (canvas == null
//                || drawingPath == null) {
//            return true;
//        }
//
//        if (drawingPath.getPoints().size() > 0) {
//            Paint paint = self.getPaint();
//
//            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
//            VDDrawingPoint endPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);
//
//            if (drawingPath.getPoints().size() == 1) {
//                paint.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(beginPoint.x, beginPoint.y, paint.getStrokeWidth() * 0.5f, paint);
//                paint.setStyle(Paint.Style.STROKE);
//            } else if (drawingPath.getPoints().size() > 1) {
//                Path path = new Path();
//                path.moveTo(beginPoint.x, beginPoint.y);
//                for (int i = 1; i < drawingPath.getPoints().size(); i++) {
//                    path.quadTo(drawingPath.getPoints().get(i - 1).x, drawingPath.getPoints().get(i - 1).y,
//                            (drawingPath.getPoints().get(i - 1).x + drawingPath.getPoints().get(i).x) / 2,
//                            (drawingPath.getPoints().get(i - 1).y + drawingPath.getPoints().get(i).y) / 2);
//                }
//
//                canvas.drawSolidShapePath(path, paint);
//            }
//        }
//
//        if (state == DrawingPointerState.End) {
//            return true;
//        }
//        return false;
//    }
//
//    /* #Accessors */
//
//    /* #Delegates */
//
//    /* #Private Methods */
//
//    /* #Public Methods */
//    public static VDEraserBrush defaultBrush() {
//        return new VDEraserBrush(10);
//    }
//
//    /* #Classes */
//
//    /* #Interfaces */
//
//    /* #Annotations @interface */
//
//    /* #Enums */
//}