package com.vilyever.drawingview;

import android.graphics.Color;

import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingBrush
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/16.
 * Feature:
 */
public class VDDrawingBrush extends VDModel {
    private final VDDrawingBrush self = this;

    private float size;
    private int color;
    private int solidColor;
    private boolean rounded;
    private Shape shape;
    private boolean oneStrokeToLayer;
    
    /* #Constructors */
    public VDDrawingBrush() {
    }

    public VDDrawingBrush(float size, int color, Shape shape) {
        this(size, color, Color.TRANSPARENT, true, shape, false);
    }

    public VDDrawingBrush(float size, int color, int solidColor, boolean rounded, Shape shape, boolean oneStrokeToLayer) {
        this.size = size;
        this.color = color;
        this.solidColor = solidColor;
        this.rounded = rounded;
        this.shape = shape;
        this.oneStrokeToLayer = oneStrokeToLayer;
    }

    /* #Overrides */    
    
    /* #Accessors */
    public float getSize() {
        return size;
    }

    public VDDrawingBrush setSize(float size) {
        this.size = size;
        return self;
    }

    public int getColor() {
        return color;
    }

    public int getSolidColor() {
        return solidColor;
    }

    public VDDrawingBrush setSolidColor(int solidColor) {
        this.solidColor = solidColor;
        return self;
    }

    public VDDrawingBrush setColor(int color) {
        this.color = color;
        return self;
    }

    public boolean isRounded() {
        if (self.getShape() == Shape.RoundedRetangle) {
            return true;
        }
        return rounded;
    }

    public VDDrawingBrush setRounded(boolean rounded) {
        this.rounded = rounded;
        return self;
    }

    public Shape getShape() {
        return shape;
    }

    public VDDrawingBrush setShape(Shape shape) {
        this.shape = shape;
        return self;
    }

    public boolean isOneStrokeToLayer() {
        if (self.getShape() == Shape.Eraser) {
            return false;
        }
        return oneStrokeToLayer;
    }

    public VDDrawingBrush setOneStrokeToLayer(boolean oneStrokeToLayer) {
        this.oneStrokeToLayer = oneStrokeToLayer;
        return self;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static VDDrawingBrush copy(VDDrawingBrush brush) {
        return new VDDrawingBrush(brush.getSize(), brush.getColor(), brush.getShape());
    }

    public static VDDrawingBrush defaultBrush() {
        return new VDDrawingBrush(5, Color.BLACK, Shape.None);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum Shape {
        None,
        Eraser, LayerEraser, LayerEraserRectangle,
        Polygon, Line, Rectangle, RoundedRetangle, Circle, Ellipse, Triangle, RightAngledRriangle, IsoscelesTriangle, Rhombus,
        CenterSquare, CenterCircle, CenterEquilateralTrangle,
    }
}