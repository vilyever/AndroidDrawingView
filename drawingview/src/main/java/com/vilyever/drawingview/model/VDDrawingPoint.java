package com.vilyever.drawingview.model;

import com.vilyever.jsonmodel.VDModel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * VDDrawingPoint
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/16.
 * Feature:
 * 绘画数据：绘制点
 */
public class VDDrawingPoint extends VDModel {
    private final VDDrawingPoint self = this;


    /* #Constructors */
    public VDDrawingPoint() {
    }

    public VDDrawingPoint(float x, float y) {
        this(VDDrawingPoint.CurrentPointerID(), x, y);
    }

    public VDDrawingPoint(int pointerID, float x, float y) {
        this.pointerID = pointerID;
        this.x = x;
        this.y = y;
        this.moment = System.currentTimeMillis();
    }

    /* Public Methods */
    /**
     * 是否是相同的point
     * @param point 比较的point
     * @return 是否相同
     */
    public boolean isSamePoint(VDDrawingPoint point) {
        return this.pointerID == point.pointerID && this.x == point.x && this.y == point.y;
    }

    /**
     * 复制point
     * @param point 复制的原point
     * @return 复制的point
     */
    public static VDDrawingPoint copy(VDDrawingPoint point) {
        return new VDDrawingPoint(point.pointerID, point.x, point.y);
    }

    /* Properties */
    /**
     * 当前point的ID
     * 每一轮touch事件（即从开始触摸Action_Down到结束触摸Action_Up、Action_Cancel）与其他轮的ID不同
     * 每一轮touch事件内的每一点ID都相同
     */
    public int pointerID;

    // x，y坐标
    private float x;
    public float getX() {
        return x * self.getDrawingRatioX();
    }
    private float y;
    public float getY() {
        return y * self.getDrawingRatioY();
    }

    // 触摸事件发生的时间
    public long moment;

    /**
     * 当前X轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @VDJsonKeyIgnore
    private float drawingRatioX = 1.0f;
    public VDDrawingPoint setDrawingRatioX(float drawingRatioX) {
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
    public VDDrawingPoint setDrawingRatioY(float drawingRatioY) {
        this.drawingRatioY = drawingRatioY;
        return this;
    }
    public float getDrawingRatioY() {
        return drawingRatioY;
    }

    // ID产生，每次开始触摸时自增
    private static final AtomicInteger atomicInteger = new AtomicInteger();
    public static int CurrentPointerID() {
        return atomicInteger.get();
    }
    public static int IncrementPointerID() {
        return atomicInteger.incrementAndGet();
    }
}