package com.vilyever.drawingview;

import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingStep
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingStep extends VDModel {
    private final VDDrawingStep self = this;

    private VDDrawingLayer drawingLayer;

    private int step;

    private boolean cleared;

    /* #Constructors */
    public VDDrawingStep() {

    }

    public VDDrawingStep(int step) {
        this.step = step;
    }

    /* #Overrides */    
    
    /* #Accessors */
    public VDDrawingLayer getDrawingLayer() {
        return drawingLayer;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public VDDrawingLayer newLayer(int hierarchy) {
        VDDrawingLayer layer = new VDDrawingLayer(hierarchy);
        self.drawingLayer = layer;
        return layer;
    }

    public VDDrawingLayer drawingLayer() {
        return self.drawingLayer;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}