package com.vilyever.drawingview.model;

import android.graphics.RectF;
import android.widget.RelativeLayout;

import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingLayer
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 * 绘画数据：图层数据
 * 每一层并不限制此类数据的数量
 */
public class VDDrawingLayer extends VDModel implements Comparable<VDDrawingLayer> {
    private final VDDrawingLayer self = this;

    // 未设定的值
    public static final int UnsetValue = -1;

    /* #Constructors */
    public VDDrawingLayer() {
        this(UnsetValue);
    }

    public VDDrawingLayer(int hierarchy) {
        this(hierarchy, null);
    }

    public VDDrawingLayer(int hierarchy, RectF frame) {
        // 预先初始化，可能影响到一些变量，故先操作
        self.init();

        this.hierarchy = hierarchy;
        this.layerType = LayerType.Image;
        setFrame(frame);
    }

    // left，top，right，bottom快捷设置
    public void setFrame(RectF frame) {
        if (frame == null) {
            self.left = UnsetValue;
            self.top = UnsetValue;
            self.right = UnsetValue;
            self.bottom = UnsetValue;
            return;
        }
        self.left = frame.left;
        self.top = frame.top;
        self.right = frame.right;
        self.bottom = frame.bottom;
    }

    // left，top，right，bottom快捷获取
    public RectF getFrame() {
        if (self.left == UnsetValue) {
            return null;
        }
        return new RectF(self.left, self.top, self.right, self.bottom);
    }

    // 获取宽度
    public float getWidth() {
        return self.getRight() - self.getLeft();
    }

    // 获取高度
    public float getHeight() {
        return self.getBottom() - self.getTop();
    }

    /**
     * 获取LayoutParams
     * 此处默认容器是RelativeLayout的子类
     * @return 生成的LayoutParams
     */
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

    /**
     * 层级
     */
    private int hierarchy;
    public VDDrawingLayer setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
        return this;
    }
    public int getHierarchy() {
        return hierarchy;
    }

    /**
     * 图层类别
     */
    public enum LayerType {
        Unkonwn, Base, Image, Text;
    }
    private LayerType layerType;
    public VDDrawingLayer setLayerType(LayerType layerType) {
        this.layerType = layerType;
        return this;
    }
    public LayerType getLayerType() {
        if (layerType == null) {
            layerType = LayerType.Unkonwn;
        }
        return layerType;
    }

    /**
     * 背景色
     */
    private int backgroundColor;
    public VDDrawingLayer setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * 背景图片标识
     */
    private String backgroundImageIdentifier;
    public VDDrawingLayer setBackgroundImageIdentifier(String backgroundImageIdentifier) {
        this.backgroundImageIdentifier = backgroundImageIdentifier;
        return this;
    }
    public String getBackgroundImageIdentifier() {
        return backgroundImageIdentifier;
    }

    /**
     * text图层的内容
     */
    private String text;
    public VDDrawingLayer setText(String text) {
        this.text = text;
        return this;
    }
    public String getText() {
        return text;
    }

    /**
     * left坐标
     */
    private float left;
    public VDDrawingLayer setLeft(float left) {
        this.left = left;
        return this;
    }
    public float getLeft() {
        return left * self.getDrawingRatioX();
    }

    /**
     * top坐标
     */
    private float top;
    public VDDrawingLayer setTop(float top) {
        this.top = top;
        return this;
    }
    public float getTop() {
        return top * self.getDrawingRatioY();
    }

    /**
     * right坐标
     */
    private float right;
    public VDDrawingLayer setRight(float right) {
        this.right = right;
        return this;
    }
    public float getRight() {
        return right * self.getDrawingRatioX();
    }

    /**
     * bottom坐标
     */
    private float bottom;
    public VDDrawingLayer setBottom(float bottom) {
        this.bottom = bottom;
        return this;
    }
    public float getBottom() {
        return bottom * self.getDrawingRatioY();
    }

    /**
     * 图层缩放比例，xy同等
     */
    private float scale;
    public VDDrawingLayer setScale(float scale) {
        this.scale = scale;
        return this;
    }
    public float getScale() {
        return scale;
    }

    /**
     * 图层旋转角度
     */
    private float rotation;
    public VDDrawingLayer setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }
    public float getRotation() {
        return rotation;
    }

    /**
     * 当前X轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @VDJsonKeyIgnore
    private float drawingRatioX = 1.0f;
    public VDDrawingLayer setDrawingRatioX(float drawingRatioX) {
        this.drawingRatioX = drawingRatioX;
        return this;
    }
    public float getDrawingRatioX() {
        return drawingRatioX;
    }

    /**
     * 当前Y轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @VDJsonKeyIgnore
    private float drawingRatioY = 1.0f;
    public VDDrawingLayer setDrawingRatioY(float drawingRatioY) {
        this.drawingRatioY = drawingRatioY;
        return this;
    }
    public float getDrawingRatioY() {
        return drawingRatioY;
    }

    /** {@link Comparable<VDDrawingLayer>} */
    @Override
    public int compareTo(VDDrawingLayer another) {
        return self.getHierarchy() - another.getHierarchy();
    }
    /** {@link Comparable<VDDrawingLayer>} */

    /**
     * 初始化
     */
    private void init() {
        self.setHierarchy(UnsetValue);
        self.setBackgroundColor(UnsetValue);
        self.setLeft(UnsetValue);
        self.setTop(UnsetValue);
        self.setRight(UnsetValue);
        self.setBottom(UnsetValue);
        self.setScale(UnsetValue);
        self.setRotation(UnsetValue);
    }
}