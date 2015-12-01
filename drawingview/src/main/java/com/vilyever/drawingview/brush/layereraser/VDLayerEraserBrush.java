package com.vilyever.drawingview.brush.layereraser;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.View;

import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.brush.VDBrush;

/**
 * VDLayerEraserBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/11/13.
 * Feature:
 */
public abstract class VDLayerEraserBrush extends VDBrush {
    final VDLayerEraserBrush self = this;


    /* #Constructors */
    public VDLayerEraserBrush() {
    }

    /* #Overrides */
    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        return null;
    }
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public abstract boolean shouldErase(@NonNull View layerView, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state);

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}