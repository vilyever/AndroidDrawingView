package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
public class VDDrawingView extends RelativeLayout {
    private final VDDrawingView self = this;

    private static boolean BeforeFirstDrawingViewCreated = true;

    private static final int UnfocusAnyLayer = -1;
    private static final int FocusAllLayer = -2;

    private DrawingDelegate delegate;

    private VDDrawingData drawingData;

    private VDDrawingBrush drawingBrush;

    private Bitmap baseBitmap;
    private Canvas baseCanvas;
    private ImageView baseImageView;

    private Bitmap layerBitmap;
    private Canvas layerCanvas;
    private ImageView layerImageView;

    private List<View> layerViews = new ArrayList<>();

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private VDRotationGestureDetector rotationDetector;
    private View gestureView;
    private int gestureViewOperationState = GestureViewOperation.None.state();

    private File drawingCacheDir;

    /* #Constructors */
    public VDDrawingView(Context context) {
        super(context);
        self.init(context, null, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        self.init(context, attrs, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        self.init(context, attrs, defStyle);
    }

    /* #Overrides */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        self.initial();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        self.destroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.gestureView != null) {
            self.scaleGestureDetector.onTouchEvent(event);
            self.rotationDetector.onTouchEvent(event);
            self.gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                self.getDrawingData().newDrawingStepOnLayer((int) self.gestureView.getTag());

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                self.getDrawingData().drawingStep().drawingLayer().setLeft(layoutParams.leftMargin);
                self.getDrawingData().drawingStep().drawingLayer().setTop(layoutParams.topMargin);
                self.getDrawingData().drawingStep().drawingLayer().setWidth(layoutParams.width);
                self.getDrawingData().drawingStep().drawingLayer().setHeight(layoutParams.height);
                self.getDrawingData().drawingStep().drawingLayer().setScale(self.gestureView.getScaleX());
                self.getDrawingData().drawingStep().drawingLayer().setRotation(self.gestureView.getRotation());

                self.gestureView.invalidate();
                self.gestureView = null;
                self.gestureViewOperationState = GestureViewOperation.None.state();

                self.didDrawNewStep();
                self.invalidate();
            }
        }
        else {
            self.gestureView = null;
            self.focusLayer(UnfocusAnyLayer);
            switch (event.getAction()) {
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

    public VDDrawingBrush getDrawingBrush() {
        if (drawingBrush == null) {
            drawingBrush = VDDrawingBrush.defaultBrush();
        }
        return drawingBrush;
    }

    public void setDrawingBrush(VDDrawingBrush drawingBrush) {
        this.drawingBrush = drawingBrush;

        self.endUnfinishedStep();
    }

    public File getDrawingCacheDir() {
        if (self.drawingCacheDir == null) {
            self.drawingCacheDir = VDFileConstant.getCacheDir(self.getClass().getSimpleName() + "/" + self.hashCode());
        }
        return drawingCacheDir;
    }

    /* #Delegates */

    /* #Private Methods */
    private void init(Context context, AttributeSet attrs, int defStyle) {
    }

    private void initial() {
        if (BeforeFirstDrawingViewCreated) {
            BeforeFirstDrawingViewCreated = false;
            VDDrawingView.clearStorageCaches();
            VDFileConstant.getCacheDir(self.getClass().getSimpleName());
        }

        disableClipOnParents(self);

        self.setFocusable(true);
        self.setFocusableInTouchMode(true);

        self.setDrawingBrush(self.getDrawingBrush());

        self.nativeClear();

        self.getDrawingData().newDrawingStepOnLayer(0).setCleared(true);
        self.didDrawNewStep();

        self.baseImageView = new ImageView(self.getContext());
        self.baseImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        self.baseImageView.setTag(0);
        self.addView(self.baseImageView);
        self.layerViews.add(self.baseImageView);

        self.layerImageView = new ImageView(self.getContext());
        self.layerImageView.setScaleType(ImageView.ScaleType.CENTER);
        self.addView(self.layerImageView);

        self.gestureDetector = new GestureDetector(self.getContext(), new GestureListener());
        self.scaleGestureDetector = new ScaleGestureDetector(self.getContext(), new ScaleListener());
        self.rotationDetector = new VDRotationGestureDetector(new RotationListener());
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
            disableClipOnParents( (View) v.getParent() );
        }
    }

    private void ensureBaseCanvas() {
        if (self.baseBitmap == null) {
            self.baseBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(),
                    Bitmap.Config.ARGB_8888);
            self.baseCanvas = new Canvas(self.baseBitmap);
            self.baseCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            self.baseImageView.setImageBitmap(self.baseBitmap);
        }

        if (self.getDrawingData().getDrawingSteps().size() > 0) {
            self.getDrawingData().getDrawingSteps().get(0).drawingLayer().setFrame(new RectF(0, 0, self.getWidth(), self.getHeight()));
        }

        self.baseImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        self.layerImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void didDrawNewStep() {
        if (self.getDelegate() != null) {
            self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
        }
    }

    private void beginDraw(float x, float y) {
        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.ensureBaseCanvas();

        if (self.getDrawingData().drawingStep().drawingLayer().drawingPath() == null
                || self.getDrawingData().drawingStep().drawingLayer().drawingPath().isCanFinish()) {
            self.layerBitmap = Bitmap.createBitmap(self.getWidth(),
                    self.getHeight(),
                    Bitmap.Config.ARGB_8888);
            self.layerCanvas = new Canvas(self.layerBitmap);

            self.layerImageView.setImageBitmap(self.layerBitmap);
            self.layerImageView.setVisibility(VISIBLE);
            self.layerImageView.bringToFront();

            if (self.getDrawingBrush().isOneStrokeToLayer()) {
                self.getDrawingData().newDrawingStepOnLayer();
            } else {
                self.getDrawingData().newDrawingStepOnLayer(0);
            }
        }

        self.getDrawingData().drawingStep().drawingLayer().newPath(self.getDrawingBrush()).addBeginPoint(new VDDrawingPoint(x, y));
        self.drawing(x, y);

        self.invalidate();
    }

    private void endDraw(float x, float y) {
        self.getDrawingData().drawingStep().drawingLayer().drawingPath().addEndPoint(new VDDrawingPoint(x, y));
        self.drawing(x, y);

        if (!self.getDrawingData().drawingStep().drawingLayer().drawingPath().isCanDraw()) {
            self.getDrawingData().cancelDrawingStep();
        }
        else {
            if (self.getDrawingData().drawingStep().drawingLayer().drawingPath().isCanFinish()) {
                if (self.layerBitmap != null) {
                    self.layerImageView.setImageBitmap(null);
                    self.layerImageView.setVisibility(INVISIBLE);

                    if (self.getDrawingBrush().isOneStrokeToLayer()) {
                        self.getDrawingData().drawingStep().drawingLayer().updateFrame();
                        self.addDrawingLayerImageView(self.getDrawingData().drawingStep().drawingLayer());
                    } else {
                        self.baseCanvas.drawBitmap(self.layerBitmap, 0.0f, 0.0f, new Paint());
                    }

                    self.layerBitmap.recycle();
                    self.layerBitmap = null;
                    self.layerCanvas = null;
                }

                self.didDrawNewStep();
            }
        }

        self.getParent().requestDisallowInterceptTouchEvent(true);
    }

    private void drawing(float x, float y) {
        self.layerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        self.getDrawingData().drawingStep().drawingLayer().drawingPath().addPoint(new VDDrawingPoint(x, y));
        if (self.getDrawingBrush().isOneStrokeToLayer()) {
            self.getDrawingData().drawingStep().drawingLayer().drawingPath().drawOnCanvas(self.layerCanvas);
        }
        else {
            if (self.getDrawingBrush().getType() == VDDrawingBrush.Type.Eraser) {
                self.getDrawingData().drawingStep().drawingLayer().drawingPath().drawOnCanvas(self.baseCanvas);
            }
            else {
                self.getDrawingData().drawingStep().drawingLayer().drawingPath().drawOnCanvas(self.layerCanvas);
            }
        }

        self.invalidate();
    }

    private void endUnfinishedStep() {
        if (self.getDrawingData() != null
                && self.getDrawingData().drawingStep() != null
                && self.getDrawingData().drawingStep().drawingLayer() != null
                && self.getDrawingData().drawingStep().drawingLayer().drawingPath() != null
                && !self.getDrawingData().drawingStep().drawingLayer().drawingPath().isCanFinish()) {
            VDDrawingPoint point = self.getDrawingData().drawingStep().drawingLayer().drawingPath().finishPathPoint();
            self.endDraw(point.x, point.y);
        }
    }

    /**
     * generate an ImageView from current drawing layer
     * @param drawingLayer current drawing layer
     */
    private void addDrawingLayerImageView(VDDrawingLayer drawingLayer) {
        drawingLayer.setLayerType(VDDrawingLayer.LayerType.Image);
        drawingLayer.setScale(1.0f);
        drawingLayer.setRotation(0.0f);

        VDDrawingLayerImageView imageView = new VDDrawingLayerImageView(self.getContext());
        imageView.setTag(drawingLayer.getHierarchy());
        imageView.setLayoutParams(drawingLayer.getLayoutParams());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setFocusable(true);

        Bitmap bitmap = Bitmap.createBitmap(imageView.getLayoutParams().width, imageView.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        drawingLayer.drawingPath().drawOnCanvas(canvas, true);
        canvas.drawBitmap(self.layerBitmap,
                -((LayoutParams) imageView.getLayoutParams()).leftMargin,
                -((LayoutParams) imageView.getLayoutParams()).topMargin,
                null);

        imageView.setImageBitmap(bitmap);

        imageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (self.gestureView == null) {
                        self.endUnfinishedStep();
                        self.gestureView = v;
                        self.focusLayer((int) v.getTag());
                    }
                }
                return false;
            }
        });

        self.addView(imageView);
        self.layerViews.add(imageView);

        self.bringChildToFront(imageView);
        self.focusLayer(drawingLayer.getHierarchy());
    }

    private void focusLayer(int layerHierarchy) {
        if (layerHierarchy <= 0) {
            layerHierarchy = UnfocusAnyLayer;
        }
        for (View view : self.layerViews) {
            view.setSelected((view.getTag() == layerHierarchy) || (layerHierarchy == FocusAllLayer));
        }
    }

    private View findViewByLayerHierarchy(int layerHierarchy) {
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

        View layerView = self.findViewByLayerHierarchy(layerHierarchy);
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

        View layerView = self.findViewByLayerHierarchy(layerHierarchy);
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

            if (view != self.baseImageView) { // never recycle the bastBitmap until this view destroyed
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

        self.ensureBaseCanvas();
        self.layerBitmap = Bitmap.createBitmap(self.getWidth(),
                                                    self.getHeight(),
                                                    Bitmap.Config.ARGB_8888);
        self.layerCanvas = new Canvas(self.layerBitmap);

        List<VDDrawingStep> stepsToDraw = self.getDrawingData().stepsToDraw();

        List<List<VDDrawingStep>> layerSteps = new ArrayList<>();
        for (int i = 0; i < stepsToDraw.size(); i++) {
            VDDrawingStep step = stepsToDraw.get(i);

            while (layerSteps.size() <= step.drawingLayer().getHierarchy()) {
                layerSteps.add(new ArrayList<VDDrawingStep>());
            }
            layerSteps.get( step.drawingLayer().getHierarchy()).add(step);
        }

        for (int layerHierarchy = 0; layerHierarchy < layerSteps.size(); layerHierarchy++) {
            if (layerSteps.get(layerHierarchy).size() == 0) {
                continue;
            }

            int lastBackgroundIndex = -1;
            int lastImageIndex = -1;

            self.layerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (int i = 0; i < layerSteps.get(layerHierarchy).size(); i++) {
                VDDrawingStep step = layerSteps.get(layerHierarchy).get(i);
                if (layerHierarchy == 0) {
                    if (step.drawingLayer().drawingPath() != null) {
                        self.layerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        step.drawingLayer().drawingPath().drawOnCanvas(self.layerCanvas);
                        self.baseCanvas.drawBitmap(self.layerBitmap, 0.0f, 0.0f, new Paint());
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
                        for (VDDrawingStep step : layerSteps.get(layerHierarchy)) {
                            if (step.drawingLayer().drawingPath() != null) {
                                step.drawingLayer().drawingPath().drawOnCanvas(self.layerCanvas);
                            }
                        }
                        self.addDrawingLayerImageView(firstDrawingStep.drawingLayer());

                        for (VDDrawingStep step : layerSteps.get(layerHierarchy)) {
                            if (self.findViewByLayerHierarchy(layerHierarchy) != null) {
                                ImageView imageView = (ImageView) self.findViewByLayerHierarchy(layerHierarchy);
                                VDDrawingLayer layer = step.drawingLayer();
                                if (layer != null) {
                                    if (layer.getLayoutParams() != null) {
                                        imageView.setLayoutParams(layer.getLayoutParams());
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
                        }

                        if (lastImageIndex > 0) {
                            // TODO: 2015/9/18 add imageview with image from imagePath
                        }
                    }
                        break;
                    case Text: {
                        // TODO: 2015/9/18 add edittext with text
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

        if (self.layerBitmap != null) {
            self.layerBitmap.recycle();
            self.layerBitmap = null;
            self.layerCanvas = null;
        }

        self.focusLayer(-1);
        self.getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void destroy() {
        self.nativeClear();
        if (self.baseBitmap != null) {
            self.baseBitmap.recycle();
        }

        VDFileWriter.clearDir(VDFileConstant.getCacheDir(self.getClass().getSimpleName() + "/ " + self.hashCode()), true);
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
        self.getDrawingData().newDrawingStepOnLayer(0).setCleared(true);
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
        self.ensureBaseCanvas();
        self.endUnfinishedStep();
        self.nativeSetBackgroundColor(color, layerHierarchy);

        self.getDrawingData().newDrawingStepOnLayer(layerHierarchy).drawingLayer().setBackgroundColor(color);
        self.didDrawNewStep();
    }

    public void setBackgroundImage(Bitmap bitmap, String identifier, int layerHierarchy) {
        self.ensureBaseCanvas();
        self.endUnfinishedStep();
        self.nativeSetBackgroundImage(bitmap, layerHierarchy);

        if (self.getDrawingCacheDir() != null) {
            String filePath = self.getDrawingCacheDir() + "/" + identifier;
            VDFileWriter.writeBitmap(new File(filePath), bitmap);
        }
        self.getDrawingData().newDrawingStepOnLayer(layerHierarchy).drawingLayer().setBackgroundImageIdentifier(identifier);
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

    /* #Classes */
    public class GestureListener implements GestureDetector.OnGestureListener {
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
    }

    public class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (self.gestureView != null
                    && ((self.gestureViewOperationState & GestureViewOperation.Scaling.state()) == GestureViewOperation.Scaling.state()) ) {
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
        None, Moving, Scaling, Rotation;
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
            }
            return state;
        }
    }
}
