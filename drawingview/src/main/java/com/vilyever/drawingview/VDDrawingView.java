package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.drawing.VDDrawingBrush;
import com.vilyever.drawingview.brush.layereraser.VDLayerEraserBrush;
import com.vilyever.drawingview.brush.drawing.VDPenBrush;
import com.vilyever.drawingview.brush.text.VDTextBrush;
import com.vilyever.drawingview.layer.VDDrawingLayerBaseView;
import com.vilyever.drawingview.layer.VDDrawingLayerImageView;
import com.vilyever.drawingview.layer.VDDrawingLayerTextView;
import com.vilyever.drawingview.layer.VDDrawingLayerViewProtocol;
import com.vilyever.drawingview.model.VDDrawingData;
import com.vilyever.drawingview.model.VDDrawingLayer;
import com.vilyever.drawingview.model.VDDrawingPoint;
import com.vilyever.drawingview.model.VDDrawingStep;
import com.vilyever.drawingview.util.VDRotationGestureDetector;

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
    private List<VDDrawingLayerViewProtocol> layerViews = new ArrayList<>();
    private VDDrawingLayerViewProtocol handlingLayerView;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private VDRotationGestureDetector rotationDetector;
    private View gestureView;
    private int gestureViewOperationState = GestureViewOperation.None.state();

    private OnTouchListener layerOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v instanceof VDDrawingLayerImageView) {
                VDDrawingLayerImageView imageView = (VDDrawingLayerImageView) v;
            }
            else if (v instanceof VDDrawingLayerTextView) {
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) v;
                if (textView.isEditing()) {
                    return false;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (self.gestureView == null) {
                    self.endUnfinishedStep();
                    self.gestureView = v;
                    self.handlingLayerView = (VDDrawingLayerViewProtocol) v;
                    self.handleLayer(self.handlingLayerView.getLayerHierarchy());
                }
            }
            return false;
        }
    };

    /* #Constructors */
    public VDDrawingView(Context context) {
        this(context, null);
    }

    public VDDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        self.init(context, attrs, defStyle);
    }

    /* #Overrides */
    private boolean shouldOnTouch; // for limit only first finger can draw.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.gestureView != null) {
            self.scaleGestureDetector.onTouchEvent(event);
            self.rotationDetector.onTouchEvent(event);
            self.gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {

                if (self.gestureViewOperationState == GestureViewOperation.None.state()) { // no change for gestureView
                    self.gestureView = null;
                    self.handlingLayerView = null;
                    return true;
                }


                if (self.gestureView instanceof VDDrawingLayerImageView) {
                    self.getDrawingData()
                            .newDrawingStepOnLayer(((VDDrawingLayerImageView) self.gestureView).getLayerHierarchy(), VDDrawingLayer.LayerType.Image)
                            .setStepType(VDDrawingStep.StepType.Frame)
                            .setBrush(VDBrush.copy(self.getBrush()));
                }
                else if (self.gestureView instanceof VDDrawingLayerTextView) { // when text view editing, disable move, scale and rotate
                    if (self.gestureViewOperationState == (GestureViewOperation.None.state() | GestureViewOperation.DoubleTap.state())) {
                        VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                        if (textView.isEditing()) {
                            self.gestureView = null;
                            self.gestureViewOperationState = GestureViewOperation.None.state();
                            return true;
                        }
                    }

                    self.getDrawingData()
                            .newDrawingStepOnLayer(((VDDrawingLayerTextView) self.gestureView).getLayerHierarchy(), VDDrawingLayer.LayerType.Text)
                            .setStepType(VDDrawingStep.StepType.Frame)
                            .setBrush(VDBrush.copy(self.getBrush()));
                }

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                self.getCurrentDrawingStep().getDrawingLayer().setLeft(layoutParams.leftMargin);
                self.getCurrentDrawingStep().getDrawingLayer().setTop(layoutParams.topMargin);
                self.getCurrentDrawingStep().getDrawingLayer().setWidth(layoutParams.width);
                self.getCurrentDrawingStep().getDrawingLayer().setHeight(layoutParams.height);
                self.getCurrentDrawingStep().getDrawingLayer().setScale(self.gestureView.getScaleX());
                self.getCurrentDrawingStep().getDrawingLayer().setRotation(self.gestureView.getRotation());

                self.gestureView = null;
                self.handlingLayerView = null;
                self.gestureViewOperationState = GestureViewOperation.None.state();

                self.getCurrentDrawingStep().setStepOver(true);
                self.didDrawNewStep();
            }
        }
        else {
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
                    self.beginDraw(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    self.drawing(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    self.endDraw(event.getX(), event.getY());
                    break;
            }
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
    private void init(Context context, AttributeSet attrs, int defStyle) {
        // layer view can draw border outside
        self.setClipChildren(false);
        self.setClipToPadding(false);

        self.addOnLayoutChangeListener(self);

        // focusable
        self.setFocusable(true);
        self.setFocusableInTouchMode(true);

        // setup base layer view
        self.baseLayerImageView = new VDDrawingLayerBaseView(self.getContext());
        self.baseLayerImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        self.addView(self.baseLayerImageView);
        self.layerViews.add(self.baseLayerImageView);

        // setup gesture listener
        self.gestureDetector = new GestureDetector(self.getContext(), new GestureListener());
        self.scaleGestureDetector = new ScaleGestureDetector(self.getContext(), new ScaleListener());
        self.rotationDetector = new VDRotationGestureDetector(new RotationListener());

        // clear the drawing view
        self.nativeClear();

        // init first drawing step that clear the view
        self.getDrawingData()
                .newDrawingStepOnBaseLayer()
                .setStepType(VDDrawingStep.StepType.Clear)
                .setStepOver(true);
        self.didDrawNewStep();
    }

    private void didDrawNewStep() {
        if (self.getDelegate() != null) {
            self.getDelegate().undoStateDidChange(self, self.canUndo(), self.canRedo());
        }
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
                    self.getDrawingData().newDrawingStepOnNextLayer(VDDrawingLayer.LayerType.Image).setStepType(VDDrawingStep.StepType.Draw).setBrush(VDBrush.copy(drawingBrush));
                    self.handlingLayerView = self.addDrawingLayerImageView(self.getCurrentDrawingStep().getDrawingLayer());
                    self.layerViews.add(self.handlingLayerView);
                    self.addView((View) self.handlingLayerView);
                }
                else {
                    self.getDrawingData()
                            .newDrawingStepOnBaseLayer()
                            .setStepType(VDDrawingStep.StepType.Draw)
                            .setBrush(VDBrush.copy(drawingBrush));
                    self.handlingLayerView = self.baseLayerImageView;
                }

                self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
                self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.Begin);
            }
            else if (self.getBrush() instanceof VDTextBrush) {
                VDTextBrush textBrush = self.getBrush();
                self.getDrawingData().newDrawingStepOnNextLayer(VDDrawingLayer.LayerType.Text).setStepType(VDDrawingStep.StepType.Frame).setBrush(VDBrush.copy(textBrush));

                self.handlingLayerView = self.addTextLayerView(self.getCurrentDrawingStep().getDrawingLayer());

                self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
                self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.Begin);
            }
            else if (self.getBrush() instanceof VDLayerEraserBrush) {
                self.getDrawingData()
                        .newDrawingStepOnBaseLayer()
                        .setStepType(VDDrawingStep.StepType.Draw)
                        .setBrush(VDBrush.copy(self.getBrush()));
                self.handlingLayerView = self.baseLayerImageView;

                self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
                self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.Begin);
            }

            self.handlingLayerView.setHandling(true);
        }
        else {
            self.drawing(x, y);
        }
    }

    private void drawing(float x, float y) {
        if (self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y))) {
            self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.Drawing);
        }
    }

    private void endDraw(float x, float y) {
        if (self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
            self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
            boolean stepOver = self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.End) != VDBrush.UnfinishFrame;

            self.getCurrentDrawingStep().setStepOver(stepOver);

            if (stepOver) {
                self.finishDraw();
            }
        }
        else if (self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text) {
            self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
            self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.End);

            self.cleanCurrentDrawing();
        }

        self.getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void finishDraw() {
        if (self.getCurrentDrawingStep().getBrush() instanceof VDDrawingBrush) {
            if (self.getCurrentDrawingStep().getDrawingLayer().getFrame() == null) {
                // noting to draw, e.g. draw line with one point is pointless
                VDDrawingBrush drawingBrush = self.getCurrentDrawingStep().getBrush();
                if (drawingBrush.isOneStrokeToLayer()) {
                    self.layerViews.remove(self.handlingLayerView);
                    self.removeView((View) self.handlingLayerView);
                    self.handlingLayerView = null;
                }

                self.cleanCurrentDrawing();
                self.getDrawingData().cancelDrawingStep();
                return;
            }
        }

        if (self.getCurrentDrawingStep().isStepOver()) {
            if (self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
                if (self.handlingLayerView == self.baseLayerImageView) {
                }
                else if (self.handlingLayerView instanceof VDDrawingLayerImageView) {
                    VDDrawingLayerImageView imageView = (VDDrawingLayerImageView) self.handlingLayerView;
                    imageView.setOnTouchListener(self.layerOnTouchListener);
                }
                else if (self.handlingLayerView instanceof VDDrawingLayerTextView) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                    textView.setOnTouchListener(self.layerOnTouchListener);
                }
            }

            self.cleanCurrentDrawing();

            self.didDrawNewStep();
        }
    }

    private void cleanCurrentDrawing() {
        if (self.handlingLayerView == self.baseLayerImageView) {
            self.handlingLayerView = null;
        }
    }

    private void endUnfinishedStep() {
        if (self.getCurrentDrawingStep() != null
                && !self.getCurrentDrawingStep().isStepOver()) {
            if (self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
                self.handlingLayerView.updateWithDrawingStep(self.getCurrentDrawingStep(), VDBrush.DrawingPointerState.ForceFinish);
                self.getCurrentDrawingStep().setStepOver(true);

                self.finishDraw();
            }
            else if(self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text) {
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                textView.endEdit();
                if (textView.isChanged()) {
                    self.getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());
                    self.getCurrentDrawingStep().setStepOver(true);
                    self.didDrawNewStep();
                }
                else {
                    if (textView.isFirstEditing()) {
                        self.layerViews.remove(textView);
                        self.removeView(textView);
                    }
                    self.getDrawingData().cancelDrawingStep();
                }
            }
        }
    }

    /**
     * generate an ImageView from current drawing layer
     * @param drawingLayer current drawing layer
     */
    private VDDrawingLayerImageView addDrawingLayerImageView(VDDrawingLayer drawingLayer) {
        drawingLayer.setScale(1.0f);
        drawingLayer.setRotation(0.0f);

        VDDrawingLayerImageView imageView = new VDDrawingLayerImageView(self.getContext());
        return imageView;
    }

    private VDDrawingLayerTextView addTextLayerView(VDDrawingLayer drawingLayer) {
        drawingLayer.setScale(1.0f);
        drawingLayer.setRotation(0.0f);

        VDDrawingLayerTextView textView = new VDDrawingLayerTextView(self.getContext());
        return textView;
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

    public void nativeSetBackgroundColor(int layerHierarchy, int color) {
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

    public void nativeSetBackgroundDrawable(int layerHierarchy, Drawable drawable) {
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
        for (VDDrawingLayerViewProtocol layerViewProtocol : self.layerViews) {
            layerViewProtocol.clearDrawing();
            if (layerViewProtocol != self.baseLayerImageView) {
                self.removeView((View) layerViewProtocol);
            }
        }

        if (self.layerViews.size() > 1) {
            self.layerViews.subList(1, self.layerViews.size()).clear();
        }

        self.baseLayerImageView.setBackground(null);
    }

    private void nativeDrawData() {
        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.baseLayerImageView.clearDrawing();

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
                step.setStepOver(true);

                if (step.getDrawingLayer().getBackgroundImageIdentifier() != null
                        || step.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    lastBackgroundIndex = i;
                }
            }

            if (layerHierarchy == 0) {
                self.baseLayerImageView.updateWithDrawingSteps(layerSteps.get(0));
            }
            else if (layerHierarchy > 0) { // 图层生成
                VDDrawingStep firstDrawingStep = layerSteps.get(layerHierarchy).get(0);
                switch (firstDrawingStep.getDrawingLayer().getLayerType()) {
                    case Image: {
                        VDDrawingLayerImageView imageView = self.addDrawingLayerImageView(firstDrawingStep.getDrawingLayer());
                        imageView.setOnTouchListener(self.layerOnTouchListener);
                        self.addView(imageView);
                        self.layerViews.add(imageView);

                        imageView.updateWithDrawingSteps(layerSteps.get(layerHierarchy));
                    }
                        break;
                    case Text: {
                        VDDrawingLayerTextView textView = self.addTextLayerView(firstDrawingStep.getDrawingLayer());
                        textView.setOnTouchListener(self.layerOnTouchListener);
                        self.addView(textView);
                        self.layerViews.add(textView);

                        textView.updateWithDrawingSteps(layerSteps.get(layerHierarchy));
                    }
                        break;
                }
            }

            if (lastBackgroundIndex >= 0) {
                VDDrawingStep lastBackgroundDrawingStep = layerSteps.get(layerHierarchy).get(lastBackgroundIndex);
                if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                    if (self.getDelegate() != null) {
                        self.nativeSetBackgroundDrawable(layerHierarchy, self.getDelegate().requireBackground(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                    }
                }
                else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    self.nativeSetBackgroundColor(layerHierarchy, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                }
            }
        }

        self.cleanCurrentDrawing();

        self.handleLayer(UnhandleAnyLayer);
        self.getParent().requestDisallowInterceptTouchEvent(false);
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
                .setStepType(VDDrawingStep.StepType.Clear)
                .setStepOver(true);
        self.didDrawNewStep();
    }

    public void drawStep(VDDrawingStep step) {
        if (step == null) {
            return;
        }
        if (step.isCleared() && self.getCurrentDrawingStep().isCleared()) {
            return;
        }
        self.endUnfinishedStep();
        self.getDrawingData().addDrawingStep(step);
        self.didDrawNewStep();
        self.nativeDrawData();
    }

    public void drawData(VDDrawingData data) {
        if (data == null) {
            return;
        }

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

        VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Image;
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
                .setStepOver(true)
                .getDrawingLayer()
                .setBackgroundColor(color);
        self.didDrawNewStep();
    }

    public void setBackgroundDrawable(int layerHierarchy, Drawable drawable, String identifier) {
        if (layerHierarchy < 0) {
            return;
        }

        self.endUnfinishedStep();
        self.nativeSetBackgroundDrawable(layerHierarchy, drawable);

        VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Image;
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
                .setStepOver(true)
                .getDrawingLayer()
                .setBackgroundImageIdentifier(identifier);
        self.didDrawNewStep();
    }

    public boolean undo() {
        if (self.canUndo()) {
            self.endUnfinishedStep();
            self.getDrawingData().undo();
            self.nativeClear();
            self.nativeDrawData();

            if (self.getDelegate() != null) {
                self.getDelegate().undoStateDidChange(self, self.canUndo(), self.canRedo());
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
                self.getDelegate().undoStateDidChange(self, self.canUndo(), self.canRedo());
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
    private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        float originalLeftMargin;
        float originalTopMargin;
        float beginX;
        float beginY;

        @Override
        public boolean onDown(MotionEvent e) {
            if (self.gestureView != null) {
                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                originalLeftMargin = layoutParams.leftMargin;
                originalTopMargin = layoutParams.topMargin;

                beginX = e.getRawX();
                beginY = e.getRawY();
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if ((self.gestureViewOperationState & GestureViewOperation.Moving.state())  != GestureViewOperation.Moving.state()) {
                self.gestureViewOperationState = self.gestureViewOperationState | GestureViewOperation.Moving.state();
            }

            if (self.gestureView instanceof VDDrawingLayerTextView) {
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                if (textView.isEditing()) {
                    return true;
                }
            }

            // if scaling or rotating, disable moving
            if (self.gestureView != null
                    && (self.gestureViewOperationState == (GestureViewOperation.None.state() | GestureViewOperation.Moving.state()))) {
                float dx = e2.getRawX() - beginX;
                float dy = e2.getRawY() - beginY;

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                layoutParams.leftMargin = (int) Math.floor(originalLeftMargin + dx);
                layoutParams.topMargin = (int) Math.floor(originalTopMargin + dy);
                self.gestureView.setLayoutParams(layoutParams);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (self.gestureView != null
                    && self.gestureView instanceof VDDrawingLayerTextView) {
                self.gestureViewOperationState = self.gestureViewOperationState | GestureViewOperation.DoubleTap.state();

                self.getDrawingData()
                        .newDrawingStepOnLayer(((VDDrawingLayerTextView) self.gestureView).getLayerHierarchy(), VDDrawingLayer.LayerType.Text)
                        .setStepType(VDDrawingStep.StepType.Text)
                        .setBrush(VDBrush.copy(self.getBrush()));
                self.getCurrentDrawingStep().getDrawingLayer().setLayerType(VDDrawingLayer.LayerType.Text);
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                textView.beginEdit(false);
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (self.gestureView != null
                    && ((self.gestureViewOperationState & GestureViewOperation.Scaling.state()) == GestureViewOperation.Scaling.state()) ) {

                if (self.gestureView instanceof VDDrawingLayerTextView) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                    if (textView.isEditing()) {
                        return true;
                    }
                }

                float scaleFactor = detector.getScaleFactor();
//                self.gestureView.setScaleX(Math.max(1.0f, self.gestureView.getScaleX() * scaleFactor));
//                self.gestureView.setScaleY(Math.max(1.0f, self.gestureView.getScaleY() * scaleFactor));
                self.gestureView.setScaleX(self.gestureView.getScaleX() * scaleFactor);
                self.gestureView.setScaleY(self.gestureView.getScaleY() * scaleFactor);
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if ((self.gestureViewOperationState & GestureViewOperation.Scaling.state()) != GestureViewOperation.Scaling.state()) {
                self.gestureViewOperationState = self.gestureViewOperationState | GestureViewOperation.Scaling.state();
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    private class RotationListener implements VDRotationGestureDetector.OnRotationGestureListener {
        private static final float TriggerAngle = 10.0f;
        private float originalRotation;
        private float triggerOffset;
        @Override
        public void OnRotation(VDRotationGestureDetector rotationDetector) {
            float angle = rotationDetector.getAngle();

            if (Math.abs(angle) > TriggerAngle
                    && ((self.gestureViewOperationState & GestureViewOperation.Rotation.state()) != GestureViewOperation.Rotation.state()) ) {
                self.gestureViewOperationState = self.gestureViewOperationState | GestureViewOperation.Rotation.state();
                originalRotation = self.gestureView.getRotation();
                triggerOffset = -Math.signum(angle) * TriggerAngle;
            }

            if (self.gestureView != null
                    && ((self.gestureViewOperationState & GestureViewOperation.Rotation.state()) == GestureViewOperation.Rotation.state()) ) {

                if (self.gestureView instanceof VDDrawingLayerTextView) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                    if (textView.isEditing()) {
                        return;
                    }
                }

                self.gestureView.setRotation(-(angle + triggerOffset - originalRotation));
            }
        }
    }

    /* #Interfaces */
    public interface DrawingDelegate {
        void undoStateDidChange(VDDrawingView drawingView, boolean canUndo, boolean canRedo);
        Drawable requireBackground(String identifier);
    }

    /* #Annotations @interface */

    /* #Enums */
    private enum GestureViewOperation {
        None, Moving, Scaling, Rotation, DoubleTap;
        public int state() {
            int state = 0;
            switch (this) {
                case None:
                    state = 0x00000000;
                    break;
                case Moving:
                    state = 0x00000001;
                    break;
                case Scaling:
                    state = 0x00000010;
                    break;
                case Rotation:
                    state = 0x00000100;
                    break;
                case DoubleTap:
                    state = 0x00001000;
                    break;
            }
            return state;
        }
    }
}
