package com.vilyever.drawingview.model;

import com.vilyever.jsonmodel.JsonModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DrawingPath
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 * 绘画数据：绘制路径
 * 由任意个{@link DrawingPoint}组成
 */
public class DrawingPath extends JsonModel {
    private final DrawingPath self = this;

    /* #Constructors */
    public DrawingPath() {
    }

    /* Public Methods */
    /**
     * 添加point
     * @param point 点
     * @return 是否添加成功
     */
    public boolean addPoint(DrawingPoint point) {
        // 不重复添加相同的point
        if (getPoints().size() > 0
                && getPoints().get(getPoints().size() - 1).isSamePoint(point)) {
            return false;
        }
        else {
            getPoints().add(point);
            point.setDrawingRatioX(getDrawingRatioX());
            point.setDrawingRatioY(getDrawingRatioY());
            return true;
        }
    }

    /* Properties */
    @JsonKey("P")
    private List<DrawingPoint> points;
    public List<DrawingPoint> getPoints() {
        if (this.points == null) {
            this.points = new ArrayList<>();
        }
        return this.points;
    }

    /**
     * 当前X轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @JsonKeyIgnore
    private float drawingRatioX = 1.0f;
    public DrawingPath setDrawingRatioX(float drawingRatioX) {
        this.drawingRatioX = drawingRatioX;
        for (DrawingPoint point : getPoints()) {
            point.setDrawingRatioX(drawingRatioX);
        }
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
    public DrawingPath setDrawingRatioY(float drawingRatioY) {
        this.drawingRatioY = drawingRatioY;
        for (DrawingPoint point : getPoints()) {
            point.setDrawingRatioY(drawingRatioY);
        }
        return this;
    }
    public float getDrawingRatioY() {
        return this.drawingRatioY;
    }
}