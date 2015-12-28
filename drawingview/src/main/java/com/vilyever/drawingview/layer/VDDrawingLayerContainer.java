package com.vilyever.drawingview.layer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import com.vilyever.drawingview.util.VDRotationGestureDetector;

/**
 * VDDrawingLayerContainer
 * AndroidDrawingView <com.vilyever.drawingview.layer>
 * Created by vilyever on 2015/12/1.
 * Feature:
 */
public class VDDrawingLayerContainer extends RelativeLayout {
    final VDDrawingLayerContainer self = this;

    private Delegate delegate;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private VDRotationGestureDetector rotationDetector;
    private View gestureView;
    private int gestureViewOperationState = GestureViewOperation.None.state();

    private OnTouchListener layerOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            VDDrawingLayerViewProtocol layerViewProtocol = (VDDrawingLayerViewProtocol) v;
            if (!layerViewProtocol.canHandle()) {
                return false;
            }

            if (v instanceof VDDrawingLayerTextView) {
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) v;
                if (textView.isEditing()) {
                    return false;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                self.gestureView = v;
                if (self.getDelegate() != null) {
                    self.getDelegate().layerViewDidBeginTouch((VDDrawingLayerViewProtocol) v);
                }
            }
            return false;
        }
    };

    /* #Constructors */
    public VDDrawingLayerContainer(Context context) {
        this(context, null);
    }

    public VDDrawingLayerContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VDDrawingLayerContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        self.initial(context);
    }
    
    /* #Overrides */
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
                    return false;
                }

                if (self.gestureView instanceof VDDrawingLayerTextView) { // when text view editing, disable move, scale and rotate
                    if (self.gestureViewOperationState == (GestureViewOperation.None.state() | GestureViewOperation.DoubleTap.state())) {
                        VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                        if (textView.isEditing()) {
                            self.gestureView = null;
                            self.gestureViewOperationState = GestureViewOperation.None.state();
                            return false;
                        }
                    }
                }

                if (self.getDelegate() != null) {
                    self.getDelegate().layerViewDidEndTransform((VDDrawingLayerViewProtocol) self.gestureView);
                }

                self.gestureView = null;
                self.gestureViewOperationState = GestureViewOperation.None.state();

                return false;
            }

            return true;
        }
        return super.onTouchEvent(event);
    }

    /* #Accessors */

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }
    /* #Delegates */
     
    /* #Private Methods */
    private void initial(Context context) {
        // layer view can draw border outside
        self.setClipChildren(false);

        self.gestureDetector = new GestureDetector(self.getContext(), new GestureListener());
        self.scaleGestureDetector = new ScaleGestureDetector(self.getContext(), new ScaleListener());
        self.rotationDetector = new VDRotationGestureDetector(new RotationListener());
    }
    
    /* #Public Methods */
    public void addLayerView(VDDrawingLayerViewProtocol layerView) {
        self.addView((View) layerView);

        ((View) layerView).setOnTouchListener(self.layerOnTouchListener);
    }

    public void removeLayerView(VDDrawingLayerViewProtocol layerView) {
        self.removeView((View) layerView);
        self.gestureView = null;
    }

    public void clear() {
        self.removeAllViews();
        self.gestureView = null;
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

                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.gestureView;
                textView.beginEdit(false);

                if (self.getDelegate() != null) {
                    self.getDelegate().layerTextViewDidBeginEdit(textView);
                }
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
                triggerOffset = -Math.signum(angle);
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
    public interface Delegate {
        void layerViewDidBeginTouch(VDDrawingLayerViewProtocol layerView);
        void layerTextViewDidBeginEdit(VDDrawingLayerTextView textView);
        void layerViewDidEndTransform(VDDrawingLayerViewProtocol layerView);
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