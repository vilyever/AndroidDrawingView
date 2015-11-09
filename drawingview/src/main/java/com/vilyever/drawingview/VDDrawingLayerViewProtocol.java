package com.vilyever.drawingview;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.brush.VDDrawingBrush;

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
     * @param state
     * @return current step's frame
     */
    RectF updateWithDrawingStep(@NonNull VDDrawingStep drawingStep, VDDrawingBrush.DrawingPointerState state);

    void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps);

    int getLayerHierarchy();
    void setHandling(boolean handling);
}
