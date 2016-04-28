package com.vilyever.drawingview.model;

import android.support.annotation.NonNull;

import com.vilyever.drawingview.brush.Brush;
import com.vilyever.drawingview.layer.DrawingLayerViewProtocol;
import com.vilyever.jsonmodel.Json;
import com.vilyever.jsonmodel.JsonModel;

import java.lang.ref.WeakReference;

/**
 * DrawingStep
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class DrawingStep extends JsonModel {
    private final DrawingStep self = this;

    /* Constructors */
    public DrawingStep() {
    }

    public DrawingStep(int step, int onLayerHierarchy, DrawingLayer.LayerType layerType, int drawingViewWidth, int drawingViewHeight) {
        this.step = step;
        newDrawingLayer(onLayerHierarchy, layerType);
        setDrawingViewWidth(drawingViewWidth);
        setDrawingViewHeight(drawingViewHeight);
    }

    /* Public Methods */
    /**
     * 复制step
     * 同步时不应传出原始step，以免被外部修改
     * @param step 原step
     * @return 复制的step
     */
    public static DrawingStep copy(@NonNull DrawingStep step) {
        return new Json<>(step.getClass()).modelFromJson(step.toJson());
    }

    /**
     * 复制step
     * @return 复制的step
     */
    public DrawingStep copy() {
        return new Json<>(this.getClass()).modelFromJson(this.toJson());
    }

    /**
     * 在重绘前调用此方法设置绘制比例
     * 在不同尺寸的view上绘制时将尽量呈现出类似的图形
     * @param currentDrawingViewWidth 当前绘制view的宽
     * @param currentDrawingViewHeight 当前绘制view的高
     */
    public void updateDrawingRatio(int currentDrawingViewWidth, int currentDrawingViewHeight) {
        float ratioX = (float) currentDrawingViewWidth / (float) getDrawingViewWidth();
        float ratioY = (float) currentDrawingViewHeight / (float) getDrawingViewHeight();
        if (getDrawingLayer() != null) {
            getDrawingLayer().setDrawingRatioX(ratioX);
            getDrawingLayer().setDrawingRatioY(ratioY);
        }
        if (getDrawingPath() != null) {
            getDrawingPath().setDrawingRatioX(ratioX);
            getDrawingPath().setDrawingRatioY(ratioY);
        }
        if (getBrush() != null) {
            getBrush().setDrawingRatio(Math.max(ratioX, ratioY));
        }
    }

    /**
     * 判断当前step是否是clear step的快捷方法
     * @return 是否是clear step
     */
    public boolean isClearStep() {
        return getStepType() == StepType.Clear;
    }

    /* Properties */
    /**
     * 当前step的序号
     * drawingView在重绘时会按此序号顺序绘制
     */
    @JsonKey("S")
    private int step;
    public DrawingStep setStep(int step) {
        this.step = step;
        return this;
    }
    public int getStep() {
        return this.step;
    }

    /**
     * step的类型
     */
    public enum StepType {
        Clear, DrawOnBase, DrawTextOnBase, Background, CreateLayer, Transform, TextChange, DeleteLayer;
    }
    @JsonKey("ST")
    private StepType stepType;
    public DrawingStep setStepType(StepType stepType) {
        this.stepType = stepType;
        return this;
    }
    public StepType getStepType() {
        return this.stepType;
    }

    /**
     * 当前绘制的状态，用于brush绘制
     */
    @JsonKey("DS")
    private Brush.DrawingState drawingState;
    public DrawingStep setDrawingState(Brush.DrawingState drawingState) {
        this.drawingState = drawingState;
        return this;
    }
    public Brush.DrawingState getDrawingState() {
        return this.drawingState;
    }

    /**
     * 当前step是否是完成的step
     * 某些step需要多次绘制才能完成
     */
    @JsonKey("SO")
    private boolean stepOver;
    public DrawingStep setStepOver(boolean stepOver) {
        this.stepOver = stepOver;
        return this;
    }
    public boolean isStepOver() {
        return this.stepOver;
    }

    /**
     * 当前step是否处于cancel状态
     * 用于远程同步
     */
    @JsonKey("C")
    private boolean canceled;
    public DrawingStep setCanceled(boolean canceled) {
        this.canceled = canceled;
        return this;
    }
    public boolean isCanceled() {
        return this.canceled;
    }

    @JsonKey("R")
    private boolean remote;
    public DrawingStep setRemote(boolean remote) {
        this.remote = remote;
        return this;
    }
    public boolean isRemote() {
        return this.remote;
    }

    /**
     * 当前step绘制用的brush
     * 此brush的设置理应使用copy的brush来设置，避免被其他位置的修改影响到
     */
    @JsonKey("BR")
    private Brush brush;
    public DrawingStep setBrush(Brush brush) {
        this.brush = brush;
        return this;
    }
    public <T extends Brush> T getBrush() {
        return (T) this.brush;
    }

    /**
     * 当前step作用的图层
     */
    @JsonKey("DL")
    private DrawingLayer drawingLayer;
    private DrawingStep setDrawingLayer(DrawingLayer drawingLayer) {
        this.drawingLayer = drawingLayer;
        return this;
    }
    public DrawingLayer getDrawingLayer() {
        return this.drawingLayer;
    }

    /**
     * 当前step记录的绘制路径
     */
    @JsonKey("DP")
    private DrawingPath drawingPath;
    private DrawingStep setDrawingPath(DrawingPath drawingPath) {
        this.drawingPath = drawingPath;
        return this;
    }
    public DrawingPath getDrawingPath() {
        if (this.drawingPath == null) {
            this.drawingPath = new DrawingPath();
        }
        return this.drawingPath;
    }

    /**
     * 当前step绘制时drawingView的宽度
     * 用于在不同分辨率下重绘时的路径偏移修正
     * 必须在step初始化后设置
     */
    @JsonKey("DVW")
    private int drawingViewWidth;
    public DrawingStep setDrawingViewWidth(int drawingViewWidth) {
        this.drawingViewWidth = drawingViewWidth;
        return this;
    }
    public int getDrawingViewWidth() {
        return this.drawingViewWidth;
    }

    /**
     * 当前step绘制时drawingView的高度
     * 用于在不同分辨率下重绘时的路径偏移修正
     * 必须在step初始化后设置
     */
    @JsonKey("DVH")
    private int drawingViewHeight;
    public DrawingStep setDrawingViewHeight(int drawingViewHeight) {
        this.drawingViewHeight = drawingViewHeight;
        return this;
    }
    public int getDrawingViewHeight() {
        return this.drawingViewHeight;
    }

    @JsonKeyIgnore
    private WeakReference<DrawingLayerViewProtocol> handlingLayer;
    public DrawingStep setHandlingLayer(DrawingLayerViewProtocol handlingLayer) {
        this.handlingLayer = new WeakReference<DrawingLayerViewProtocol>(handlingLayer);
        return this;
    }
    public DrawingLayerViewProtocol getHandlingLayer() {
        return this.handlingLayer == null ? null : this.handlingLayer.get();
    }

    /* Private Methods */
    /**
     * 设置当前step是在哪一层级，哪种图层类型绘制
     * 默认在初始化后如果该step是与图层有关的应当调用此方法一次且仅一次
     * @param hierarchy 层级
     * @param layerType 图层类型
     * @return 生成的图层layer
     */
    private DrawingLayer newDrawingLayer(int hierarchy, DrawingLayer.LayerType layerType) {
        if (getDrawingLayer() == null) {
            setDrawingLayer(new DrawingLayer(hierarchy));
            getDrawingLayer().setLayerType(layerType);
        }
        return getDrawingLayer();
    }
}