package com.vilyever.drawingview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.jsonmodel.VDJson;

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

    private DrawingDelegate delegate;

    private List<View> layerViews = new ArrayList<>();

    private Bitmap baseBitmap;
    private Canvas baseCanvas;
    private VDDrawingBrush drawingBrush;

    private VDDrawingData drawingData;

    private Bitmap layerBitmap;
    private Canvas layerCanvas;

    private Map<Long, String> drawingStepsData = new HashMap<>();
    private long drawingStep = -1;
    private long undoStep = -1;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private VDRotationGestureDetector rotationDetector;
    private View gestureView;
    private GestureViewOperation gestureViewOperation = GestureViewOperation.None;

    /* #Constructors */
    public VDDrawingView(Context context) {
        super(context);
        init(null, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
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
                if (self.gestureViewOperation != GestureViewOperation.None) {
                    for (VDDrawingLayer layer : self.drawingData.getDrawingLayers()) {
                        if (layer.getHierarchy() == self.gestureView.getTag()) {
                            switch (self.gestureViewOperation) {
                                case None:
                                    break;
                                case Moving:
                                    RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.gestureView.getLayoutParams();
                                    layer.setLeft(layoutParams.leftMargin);
                                    layer.setTop(layoutParams.topMargin);
                                    break;
                                case Scaling:
                                    layer.setScale(self.gestureView.getScaleX());
                                    break;
                                case Rotation:
                                    layer.setRotation(self.gestureView.getRotation());
                                    break;
                            }
                            break;
                        }
                    }
                    self.invalidate();
                    self.saveDrawingCache();
                    self.drawingData.setCleared(false);
                }

                self.gestureViewOperation = GestureViewOperation.None;
                self.gestureView = null;
            }
        }
        else {
            self.focusLayer(-1);
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
        super.setBackgroundColor(color);
        self.ensureBaseLayer();
        self.drawingData.baseLayer().setBackgroundColor(color);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        Bitmap bitmap = VDBitmapConvertor.drawableToBitmap(background);
        self.ensureBaseLayer();
        // TODO: 2015/9/22 save the background as bitmap file and set to base layer
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        Drawable drawable = null;
        if (resid != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(self.getContext().getResources(), resid);
            self.ensureBaseLayer();
            // TODO: 2015/9/22 save the background as bitmap file and set to base layer
        }
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

    /* #Delegates */

    /* #Private Methods */
    private void init(AttributeSet attrs, int defStyle) {
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

//        SharedPreferences sharedPreferences = self.getContext().getSharedPreferences(self.getClass().getName(), 0);
//        sharedPreferences.edit().putString(self.getClass().getSimpleName() + self.drawingStep, self.drawingData.toJson().toString()).apply();
        self.drawingStepsData.put(self.drawingStep, self.drawingData.toJson().toString());

        self.undoStep = self.drawingStep;

        if (self.getDelegate() != null) {
            self.getDelegate().undoStateDidChangeFromDrawingView(self, self.canUndo(), self.canRedo());
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
        self.layerBitmap.eraseColor(Color.TRANSPARENT);
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
            view.setSelected(view.getTag() == layerHierarchy);
        }
    }

    private void nativeClear() {
        self.drawingData = new VDDrawingData();

        if (self.baseBitmap != null) {
            self.baseBitmap.eraseColor(Color.TRANSPARENT);
        }

        for (View view : self.layerViews) {
            if (view instanceof ImageView) {
                ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap().recycle();
            }
            self.removeView(view);
        }

        self.layerViews.clear();
    }

    private void nativeDrawData() {
        self.getParent().requestDisallowInterceptTouchEvent(true);

        self.ensureBaseLayer();

        for (VDDrawingPath drawingPath : self.drawingData.baseLayer().getPaths()) {
            drawingPath.drawOnCanvas(self.baseCanvas);
        }

        for (int i = 1; i < self.drawingData.getDrawingLayers().size(); i++) {
            VDDrawingLayer drawingLayer = self.drawingData.getDrawingLayers().get(i);
            if (drawingLayer.getImagePath() != null
                && !drawingLayer.getImagePath().isEmpty()) {
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

                self.focusLayer(-1);
            }
        }

        if (self.layerBitmap != null) {
            self.layerBitmap.recycle();
            self.layerBitmap = null;
            self.layerCanvas = null;
        }

        self.getParent().requestDisallowInterceptTouchEvent(false);
    }

    /* #Public Methods */
    public void clear() {
        if (self.drawingData.isCleared()) { // means current state is clear
            return;
        }
        self.nativeClear();
        self.invalidate();
        self.saveDrawingCache();
        self.drawingData.setCleared(true);
    }

    public void destroy() {
        self.nativeClear();
        if (self.baseBitmap != null) {
            self.baseBitmap.recycle();
        }
//        SharedPreferences sharedPreferences = self.getContext().getSharedPreferences(self.getClass().getName(), 0);
//        sharedPreferences.edit().clear().apply();
    }

    public void drawData(VDDrawingData data) {
        if (data == null) {
            return;
        }
        self.saveDrawingCache();
        self.nativeClear();
        self.drawingData = data;
        self.nativeDrawData();
        self.invalidate();
        self.saveDrawingCache();
        self.drawingData.setCleared(false);
    }

    public boolean undo() {
        if (self.canUndo()) {
            self.undoStep--;
//            SharedPreferences sharedPreferences = self.getContext().getSharedPreferences(self.getClass().getName(), 0);
//            VDDrawingData cacheData = new VDJson<>(VDDrawingData.class).modelFromJsonString(sharedPreferences.getString(self.getClass().getSimpleName() + self.undoStep, null));
            VDDrawingData cacheData = new VDJson<>(VDDrawingData.class).modelFromJsonString(self.drawingStepsData.get(self.undoStep));
            self.nativeClear();
            self.drawingData = cacheData;
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
//            SharedPreferences sharedPreferences = self.getContext().getSharedPreferences(self.getClass().getName(), 0);
//            VDDrawingData cacheData = new VDJson<>(VDDrawingData.class).modelFromJsonString(sharedPreferences.getString(self.getClass().getSimpleName() + self.undoStep, null));
            VDDrawingData cacheData = new VDJson<>(VDDrawingData.class).modelFromJsonString(self.drawingStepsData.get(self.undoStep));
            self.nativeClear();
            self.drawingData = cacheData;
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
            if (self.gestureViewOperation == GestureViewOperation.None) {
                self.gestureViewOperation = GestureViewOperation.Moving;
            }

            if (self.gestureViewOperation == GestureViewOperation.Moving
                    && self.gestureView != null) {
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
            float scaleFactor = detector.getScaleFactor();
            if (self.gestureViewOperation == GestureViewOperation.Scaling
                    && self.gestureView != null) {
                self.gestureView.setScaleX(Math.max(1.0f, self.gestureView.getScaleX() * scaleFactor));
                self.gestureView.setScaleY(Math.max(1.0f, self.gestureView.getScaleY() * scaleFactor));
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (self.gestureViewOperation == GestureViewOperation.None
                    || self.gestureViewOperation == GestureViewOperation.Moving) {
                self.gestureViewOperation = GestureViewOperation.Scaling;
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
                    && (self.gestureViewOperation == GestureViewOperation.None
                        || self.gestureViewOperation == GestureViewOperation.Moving) ) {
                self.gestureViewOperation = GestureViewOperation.Rotation;
                originalRotation = self.gestureView.getRotation();
                triggerOffset = -Math.signum(angle) * TriggerAngle;
            }

            if (self.gestureViewOperation == GestureViewOperation.Rotation
                    && self.gestureView != null) {
//                self.gestureView.setRotation(-(angle - self.gestureView.getRotation()));
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
        None, Moving, Scaling, Rotation
    }
}
