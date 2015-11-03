package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.VDDrawingBrush;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerDrawingView extends ImageView {
    private final VDDrawingLayerDrawingView self = this;

    private Bitmap drawingBitmap;
    private Canvas drawingCanvas;
    private List<VDDrawingStep> drawingSteps = new ArrayList<>();


    /* #Constructors */
    public VDDrawingLayerDrawingView(Context context) {
        super(context);
        self.setBackground(null);
    }

    /* #Overrides */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (self.drawingBitmap != null) {
            self.drawingBitmap.recycle();
            self.drawingBitmap = null;
            self.drawingCanvas = null;
        }
    }

    /* #Accessors */

    /* #Delegates */

    /* #Private Methods */
    private void createDrawingBitmap() {
        if (self.drawingBitmap == null) {
            self.drawingBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
            self.drawingCanvas = new Canvas(self.drawingBitmap);
            self.setImageBitmap(drawingBitmap);
        }
    }

    private void destroyDrawingBitmap() {
        if (self.drawingBitmap != null) {
            self.setImageBitmap(null);
            self.drawingBitmap.recycle();
            self.drawingBitmap = null;
            self.drawingCanvas = null;
        }
    }

    /* #Public Methods */
    public void clearDrawing() {
        self.drawingSteps.clear();
        if (self.drawingCanvas != null) {
            self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
    }

    /**
     *
     * @param drawingStep
     * @param state
     * @return current step's frame
     */
    public RectF updateWithDrawingStep(@NonNull VDDrawingStep drawingStep, VDDrawingBrush.DrawingPointerState state) {
        self.drawingSteps.add(drawingStep);

        self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        for (int i = 0; i < self.drawingSteps.size() - 1; i++) {
            VDDrawingStep step = self.drawingSteps.get(i);
            step.getBrush().drawPath(self.drawingCanvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
        }

        RectF frame = drawingStep.getBrush().drawPath(self.drawingCanvas, drawingStep.getDrawingPath(), state);
        drawingStep.getDrawingLayer().setFrame(frame);
        self.invalidate();

        return frame;
    }

    public void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps) {
        self.drawingSteps.addAll(drawingSteps);

        self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        for (int i = 0; i < self.drawingSteps.size(); i++) {
            VDDrawingStep step = self.drawingSteps.get(i);
            step.getBrush().drawPath(self.drawingCanvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
        }
    }

    public void prepare() {
        self.createDrawingBitmap();
        self.clearDrawing();
    }

    public void finish() {
        self.clearDrawing();
        self.destroyDrawingBitmap();
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}