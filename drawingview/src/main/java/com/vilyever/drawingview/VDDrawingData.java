package com.vilyever.drawingview;

import com.vilyever.jsonmodel.VDModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VDDrawingData
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingData extends VDModel {
    private final VDDrawingData self = this;

    private List<VDDrawingLayer> drawingLayers = new ArrayList<>();

    private boolean cleared;

    /* #Constructors */    
    
    /* #Overrides */    
    
    /* #Accessors */
    public List<VDDrawingLayer> getDrawingLayers() {
        return drawingLayers;
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
        self.drawingLayers.add(layer);
        Collections.sort(self.drawingLayers);
    }

    public VDDrawingLayer newLayer() {
        long hierarchy = 0;
        if (self.drawingLayers.size() > 0) {
            hierarchy = self.drawingLayers.get(self.drawingLayers.size() - 1).getHierarchy() + 1;
        }
        VDDrawingLayer layer = new VDDrawingLayer(hierarchy);
        self.addLayer(layer);
        return layer;
    }

    public VDDrawingLayer baseLayer() {
        if (self.drawingLayers.size() == 0) {
            return null;
        }
        return self.drawingLayers.get(0);
    }

    public VDDrawingLayer topLayer() {
        if (self.drawingLayers.size() == 0) {
            return null;
        }

        return self.drawingLayers.get(self.drawingLayers.size() - 1);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}