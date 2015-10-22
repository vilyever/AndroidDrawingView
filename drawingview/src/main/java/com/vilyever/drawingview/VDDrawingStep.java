package com.vilyever.drawingview;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.vilyever.drawingview.brush.VDDrawingBrush;
import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingStep
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingStep extends VDModel {
    private final VDDrawingStep self = this;

    private VDDrawingBrush drawingBrush;
    private VDDrawingLayer drawingLayer;
    private VDDrawingPath drawingPath;

    private int step;

    private boolean cleared;

    @VDJsonKeyIgnore
    private Canvas drawingCanvas;

    @VDJsonKeyIgnore
    private boolean stepOver;

    /* #Constructors */
    public VDDrawingStep(int step) {
        this.step = step;
    }

    /* #Overrides */    
    
    /* #Accessors */
    public VDDrawingBrush drawingBrush() {
        return drawingBrush;
    }

    public void setDrawingBrush(VDDrawingBrush drawingBrush) {
        this.drawingBrush = drawingBrush;
    }

    public VDDrawingLayer drawingLayer() {
        return drawingLayer;
    }

    public VDDrawingLayer drawingLayer(int hierarchy) {
        if (self.drawingLayer == null) {
            self.drawingLayer = new VDDrawingLayer(hierarchy);
        }
        return drawingLayer;
    }

    public VDDrawingPath drawingPath() {
        if (self.drawingPath == null) {
            self.drawingPath = new VDDrawingPath();
        }
        return drawingPath;
    }

    public int getStep() {
        return step;
    }

    public boolean isCleared() {
        return cleared;
    }

    public VDDrawingStep setCleared(boolean cleared) {
        this.cleared = cleared;
        return self;
    }

    public Canvas getDrawingCanvas() {
        return drawingCanvas;
    }

    public VDDrawingStep setDrawingCanvas(Canvas drawingCanvas) {
        this.drawingCanvas = drawingCanvas;
        return self;
    }

    public boolean isStepOver() {
        return stepOver;
    }

    public VDDrawingStep setStepOver(boolean stepOver) {
        this.stepOver = stepOver;
        return self;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public VDDrawingLayer newDrawingLayer(int hierarchy) {
        return self.drawingLayer(hierarchy);
    }

    public boolean updateDrawing(VDDrawingBrush.DrawingPointerState state) {
        if (self.drawingBrush() == null
                || self.getDrawingCanvas() == null
                || self.drawingPath() == null) {
            return true;
        }
        return self.drawingBrush().drawPath(self.getDrawingCanvas(), self.drawingPath(), state);
    }

    public void updateDrawingLayerFrame() {
        if (self.drawingLayer() != null
                && self.drawingBrush() != null
                && self.drawingPath() != null) {
            self.drawingLayer().setFrame(self.drawingBrush().getDrawingFrame(self.drawingPath()));
        }
    }

    public RectF getDrawingLayerFrame() {
        self.updateDrawingLayerFrame();
        return self.drawingLayer().getFrame();
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}