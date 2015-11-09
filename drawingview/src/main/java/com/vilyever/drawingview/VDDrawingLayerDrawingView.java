package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

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
public class VDDrawingLayerDrawingView extends SurfaceView implements Runnable {
    private final VDDrawingLayerDrawingView self = this;

    private Thread drawingThread;
    private boolean drawingThreadRunning;
    private boolean shouldDraw;

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    private VDDrawingStep currentDrawingStep;
    private VDBrush.DrawingPointerState currentDrawingState;

    /* #Constructors */
    public VDDrawingLayerDrawingView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        self.drawingThreadRunning = true;
        if (self.drawingThread == null) {
            self.drawingThread = new Thread(self);
            self.drawingThread.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        self.drawingThreadRunning = false;
        if (self.drawingThread != null) {
            self.drawingThread.interrupt();
            self.drawingThread = null;
        }
    }

    /* #Accessors */
    public List<VDDrawingStep> getDrawingSteps() {
        return drawingSteps;
    }

    /* #Delegates */
    // Runnable
    @Override
    public void run() {
        while (self.drawingThreadRunning) {
            if (self.getHolder().getSurface().isValid()) {
                if (self.shouldDraw) {
                    Canvas canvas = self.getHolder().lockCanvas();
                    if (canvas != null) {
                        self.shouldDraw = false;

                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        for (int i = 0; i < self.drawingSteps.size() - 1; i++) {
                            VDDrawingStep step = self.drawingSteps.get(i);
                            step.getBrush().drawPath(canvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
                        }

                        if (self.currentDrawingStep != null) {
                            self.currentDrawingStep.getBrush().drawPath(canvas, self.currentDrawingStep.getDrawingPath(), self.currentDrawingState);
                        }

                        self.getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    /* #Private Methods */
    private void init(Context context) {
        self.setZOrderOnTop(true);
        self.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    /* #Public Methods */
    public void clearDrawing() {
        self.drawingSteps.clear();
        self.currentDrawingStep = null;
        self.currentDrawingState = null;

        self.shouldDraw = true;
    }

    /**
     *
     * @param drawingStep
     * @param state
     * @return current step's frame
     */
    public RectF updateWithDrawingStep(@NonNull VDDrawingStep drawingStep, VDDrawingBrush.DrawingPointerState state) {
        if (!self.drawingSteps.contains(drawingStep)) {
            self.drawingSteps.add(drawingStep);
        }

        self.currentDrawingStep = drawingStep;
        self.currentDrawingState = state;

        self.shouldDraw = true;

        RectF frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), null);
        drawingStep.getDrawingLayer().setFrame(frame);

        return frame;
    }

    public void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps) {
        self.drawingSteps.addAll(drawingSteps);
    }

    /* #Classes */

    /* #Interfaces */

    /* #Annotations @interface */

    /* #Enums */
}