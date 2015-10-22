package com.vilyever.drawingview;

import com.vilyever.jsonmodel.VDModel;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingPath
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingPath extends VDModel {
    private final VDDrawingPath self = this;

    private List<VDDrawingPoint> points = new ArrayList<>();

    /* #Constructors */
    public VDDrawingPath() {
    }

    /* #Overrides */
    
    /* #Accessors */
    public List<VDDrawingPoint> getPoints() {
        return points;
    }

    /* #Delegates */

    /* #Private Methods */    
    
    /* #Public Methods */
    public void addPoint(VDDrawingPoint point) {
        if (self.getPoints().size() > 0
                && self.getPoints().get(self.getPoints().size() - 1).isSamePoint(point)) {
        }
        else {
            self.getPoints().add(point);
        }
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}