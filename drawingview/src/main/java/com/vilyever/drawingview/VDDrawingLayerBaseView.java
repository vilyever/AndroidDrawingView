package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
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
public class VDDrawingLayerBaseView extends SurfaceView implements Runnable, VDDrawingLayerViewProtocol {
    private final VDDrawingLayerBaseView self = this;

    private Thread drawingThread;
    private boolean drawingThreadRunning;
    private boolean shouldDraw;

    private Drawable backgroundDrawable;

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

                        if (self.backgroundDrawable != null) {
                            self.backgroundDrawable.setBounds(0, 0, self.getWidth(), self.getHeight());
                            self.backgroundDrawable.draw(canvas);
                        }
                        else {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        }

                        for (int i = 0; i < self.drawingSteps.size() - 1; i++) {
                            VDDrawingStep step = self.drawingSteps.get(i);
                            step.getBrush().drawPath(canvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
                        }

                        if (self.currentDrawingStep != null) {
                            self.currentDrawingStep.getBrush().drawPath(canvas, self.currentDrawingStep.getDrawingPath(), self.currentDrawingState);
                        }
                        else {
                            if (self.drawingSteps.size() > 0) {
                                VDDrawingStep step = self.drawingSteps.get(self.drawingSteps.size() - 1);
                                step.getBrush().drawPath(canvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
                            }
                        }

                        self.getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    // VDDrawingLayerViewProtocol
    public void clearDrawing() {
        self.drawingSteps.clear();
        self.currentDrawingStep = null;
        self.currentDrawingState = null;

        self.shouldDraw = true;
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

        self.shouldDraw = true;

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

        self.shouldDraw = true;
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
//        self.setZOrderOnTop(true);
        self.setZOrderMediaOverlay(true);
        self.getHolder().setFormat(PixelFormat.TRANSPARENT);

        self.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                self.drawingThreadRunning = true;
                if (self.drawingThread == null) {
                    self.drawingThread = new Thread(self);
                    self.drawingThread.start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                self.drawingThreadRunning = false;
                if (self.drawingThread != null) {
                    self.drawingThread.interrupt();
                    self.drawingThread = null;
                }
            }
        });
    }

    /* #Public Methods */
    public void updateBackground(Drawable drawable) {
        self.backgroundDrawable = drawable;
        self.shouldDraw = true;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}