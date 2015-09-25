package com.vilyever.drawingview;

import com.vilyever.jsonmodel.VDModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VDDrawingStep
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingStep extends VDModel {
    private final VDDrawingStep self = this;

    private List<VDDrawingLayer> drawingLayers = new ArrayList<>();

    private long step;

    private boolean cleared;

    /* #Constructors */
    public VDDrawingStep() {

    }

    public VDDrawingStep(long step) {
        this.step = step;
    }

    /* #Overrides */    
    
    /* #Accessors */
    public List<VDDrawingLayer> getDrawingLayers() {
        return drawingLayers;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
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
    public void addLayer(VDDrawingLayer layer) {
        self.getDrawingLayers().add(layer);
        Collections.sort(self.getDrawingLayers());
    }

    public VDDrawingLayer newLayer() {
        long hierarchy = 0;
        if (self.getDrawingLayers().size() > 0) {
            hierarchy = self.getDrawingLayers().get(self.getDrawingLayers().size() - 1).getHierarchy() + 1;
        }
        VDDrawingLayer layer = new VDDrawingLayer(hierarchy);
        self.addLayer(layer);
        return layer;
    }

    public VDDrawingLayer drawingLayer() {
        if (self.getDrawingLayers().size() == 0) {
            return null;
        }

        return self.getDrawingLayers().get(self.getDrawingLayers().size() - 1);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}