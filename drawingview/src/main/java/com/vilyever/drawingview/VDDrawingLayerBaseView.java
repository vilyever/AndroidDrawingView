package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.vilyever.drawingview.brush.VDBrush;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerBaseView extends ImageView implements VDDrawingLayerViewProtocol {
    private final VDDrawingLayerBaseView self = this;

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    /* #Constructors */
    public VDDrawingLayerBaseView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < self.drawingSteps.size(); i++) {
            VDDrawingStep step = self.drawingSteps.get(i);
            step.getBrush().drawPath(canvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
        }
    }

    /* #Accessors */
    public List<VDDrawingStep> getDrawingSteps() {
        return drawingSteps;
    }

    /* #Delegates */
    // VDDrawingLayerViewProtocol
    public void clearDrawing() {
        self.drawingSteps.clear();
        self.invalidate();
    }

    @Override
    public void updateWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        self.drawingSteps.add(drawingStep);
        self.invalidate();
    }

    @Override
    public void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps) {
        self.drawingSteps.addAll(drawingSteps);
        self.invalidate();
    }

    @Override
    public int getLayerHierarchy() {
        return 0;
    }

    @Override
    public void setHandling(boolean handling) {
    }

    /* #Private Methods */
    private void init(Context context) {
        self.setBackground(null);
    }

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}