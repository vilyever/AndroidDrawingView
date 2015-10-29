package com.vilyever.drawingview;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.VDDrawingBrush;
import com.vilyever.jsonmodel.VDModel;

import java.lang.ref.WeakReference;

/**
 * VDDrawingStep
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingStep extends VDModel {
    private final VDDrawingStep self = this;

    private VDBrush brush;
    private VDDrawingLayer drawingLayer;
    private VDDrawingPath drawingPath;

    private int step;

    private boolean cleared;

    @VDJsonKeyIgnore
    private WeakReference<Canvas> drawingCanvas;

    @VDJsonKeyIgnore
    private boolean stepOver;

    /* #Constructors */
    public VDDrawingStep() {
    }

    public VDDrawingStep(int step) {
        this.step = step;
    }

    /* #Overrides */    
    
    /* #Accessors */
    public <T extends VDBrush> T drawingBrush() {
        return (T) brush;
    }

    public void setBrush(VDBrush brush) {
        this.brush = brush;
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
        if (self.drawingCanvas != null) {
            return self.drawingCanvas.get();
        }
        return null;
    }

    public VDDrawingStep setDrawingCanvas(Canvas drawingCanvas) {
        this.drawingCanvas = new WeakReference<>(drawingCanvas);
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

    public RectF updateDrawing(VDDrawingBrush.DrawingPointerState state) {
        if (self.drawingBrush() == null
                || self.getDrawingCanvas() == null
                || self.drawingPath() == null) {
            return null;
        }
        RectF frame = self.drawingBrush().drawPath(self.getDrawingCanvas(), self.drawingPath(), state);
        self.drawingLayer().setFrame(frame);
        return frame;
    }

    public RectF getDrawingLayerFrame() {
        return self.drawingLayer().getFrame();
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}