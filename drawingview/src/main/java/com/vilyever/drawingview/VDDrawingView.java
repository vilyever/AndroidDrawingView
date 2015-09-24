package com.vilyever.drawingview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.filereadwrite.VDFileConstant;
import com.vilyever.filereadwrite.VDFileReader;
import com.vilyever.filereadwrite.VDFileWriter;
import com.vilyever.jsonmodel.VDJson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VDDrawingView
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 */
public class VDDrawingView extends RelativeLayout {
    private final VDDrawingView self = this;

    private static final int UndoCachesCount = 10;
    private static boolean BeforeFirstDrawingViewCreated = true;

    private static final long UnfocusAnyLayer = -1;
    private static final long FocusAllLayer = -2;

    private DrawingDelegate delegate;

    private List<View> layerViews = new ArrayList<>();

    private Bitmap baseBitmap;
    private Canvas baseCanvas;
    private VDDrawingBrush drawingBrush;

    private VDDrawingData drawingData;

    private Bitmap layerBitmap;
    private Canvas layerCanvas;

    private Map<Long, VDDrawingData> drawingStepsData = new HashMap<>();
    private long drawingStep = -1;
    private long undoStep = -1;
    private AsyncTask<Long, Void, Void> lastReadTask;
    private File drawingCacheDir;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private VDRotationGestureDetector rotationDetector;
    private View gestureView;
//    private GestureViewOperation gestureViewOperation = GestureViewOperation.None;
    private int gestureViewOperationState = GestureViewOperation.None.state();

    /* #Constructors */
    public VDDrawingView(Context context) {
        super(context);
//        init(null, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init(attrs, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        init(attrs, defStyle);
    }

    /* #Overrides */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (self.baseBitmap != null) {
            canvas.drawBitmap(self.baseBitmap, 0, 0, null);
        }

        if (self.layerBitmap != null) {
            canvas.drawBitmap(self.layerBitmap, 0, 0, null);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.gestureView != null) {
            self.scaleGestureDetector.onTouchEvent(event);
            self.rotationDetector.onTouchEvent(event);
            self.gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                for (VDDrawingLayer layer : self.drawingData.getDrawingLayers()) {
                    if (layer.getHierarchy() == self.gestureView.getTag()) {
                        RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                        layer.setLeft(layoutParams.leftMargin);
                        layer.setTop(layoutParams.topMargin);
                        layer.setScale(self.gestureView.getScaleX());
                        layer.setRotation(self.gestureView.getRotation());
                        break;
                    }
                }
                self.drawingData.setCleared(false);
                self.saveDrawingCache();
                self.invalidate();

                self.gestureView = null;
                self.gestureViewOperationState = GestureViewOperation.None.state();
            }
        }
        else {
            self.focusLayer(UnfocusAnyLayer);
            self.gestureView = null;
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

    @Override
    public void setBackgroundColor(int color) {
        self.ensureBaseLayer();
        self.drawingData.baseLayer().setBackgroundColor(color);
        self.saveDrawingCache();
        self.nativeSetBackgroundColor(color);
    }

    @Override
    public void setBackground(Drawable background) {
    }

    @Override
    public void setBackgroundResource(int resid) {
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
    }
    @Override
    public void setBackgroundTintList(ColorStateList tint) {
    }
    @Override
    public void setDrawingCacheBackgroundColor(int color) {
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        self.init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        self.destroy();
    }

    /* #Accessors */
    public DrawingDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(DrawingDelegate delegate) {
        this.delegate = delegate;
    }

    public VDDrawingBrush getDrawingBrush() {
        if (drawingBrush == null) {
            drawingBrush = VDDrawingBrush.defaultBrush();
        }
        return drawingBrush;
    }

    public void setDrawingBrush(VDDrawingBrush drawingBrush) {
        this.drawingBrush = drawingBrush;
    }

    public VDDrawingData getDrawingData() {
        return drawingData;
    }

    public File getDrawingCacheDir() {
        if (self.drawingCacheDir == null) {
            self.drawingCacheDir = VDFileConstant.getCacheDir(self.getClass().getSimpleName() + "/ " + self.hashCode());
        }
        return drawingCacheDir;
    }

    /* #Delegates */

    /* #Private Methods */
    private void init() {
        if (BeforeFirstDrawingViewCreated) {
            BeforeFirstDrawingViewCreated = false;
            VDDrawingView.clearStorageCaches();
            VDFileConstant.getCacheDir(self.getClass().getSimpleName());
        }

        self.setFocusable(true);
        self.setFocusableInTouchMode(true);

        self.setDrawingBrush(self.getDrawingBrush());

        self.nativeClear();

        self.saveDrawingCache();

        self.gestureDetector = new GestureDetector(self.getContext(), new GestureListener());
        self.scaleGestureDetector = new ScaleGestureDetector(self.getContext(), new ScaleListener());
        self.rotationDetector = new VDRotationGestureDetector(new RotationListener());
    }

    private void ensureBaseLayer() {
        if (self.baseBitmap == null) {
            self.baseBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(),
                    Bitmap.Config.ARGB_8888);
            self.baseCanvas = new Canvas(self.baseBitmap);
            self.baseCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        if (self.drawingData.baseLayer() == null) {
            self.drawingData.addLayer(new VDDrawingLayer(0, new RectF(0, 0, self.getWidth(), self.getHeight())));
        }
    }

    private void saveDrawingCache() {
        if (self.undoStep != self.drawingStep) {
            self.drawingStep = self.undoStep + 1;
        }
        else {
            self.drawingStep++;
        }

        if (self.getDrawingCacheDir() != null) {
            String filePath = self.getDrawingCacheDir().getAbsolutePath() + "/" + self.drawingStep;
            VDFileWriter.writeText(new File(filePath), self.drawingData.toJson().toString());
        }

        self.undoStep = self.drawingStep;

        self.refreshUndoCache();

        if (self.getDelegate() != null) {
            self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
        }
    }

    private VDDrawingData readDrawingData(long step) {
        if (step > self.drawingStep) {
            return null;
        }

        self.refreshUndoCache();
        if (self.drawingStepsData.containsKey(step)) {
            return self.drawingStepsData.get(step);
        }
        else {
            if (self.getDrawingCacheDir() != null) {
                String filePath = self.getDrawingCacheDir().getAbsolutePath() + "/" + step;
                String json = VDFileReader.readText(new File(filePath));
                VDDrawingData drawingData = new VDJson<>(VDDrawingData.class).modelFromJsonString(json);
                self.drawingStepsData.put(step, drawingData);
                return drawingData;
            }
            else {
                return null;
            }
        }
    }

    private void refreshUndoCache() {
        long minStep = 0;
        long maxStep = 0;
        if (self.drawingStep == self.undoStep) {
            minStep = Math.max(0, self.drawingStep - UndoCachesCount);
            maxStep = self.drawingStep;

            if (self.drawingStepsData.containsKey(self.drawingStep)) {
                self.drawingStepsData.remove(self.drawingStep);
            }

        }
        else {
            minStep = Math.max(0, self.undoStep - UndoCachesCount);
            maxStep = Math.min(self.drawingStep, self.undoStep + UndoCachesCount);
        }

        for (long i = 0; i < minStep; i++) {
            if (self.drawingStepsData.containsKey(i)) {
                self.drawingStepsData.remove(i);
            }
        }
        for (long i = maxStep + 1; i <= self.drawingStep; i++) {
            if (self.drawingStepsData.containsKey(i)) {
                self.drawingStepsData.remove(i);
            }
        }

        List<Long> willReadSteps = new ArrayList<>();

        for (long i = minStep; i <= maxStep; i++) {
            if (!self.drawingStepsData.containsKey(i)) {
                willReadSteps.add(i);
            }
        }

        if (willReadSteps.size() > 0) {
            if (self.lastReadTask != null) {
                self.lastReadTask.cancel(true);
            }
            self.lastReadTask = new AsyncTask<Long, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Long... params) {
                                                for (long step : params) {
                                                    if (self.drawingStepsData.containsKey(step)) {
                                                        continue;
                                                    }

                                                    if (self.drawingCacheDir != null) {
                                                        String filePath = self.drawingCacheDir.getAbsolutePath() + "/" + step;
                                                        String json = VDFileReader.readText(new File(filePath));
                                                        VDDrawingData drawingData = new VDJson<>(VDDrawingData.class).modelFromJsonString(json);
                                                        self.drawingStepsData.put(step, drawingData);
                                                    }
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                super.onPostExecute(aVoid);
                                                self.lastReadTask = null;
                                            }
                                        }.execute(willReadSteps.toArray(new Long[willReadSteps.size()]));
        }
    }

    private void beginDraw(float x, float y) {
        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.ensureBaseLayer();

        self.layerBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(),
                Bitmap.Config.ARGB_8888);
        self.layerCanvas = new Canvas(self.layerBitmap);

        if (self.getDrawingBrush().isOneStrokeToLayer()) {
            self.drawingData.newLayer().newPath(self.getDrawingBrush()).addPoint(new VDDrawingPoint(x, y));
            self.drawingData.topLayer().currentPath().drawOnCanvas(self.layerCanvas);
        }
        else {
            self.drawingData.baseLayer().newPath(self.getDrawingBrush()).addPoint(new VDDrawingPoint(x, y));
            self.drawingData.baseLayer().currentPath().drawOnCanvas(self.layerCanvas);
        }

        self.invalidate();
    }

    private void endDraw(float x, float y) {
        self.drawing(x, y);

        if (self.layerBitmap != null) {
            if (self.getDrawingBrush().isOneStrokeToLayer()) {
                self.drawingData.topLayer().setFrame(self.drawingData.topLayer().currentPath().getFrame(true));
                self.addDrawingLayerImageView(self.drawingData.topLayer(), false);
            }
            else {
                self.drawingData.baseLayer().currentPath().drawOnCanvas(self.baseCanvas);
            }

            self.layerBitmap.recycle();
            self.layerBitmap = null;
            self.layerCanvas = null;
        }

        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.saveDrawingCache();
        self.drawingData.setCleared(false);
    }

    private void drawing(float x, float y) {
        self.layerCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (self.getDrawingBrush().isOneStrokeToLayer()) {
            self.drawingData.topLayer().currentPath().addPoint(new VDDrawingPoint(x, y));
            self.drawingData.topLayer().currentPath().drawOnCanvas(self.layerCanvas);
        }
        else {
            self.drawingData.baseLayer().currentPath().addPoint(new VDDrawingPoint(x, y));
            if (self.getDrawingBrush().getShape() == VDDrawingBrush.Shape.Eraser) {
                self.drawingData.baseLayer().currentPath().drawOnCanvas(self.baseCanvas);
            }
            else {
                self.drawingData.baseLayer().currentPath().drawOnCanvas(self.layerCanvas);
            }
        }

        self.invalidate();
    }

    /**
     * generate an ImageView from current drawing layer
     * @param drawingLayer current drawing layer
     * @param drawFromZero for realtime drawing, always false, for data drawing, true(cause the path points to save had remove the offset to layer's frame)
     */
    private void addDrawingLayerImageView(VDDrawingLayer drawingLayer, boolean drawFromZero) {
        ImageView imageView = new ImageView(self.getContext());
        imageView.setTag(drawingLayer.getHierarchy());
        imageView.setLayoutParams(drawingLayer.getLayoutParams());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setFocusable(true);

        Bitmap bitmap = Bitmap.createBitmap(imageView.getLayoutParams().width, imageView.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Canvas canvas = new Canvas(bitmap);
        if (!drawFromZero) {
            canvas.drawBitmap(self.layerBitmap, -((LayoutParams) imageView.getLayoutParams()).leftMargin, -((LayoutParams) imageView.getLayoutParams()).topMargin, null);
        }
        else {
            canvas.drawBitmap(self.layerBitmap, 0, 0, null);
        }

        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundResource(R.drawable.drawing_layer_background);

        imageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    self.focusLayer((Long) v.getTag());
                    self.gestureView = v;
                }
                return false;
            }
        });

        self.addView(imageView);
        self.layerViews.add(imageView);

        imageView.setScaleX(drawingLayer.getScale());
        imageView.setScaleY(drawingLayer.getScale());
        imageView.setRotation(drawingLayer.getRotation());
        self.focusLayer(drawingLayer.getHierarchy());
    }

    private void focusLayer(long layerHierarchy) {
        for (View view : self.layerViews) {
            view.setSelected((view.getTag() == layerHierarchy) || (layerHierarchy == FocusAllLayer));
        }
    }

    public void nativeSetBackgroundColor(int color) {
        super.setBackgroundColor(color);
        self.invalidate();
    }

    public void nativeSetBackgroundImage(Bitmap bitmap) {
        Bitmap preBitmap =null;
        if (self.getBackground() != null
                && self.getBackground() instanceof BitmapDrawable) {
            preBitmap = ((BitmapDrawable) self.getBackground()).getBitmap();
        }
        super.setBackground(new BitmapDrawable(self.getResources(), bitmap));
        if (preBitmap != null) {
            preBitmap.recycle();
        }

        self.invalidate();
    }

    private void nativeClear() {
        self.drawingData = new VDDrawingData();

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

        self.layerViews.clear();
    }

    private void nativeDrawData() {
        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.ensureBaseLayer();

        if (self.drawingData.baseLayer().getBackgroundImageIdentifier() != null) {
            if (self.getDrawingCacheDir() != null) {
                String filePath = self.getDrawingCacheDir().getAbsolutePath() + "/" + self.drawingData.baseLayer().getBackgroundImageIdentifier();
                File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    self.nativeSetBackgroundImage( VDFileReader.readBitmap(imageFile));
                }
                else {
                    // TODO: 2015/9/24 call delegate to put the image by identifier
                }
            }
        }
        else {
            self.nativeSetBackgroundColor(self.drawingData.baseLayer().getBackgroundColor());
        }

        for (VDDrawingPath drawingPath : self.drawingData.baseLayer().getPaths()) {
            drawingPath.drawOnCanvas(self.baseCanvas);
        }

        for (int i = 1; i < self.drawingData.getDrawingLayers().size(); i++) {
            VDDrawingLayer drawingLayer = self.drawingData.getDrawingLayers().get(i);
            if (drawingLayer.getImageIdentifer() != null
                && !drawingLayer.getImageIdentifer().isEmpty()) {
                // TODO: 2015/9/18 add imageview with image from imagePath
            }
            else if (drawingLayer.getText() != null
                    && !drawingLayer.getText().isEmpty()) {
                // TODO: 2015/9/18 add edittext with text
            }
            else {
                self.layerBitmap = Bitmap.createBitmap(self.getWidth(), self.getHeight(),
                        Bitmap.Config.ARGB_8888);
                self.layerCanvas = new Canvas(self.layerBitmap);

                for (VDDrawingPath drawingPath : drawingLayer.getPaths()) {
                    drawingPath.drawOnCanvas(self.layerCanvas);
                    self.addDrawingLayerImageView(drawingLayer, true);
                }

                self.focusLayer(UnfocusAnyLayer);
            }
        }

        if (self.layerBitmap != null) {
            self.layerBitmap.recycle();
            self.layerBitmap = null;
            self.layerCanvas = null;
        }

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
        if (self.drawingData.isCleared()) { // means current state is clear
            return;
        }
        self.nativeClear();
        self.drawingData.setCleared(true);
        self.saveDrawingCache();
        self.invalidate();
    }

    public void drawData(VDDrawingData data) {
        if (data == null) {
            return;
        }
        if (data.isCleared() && self.drawingData.isCleared()) {
            return;
        }
        self.nativeClear();
        self.drawingData = data;
        self.nativeDrawData();
        self.saveDrawingCache();
        self.invalidate();
    }

    public void setBackgroundImage(Bitmap bitmap, String identifier) {
        self.nativeSetBackgroundImage(bitmap);

        self.ensureBaseLayer();
        if (self.getDrawingCacheDir() != null) {
            String filePath = self.getDrawingCacheDir() + "/" + identifier;
            VDFileWriter.writeBitmap(new File(filePath), bitmap);
        }
        self.drawingData.baseLayer().setBackgroundImageIdentifier(identifier);
        self.saveDrawingCache();
    }

    public boolean undo() {
        if (self.canUndo()) {
            self.undoStep--;

            self.nativeClear();
            self.drawingData = self.readDrawingData(self.undoStep);
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
            self.undoStep++;

            self.nativeClear();
            self.drawingData = self.readDrawingData(self.undoStep);
            self.nativeDrawData();
            self.invalidate();

            if (self.getDelegate() != null) {
                self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
            }
        }
        return false;
    }

    public boolean canUndo() {
        return self.drawingStep > 0 && self.undoStep > 0;
    }

    public boolean canRedo() {
        return self.undoStep < self.drawingStep;
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
