package com.vilyever.drawingview;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * VDDrawingLayerViewDelegate
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/30.
 * Feature:
 */
public interface VDDrawingLayerViewProtocol {
    void clearDrawing();
    void updateWithDrawingStep(@NonNull VDDrawingStep drawingStep);
    void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps);

    int getLayerHierarchy();
    void setHandling(boolean handling);
}
