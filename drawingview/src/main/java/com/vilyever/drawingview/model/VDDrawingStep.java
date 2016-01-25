package com.vilyever.drawingview.model;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.jsonmodel.VDModel;

/**
 * VDDrawingStep
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingStep extends VDModel {
    private final VDDrawingStep self = this;

    /* #Constructors */
    public VDDrawingStep() {
    }

    public VDDrawingStep(int step, int onLayerHierarchy, VDDrawingLayer.LayerType layerType, int drawingViewWidth, int drawingViewHeight) {
        this.step = step;
        newDrawingLayer(onLayerHierarchy, layerType);
        setDrawingViewWidth(drawingViewWidth);
        setDrawingViewHeight(drawingViewHeight);
    }

    /* Public Methods */
    /**
     * 在重绘前调用此方法设置绘制比例
     * 在不同尺寸的view上绘制时将尽量呈现出类似的图形
     * @param currentDrawingViewWidth 当前绘制view的宽
     * @param currentDrawingViewHeight 当前绘制view的高
     */
    public void updateDrawingRatio(int currentDrawingViewWidth, int currentDrawingViewHeight) {
        float ratioX = (float) currentDrawingViewWidth / (float) self.getDrawingViewWidth();
        float ratioY = (float) currentDrawingViewHeight / (float) self.getDrawingViewHeight();
        if (self.getDrawingLayer() != null) {
            self.getDrawingLayer().setDrawingRatioX(ratioX);
            self.getDrawingLayer().setDrawingRatioY(ratioY);
        }
        if (self.getDrawingPath() != null) {
            self.getDrawingPath().setDrawingRatioX(ratioX);
            self.getDrawingPath().setDrawingRatioY(ratioY);
        }
        if (self.getBrush() != null) {
            self.getBrush().setDrawingRatio(Math.max(ratioX, ratioY));
        }
    }

    /**
     * 判断当前step是否是clear step的快捷方法
     * @return 是否是clear step
     */
    public boolean isClearStep() {
        return self.getStepType() == StepType.Clear;
    }

    /* Properties */
    /**
     * 当前step的序号
     * drawingView在重绘时会按此序号顺序绘制
     */
    private int step;
    public VDDrawingStep setStep(int step) {
        this.step = step;
        return this;
    }
    public int getStep() {
        return step;
    }

    /**
     * step的类型
     */
    public enum StepType {
        Clear, DrawOnBase, Background, CreateLayer, Transform, TextChange, DeleteLayer;
    }
    private StepType stepType;
    public VDDrawingStep setStepType(StepType stepType) {
        this.stepType = stepType;
        return this;
    }
    public StepType getStepType() {
        return stepType;
    }

    /**
     * 当前绘制的状态，用于brush绘制
     */
    private VDBrush.DrawingState drawingState;
    public VDDrawingStep setDrawingState(VDBrush.DrawingState drawingState) {
        this.drawingState = drawingState;
        return this;
    }
    public VDBrush.DrawingState getDrawingState() {
        return drawingState;
    }

    /**
     * 当前step是否是完成的step
     * 某些step需要多次绘制才能完成
     */
    private boolean stepOver;
    public VDDrawingStep setStepOver(boolean stepOver) {
        this.stepOver = stepOver;
        return this;
    }
    public boolean isStepOver() {
        return stepOver;
    }

    /**
     * 当前step是否处于cancel状态
     * 用于远程同步
     */
    private boolean canceled;
    public VDDrawingStep setCanceled(boolean canceled) {
        this.canceled = canceled;
        return this;
    }
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * 当前step绘制用的brush
     * 此brush的设置理应使用copy的brush来设置，避免被其他位置的修改影响到
     */
    private VDBrush brush;
    public VDDrawingStep setBrush(VDBrush brush) {
        this.brush = brush;
        return this;
    }
    public <T extends VDBrush> T getBrush() {
        return (T) brush;
    }

    /**
     * 当前step作用的图层
     */
    private VDDrawingLayer drawingLayer;
    private VDDrawingStep setDrawingLayer(VDDrawingLayer drawingLayer) {
        this.drawingLayer = drawingLayer;
        return this;
    }
    public VDDrawingLayer getDrawingLayer() {
        return drawingLayer;
    }

    /**
     * 当前step记录的绘制路径
     */
    private VDDrawingPath drawingPath;
    private VDDrawingStep setDrawingPath(VDDrawingPath drawingPath) {
        this.drawingPath = drawingPath;
        return this;
    }
    public VDDrawingPath getDrawingPath() {
        if (drawingPath == null) {
            drawingPath = new VDDrawingPath();
        }
        return drawingPath;
    }

    /**
     * 当前step绘制时drawingView的宽度
     * 用于在不同分辨率下重绘时的路径偏移修正
     * 必须在step初始化后设置
     */
    private int drawingViewWidth;
    public VDDrawingStep setDrawingViewWidth(int drawingViewWidth) {
        this.drawingViewWidth = drawingViewWidth;
        return this;
    }
    public int getDrawingViewWidth() {
        return drawingViewWidth;
    }

    /**
     * 当前step绘制时drawingView的高度
     * 用于在不同分辨率下重绘时的路径偏移修正
     * 必须在step初始化后设置
     */
    private int drawingViewHeight;
    public VDDrawingStep setDrawingViewHeight(int drawingViewHeight) {
        this.drawingViewHeight = drawingViewHeight;
        return this;
    }
    public int getDrawingViewHeight() {
        return drawingViewHeight;
    }

    /* Private Methods */
    /**
     * 设置当前step是在哪一层级，哪种图层类型绘制
     * 默认在初始化后如果该step是与图层有关的应当调用此方法一次且仅一次
     * @param hierarchy 层级
     * @param layerType 图层类型
     * @return 生成的图层layer
     */
    private VDDrawingLayer newDrawingLayer(int hierarchy, VDDrawingLayer.LayerType layerType) {
        if (self.getDrawingLayer() == null) {
            self.setDrawingLayer(new VDDrawingLayer(hierarchy));
            self.getDrawingLayer().setLayerType(layerType);
        }
        return self.getDrawingLayer();
    }
}