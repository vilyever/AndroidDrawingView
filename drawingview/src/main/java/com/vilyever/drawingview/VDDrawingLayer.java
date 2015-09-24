package com.vilyever.drawingview;

import android.graphics.Color;
import android.graphics.RectF;
import android.widget.RelativeLayout;

import com.vilyever.jsonmodel.VDModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VDDrawingLayer
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingLayer extends VDModel implements Comparable<VDDrawingLayer> {
    private final VDDrawingLayer self = this;

    private long hierarchy; // from 0 to max

    private int backgroundColor = Color.TRANSPARENT;
    private String backgroundImageIdentifier;

    private String imageIdentifer;
    private String text;
    private int textSize;
    private int textColor;
    private int textType;

    private float left;
    private float top;
    private float width;
    private float height;

    private float scale = 1.0f;
    private float rotation = 0.0f;

    private List<VDDrawingPath> paths = new ArrayList<>();

    /* #Constructors */
    public VDDrawingLayer() {
    }

    public VDDrawingLayer(long hierarchy) {
        this(hierarchy, null);
    }

    public VDDrawingLayer(long hierarchy, RectF frame) {
        this.hierarchy = hierarchy;
        self.setFrame(frame);
    }

    /* #Overrides */    
    
    /* #Accessors */
    public long getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(long hierarchy) {
        this.hierarchy = hierarchy;
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

    public String getImageIdentifer() {
        return imageIdentifer;
    }

    public void setImageIdentifer(String imageIdentifer) {
        this.imageIdentifer = imageIdentifer;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextType() {
        return textType;
    }

    public void setTextType(int textType) {
        this.textType = textType;
    }

    public RectF getFrame() {
        return new RectF(self.left, self.top, self.left + self.width, self.top + self.height);
    }

    public void setFrame(RectF frame) {
        if (frame == null) {
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

    public List<VDDrawingPath> getPaths() {
        return paths;
    }

    /* #Delegates */
    // Comparable<VDDrawingLayer>
    @Override
    public int compareTo(VDDrawingLayer another) {
        return (int) (self.getHierarchy() - another.getHierarchy());
    }
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public void addPath(VDDrawingPath path) {
        self.paths.add(path);
        Collections.sort(self.paths);
    }

    public VDDrawingPath newPath(VDDrawingBrush brush) {
        long sequence = 0;
        if (self.paths.size() > 0) {
            sequence = self.paths.get(self.paths.size() - 1).getSequence() + 1;
        }
        VDDrawingPath path = new VDDrawingPath(sequence, brush);
        self.addPath(path);
        return path;
    }

    public VDDrawingPath currentPath() {
        if (self.paths.size() == 0) {
            return null;
        }

        return self.paths.get(self.paths.size() - 1);
    }

    public RelativeLayout.LayoutParams getLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Math.floor(self.width),
                (int) Math.floor(self.height));
        layoutParams.leftMargin = (int) Math.floor(self.left);
        layoutParams.topMargin = (int) Math.floor(self.top);
        layoutParams.rightMargin = -(int) Math.floor(self.width);
        layoutParams.bottomMargin = -(int) Math.floor(self.height);

        return layoutParams;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}