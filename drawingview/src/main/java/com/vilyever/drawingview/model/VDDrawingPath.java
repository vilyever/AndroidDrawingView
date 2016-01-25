package com.vilyever.drawingview.model;

import com.vilyever.jsonmodel.VDModel;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingPath
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 * 绘画数据：绘制路径
 * 由任意个{@link VDDrawingPoint}组成
 */
public class VDDrawingPath extends VDModel {
    private final VDDrawingPath self = this;

    /* #Constructors */
    public VDDrawingPath() {
    }

    /* Public Methods */
    /**
     * 添加point
     * @param point 点
     * @return 是否添加成功
     */
    public boolean addPoint(VDDrawingPoint point) {
        // 不重复添加相同的point
        if (self.getPoints().size() > 0
                && self.getPoints().get(self.getPoints().size() - 1).isSamePoint(point)) {
            return false;
        }
        else {
            self.getPoints().add(point);
            point.setDrawingRatioX(self.getDrawingRatioX());
            point.setDrawingRatioY(self.getDrawingRatioY());
            return true;
        }
    }

    /* Properties */
    private List<VDDrawingPoint> points;
    public List<VDDrawingPoint> getPoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return points;
    }

    /**
     * 当前X轴绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     */
    @VDJsonKeyIgnore
    private float drawingRatioX = 1.0f;
    public VDDrawingPath setDrawingRatioX(float drawingRatioX) {
        this.drawingRatioX = drawingRatioX;
        for (VDDrawingPoint point : self.getPoints()) {
            point.setDrawingRatioX(drawingRatioX);
        }
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
    public VDDrawingPath setDrawingRatioY(float drawingRatioY) {
        this.drawingRatioY = drawingRatioY;
        for (VDDrawingPoint point : self.getPoints()) {
            point.setDrawingRatioY(drawingRatioY);
        }
        return this;
    }
    public float getDrawingRatioY() {
        return drawingRatioY;
    }
}