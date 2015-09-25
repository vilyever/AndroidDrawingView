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

    public static final long NextHierarchy = -1;

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    private long topHierarchy = -1;

    /* #Constructors */    
    
    /* #Overrides */    
    
    /* #Accessors */
    public List<VDDrawingStep> getDrawingSteps() {
        return drawingSteps;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public VDDrawingStep newDrawingStepWithLayer() {
        return newDrawingStepWithLayer(NextHierarchy);
    }

    public VDDrawingStep newDrawingStepWithLayer(long layerHierarchy) {
        long step = 0;
        if (self.getDrawingSteps().size() > 0) {
            step = self.getDrawingSteps().get(self.getDrawingSteps().size() - 1).getStep() + 1;
        }

        VDDrawingStep drawingStep = new VDDrawingStep(step);

        if (layerHierarchy >= 0) {
            drawingStep.addLayer(new VDDrawingLayer(layerHierarchy));
            self.topHierarchy = layerHierarchy > self.topHierarchy ? layerHierarchy : self.topHierarchy;
        }
        else if (layerHierarchy == NextHierarchy) {
            self.topHierarchy++;
            drawingStep.addLayer(new VDDrawingLayer(self.topHierarchy));
        }

        return drawingStep;
    }

    public void addDrawingStep(VDDrawingStep drawingStep) {
        self.getDrawingSteps().add(drawingStep);
        if (drawingStep.isCleared()) {
            self.topHierarchy = 0;
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

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}