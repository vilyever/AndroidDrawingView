package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.VDDrawingBrush;
import com.vilyever.drawingview.brush.VDPenBrush;
import com.vilyever.drawingview.brush.VDTextBrush;
import com.vilyever.filereadwrite.VDFileConstant;
import com.vilyever.filereadwrite.VDFileReader;
import com.vilyever.filereadwrite.VDFileWriter;

import java.io.File;
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

    private static boolean BeforeFirstDrawingViewCreated = true;

    private static final int UnfocusAnyLayer = -1;
    private static final int FocusAllLayer = -2;

    private boolean initialed;

    private DrawingDelegate delegate;

    private VDDrawingData drawingData;

    private VDBrush brush;

    private Bitmap baseBitmap;
    private Canvas baseCanvas;
    private ImageView baseImageView;

    private Bitmap drawingLayerBitmap;
    private Canvas drawingLayerCanvas;
    private ImageView drawingLayerImageView;

    private Bitmap calibrateLayerBitmap;
    private Canvas calibrateLayerCanvas;

    private List<View> layerViews = new ArrayList<>();
    private View handlingLayerView;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private VDRotationGestureDetector rotationDetector;
    private View gestureView;
    private int gestureViewOperationState = GestureViewOperation.None.state();

    private File drawingCacheDir;

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
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        self.initial();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        self.destroy();
    }

    private boolean shouldOnTouch; // for limit only first finger can draw.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.gestureView != null) {
            self.scaleGestureDetector.onTouchEvent(event);
            self.rotationDetector.onTouchEvent(event);
            self.gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {

                if (self.gestureViewOperationState == GestureViewOperation.None.state()) {
                    self.gestureView = null;
                    self.handlingLayerView = null;
                    return true;
                }

                if (self.gestureView instanceof VDDrawingLayerTextView
                        && self.gestureViewOperationState == (GestureViewOperation.None.state() | GestureViewOperation.DoubleTap.state())) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                    if (textView.isEditing()) {
                        self.gestureView = null;
                        self.gestureViewOperationState = GestureViewOperation.None.state();
                        return true;
                    }
                }

                self.getDrawingData().newDrawingStepOnLayer((int) self.gestureView.getTag()).setBrush(VDBrush.copy(self.getBrush()));

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                self.getDrawingData().drawingStep().drawingLayer().setLeft(layoutParams.leftMargin);
                self.getDrawingData().drawingStep().drawingLayer().setTop(layoutParams.topMargin);
                self.getDrawingData().drawingStep().drawingLayer().setWidth(layoutParams.width);
                self.getDrawingData().drawingStep().drawingLayer().setHeight(layoutParams.height);
                self.getDrawingData().drawingStep().drawingLayer().setScale(self.gestureView.getScaleX());
                self.getDrawingData().drawingStep().drawingLayer().setRotation(self.gestureView.getRotation());

                self.gestureView.invalidate();
                self.gestureView = null;
                self.handlingLayerView = null;
                self.gestureViewOperationState = GestureViewOperation.None.state();

                self.getDrawingData().drawingStep().setStepOver(true);
                self.didDrawNewStep();
                self.invalidate();
            }
        }
        else {
            int action = event.getAction();
            int maskAction = event.getAction() & MotionEvent.ACTION_MASK;

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

    public VDDrawingStep getDrawingStep() {
        return self.getDrawingData().drawingStep();
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

    public Canvas getBaseCanvas() {
        return baseCanvas;
    }

    public Canvas getDrawingLayerCanvas() {
        return drawingLayerCanvas;
    }

    public File getDrawingCacheDir() {
        if (self.drawingCacheDir == null) {
            self.drawingCacheDir = VDFileConstant.getCacheDir(self.getClass().getSimpleName() + "/" + self.hashCode());
        }
        return drawingCacheDir;
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

        System.out.println("width " + width + "  ,  height " + height);

        if (width <= 0 || height <= 0) {
            return;
        }

        WindowManager wm = (WindowManager) VDContextHolder.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (width > size.x || height > size.y) {
            return;
        }

        if (changed) {
            if (self.baseBitmap != null) {
                self.baseImageView.setImageBitmap(null);
                self.baseBitmap.recycle();
            }

            self.baseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            self.baseCanvas = new Canvas(self.baseBitmap);
            self.baseCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            self.baseImageView.setImageBitmap(self.baseBitmap);

            self.getDrawingData().getDrawingSteps().get(0).drawingLayer().setFrame(new RectF(0, 0, width, height));
            self.nativeClear();
            self.nativeDrawData();
        }
    }

    /* #Private Methods */
    private void init(Context context, AttributeSet attrs, int defStyle) {
//    }
//
//    private void initial() {
        // clear caches
        if (BeforeFirstDrawingViewCreated) {
            BeforeFirstDrawingViewCreated = false;
            VDDrawingView.clearStorageCaches();
            VDFileConstant.getCacheDir(self.getClass().getSimpleName());
        }

        if (!self.initialed) {
            // layer view can display ouside
            disableClipOnParents(self);

            self.addOnLayoutChangeListener(self);

            // focusable
            self.setFocusable(true);
            self.setFocusableInTouchMode(true);

            // clear the drawing view
            self.nativeClear();

            // init first drawing step that clear the view
            self.getDrawingData().newDrawingStepOnLayer(0).setCleared(true).setStepOver(true);
            self.didDrawNewStep();

            // setup base layer view
            self.baseImageView = new ImageView(self.getContext());
            self.baseImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            self.baseImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            self.baseImageView.setTag(0);
            self.addView(self.baseImageView);
            self.layerViews.add(self.baseImageView);

            // setup drawing layer view, which is a temp display view
            self.drawingLayerImageView = new ImageView(self.getContext());
            self.drawingLayerImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            self.drawingLayerImageView.setScaleType(ImageView.ScaleType.CENTER);
            self.addView(self.drawingLayerImageView);

            // setup gesture listener
            self.gestureDetector = new GestureDetector(self.getContext(), new GestureListener());
            self.scaleGestureDetector = new ScaleGestureDetector(self.getContext(), new ScaleListener());
            self.rotationDetector = new VDRotationGestureDetector(new RotationListener());

            self.initialed = true;
        }
    }

    private static void disableClipOnParents(View v) {
        if (v == null) {
            return;
        }

        if (v instanceof ViewGroup) {
            ( (ViewGroup) v).setClipChildren(false);
            ( (ViewGroup) v).setClipToPadding(false);
        }

        if (v.getParent() != null
                && v.getParent() instanceof View) {
            disableClipOnParents((View) v.getParent());
        }
    }

    private void didDrawNewStep() {
        if (self.getDelegate() != null) {
            self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
        }
    }

    private void beforeDrawing() {
        self.drawingLayerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
            VDDrawingBrush drawingBrush = self.getBrush();
            if (drawingBrush.isEraser()) {
                self.drawingLayerCanvas.drawBitmap(self.baseBitmap, 0.0f, 0.0f, null);
            }
        }
    }

    private void beginDraw(float x, float y) {
        self.endUnfinishedStep();

        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.focusLayer(UnfocusAnyLayer);

        VDDrawingPoint.IncrementPointerID();

        if (self.getDrawingData().drawingStep().isStepOver()) {
            self.drawingLayerBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
            self.drawingLayerCanvas = new Canvas(self.drawingLayerBitmap);
            self.drawingLayerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            self.drawingLayerImageView.setImageBitmap(self.drawingLayerBitmap);

            self.drawingLayerImageView.setVisibility(VISIBLE);
            self.drawingLayerImageView.bringToFront();

            if (self.getBrush() instanceof VDDrawingBrush) {
                VDDrawingBrush drawingBrush = self.getBrush();
                if (drawingBrush.isEraser()) {
                    self.baseImageView.setVisibility(INVISIBLE);
                    self.removeView(self.drawingLayerImageView);
                    self.addView(self.drawingLayerImageView, 1);
                    self.getDrawingData().newDrawingStepOnLayer(0).setDrawingCanvas(self.drawingLayerCanvas).setBrush(VDBrush.copy(drawingBrush));
                }
                else {
                    if (drawingBrush.isOneStrokeToLayer()) {
                        self.getDrawingData().newDrawingStepOnLayer().setDrawingCanvas(self.drawingLayerCanvas).setBrush(VDBrush.copy(drawingBrush));
                    } else {
                        self.getDrawingData().newDrawingStepOnLayer(0).setDrawingCanvas(self.drawingLayerCanvas).setBrush(VDBrush.copy(drawingBrush));
                    }
                }

                self.beforeDrawing();
                self.getDrawingData().drawingStep().drawingPath().addPoint(new VDDrawingPoint(VDDrawingPoint.CurrentPointerID(), x, y));
                self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.Begin);
            }
            else if (self.getBrush() instanceof VDTextBrush) {
                VDTextBrush textBrush = self.getBrush();
                self.getDrawingData().newDrawingStepOnLayer().setDrawingCanvas(self.drawingLayerCanvas).setBrush(VDBrush.copy(textBrush));
                self.getDrawingData().drawingStep().drawingLayer().setLayerType(VDDrawingLayer.LayerType.Text);

                self.beforeDrawing();
                self.getDrawingData().drawingStep().drawingPath().addPoint(new VDDrawingPoint(VDDrawingPoint.CurrentPointerID(), x, y));
                self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.Begin);

                VDDrawingLayerTextView textView = self.addTextLayerView(self.getDrawingData().drawingStep().drawingLayer());
                textView.setTextSize(textBrush.getSize());
                textView.setTextColor(textBrush.getColor());
                textView.setTypeface(null, textBrush.getTypefaceStyle());
            }
        }
        else {
            self.drawing(x, y);
        }

        self.invalidate();
    }

    private void drawing(float x, float y) {
        if (self.getDrawingData().drawingStep().drawingPath().addPoint(new VDDrawingPoint(VDDrawingPoint.CurrentPointerID(), x, y))) {
            self.beforeDrawing();
            self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.Drawing);
            self.invalidate();

            if (self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text) {
                if (self.handlingLayerView != null) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                    textView.setLayoutParams(self.getDrawingData().drawingStep().drawingLayer().getLayoutParams());
                }
            }
        }
    }

    private void endDraw(float x, float y) {
        if (self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
            self.beforeDrawing();
            self.getDrawingData().drawingStep().drawingPath().addPoint(new VDDrawingPoint(VDDrawingPoint.CurrentPointerID(), x, y));
            boolean stepOver = self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.End) != VDBrush.UnfinishFrame;
            self.getDrawingData().drawingStep().setStepOver(stepOver);
            self.invalidate();

            if (stepOver) {
                self.finishDraw();
            }
        }
        else if (self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text) {
            self.beforeDrawing();
            self.getDrawingData().drawingStep().drawingPath().addPoint(new VDDrawingPoint(VDDrawingPoint.CurrentPointerID(), x, y));
            self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.End);
            self.invalidate();

            if (self.handlingLayerView != null) {
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                textView.setLayoutParams(self.getDrawingData().drawingStep().drawingLayer().getLayoutParams());
                textView.beginEdit(true);
            }

            self.cleanCurrentDrawing();
        }

        self.getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void finishDraw() {
        if (self.getDrawingData().drawingStep().getDrawingLayerFrame() == null) {
            // noting to draw, e.g. draw line with one point is pointless
            self.getDrawingData().cancelDrawingStep();
            return;
        }

        if (self.getDrawingData().drawingStep().isStepOver()) {
            if (self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
                VDDrawingBrush drawingBrush = self.getDrawingData().drawingStep().drawingBrush();
                if (!drawingBrush.isEraser()) {
                    if (drawingBrush.isOneStrokeToLayer()) {
                        // recreate a layer canvas that fit the layer frame
                        self.calibrateLayerBitmap = Bitmap.createBitmap((int) self.getDrawingData().drawingStep().drawingLayer().getWidth(),
                                (int) self.getDrawingData().drawingStep().drawingLayer().getHeight(),
                                Bitmap.Config.ARGB_8888);
                        self.calibrateLayerCanvas = new Canvas(self.calibrateLayerBitmap);
                        self.calibrateLayerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        self.getDrawingData().drawingStep().setDrawingCanvas(self.calibrateLayerCanvas);
                        self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.CalibrateToOrigin);
                        self.invalidate();
                        self.addDrawingLayerImageView(self.getDrawingData().drawingStep().drawingLayer());

                        self.calibrateLayerBitmap.recycle();
                        self.calibrateLayerBitmap = null;
                        self.calibrateLayerCanvas = null;
                    }
                    else {
                        self.baseCanvas.drawBitmap(self.drawingLayerBitmap, 0.0f, 0.0f, null);
                    }
                }
                else {
                    self.getDrawingData().drawingStep().setDrawingCanvas(self.baseCanvas);
                    self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.End);
                }
            }

            self.cleanCurrentDrawing();

            self.didDrawNewStep();
        }
    }

    private void cleanCurrentDrawing() {
        self.drawingLayerImageView.setImageBitmap(null);
        self.drawingLayerBitmap.recycle();
        self.drawingLayerBitmap = null;
        self.drawingLayerCanvas = null;

        self.baseImageView.setVisibility(VISIBLE);
        self.drawingLayerImageView.setVisibility(INVISIBLE);
    }

    private void endUnfinishedStep() {
        if (self.getDrawingData() != null
                && self.getDrawingData().drawingStep() != null
                && !self.getDrawingData().drawingStep().isStepOver()) {
            if (self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Image) {
                self.beforeDrawing();
                self.getDrawingData().drawingStep().updateDrawing(VDBrush.DrawingPointerState.ForceFinish);
                self.getDrawingData().drawingStep().setStepOver(true);
                self.invalidate();

                self.finishDraw();
            }
            else if(self.getDrawingData().drawingStep().drawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text) {
                if (self.handlingLayerView != null) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.handlingLayerView;
                    textView.endEdit();
                    if (textView.isChanged()) {
                        self.getDrawingData().drawingStep().drawingLayer().setText(textView.getText().toString());
                        self.getDrawingData().drawingStep().setStepOver(true);
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
    }

    /**
     * generate an ImageView from current drawing layer
     * @param drawingLayer current drawing layer
     */
    private VDDrawingLayerImageView addDrawingLayerImageView(VDDrawingLayer drawingLayer) {
        drawingLayer.setScale(1.0f);
        drawingLayer.setRotation(0.0f);

        VDDrawingLayerImageView imageView = new VDDrawingLayerImageView(self.getContext());
        imageView.setLayoutParamsWithDefaultPadding(drawingLayer.getLayoutParams());
        imageView.setTag(drawingLayer.getHierarchy());

        Bitmap bitmap = Bitmap.createBitmap((int) Math.floor(drawingLayer.getWidth()),
                                            (int) Math.floor(drawingLayer.getHeight()),
                                            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawBitmap(self.calibrateLayerBitmap, 0, 0, null);

        imageView.setImageBitmap(bitmap);

        imageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (self.gestureView == null) {
                        self.endUnfinishedStep();
                        self.gestureView = v;
                        self.handlingLayerView = v;
                        self.focusLayer((int) v.getTag());
                    }
                }
                return false;
            }
        });

        self.addView(imageView);
        self.layerViews.add(imageView);
        self.handlingLayerView = imageView;

        self.focusLayer(drawingLayer.getHierarchy());

        return imageView;
    }

    private VDDrawingLayerTextView addTextLayerView(VDDrawingLayer drawingLayer) {
        drawingLayer.setScale(1.0f);
        drawingLayer.setRotation(0.0f);

        final VDDrawingLayerTextView textView = new VDDrawingLayerTextView(self.getContext());
        textView.setLayoutParamsWithDefaultPadding(drawingLayer.getLayoutParams());
        textView.setTag(drawingLayer.getHierarchy());

        textView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                VDDrawingLayerTextView tv = (VDDrawingLayerTextView) v;
                if (tv.isEditing()) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (self.gestureView == null) {
                        self.endUnfinishedStep();
                        self.gestureView = v;
                        self.handlingLayerView = v;
                        self.focusLayer((int) v.getTag());
                    }
                }
                return false;
            }
        });

        self.addView(textView);
        self.layerViews.add(textView);
        self.handlingLayerView = textView;

        self.focusLayer(drawingLayer.getHierarchy());

        return textView;
    }

    private void focusLayer(int layerHierarchy) {
        if (layerHierarchy <= 0) {
            layerHierarchy = UnfocusAnyLayer;
        }
        for (View view : self.layerViews) {
            view.setSelected((view.getTag() == layerHierarchy) || (layerHierarchy == FocusAllLayer));
        }
    }

    private View findLayerViewByLayerHierarchy(int layerHierarchy) {
        for (View view : self.layerViews) {
            if (view.getTag() == layerHierarchy) {
                return view;
            }
        }

        return null;
    }

    public void nativeSetBackgroundColor(int color, int layerHierarchy) {
        if (layerHierarchy < 0) {
            return;
        }

        View layerView = self.findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerView == null) {
            return;
        }

        layerView.setBackgroundColor(color);
        self.invalidate();
    }

    public void nativeSetBackgroundImage(Bitmap bitmap, int layerHierarchy) {
        if (layerHierarchy < 0) {
            return;
        }

        View layerView = self.findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerView == null) {
            return;
        }

        if (layerView.getBackground() != null
                && layerView.getBackground() instanceof BitmapDrawable) {
            Bitmap preBitmap = ((BitmapDrawable) layerView.getBackground()).getBitmap();
            if (preBitmap != null) {
                preBitmap.recycle();
            }
        }

        layerView.setBackground(new BitmapDrawable(layerView.getResources(), bitmap));

        self.invalidate();
    }

    private void nativeClear() {
        if (self.baseBitmap != null) {
            self.baseCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        for (View view : self.layerViews) {
            if (view.getBackground() != null
                    && view.getBackground() instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) view.getBackground()).getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }

            view.setBackground(null);

            if (view != self.baseImageView) { // never recycle the bastBitmap until this view initialed
                if (view instanceof ImageView) {

                    ImageView imageView = (ImageView) view;
                    if (imageView.getDrawable() != null
                            && imageView.getDrawable() instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                    }
                }
                self.removeView(view);
            }
        }

        if (self.layerViews.size() > 1) {
            self.layerViews.subList(1, self.layerViews.size()).clear();
        }
    }

    private void nativeDrawData() {
        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.drawingLayerBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888);
        self.drawingLayerCanvas = new Canvas(self.drawingLayerBitmap);
        self.drawingLayerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        self.drawingLayerImageView.setImageBitmap(self.drawingLayerBitmap);

        List<VDDrawingStep> stepsToDraw = self.getDrawingData().stepsToDraw();

        List<List<VDDrawingStep>> layerSteps = new ArrayList<>();
        for (int i = 0; i < stepsToDraw.size(); i++) {
            VDDrawingStep step = stepsToDraw.get(i);

            while (layerSteps.size() <= step.drawingLayer().getHierarchy()) {
                layerSteps.add(new ArrayList<VDDrawingStep>());
            }
            layerSteps.get(step.drawingLayer().getHierarchy()).add(step);
        }

        for (int layerHierarchy = 0; layerHierarchy < layerSteps.size(); layerHierarchy++) {
            if (layerSteps.get(layerHierarchy).size() == 0) {
                continue;
            }

            int lastBackgroundIndex = -1;
            int lastImageIndex = -1;

            self.drawingLayerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (int i = 0; i < layerSteps.get(layerHierarchy).size(); i++) {
                VDDrawingStep step = layerSteps.get(layerHierarchy).get(i);
                step.setStepOver(true);

                if (layerHierarchy == 0) {
                    if (step.drawingPath() != null) {
                        VDDrawingBrush drawingBrush = step.drawingBrush();
                        if (drawingBrush.isEraser()) {
                            step.setDrawingCanvas(self.baseCanvas);
                            step.updateDrawing(VDBrush.DrawingPointerState.ForceFinish);
                        } else {
                            self.drawingLayerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            step.setDrawingCanvas(self.drawingLayerCanvas);
                            step.updateDrawing(VDBrush.DrawingPointerState.ForceFinish);
                            self.baseCanvas.drawBitmap(self.drawingLayerBitmap, 0.0f, 0.0f, null);
                        }
                    }
                }

                if (step.drawingLayer().getBackgroundImageIdentifier() != null
                        || step.drawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    lastBackgroundIndex = i;
                }

                if (step.drawingLayer().getImageIdentifer() != null) {
                    lastImageIndex = i;
                }
            }

            if (layerHierarchy > 0) {
                VDDrawingStep firstDrawingStep = layerSteps.get(layerHierarchy).get(0);
                switch (firstDrawingStep.drawingLayer().getLayerType()) {
                    case Image: {
                        // recreate a layer canvas that fit the layer frame
                        if (self.calibrateLayerBitmap != null) {
                            self.calibrateLayerBitmap.recycle();
                        }
                        self.calibrateLayerBitmap = Bitmap.createBitmap((int) Math.floor(firstDrawingStep.drawingLayer().getWidth()),
                                (int) Math.floor(firstDrawingStep.drawingLayer().getHeight()),
                                        Bitmap.Config.ARGB_8888);
                        self.calibrateLayerCanvas = new Canvas(self.calibrateLayerBitmap);

                        for (VDDrawingStep step : layerSteps.get(layerHierarchy)) {
                            if (step.drawingPath() != null) {
                                step.setDrawingCanvas(self.calibrateLayerCanvas);
                                step.updateDrawing(VDBrush.DrawingPointerState.CalibrateToOrigin);
                            }
                        }
                        VDDrawingLayerImageView imageView = self.addDrawingLayerImageView(firstDrawingStep.drawingLayer());

                        self.calibrateLayerBitmap.recycle();
                        self.calibrateLayerBitmap = null;
                        self.calibrateLayerCanvas = null;

                        for (VDDrawingStep step : layerSteps.get(layerHierarchy)) {
                            VDDrawingLayer layer = step.drawingLayer();
                            if (layer != null) {
                                if (layer.getLayoutParams() != null) {
                                    imageView.setLayoutParamsWithDefaultPadding(layer.getLayoutParams());
                                }

                                if (layer.getScale() != VDDrawingLayer.UnsetValue) {
                                    imageView.setScaleX(layer.getScale());
                                    imageView.setScaleY(layer.getScale());
                                }

                                if (layer.getRotation() != VDDrawingLayer.UnsetValue) {
                                    imageView.setRotation(layer.getRotation());
                                }
                            }
                        }

                        if (lastImageIndex > 0) {
                            // TODO: 2015/9/18 add imageview with image from imagePath
                        }
                    }
                        break;
                    case Text: {
                        // TODO: 2015/9/18 add edittext with text
                        for (VDDrawingStep step : layerSteps.get(layerHierarchy)) {
                            if (step.drawingPath() != null) {
                                step.setDrawingCanvas(self.drawingLayerCanvas);
                                step.updateDrawing(VDBrush.DrawingPointerState.End);
                            }
                        }
                        VDDrawingLayerTextView textView = self.addTextLayerView(firstDrawingStep.drawingLayer());
                        VDTextBrush textBrush = firstDrawingStep.drawingBrush();
                        textView.setTextSize(textBrush.getSize());
                        textView.setTextColor(textBrush.getColor());
                        textView.setTypeface(null, textBrush.getTypefaceStyle());

                        for (VDDrawingStep step : layerSteps.get(layerHierarchy)) {
                            VDDrawingLayer layer = step.drawingLayer();
                            if (layer != null) {
                                if (layer.getText() != null) {
                                    textView.setText(layer.getText());
                                }

                                if (layer.getLayoutParams() != null) {
                                    textView.setLayoutParamsWithDefaultPadding(layer.getLayoutParams());
                                }

                                if (layer.getScale() != VDDrawingLayer.UnsetValue) {
                                    textView.setScaleX(layer.getScale());
                                    textView.setScaleY(layer.getScale());
                                }

                                if (layer.getRotation() != VDDrawingLayer.UnsetValue) {
                                    textView.setRotation(layer.getRotation());
                                }
                            }
                        }
                    }
                        break;
                }
            }

            if (lastBackgroundIndex >= 0) {
                VDDrawingStep lastBackgroundDrawingStep = layerSteps.get(layerHierarchy).get(lastBackgroundIndex);
                if (lastBackgroundDrawingStep.drawingLayer().getBackgroundImageIdentifier() != null) {
                    if (self.getDrawingCacheDir() != null) {
                        String filePath = self.getDrawingCacheDir().getAbsolutePath() + "/" + lastBackgroundDrawingStep.drawingLayer().getBackgroundImageIdentifier();
                        File imageFile = new File(filePath);
                        if (imageFile.exists()) {
                            self.nativeSetBackgroundImage(VDFileReader.readBitmap(imageFile), layerHierarchy);
                        }
                        else {
                            // TODO: 2015/9/24 call delegate to put the image by identifier
                        }
                    }
                }
                else if (lastBackgroundDrawingStep.drawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    self.nativeSetBackgroundColor(lastBackgroundDrawingStep.drawingLayer().getBackgroundColor(), layerHierarchy);
                }
            }
        }

        self.drawingLayerImageView.setImageBitmap(null);
        self.drawingLayerBitmap.recycle();
        self.drawingLayerBitmap = null;
        self.drawingLayerCanvas = null;

        self.focusLayer(-1);
        self.getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void destroy() {
        self.nativeClear();
        if (self.baseBitmap != null) {
            self.baseImageView.setImageBitmap(null);
            self.baseBitmap.recycle();
            self.baseBitmap = null;
            self.baseCanvas = null;
        }

        if (self.drawingLayerBitmap != null) {
            self.drawingLayerImageView.setImageBitmap(null);
            self.drawingLayerBitmap.recycle();
            self.drawingLayerBitmap = null;
            self.drawingLayerCanvas = null;
        }

//        VDFileWriter.clearDir(VDFileConstant.getCacheDir(self.getClass().getSimpleName() + "/ " + self.hashCode()), true);
    }

    private static void clearStorageCaches() {
        VDFileWriter.clearDir(VDFileConstant.getCacheDir(VDDrawingView.class.getSimpleName()), false);
    }

    /* #Public Methods */
    public void clear() {
        if (self.getDrawingData().drawingStep().isCleared()) { // means current state is clear
            return;
        }
        self.endUnfinishedStep();
        self.nativeClear();
        self.getDrawingData().newDrawingStepOnLayer(0).setCleared(true).setStepOver(true);
        self.didDrawNewStep();
        self.invalidate();
    }

    public void drawStep(VDDrawingStep step) {
        if (step == null) {
            return;
        }
        if (step.isCleared() && self.getDrawingData().drawingStep().isCleared()) {
            return;
        }
        self.endUnfinishedStep();
        self.getDrawingData().addDrawingStep(step);
        self.didDrawNewStep();
        self.nativeDrawData();
        self.invalidate();
    }

    public void drawData(VDDrawingData data) {
        if (data == null) {
            return;
        }

        self.drawingData = data;
        self.nativeClear();
        self.nativeDrawData();
        self.invalidate();
    }

    public void setBackgroundColor(int color, int layerHierarchy) {
        self.endUnfinishedStep();
        self.nativeSetBackgroundColor(color, layerHierarchy);

        self.getDrawingData().newDrawingStepOnLayer(layerHierarchy).setStepOver(true).drawingLayer().setBackgroundColor(color);
        self.didDrawNewStep();
    }

    public void setBackgroundImage(Bitmap bitmap, String identifier, int layerHierarchy) {
        self.endUnfinishedStep();
        self.nativeSetBackgroundImage(bitmap, layerHierarchy);

        if (self.getDrawingCacheDir() != null) {
            String filePath = self.getDrawingCacheDir() + "/" + identifier;
            VDFileWriter.writeBitmap(new File(filePath), bitmap);
        }
        self.getDrawingData().newDrawingStepOnLayer(layerHierarchy).setStepOver(true).drawingLayer().setBackgroundImageIdentifier(identifier);
        self.didDrawNewStep();
    }

    public boolean undo() {
        if (self.canUndo()) {
            self.endUnfinishedStep();
            self.getDrawingData().undo();
            self.nativeClear();
            self.nativeDrawData();
            self.invalidate();

            if (self.getDelegate() != null) {
                self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
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
            self.invalidate();

            if (self.getDelegate() != null) {
                self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
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

    public void removeLayersAtPoint(VDDrawingPoint point) {

    }

    public void removeLayersInRect(RectF rect) {

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

                self.gestureView.invalidate();
                self.invalidate();
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

                self.getDrawingData().newDrawingStepOnLayer((int) self.gestureView.getTag()).setBrush(VDBrush.copy(self.getBrush()));
                self.getDrawingData().drawingStep().drawingLayer().setLayerType(VDDrawingLayer.LayerType.Text);
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

                self.gestureView.invalidate();
                self.invalidate();
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

                self.gestureView.invalidate();
                self.invalidate();
            }
        }
    }

    /* #Interfaces */
    public interface DrawingDelegate {
        void undoStateDidChangeFromDrawingView(VDDrawingView drawingView, boolean canUndo, boolean canRedo);
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
