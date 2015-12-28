package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingStep;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerBaseView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerBaseView extends ImageView implements Runnable, VDDrawingLayerViewProtocol {
    private final VDDrawingLayerBaseView self = this;

    private Delegate delegate;

    private Bitmap drawingBitmap;
    private Canvas drawingCanvas;

    private Bitmap tempBitmap;

    private List<VDDrawingStep> drawnSteps = new ArrayList<>();

    private VDDrawingStep currentDrawingStep;

    private Thread drawingThread;

    private boolean busying;

    private Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (self.getDelegate() != null) {
                self.getDelegate().busyStateDidChange(self, busying);
            }
            return false;
        }
    });

    /* #Constructors */
    public VDDrawingLayerBaseView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(self.drawingBitmap, 0, 0, null);
    }

    /* #Accessors */

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public List<VDDrawingStep> getDrawnSteps() {
        return drawnSteps;
    }

    public Bitmap getDrawingBitmap() {
        return drawingBitmap;
    }

    private void setBusying(boolean busying) {
        this.busying = busying;

        self.uiHandler.sendEmptyMessage(0);
    }

    public boolean isBusying() {
        return busying;
    }

    /* #Delegates */
    // Runnable
    @Override
    public void run() {
        long beginTime = System.currentTimeMillis();
        try {
            self.setBusying(true);
            if (self.drawingCanvas != null) {
                self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                for (int i = 0; i < self.drawnSteps.size(); i++) {
                    VDDrawingStep step = self.drawnSteps.get(i);
                    step.getBrush().drawPath(self.drawingCanvas, step.getDrawingPath(), new VDBrush.DrawingState(VDBrush.DrawingPointerState.ForceFinish));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
//            while (System.currentTimeMillis() - beginTime < 500) {
//                Thread.yield();
//            }

            self.setBusying(false);
            self.postInvalidate();
        }
    }

    // VDDrawingLayerViewProtocol
    public void clearDrawing() {
        self.drawnSteps.clear();
        self.currentDrawingStep = null;

        self.checkDrawingBitmap();
        if (self.drawingCanvas != null) {
            self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        self.invalidate();
    }

    @Override
    public VDBrush.Frame appendWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        if (self.isBusying()) {
            return null;
        }

        if (drawingStep.getStepType() != VDDrawingStep.StepType.Draw) {
            return null;
        }

        self.currentDrawingStep = drawingStep;

        if (drawingStep.getDrawingState().isVeryBegin()) {
            if (self.drawingBitmap != null) {
                self.tempBitmap = Bitmap.createBitmap(self.drawingBitmap, 0, 0, self.drawingBitmap.getWidth(), self.drawingBitmap.getHeight());
            }
        }

        if (drawingStep.getDrawingState().isVeryEnd()) {
            self.currentDrawingStep = null;
            if (self.tempBitmap != null && !self.tempBitmap.isRecycled()) {
                self.tempBitmap.recycle();
            }
            self.tempBitmap = null;
            System.gc();
            return null;
        }

        VDBrush.Frame frame = null;

        self.checkDrawingBitmap();
        if (self.drawingCanvas != null) {
            self.drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (self.tempBitmap != null) {
                self.drawingCanvas.drawBitmap(self.tempBitmap, 0, 0, null);
            }
            frame = self.currentDrawingStep.getBrush().drawPath(self.drawingCanvas, self.currentDrawingStep.getDrawingPath(), drawingStep.getDrawingState());
            drawingStep.getDrawingLayer().setFrame(frame);
            self.invalidate();
        }

        return frame;
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps) {
        self.drawnSteps.clear();
        self.drawnSteps.addAll(drawnSteps);

        self.checkDrawingBitmap();

        try {
            if (self.drawingThread!= null) {
                self.drawingThread.interrupt();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        self.drawingThread = new Thread(self);
        self.drawingThread.start();
    }

    @Override
    public int getLayerHierarchy() {
        return 0;
    }

    @Override
    public void setHandling(boolean handling) {
    }

    @Override
    public boolean canHandle() {
        return false;
    }

    @Override
    public void setCanHandle(boolean canHandle) {

    }

    /* #Private Methods */
    private void init(Context context) {
    }

    private void checkDrawingBitmap() {
        if (self.getWidth() > 0 && self.getHeight() > 0) {
            try {
                if (self.drawingBitmap == null) {
                    self.drawingBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
//                    self.setImageBitmap(self.drawingBitmap);
                    self.drawingCanvas = new Canvas(self.drawingBitmap);
                }
                else if (self.drawingBitmap.getWidth() != self.getWidth()
                        || self.drawingBitmap.getHeight() != self.getHeight()) {
                    self.drawingBitmap.recycle();
                    self.drawingBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
//                    self.setImageBitmap(self.drawingBitmap);
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
    }

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */
    public interface Delegate {
        void busyStateDidChange(VDDrawingLayerBaseView baseView, boolean busying);
    }

    /* #Annotations @interface */
    
    /* #Enums */
}