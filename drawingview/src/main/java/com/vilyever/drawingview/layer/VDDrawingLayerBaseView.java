package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.drawing.VDDrawingBrush;
import com.vilyever.drawingview.model.VDDrawingStep;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerBaseView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerBaseView extends ImageView implements VDDrawingLayerViewProtocol {
    private final VDDrawingLayerBaseView self = this;

    private Bitmap drawingBitmap;
    private Canvas drawingCanvas;

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    private VDDrawingStep currentDrawingStep;
    private VDBrush.DrawingPointerState currentDrawingState;

    /* #Constructors */
    public VDDrawingLayerBaseView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */

    /* #Accessors */
    public List<VDDrawingStep> getDrawingSteps() {
        return drawingSteps;
    }

    public Bitmap getDrawingBitmap() {
        return drawingBitmap;
    }

    /* #Delegates */
    // VDDrawingLayerViewProtocol
    public void clearDrawing() {
        self.drawingSteps.clear();
        self.currentDrawingStep = null;
        self.currentDrawingState = null;

        self.updateDrawingCanvas();
    }

    @Override
    public RectF updateWithDrawingStep(@NonNull VDDrawingStep drawingStep, VDDrawingBrush.DrawingPointerState state) {
        if (drawingStep.getStepType() != VDDrawingStep.StepType.Draw) {
            return null;
        }

        if (!self.drawingSteps.contains(drawingStep)) {
            self.drawingSteps.add(drawingStep);
        }

        self.currentDrawingStep = drawingStep;
        self.currentDrawingState = state;

        self.updateDrawingCanvas();

        VDBrush.DrawingPointerState frameState = VDBrush.DrawingPointerState.FetchFrame;
        if (state.shouldForceFinish()) {
            frameState = VDBrush.DrawingPointerState.ForceFinishFetchFrame;
        }
        RectF frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), frameState);
        drawingStep.getDrawingLayer().setFrame(frame);

        return frame;
    }

    @Override
    public void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps) {
        for (VDDrawingStep step : drawingSteps) {
            if (step.getStepType() == VDDrawingStep.StepType.Draw) {
                self.drawingSteps.add(step);
            }
        }

        self.updateDrawingCanvas();
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
    }

    private void updateDrawingCanvas() {
        if (self.getWidth() > 0 && self.getHeight() > 0) {
            try {
                if (self.drawingBitmap == null) {
                    self.drawingBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
                    self.setImageBitmap(self.drawingBitmap);
                    self.drawingCanvas = new Canvas(self.drawingBitmap);
                } else if (self.drawingBitmap.getWidth() != self.getWidth()
                        || self.drawingBitmap.getHeight() != self.getHeight()) {
                    self.drawingBitmap.recycle();
                    self.drawingBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
                    self.setImageBitmap(self.drawingBitmap);
                    self.drawingCanvas = new Canvas(self.drawingBitmap);
                }
            }
            catch (Exception e) {
                // in recycler view, the view's size may be very large when init
                if (!(e instanceof IllegalArgumentException)) {
                    e.printStackTrace();
                }
            }
        }

        if (self.drawingCanvas == null) {
            return;
        }

        self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        for (int i = 0; i < self.drawingSteps.size() - 1; i++) {
            VDDrawingStep step = self.drawingSteps.get(i);
            step.getBrush().drawPath(self.drawingCanvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
        }

        if (self.currentDrawingStep != null) {
            self.currentDrawingStep.getBrush().drawPath(self.drawingCanvas, self.currentDrawingStep.getDrawingPath(), self.currentDrawingState);
        }
        else {
            if (self.drawingSteps.size() > 0) {
                VDDrawingStep step = self.drawingSteps.get(self.drawingSteps.size() - 1);
                step.getBrush().drawPath(self.drawingCanvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
            }
        }

        self.invalidate();
    }

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}