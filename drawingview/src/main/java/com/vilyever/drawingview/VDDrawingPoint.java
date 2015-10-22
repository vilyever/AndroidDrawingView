package com.vilyever.drawingview;

import com.vilyever.jsonmodel.VDModel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * VDDrawingPoint
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/16.
 * Feature:
 */
public class VDDrawingPoint extends VDModel {
    public int pointerID;
    public float x;
    public float y;
    public long moment;

    /* #Constructors */
    public VDDrawingPoint() {
        this(0, 0);
    }

    public VDDrawingPoint(float x, float y) {
        this(0, x, y);
    }

    public VDDrawingPoint(int pointerID, float x, float y) {
        this.pointerID = pointerID;
        this.x = x;
        this.y = y;
        this.moment = System.currentTimeMillis();
    }
    
    /* #Overrides */    
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public boolean isSamePoint(VDDrawingPoint point) {
        return this.pointerID == point.pointerID && this.x == point.x && this.y == point.y;
    }

    public static VDDrawingPoint copy(VDDrawingPoint point) {
        return new VDDrawingPoint(point.pointerID, point.x, point.y);
    }

    private static final AtomicInteger atomicInteger = new AtomicInteger();
    public static int CurrentPointerID() {
        return atomicInteger.get();
    }
    public static int IncrementPointerID() {
        return atomicInteger.incrementAndGet();
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}