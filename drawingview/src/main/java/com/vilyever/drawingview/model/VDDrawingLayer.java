package com.vilyever.drawingview.model;

import android.graphics.RectF;
import android.widget.RelativeLayout;

import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingLayer
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingLayer extends VDModel implements Comparable<VDDrawingLayer> {
    private final VDDrawingLayer self = this;

    public static final int UnsetValue = -1;

    private int hierarchy; // from 0 to max

    private LayerType layerType = LayerType.Image;

    private int backgroundColor = UnsetValue;
    private String backgroundImageIdentifier;

    private String text;

    private float left = UnsetValue;
    private float top = UnsetValue;
    private float width = UnsetValue;
    private float height = UnsetValue;

    private float scale = UnsetValue;
    private float rotation = UnsetValue;

    private boolean deleted;

    /* #Constructors */
    public VDDrawingLayer() {
    }

    public VDDrawingLayer(int hierarchy) {
        this(hierarchy, null);
    }

    public VDDrawingLayer(int hierarchy, RectF frame) {
        this.hierarchy = hierarchy;
        self.layerType = LayerType.Image;
        self.setFrame(frame);
    }

    /* #Overrides */    
    
    /* #Accessors */
    public int getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public void setLayerType(LayerType layerType) {
        this.layerType = layerType;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBackgroundImageIdentifier() {
        return backgroundImageIdentifier;
    }

    public void setBackgroundImageIdentifier(String backgroundImageIdentifier) {
        this.backgroundImageIdentifier = backgroundImageIdentifier;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public RectF getFrame() {
        if (self.left == UnsetValue) {
            return null;
        }
        return new RectF(self.left, self.top, self.left + self.width, self.top + self.height);
    }

    public void setFrame(RectF frame) {
        if (frame == null) {
            self.left = UnsetValue;
            self.top = UnsetValue;
            self.width = UnsetValue;
            self.height = UnsetValue;
            return;
        }
        self.left = frame.left;
        self.top = frame.top;
        self.width = frame.right - frame.left;
        self.height = frame.bottom - frame.top;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /* #Delegates */
    // Comparable<VDDrawingLayer>
    @Override
    public int compareTo(VDDrawingLayer another) {
        return self.getHierarchy() - another.getHierarchy();
    }
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public RelativeLayout.LayoutParams getLayoutParams() {
        if (self.getLeft() == UnsetValue) {
            return null;
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Math.floor(self.getWidth()),
                                                                                    (int) Math.floor(self.getHeight()));
        layoutParams.leftMargin = (int) Math.floor(self.getLeft());
        layoutParams.topMargin = (int) Math.floor(self.getTop());
        layoutParams.rightMargin = -Integer.MAX_VALUE;
        layoutParams.bottomMargin = -Integer.MAX_VALUE;

        return layoutParams;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum LayerType {
        Unkonwn, Base, Image, Text;
    }
}