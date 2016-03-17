package com.vilyever.drawingview.model;

import android.graphics.RectF;
import android.widget.RelativeLayout;

import com.vilyever.jsonmodel.JsonModel;

/**
 * DrawingLayer
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 * 绘画数据：图层数据
 * 每一层并不限制此类数据的数量
 */
public class DrawingLayer extends JsonModel implements Comparable<DrawingLayer> {
    private final DrawingLayer self = this;

    // 未设定的值
    public static final int UnsetValue = -1;

    /* #Constructors */
    public DrawingLayer() {
        this(UnsetValue);
    }

    public DrawingLayer(int hierarchy) {
        this(hierarchy, null);
    }

    public DrawingLayer(int hierarchy, RectF frame) {
        // 预先初始化，可能影响到一些变量，故先操作
        init();

        this.hierarchy = hierarchy;
        this.layerType = LayerType.LayerDrawing;
        setFrame(frame);
    }

    /* Public Methods */
    // left，top，right，bottom快捷设置
    public void setFrame(RectF frame) {
        if (frame == null) {
            setLeft(UnsetValue);
            setTop(UnsetValue);
            setRight(UnsetValue);
            setBottom(UnsetValue);
            return;
        }
        setLeft(frame.left);
        setTop(frame.top);
        setRight(frame.right);
        setBottom(frame.bottom);
    }

    // left，top，right，bottom快捷获取
    public RectF getFrame() {
        if (getLeft() == UnsetValue) {
            return null;
        }
        return new RectF(getLeft(), getTop(), getRight(), getBottom());
    }

    // 获取宽度
    public float getWidth() {
        return getRight() - getLeft();
    }

    // 获取高度
    public float getHeight() {
        return getBottom() - getTop();
    }

    /**
     * 获取LayoutParams
     * 此处默认容器是RelativeLayout的子类
     * @return 生成的LayoutParams
     */
    public RelativeLayout.LayoutParams getLayoutParams() {
        if (getLeft() == UnsetValue) {
            return null;
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Math.floor(getWidth()),
                (int) Math.floor(getHeight()));
        layoutParams.leftMargin = (int) Math.floor(getLeft());
        layoutParams.topMargin = (int) Math.floor(getTop());
        layoutParams.rightMargin = -Integer.MAX_VALUE;
        layoutParams.bottomMargin = -Integer.MAX_VALUE;

        return layoutParams;
    }

    /* Properties */
    /**
     * 层级
     */
    private int hierarchy;
    public DrawingLayer setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
        return this;
    }
    public int getHierarchy() {
        return this.hierarchy;
    }

    /**
     * 图层类别
     */
    public enum LayerType {
        Unknown, BaseDrawing, BaseText, LayerDrawing, LayerText;
    }
    private LayerType layerType;
    public DrawingLayer setLayerType(LayerType layerType) {
        this.layerType = layerType;
        return this;
    }
    public LayerType getLayerType() {
        if (this.layerType == null) {
            this.layerType = LayerType.Unknown;
        }
        return this.layerType;
    }

    /**
     * 背景色
     */
    private int backgroundColor;
    public DrawingLayer setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }
    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    /**
     * 背景图片标识
     */
    private String backgroundImageIdentifier;
    public DrawingLayer setBackgroundImageIdentifier(String backgroundImageIdentifier) {
        this.backgroundImageIdentifier = backgroundImageIdentifier;
        return this;
    }
    public String getBackgroundImageIdentifier() {
        return this.backgroundImageIdentifier;
    }

    /**
     * text图层的内容
     */
    private String text;
    public DrawingLayer setText(String text) {
        this.text = text;
        return this;
    }
    public String getText() {
        return this.text;
    }

    /**
     * left坐标
     */
    private float left;
    public DrawingLayer setLeft(float left) {
        this.left = left;
        return this;
    }
    public float getLeft() {
        return this.left * getDrawingRatioX();
    }

    /**
     * top坐标
     */
    private float top;
    public DrawingLayer setTop(float top) {
        this.top = top;
        return this;
    }
    public float getTop() {
        return this.top * getDrawingRatioY();
    }

    /**
     * right坐标
     */
    private float right;
    public DrawingLayer setRight(float right) {
        this.right = right;
        return this;
    }
    public float getRight() {
        return this.right * getDrawingRatioX();
    }

    /**
     * bottom坐标
     */
    private float bottom;
    public DrawingLayer setBottom(float bottom) {
        this.bottom = bottom;
        return this;
    }
    public float getBottom() {
        return this.bottom * getDrawingRatioY();
    }

    /**
     * 图层缩放比例，xy同等
     */
    private float scale;
    public DrawingLayer setScale(float scale) {
        this.scale = scale;
        return this;
    }
    public float getScale() {
        return this.scale;
    }

    /**
     * 图层旋转角度
     */
    private float rotation;
    public DrawingLayer setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }
    public float getRotation() {
        return this.rotation;
    }

    /**
     * 当前X轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @JsonKeyIgnore
    private float drawingRatioX = 1.0f;
    public DrawingLayer setDrawingRatioX(float drawingRatioX) {
        this.drawingRatioX = drawingRatioX;
        return this;
    }
    public float getDrawingRatioX() {
        return this.drawingRatioX;
    }

    /**
     * 当前Y轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @JsonKeyIgnore
    private float drawingRatioY = 1.0f;
    public DrawingLayer setDrawingRatioY(float drawingRatioY) {
        this.drawingRatioY = drawingRatioY;
        return this;
    }
    public float getDrawingRatioY() {
        return this.drawingRatioY;
    }

    /* Delegates */
    /** {@link Comparable< DrawingLayer >} */
    @Override
    public int compareTo(DrawingLayer another) {
        return getHierarchy() - another.getHierarchy();
    }
    /** {@link Comparable< DrawingLayer >} */

    /* Private Methods */
    /**
     * 初始化
     */
    private void init() {
        setHierarchy(UnsetValue);
        setBackgroundColor(UnsetValue);
        setLeft(UnsetValue);
        setTop(UnsetValue);
        setRight(UnsetValue);
        setBottom(UnsetValue);
        setScale(UnsetValue);
        setRotation(UnsetValue);
    }
}