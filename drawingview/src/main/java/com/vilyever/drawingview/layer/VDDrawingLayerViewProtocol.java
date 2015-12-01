package com.vilyever.drawingview.layer;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingStep;

import java.util.List;

/**
 * VDDrawingLayerViewDelegate
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/30.
 * Feature:
 */
public interface VDDrawingLayerViewProtocol {

    void clearDrawing();

    /**
     * @param drawingStep
     * @return current step's frame
     */
    RectF appendWithDrawingStep(@NonNull VDDrawingStep drawingStep);

    void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps);

    int getLayerHierarchy();
    void setHandling(boolean handling);
}
