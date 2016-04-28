package com.vilyever.drawingview.model;

import com.vilyever.jsonmodel.JsonModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DrawingData
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/25.
 * Feature:
 */
public class DrawingData extends JsonModel {
    private final DrawingData self = this;

    /**
     * 下一step在当前最大step之后的标识
     */
    public static final int NextLayerHierarchy = -1;

    /* Public Methods */
    /**
     * 生成作用在下一图层的下一step
     * @param layerType 图层type
     * @param drawingViewWidth 当前drawingView宽
     * @param drawingViewHeight 当前drawingView高
     * @return 生成的step
     */
    public DrawingStep newDrawingStepOnNextLayer(DrawingLayer.LayerType layerType, int drawingViewWidth, int drawingViewHeight) {
        return newDrawingStepOnLayer(NextLayerHierarchy, layerType, drawingViewWidth, drawingViewHeight);
    }

    /**
     * 生成作用在BASE图层的下一绘图step
     * @param drawingViewWidth 当前drawingView宽
     * @param drawingViewHeight 当前drawingView高
     * @return 生成的step
     */
    public DrawingStep newDrawingStepOnBaseLayer(int drawingViewWidth, int drawingViewHeight) {
        return newDrawingStepOnLayer(0, DrawingLayer.LayerType.BaseDrawing, drawingViewWidth, drawingViewHeight);
    }

    /**
     * 生成作用在BASE图层的下一文本step
     * @param drawingViewWidth 当前drawingView宽
     * @param drawingViewHeight 当前drawingView高
     * @return 生成的step
     */
    public DrawingStep newTextStepOnBaseLayer(int drawingViewWidth, int drawingViewHeight) {
        return newDrawingStepOnLayer(0, DrawingLayer.LayerType.BaseText, drawingViewWidth, drawingViewHeight);
    }

    /**
     * 生成作用在指定图层的下一step
     * @param layerHierarchy 层级
     * @param layerType 图层type
     * @param drawingViewWidth 当前drawingView宽
     * @param drawingViewHeight 当前drawingView高
     * @return 生成的step
     */
    public DrawingStep newDrawingStepOnLayer(int layerHierarchy, DrawingLayer.LayerType layerType, int drawingViewWidth, int drawingViewHeight) {
        int step = 0;
        if (getSteps().size() > 0) {
            step = getSteps().get(getSteps().size() - 1).getStep() + 1;
        }

        if (layerHierarchy == NextLayerHierarchy) {
            setTopLayerHierarchy(getTopLayerHierarchy() + 1);
            layerHierarchy = getTopLayerHierarchy();
        }
        else {
            setTopLayerHierarchy(Math.max(layerHierarchy, getTopLayerHierarchy()));
        }

        DrawingStep drawingStep = new DrawingStep(step, layerHierarchy, layerType, drawingViewWidth, drawingViewHeight);
        addDrawingStep(drawingStep);

        return drawingStep;
    }

    /**
     * 添加step
     * @param drawingStep 添加的step
     */
    public void addDrawingStep(DrawingStep drawingStep) {
        if (getShowingStepIndex() != getSteps().size() - 1) {
            removeSteps(getShowingStepIndex() + 1, getSteps().size());
        }
        setShowingStepIndex(getShowingStepIndex() + 1);

        getSteps().add(drawingStep);
        if (drawingStep.isClearStep()) {
            setTopLayerHierarchy(0);
        }
    }

    /**
     * 撤销移除当前step
     */
    public void cancelDrawingStep() {
        if (getDrawingStep().isStepOver()) {
            return;
        }

        if (getDrawingStep().getDrawingLayer().getHierarchy() > 0
                && getDrawingStep().getStepType() == DrawingStep.StepType.CreateLayer) {
            setTopLayerHierarchy(getTopLayerHierarchy() - 1);
        }
        getSteps().remove(getDrawingStep());
        setShowingStepIndex(getShowingStepIndex() - 1);
    }

    public void replaceDrawingStep(DrawingStep step) {
        getSteps().set(getShowingStepIndex(), step);
    }

    /**
     * 获取当前正在绘制或刚刚绘制完成的step
     * @return 找到的step
     */
    public DrawingStep getDrawingStep() {
        if (getSteps().size() > getShowingStepIndex()
                && getShowingStepIndex() >= 0) {
            return getSteps().get(getShowingStepIndex());
        }
        return null;
    }

    /**
     * 获取重绘所需的最小step集，即最后一次clear之前的绘制都无需重绘
     * @return 重绘step集
     */
    public List<DrawingStep> getStepsToDraw() {
        int lastClearedIndex = -1;
        for (int i = 0; i <= getShowingStepIndex(); i++) {
            if (getSteps().get(i).isClearStep()) lastClearedIndex = i;
        }

        if (lastClearedIndex == getShowingStepIndex()) {
            return new ArrayList<>();
        }
        else {
            List<DrawingStep> stepsToDraw = getSteps().subList(lastClearedIndex + 1, getShowingStepIndex() + 1);
            return stepsToDraw;
        }
    }

    /**
     * 移除step
     * @param from include
     * @param to exclude
     */
    public void removeSteps(int from, int to) {
        getSteps().subList(from, to).clear();
    }

    /**
     * 当前是否可以撤销
     * @return 可否撤销
     */
    public boolean canUndo() {
        return getShowingStepIndex() > 0;
    }

    /**
     * 当前是否可以重做
     * @return 可否重做
     */
    public boolean canRedo() {
        return getShowingStepIndex() < getSteps().size() - 1;
    }

    /**
     * 撤销一步step
     * @return 撤销是否成功
     */
    public boolean undo() {
        if (canUndo()) {
            if (!getDrawingStep().isStepOver()) {
                cancelDrawingStep();
            }
            else {
                setShowingStepIndex(getShowingStepIndex() - 1);
            }
            return true;
        }
        return false;
    }

    /**
     * 重做一步step
     * @return 重做是否成功
     */
    public boolean redo() {
        if (canRedo()) {
            setShowingStepIndex(getShowingStepIndex() + 1);
            return true;
        }
        return false;
    }

    /* Properties */
    /**
     * 存储所有step
     */
    private List<DrawingStep> steps;
    public List<DrawingStep> getSteps() {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        return this.steps;
    }

    /**
     * 当前最大层级
     */
    @JsonKey("TLH")
    private int topLayerHierarchy = -1;
    private DrawingData setTopLayerHierarchy(int topLayerHierarchy) {
        this.topLayerHierarchy = topLayerHierarchy;
        return this;
    }
    public int getTopLayerHierarchy() {
        return this.topLayerHierarchy;
    }

    /**
     * 当前展示的最大step游标
     * 用于撤销重做
     */
    @JsonKey("SSI")
    private int showingStepIndex = -1;
    private DrawingData setShowingStepIndex(int showingStepIndex) {
        this.showingStepIndex = showingStepIndex;
        return this;
    }
    public int getShowingStepIndex() {
        return this.showingStepIndex;
    }
}