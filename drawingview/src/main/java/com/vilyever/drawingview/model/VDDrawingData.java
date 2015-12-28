package com.vilyever.drawingview.model;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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

    public static final int NextLayerHierarchy = -1;

    private List<VDDrawingStep> steps = new ArrayList<>();

    private int topLayerHierarchy = -1;

    private int showingStepIndex = -1;

    /* #Constructors */

    /* #Overrides */    
    
    /* #Accessors */
    public List<VDDrawingStep> getSteps() {
        return steps;
    }

    public int getTopLayerHierarchy() {
        return topLayerHierarchy;
    }

    public int getShowingStepIndex() {
        return showingStepIndex;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public VDDrawingStep newDrawingStepOnNextLayer(VDDrawingLayer.LayerType layerType) {
        return newDrawingStepOnLayer(NextLayerHierarchy, layerType);
    }

    public VDDrawingStep newDrawingStepOnBaseLayer() {
        return newDrawingStepOnLayer(0, VDDrawingLayer.LayerType.Base);
    }

    public VDDrawingStep newDrawingStepOnLayer(int layerHierarchy, VDDrawingLayer.LayerType layerType) {
        int step = 0;
        if (self.getSteps().size() > 0) {
            step = self.getSteps().get(self.getSteps().size() - 1).getStep() + 1;
        }

        VDDrawingStep drawingStep = new VDDrawingStep(step);

        if (layerHierarchy == NextLayerHierarchy) {
            self.topLayerHierarchy++;
            drawingStep.newDrawingLayer(self.topLayerHierarchy, layerType);
        }
        else {
            drawingStep.newDrawingLayer(layerHierarchy, layerType);
            self.topLayerHierarchy = layerHierarchy > self.topLayerHierarchy ? layerHierarchy : self.topLayerHierarchy;
        }

        self.addDrawingStep(drawingStep);

        return drawingStep;
    }

    public void addDrawingStep(VDDrawingStep drawingStep) {
        if (self.showingStepIndex != self.getSteps().size() - 1) {
            self.removeSteps(self.showingStepIndex + 1, self.getSteps().size());
        }
        self.showingStepIndex++;

        self.getSteps().add(drawingStep);
        if (drawingStep.isCleared()) {
            self.topLayerHierarchy = 0;
        }
    }

    public void cancelDrawingStep() {
        if (self.getDrawingStep().isCleared()) {
            return;
        }

        self.showingStepIndex--;
        if (self.getDrawingStep().getDrawingLayer().getHierarchy() > 0) {
            self.topLayerHierarchy--;
        }
        self.getSteps().remove(self.getSteps().size() - 1);
    }

    public VDDrawingStep getDrawingStep() {
        if (self.getSteps().size() > self.showingStepIndex
            && self.showingStepIndex >= 0) {
            return self.getSteps().get(self.showingStepIndex);
        }
        return null;
    }

    public List<VDDrawingStep> getStepsToDraw() {
        int lastClearedIndex = -1;
        for (int i = 0; i <= self.showingStepIndex; i++) {
            if (self.getSteps().get(i).isCleared()) lastClearedIndex = i;
        }

        if (lastClearedIndex == self.showingStepIndex) {
            return new ArrayList<>();
        }
        else {
            List<VDDrawingStep> stepsToDraw = self.getSteps().subList(lastClearedIndex + 1, self.showingStepIndex + 1);
            return stepsToDraw;
        }
    }

    /**
     *
     * @param from include
     * @param to exclude
     */
    public void removeSteps(int from, int to) {
        self.getSteps().subList(from, to).clear();
    }

    public boolean canUndo() {
        return self.showingStepIndex > 0;
    }

    public boolean canRedo() {
        return self.showingStepIndex < self.getSteps().size() - 1;
    }

    public boolean undo() {
        if (self.canUndo()) {
            if (!self.getDrawingStep().isStepOver()) {
                self.cancelDrawingStep();
            }
            else {
                self.showingStepIndex--;
            }
            return true;
        }
        return false;
    }

    public boolean redo() {
        if (self.canRedo()) {
            self.showingStepIndex++;
            return true;
        }
        return false;
    }

    /* #Classes */
    public class Ttt extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public boolean onUnbind(Intent intent) {
            return super.onUnbind(intent);
        }
    }

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}