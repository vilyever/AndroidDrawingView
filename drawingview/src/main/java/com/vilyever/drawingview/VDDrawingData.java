package com.vilyever.drawingview;

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

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    private int topLayerHierarchy = -1;

    private int showingStep = -1;

    /* #Constructors */    
    
    /* #Overrides */    
    
    /* #Accessors */
    public List<VDDrawingStep> getDrawingSteps() {
        return drawingSteps;
    }

    public int getTopLayerHierarchy() {
        return topLayerHierarchy;
    }

    public int getShowingStep() {
        return showingStep;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public VDDrawingStep newDrawingStepOnLayer() {
        return newDrawingStepOnLayer(NextLayerHierarchy);
    }

    public VDDrawingStep newDrawingStepOnLayer(int layerHierarchy) {
        int step = 0;
        if (self.getDrawingSteps().size() > 0) {
            step = self.getDrawingSteps().get(self.getDrawingSteps().size() - 1).getStep() + 1;
        }

        VDDrawingStep drawingStep = new VDDrawingStep(step);

        if (layerHierarchy >= 0) {
            drawingStep.newLayer(layerHierarchy);
            self.topLayerHierarchy = layerHierarchy > self.topLayerHierarchy ? layerHierarchy : self.topLayerHierarchy;
        }
        else if (layerHierarchy == NextLayerHierarchy) {
            self.topLayerHierarchy++;
            drawingStep.newLayer(self.topLayerHierarchy);
        }

        self.addDrawingStep(drawingStep);

        return drawingStep;
    }

    public void addDrawingStep(VDDrawingStep drawingStep) {
        if (self.showingStep != self.getDrawingSteps().size() - 1) {
            self.removeDrawingSteps(self.showingStep + 1, self.getDrawingSteps().size());
        }
        self.showingStep++;

        self.getDrawingSteps().add(drawingStep);
        if (drawingStep.isCleared()) {
            self.topLayerHierarchy = 0;
        }
    }

    public void cancelDrawingStep() {
        if (self.drawingStep().isCleared()) {
            return;
        }

        self.showingStep--;
        if (self.drawingStep().drawingLayer().getHierarchy() > 0) {
            self.topLayerHierarchy--;
        }
        self.getDrawingSteps().remove(self.getDrawingSteps().size() - 1);
    }

    public VDDrawingStep drawingStep() {
        if (self.getDrawingSteps().size() > self.showingStep
            && self.showingStep >= 0) {
            return self.getDrawingSteps().get(self.showingStep);
        }
        return null;
    }

    public List<VDDrawingStep> stepsToDraw( ) {
        int lastClearedIndex = 0;
        for (int i = 1; i <= self.showingStep; i++) {
            if (self.getDrawingSteps().get(i).isCleared()) lastClearedIndex = i;
        }

        if (lastClearedIndex == self.showingStep) {
            return new ArrayList<>();
        }
        else {
            List<VDDrawingStep> stepsToDraw = self.getDrawingSteps().subList(lastClearedIndex + 1, self.showingStep + 1);
            return stepsToDraw;
        }
    }

    /**
     *
     * @param from include
     * @param to exclude
     */
    public void removeDrawingSteps(int from, int to) {
        self.getDrawingSteps().subList(from, to).clear();
    }

    public boolean canUndo() {
        return self.showingStep > 0;
    }

    public boolean canRedo() {
        return self.showingStep < self.getDrawingSteps().size() - 1;
    }

    public boolean undo() {
        if (self.canUndo()) {
            self.showingStep--;
        }
        return false;
    }

    public boolean redo() {
        if (self.canRedo()) {
            self.showingStep++;
        }
        return false;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}