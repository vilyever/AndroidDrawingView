package com.vilyever.drawingview.model;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingData
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/25.
 * Feature:
 */
public class VDDrawingData {
    private final VDDrawingData self = this;

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
    public VDDrawingStep newDrawingStepOnNextLayer(VDDrawingLayer.LayerType layerType, int drawingViewWidth, int drawingViewHeight) {
        return newDrawingStepOnLayer(NextLayerHierarchy, layerType, drawingViewWidth, drawingViewHeight);
    }

    /**
     * 生成作用在BASE图层的下一step
     * @param drawingViewWidth 当前drawingView宽
     * @param drawingViewHeight 当前drawingView高
     * @return 生成的step
     */
    public VDDrawingStep newDrawingStepOnBaseLayer(int drawingViewWidth, int drawingViewHeight) {
        return newDrawingStepOnLayer(0, VDDrawingLayer.LayerType.Base, drawingViewWidth, drawingViewHeight);
    }

    /**
     * 生成作用在指定图层的下一step
     * @param layerHierarchy 层级
     * @param layerType 图层type
     * @param drawingViewWidth 当前drawingView宽
     * @param drawingViewHeight 当前drawingView高
     * @return 生成的step
     */
    public VDDrawingStep newDrawingStepOnLayer(int layerHierarchy, VDDrawingLayer.LayerType layerType, int drawingViewWidth, int drawingViewHeight) {
        int step = 0;
        if (self.getSteps().size() > 0) {
            step = self.getSteps().get(self.getSteps().size() - 1).getStep() + 1;
        }

        if (layerHierarchy == NextLayerHierarchy) {
            self.setTopLayerHierarchy(self.getTopLayerHierarchy() + 1);
            layerHierarchy = self.getTopLayerHierarchy();
        }
        else {
            self.setTopLayerHierarchy(Math.max(layerHierarchy, self.getTopLayerHierarchy()));
        }

        VDDrawingStep drawingStep = new VDDrawingStep(step, layerHierarchy, layerType, drawingViewWidth, drawingViewHeight);
        self.addDrawingStep(drawingStep);

        return drawingStep;
    }

    /**
     * 添加step
     * @param drawingStep 添加的step
     */
    public void addDrawingStep(VDDrawingStep drawingStep) {
        if (self.getShowingStepIndex() != self.getSteps().size() - 1) {
            self.removeSteps(self.getShowingStepIndex() + 1, self.getSteps().size());
        }
        self.setShowingStepIndex(self.getShowingStepIndex() + 1);

        self.getSteps().add(drawingStep);
        if (drawingStep.isClearStep()) {
            self.setTopLayerHierarchy(0);
        }
    }

    /**
     * 撤销移除当前step
     */
    public void cancelDrawingStep() {
        if (self.getDrawingStep().isStepOver()) {
            return;
        }

        if (self.getDrawingStep().getDrawingLayer().getHierarchy() > 0) {
            self.setTopLayerHierarchy(self.getTopLayerHierarchy() - 1);
        }
        self.getSteps().remove(self.getDrawingStep());
        self.setShowingStepIndex(self.getShowingStepIndex() - 1);
    }

    /**
     * 获取当前正在绘制或刚刚绘制完成的step
     * @return 找到的step
     */
    public VDDrawingStep getDrawingStep() {
        if (self.getSteps().size() > self.getShowingStepIndex()
                && self.getShowingStepIndex() >= 0) {
            return self.getSteps().get(self.getShowingStepIndex());
        }
        return null;
    }

    /**
     * 获取重绘所需的最小step集，即最后一次clear之前的绘制都无需重绘
     * @return 重绘step集
     */
    public List<VDDrawingStep> getStepsToDraw() {
        int lastClearedIndex = -1;
        for (int i = 0; i <= self.getShowingStepIndex(); i++) {
            if (self.getSteps().get(i).isClearStep()) lastClearedIndex = i;
        }

        if (lastClearedIndex == self.getShowingStepIndex()) {
            return new ArrayList<>();
        }
        else {
            List<VDDrawingStep> stepsToDraw = self.getSteps().subList(lastClearedIndex + 1, self.getShowingStepIndex() + 1);
            return stepsToDraw;
        }
    }

    /**
     * 移除step
     * @param from include
     * @param to exclude
     */
    public void removeSteps(int from, int to) {
        self.getSteps().subList(from, to).clear();
    }

    /**
     * 当前是否可以撤销
     * @return 可否撤销
     */
    public boolean canUndo() {
        return self.getShowingStepIndex() > 0;
    }

    /**
     * 当前是否可以重做
     * @return 可否重做
     */
    public boolean canRedo() {
        return self.getShowingStepIndex() < self.getSteps().size() - 1;
    }

    /**
     * 撤销一步step
     * @return 撤销是否成功
     */
    public boolean undo() {
        if (self.canUndo()) {
            if (!self.getDrawingStep().isStepOver()) {
                self.cancelDrawingStep();
            }
            else {
                self.setShowingStepIndex(self.getShowingStepIndex() - 1);
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
        if (self.canRedo()) {
            self.setShowingStepIndex(self.getShowingStepIndex() + 1);
            return true;
        }
        return false;
    }

    /* Properties */
    /**
     * 存储所有step
     */
    private List<VDDrawingStep> steps;
    public List<VDDrawingStep> getSteps() {
        if (steps == null) {
            steps = new ArrayList<>();
        }
        return steps;
    }

    /**
     * 当前最大层级
     */
    private int topLayerHierarchy = -1;
    private VDDrawingData setTopLayerHierarchy(int topLayerHierarchy) {
        this.topLayerHierarchy = topLayerHierarchy;
        return this;
    }
    public int getTopLayerHierarchy() {
        return topLayerHierarchy;
    }

    /**
     * 当前展示的最大step游标
     * 用于撤销重做
     */
    private int showingStepIndex = -1;
    private VDDrawingData setShowingStepIndex(int showingStepIndex) {
        this.showingStepIndex = showingStepIndex;
        return this;
    }
    public int getShowingStepIndex() {
        return showingStepIndex;
    }
}