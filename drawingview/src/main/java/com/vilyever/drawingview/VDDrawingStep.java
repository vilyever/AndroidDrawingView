package com.vilyever.drawingview;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.jsonmodel.VDModel;

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
    private boolean stepOver;

    /* #Constructors */
    public VDDrawingStep() {
    }

    public VDDrawingStep(int step) {
        this.step = step;
    }

    /* #Overrides */    
    
    /* #Accessors */
    public <T extends VDBrush> T getBrush() {
        return (T) brush;
    }

    public void setBrush(VDBrush brush) {
        this.brush = brush;
    }

    public VDDrawingLayer getDrawingLayer() {
        return drawingLayer;
    }

    public VDDrawingPath getDrawingPath() {
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
    public VDDrawingLayer newDrawingLayer(int hierarchy, VDDrawingLayer.LayerType layerType) {
        if (self.drawingLayer == null) {
            self.drawingLayer = new VDDrawingLayer(hierarchy);
            self.drawingLayer.setLayerType(layerType);
        }
        return drawingLayer;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}