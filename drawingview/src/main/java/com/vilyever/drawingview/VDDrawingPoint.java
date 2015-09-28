package com.vilyever.drawingview;

import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingPoint
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/16.
 * Feature:
 */
public class VDDrawingPoint extends VDModel {
    public float x;
    public float y;

    /* #Constructors */
    public VDDrawingPoint() {
    }

    public VDDrawingPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /* #Overrides */    
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public boolean isSamePoint(VDDrawingPoint point) {
        return this.x == point.x && this.y == point.y;
    }

    public static VDDrawingPoint copy(VDDrawingPoint point) {
        return new VDDrawingPoint(point.x, point.y);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}