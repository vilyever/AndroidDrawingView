//package com.vilyever.drawingview.brush;
//
//import android.graphics.Color;
//
//import com.vilyever.drawingview.VDDrawingPath;
//import com.vilyever.drawingview.VDDrawingView;
//
///**
// * VDLayerEraserBrush
// * AndroidDrawingView <com.vilyever.drawingview.brush>
// * Created by vilyever on 2015/10/21.
// * Feature:
// */
//public class VDLayerEraserBrush extends VDDrawingBrush {
//    final VDLayerEraserBrush self = this;
//
//
//    /* #Constructors */
//    public VDLayerEraserBrush() {
//        super(0, Color.TRANSPARENT);
//    }
//
//    /* #Overrides */
//
//    @Override
//    public boolean disableLayerTouch() {
//        return true;
//    }
//
//    @Override
//    public boolean drawSolidShapePath(VDDrawingView drawingView, VDDrawingPath drawingPath, DrawingPointerState state) {
//        for (int i = 0; i < drawingPath.getPoints().size(); i++) {
//            drawingView.removeLayersAtPoint(drawingPath.getPoints().get(i));
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
//
//    /* #Classes */
//
//    /* #Interfaces */
//
//    /* #Annotations @interface */
//
//    /* #Enums */
//}