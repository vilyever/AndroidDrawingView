package com.vilyever.drawingview.model;

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

    private StepType stepType;

    private VDBrush.DrawingState drawingState;

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

    public StepType getStepType() {
        return stepType;
    }

    public VDDrawingStep setStepType(StepType stepType) {
        this.stepType = stepType;
        return self;
    }

    public VDBrush.DrawingState getDrawingState() {
        return drawingState;
    }

    public void setDrawingState(VDBrush.DrawingState drawingState) {
        this.drawingState = drawingState;
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

    public boolean isCleared() {
        return self.stepType == StepType.Clear;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum StepType {
        Clear, Draw, Background, CreateLayer, Transform, Text, DeleteLayer;
    }
}