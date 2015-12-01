package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.drawing.VDDrawingBrush;
import com.vilyever.drawingview.brush.drawing.VDPenBrush;
import com.vilyever.drawingview.brush.text.VDTextBrush;
import com.vilyever.drawingview.layer.VDDrawingLayerBaseView;
import com.vilyever.drawingview.layer.VDDrawingLayerContainer;
import com.vilyever.drawingview.layer.VDDrawingLayerImageView;
import com.vilyever.drawingview.layer.VDDrawingLayerTextView;
import com.vilyever.drawingview.layer.VDDrawingLayerViewProtocol;
import com.vilyever.drawingview.model.VDDrawingData;
import com.vilyever.drawingview.model.VDDrawingLayer;
import com.vilyever.drawingview.model.VDDrawingPoint;
import com.vilyever.drawingview.model.VDDrawingStep;
import com.vilyever.drawingview.util.VDDrawingAnimationView;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingView
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingView extends RelativeLayout implements View.OnLayoutChangeListener {
    private final VDDrawingView self = this;

    private static final int UnhandleAnyLayer = -1;
    private static final int HandleAllLayer = -2;

    private DrawingDelegate delegate;

    private VDDrawingData drawingData;

    private VDBrush brush;

    private VDDrawingLayerBaseView baseLayerImageView;

    private VDDrawingLayerContainer layerContainer;
    private List<VDDrawingLayerViewProtocol> layerViews = new ArrayList<>();
    private VDDrawingLayerViewProtocol handlingLayerView;

    private VDDrawingAnimationView animationView;

    /* #Constructors */
    public VDDrawingView(Context context) {
        this(context, null);
    }

    public VDDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        self.initial(context);
    }

    /* #Overrides */
    private boolean shouldOnTouch; // for limit only first finger can draw.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.baseLayerImageView.isBusying()) {
            return true;
        }

        int action = event.getAction();
        int maskAction = event.getAction() & MotionEvent.ACTION_MASK;

        // only handle the first touch finger
        if (action == MotionEvent.ACTION_DOWN) {
            self.shouldOnTouch = true;
        }

        if (!shouldOnTouch) {
            return true;
        }

        if (maskAction == MotionEvent.ACTION_POINTER_UP
                && event.getActionIndex() == 0) {
            action = MotionEvent.ACTION_UP;
            shouldOnTouch = false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                self.getParent().requestDisallowInterceptTouchEvent(true);
                self.beginDraw(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                self.drawing(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                self.endDraw(event.getX(), event.getY());
                self.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return true;
    }

    /* #Accessors */
    public DrawingDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(DrawingDelegate delegate) {
        this.delegate = delegate;
    }

    public VDDrawingData getDrawingData() {
        if (self.drawingData == null) {
            self.drawingData = new VDDrawingData();
            // initial first drawing step that clear the view
            self.drawingData
                    .newDrawingStepOnBaseLayer()
                    .setStepType(VDDrawingStep.StepType.Clear)
                    .setStepOver(true);
        }
        return drawingData;
    }

    public VDDrawingStep getCurrentDrawingStep() {
        return self.getDrawingData().getDrawingStep();
    }

    public <T extends VDBrush> T getBrush() {
        if (self.brush == null) {
            self.brush = VDPenBrush.defaultBrush();
        }
        return (T) brush;
    }

    public void setBrush(VDBrush brush) {
        this.brush = brush;

        self.endUnfinishedStep();
    }

    /* #Delegates */
    // View.OnLayoutChangeListener
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;

        int width = right - left;
        int height = bottom - top;

        boolean changed = (oldWidth != width) || (oldHeight != height);

        if (changed) {
            self.getDrawingData().getSteps().get(0).getDrawingLayer().setFrame(new RectF(0, 0, width, height));
            self.nativeClear();
            self.nativeDrawData();
        }
    }

    /* #Private Methods */
    private void initial(Context context) {
        self.addOnLayoutChangeListener(self);

        // focusable, which can clear focus from edittext
        self.setFocusable(true);
        self.setFocusableInTouchMode(true);

        // setup base layer view
        self.baseLayerImageView = new VDDrawingLayerBaseView(context);
        self.baseLayerImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        self.layerViews.add(self.baseLayerImageView);
        self.addView(self.baseLayerImageView);
        self.baseLayerImageView.setDelegate(new VDDrawingLayerBaseView.Delegate() {
            @Override
            public void busyStateDidChange(VDDrawingLayerBaseView baseView, boolean busying) {
                self.baseLayerImageView.setVisibility(busying ? INVISIBLE : VISIBLE);
                self.layerContainer.setVisibility(busying ? INVISIBLE : VISIBLE);

                self.animationView.setVisibility(busying ? VISIBLE : GONE);
                self.animationView.setAnimated(busying);
            }
        });

        self.layerContainer = new VDDrawingLayerContainer(context);
        self.layerContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        self.addView(self.layerContainer);
        self.layerContainer.setDelegate(new VDDrawingLayerContainer.Delegate() {
            @Override
            public void layerTextViewDidBeginEdit(VDDrawingLayerTextView textView) {
                self.getDrawingData()
                        .newDrawingStepOnLayer(textView.getLayerHierarchy(), VDDrawingLayer.LayerType.Text)
                        .setStepType(VDDrawingStep.StepType.Text);
            }

            @Override
            public void layerViewDidBeginTouch(VDDrawingLayerViewProtocol layerView) {
                self.endUnfinishedStep();
                self.handleLayer(layerView.getLayerHierarchy());
            }

            @Override
            public void layerViewDidEndTransform(VDDrawingLayerViewProtocol layerView) {
                VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Image;
                if (layerView instanceof VDDrawingLayerTextView) {
                    layerType = VDDrawingLayer.LayerType.Text;
                }

                self.getDrawingData()
                        .newDrawingStepOnLayer(layerView.getLayerHierarchy(), layerType)
                        .setStepType(VDDrawingStep.StepType.Transform);

                View v = (View) layerView;

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
                self.getCurrentDrawingStep().getDrawingLayer().setLeft(layoutParams.leftMargin);
                self.getCurrentDrawingStep().getDrawingLayer().setTop(layoutParams.topMargin);
                self.getCurrentDrawingStep().getDrawingLayer().setWidth(layoutParams.width);
                self.getCurrentDrawingStep().getDrawingLayer().setHeight(layoutParams.height);
                self.getCurrentDrawingStep().getDrawingLayer().setScale(v.getScaleX());
                self.getCurrentDrawingStep().getDrawingLayer().setRotation(v.getRotation());

                self.overCurrentStep();
            }
        });

        self.animationView = new VDDrawingAnimationView(context);
        self.animationView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        self.animationView.setVisibility(View.GONE);
        self.addView(self.animationView);
    }

    private void beginDraw(float x, float y) {
        if (self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text) {
            self.endUnfinishedStep();
        }

        VDDrawingPoint.IncrementPointerID();

        if (self.getCurrentDrawingStep().isStepOver()) {
            self.handleLayer(UnhandleAnyLayer);

            if (self.getBrush() instanceof VDDrawingBrush) {
                VDDrawingBrush drawingBrush = self.getBrush();

                if (drawingBrush.isOneStrokeToLayer()) {
                    self.getDrawingData().newDrawingStepOnNextLayer(VDDrawingLayer.LayerType.Image).setStepType(VDDrawingStep.StepType.CreateLayer).setBrush(VDBrush.copy(drawingBrush));
                    self.handlingLayerView = new VDDrawingLayerImageView(self.getContext());
                    self.layerViews.add(self.handlingLayerView);
                    self.layerContainer.addLayerView(self.handlingLayerView);
                }
                else {
                    self.getDrawingData()
                            .newDrawingStepOnBaseLayer()
                            .setStepType(VDDrawingStep.StepType.Draw)
                            .setBrush(VDBrush.copy(drawingBrush));
                    self.handlingLayerView = self.baseLayerImageView;
                }

                self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
                self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.VeryBegin, VDBrush.DrawingPointerState.TouchDown));
                self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());
            }
            else if (self.getBrush() instanceof VDTextBrush) {
                VDTextBrush textBrush = self.getBrush();
                self.getDrawingData().newDrawingStepOnNextLayer(VDDrawingLayer.LayerType.Text).setStepType(VDDrawingStep.StepType.CreateLayer).setBrush(VDBrush.copy(textBrush));

                self.handlingLayerView = new VDDrawingLayerTextView(self.getContext());
                self.layerViews.add(self.handlingLayerView);
                self.layerContainer.addLayerView(self.handlingLayerView);

                self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
                self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.VeryBegin, VDBrush.DrawingPointerState.TouchDown));
                self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());
            }

            self.handlingLayerView.setHandling(true);
        }
        else {
            self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchDown));
            self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());
        }
    }

    private void drawing(float x, float y) {
        if (self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y))) {
            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchMoving));
            self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());
        }
    }

    private void endDraw(float x, float y) {
        self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));

        switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
            case Base:
            case Image:
                self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchUp));
                boolean stepOver = self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep())
                        != VDBrush.UnfinishFrame;

                if (stepOver) {
                    self.finishDraw();
                }
                break;
            case Text:
                self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchUp));
                self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                textView.beginEdit(true);
                break;
        }
    }

    private void finishDraw() {
        self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.VeryEnd));
        self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());

        if (self.getCurrentDrawingStep().getDrawingLayer().getFrame() == null) {
            // noting to draw, e.g. draw line with one point is pointless
            switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
                case Base:
                    break;
                case Image:
                case Text:
                    self.layerContainer.removeLayerView(self.handlingLayerView);
                    self.layerViews.remove(self.handlingLayerView);
                    self.handlingLayerView = null;
                    break;
            }

            self.cancelCurrentStep();
            return;
        }

        self.overCurrentStep();
    }

    private void endUnfinishedStep() {
        if (self.getCurrentDrawingStep() != null
                && !self.getCurrentDrawingStep().isStepOver()) {

            switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
                case Base:
                    self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.ForceFinish));
                    self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());

                    self.finishDraw();
                    break;
                case Image:
                    VDDrawingLayerImageView imageView = (VDDrawingLayerImageView) self.handlingLayerView;
                    switch (self.getCurrentDrawingStep().getStepType()) {
                        case CreateLayer:
                            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.ForceFinish));
                            self.handlingLayerView.appendWithDrawingStep(self.getCurrentDrawingStep());

                            self.finishDraw();
                            break;
                    }
                    break;
                case Text:
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                    switch (self.getCurrentDrawingStep().getStepType()) {
                        case CreateLayer:
                            textView.endEdit();
                            if (textView.isChanged()) {
                                self.getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());
                                self.overCurrentStep();
                            }
                            else {
                                self.layerContainer.removeLayerView(textView);
                                self.layerViews.remove(textView);
                                self.handlingLayerView = null;
                                self.cancelCurrentStep();
                            }
                            break;
                        case Text:
                            textView.endEdit();
                            if (textView.isChanged()) {
                                self.getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());
                                self.overCurrentStep();
                            }
                            else {
                                self.cancelCurrentStep();
                            }
                            break;
                    }
                    break;
            }
        }
    }

    private void cancelCurrentStep() {
        self.getDrawingData().cancelDrawingStep();
    }

    private void overCurrentStep() {
        self.getCurrentDrawingStep().setStepOver(true);
        if (self.getDelegate() != null) {
            self.getDelegate().didChangeDrawing(self, self.canUndo(), self.canRedo());
        }
    }

    private void handleLayer(int layerHierarchy) {
        self.handlingLayerView = null;

        for (VDDrawingLayerViewProtocol layerViewProtocol : self.layerViews) {
            layerViewProtocol.setHandling((layerViewProtocol.getLayerHierarchy() == layerHierarchy) || (layerHierarchy == HandleAllLayer));
            if (layerViewProtocol.getLayerHierarchy() == layerHierarchy
                    && layerHierarchy != HandleAllLayer) {
                self.handlingLayerView = layerViewProtocol;
            }
        }
    }

    private VDDrawingLayerViewProtocol findLayerViewByLayerHierarchy(int layerHierarchy) {
        if (layerHierarchy == 0) {
            return self.baseLayerImageView;
        }

        for (VDDrawingLayerViewProtocol layerViewProtocol : self.layerViews) {
            if (layerViewProtocol.getLayerHierarchy() == layerHierarchy) {
                return layerViewProtocol;
            }
        }

        return null;
    }

    private void nativeSetBackgroundColor(int layerHierarchy, int color) {
        if (layerHierarchy < 0) {
            return;
        }
        else if (layerHierarchy == 0) {
            self.baseLayerImageView.setBackgroundColor(color);
        }
        else {
            VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(layerHierarchy);
            if (layerViewProtocol == null) {
                return;
            }

            View layerView = (View) layerViewProtocol;
            layerView.setBackgroundColor(color);
        }
    }

    private void nativeSetBackgroundDrawable(int layerHierarchy, Drawable drawable) {
        if (layerHierarchy < 0) {
            return;
        }
        else if (layerHierarchy == 0) {
            self.baseLayerImageView.setBackground(drawable);
        }
        else {
            VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(layerHierarchy);
            if (layerViewProtocol == null) {
                return;
            }

            View layerView = (View) layerViewProtocol;
            layerView.setBackground(drawable);
        }
    }

    private void nativeClear() {
        self.layerContainer.clear();

        if (self.layerViews.size() > 1) {
            self.layerViews.subList(1, self.layerViews.size()).clear();
        }

        self.handlingLayerView = null;

        self.baseLayerImageView.clearDrawing();
        self.baseLayerImageView.setBackground(null);
    }

    private void nativeDrawData() {
        List<VDDrawingStep> stepsToDraw = self.getDrawingData().getStepsToDraw();

        // 筛选出每层layer涉及的step
        List<List<VDDrawingStep>> layerSteps = new ArrayList<>();
        for (int i = 0; i < stepsToDraw.size(); i++) {
            VDDrawingStep step = stepsToDraw.get(i);

            while (layerSteps.size() <= step.getDrawingLayer().getHierarchy()) {
                layerSteps.add(new ArrayList<VDDrawingStep>());
            }
            layerSteps.get(step.getDrawingLayer().getHierarchy()).add(step);
        }

        for (int layerHierarchy = 0; layerHierarchy < layerSteps.size(); layerHierarchy++) {
            if (layerSteps.get(layerHierarchy).size() == 0) {
                continue;
            }

            int lastBackgroundIndex = -1;

            for (int i = 0; i < layerSteps.get(layerHierarchy).size(); i++) {
                VDDrawingStep step = layerSteps.get(layerHierarchy).get(i);

                if (step.getDrawingLayer().getBackgroundImageIdentifier() != null
                        || step.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    lastBackgroundIndex = i;
                }
            }

            if (layerHierarchy == 0) {
                self.baseLayerImageView.refreshWithDrawnSteps(layerSteps.get(0));
            }
            else if (layerHierarchy > 0) { // 图层生成
                VDDrawingStep firstDrawingStep = layerSteps.get(layerHierarchy).get(0);
                VDDrawingStep lastDrawingStep = layerSteps.get(layerHierarchy).get(layerSteps.get(layerHierarchy).size() - 1);
                if (lastDrawingStep.getStepType() == VDDrawingStep.StepType.DeleteLayer) {
                    // if the layer deleted, skip it.
                    continue;
                }
                switch (firstDrawingStep.getDrawingLayer().getLayerType()) {
                    case Image: {
                        VDDrawingLayerImageView imageView = new VDDrawingLayerImageView(self.getContext());
                        self.layerContainer.addLayerView(imageView);
                        self.layerViews.add(imageView);

                        imageView.refreshWithDrawnSteps(layerSteps.get(layerHierarchy));
                        break;
                    }
                    case Text: {
                        VDDrawingLayerTextView textView = new VDDrawingLayerTextView(self.getContext());
                        self.layerContainer.addLayerView(textView);
                        self.layerViews.add(textView);

                        textView.refreshWithDrawnSteps(layerSteps.get(layerHierarchy));
                        break;
                    }
                }
            }

            if (lastBackgroundIndex >= 0) {
                VDDrawingStep lastBackgroundDrawingStep = layerSteps.get(layerHierarchy).get(lastBackgroundIndex);
                if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                    if (self.getDelegate() != null) {
                        self.nativeSetBackgroundDrawable(layerHierarchy, self.getDelegate().gainBackground(self, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                    }
                }
                else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    self.nativeSetBackgroundColor(layerHierarchy, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                }
            }
        }

        self.handleLayer(UnhandleAnyLayer);
    }

    private void destroy() {
        self.nativeClear();
    }

    /* #Public Methods */
    public void clear() {
        if (self.getCurrentDrawingStep().isCleared()) { // means current state is clear
            return;
        }
        self.endUnfinishedStep();
        self.nativeClear();
        self.getDrawingData()
                .newDrawingStepOnBaseLayer()
                .setStepType(VDDrawingStep.StepType.Clear);
        self.overCurrentStep();
    }

    public void drawNextStep(@NonNull VDDrawingStep step) {
        // TODO: 2015/11/26  just draw this step, if the step is same as current step but the detail change, consider reuse
        if (step.getStep() == self.getCurrentDrawingStep().getStep()) {
            switch (step.getStepType()) {
                case Clear:
                    break;
                case Draw:
                    break;
                case Background:
                    break;
                case CreateLayer:
                    break;
                case Transform:
                    break;
                case Text:
                    break;
                case DeleteLayer:
                    break;
            }
        }

        if (step.isCleared() && self.getCurrentDrawingStep().isCleared()) {
            return;
        }
        self.endUnfinishedStep();




        self.getDrawingData().addDrawingStep(step);
        self.nativeClear();
        self.nativeDrawData();
        self.overCurrentStep();
    }

    public void refreshWithDrawingData(VDDrawingData data) {
        self.cancelCurrentStep();
        self.drawingData = data;
        self.nativeClear();
        self.nativeDrawData();
    }

    public void setBackgroundColor(int layerHierarchy, int color) {
        if (layerHierarchy < 0) {
            return;
        }

        self.endUnfinishedStep();
        self.nativeSetBackgroundColor(layerHierarchy, color);

        VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Base;
        VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerViewProtocol instanceof VDDrawingLayerImageView) {
            layerType = VDDrawingLayer.LayerType.Image;
        }
        else if (layerViewProtocol instanceof VDDrawingLayerTextView) {
            layerType = VDDrawingLayer.LayerType.Text;
        }

        self.getDrawingData()
                .newDrawingStepOnLayer(layerHierarchy, layerType)
                .setStepType(VDDrawingStep.StepType.Background)
                .getDrawingLayer()
                .setBackgroundColor(color);
        self.overCurrentStep();
    }

    public void setBackgroundDrawable(int layerHierarchy, Drawable drawable, String identifier) {
        if (layerHierarchy < 0) {
            return;
        }

        self.endUnfinishedStep();
        self.nativeSetBackgroundDrawable(layerHierarchy, drawable);

        VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Base;
        VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerViewProtocol instanceof VDDrawingLayerImageView) {
            layerType = VDDrawingLayer.LayerType.Image;
        }
        else if (layerViewProtocol instanceof VDDrawingLayerTextView) {
            layerType = VDDrawingLayer.LayerType.Text;
        }

        self.getDrawingData()
                .newDrawingStepOnLayer(layerHierarchy, layerType)
                .setStepType(VDDrawingStep.StepType.Background)
                .getDrawingLayer()
                .setBackgroundImageIdentifier(identifier);
        self.overCurrentStep();
    }

    public boolean deleteHandlingLayer() {
        if (self.handlingLayerView != null
                && self.handlingLayerView.getLayerHierarchy() > 0
                && self.getCurrentDrawingStep().isStepOver()) {
            self.endUnfinishedStep();

            self.layerContainer.removeLayerView(self.handlingLayerView);
            self.layerViews.remove(self.handlingLayerView);

            self.getDrawingData()
                    .newDrawingStepOnLayer(self.handlingLayerView.getLayerHierarchy(), VDDrawingLayer.LayerType.Unkonwn)
                    .setStepType(VDDrawingStep.StepType.DeleteLayer);
            self.overCurrentStep();
        }

        return false;
    }

    public boolean undo() {
        if (self.canUndo()) {
            self.endUnfinishedStep();
            self.getDrawingData().undo();
            self.nativeClear();
            self.nativeDrawData();

            if (self.getDelegate() != null) {
                self.getDelegate().didChangeDrawing(self, self.canUndo(), self.canRedo());
            }
        }
        return false;
    }

    public boolean redo() {
        if (self.canRedo()) {
            self.endUnfinishedStep();
            self.getDrawingData().redo();
            self.nativeClear();
            self.nativeDrawData();

            if (self.getDelegate() != null) {
                self.getDelegate().didChangeDrawing(self, self.canUndo(), self.canRedo());
            }
        }
        return false;
    }

    public boolean canUndo() {
        return self.getDrawingData().canUndo();
    }

    public boolean canRedo() {
        return self.getDrawingData().canRedo();
    }

    /* #Classes */

    /* #Interfaces */
    public interface DrawingDelegate {
        void didChangeDrawing(VDDrawingView drawingView, boolean canUndo, boolean canRedo);
        Drawable gainBackground(VDDrawingView drawingView, String identifier);
    }

    /* #Annotations @interface */

    /* #Enums */
}
