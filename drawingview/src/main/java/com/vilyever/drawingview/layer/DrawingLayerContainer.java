package com.vilyever.drawingview.layer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import com.vilyever.drawingview.util.RotationGestureDetector;

/**
 * VDDrawingLayerContainer
 * AndroidDrawingView <com.vilyever.drawingview.layer>
 * Created by vilyever on 2015/12/1.
 * Feature:
 * 图层容器，管理除base图层以外的所有图层的触摸响应
 */
public class DrawingLayerContainer extends RelativeLayout {
    final DrawingLayerContainer self = this;

    /* #Constructors */
    public DrawingLayerContainer(Context context) {
        this(context, null);
    }

    public DrawingLayerContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingLayerContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /* Public Methods */
    /**
     * 添加图层
     * @param layerView 添加的图层
     */
    public void addLayerView(DrawingLayerViewProtocol layerView) {
        addView((View) layerView);

        // 由容器控制layer的触摸响应
        ((View) layerView).setOnTouchListener(getLayerOnTouchListener());
    }

    /**
     * 移除图层
     * @param layerView 移除的图层
     */
    public void removeLayerView(DrawingLayerViewProtocol layerView) {
        removeView((View) layerView);
        setGestureView(null);
    }

    /**
     * 清空图层
     */
    public void clear() {
        if (getChildCount() > 0) {
            removeAllViews();
        }
        setGestureView(null);
    }

    /* Properties */
    public interface LayerDelegate {
        /**
         * 图层被触摸ACTION_DOWN时的反馈，此时理应准备开始下一step，接收者应在此时结束先前未完成的step
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void onLayerViewTouchBegin(DrawingLayerContainer container, DrawingLayerViewProtocol layerView);

        /**
         * 图层每次变换（平移/旋转/缩放）时的反馈，此反馈是连续微小的主要用于远程同步
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void onLayerViewTransforming(DrawingLayerContainer container, DrawingLayerViewProtocol layerView);

        /**
         * 图层结束变换，此时当前变化step理应结束
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void onLayerViewTransformEnd(DrawingLayerContainer container, DrawingLayerViewProtocol layerView);

        /**
         * 对于text图层有一个双击进行编辑的操作，此操作与其他操作独立
         * @param container 当前容器
         * @param textView 被触摸的text图层
         */
        void onLayerTextViewEditBegin(DrawingLayerContainer container, DrawingLayerTextView textView);

        /**
         * 不进行任何操作，仅显示选中图层
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void requireHandlingLayerView(DrawingLayerContainer container, DrawingLayerViewProtocol layerView);
    }
    private LayerDelegate layerDelegate;
    public DrawingLayerContainer setLayerDelegate(LayerDelegate layerDelegate) {
        this.layerDelegate = layerDelegate;
        return this;
    }
    public LayerDelegate getLayerDelegate() {
        return this.layerDelegate;
    }

    /**
     * 手势检测，主要用于平移和text图层的双击编辑
     */
    private GestureDetector gestureDetector;
    private DrawingLayerContainer setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
        return this;
    }
    private GestureDetector getGestureDetector() {
        if (this.gestureDetector == null) {
            this.gestureDetector = new GestureDetector(getContext(), new GestureListener());
        }
        return this.gestureDetector;
    }
    private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        float originalLeftMargin;
        float originalTopMargin;
        float beginX;
        float beginY;

        @Override
        public boolean onDown(MotionEvent e) {
            // 记录初始位置
            if (self.getGestureView() != null) {
                RelativeLayout.LayoutParams layoutParams = (LayoutParams) getGestureView().getLayoutParams();
                this.originalLeftMargin = layoutParams.leftMargin;
                this.originalTopMargin = layoutParams.topMargin;

                this.beginX = e.getRawX();
                this.beginY = e.getRawY();
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
            // 存储平移操作
            if ((self.getGestureViewOperationState() & GestureViewOperation.Moving.state())  != GestureViewOperation.Moving.state()) {
                self.setGestureViewOperationState(getGestureViewOperationState() | GestureViewOperation.Moving.state());
            }

            // 判断是否在操作处于编辑状态的text图层，若是，则禁止变换
            if (self.getGestureView() instanceof DrawingLayerTextView) {
                DrawingLayerTextView textView = (DrawingLayerTextView) self.getGestureView();
                if (textView.isEditing()) {
                    return true;
                }
            }

            // 缩放和旋转的操作优先于平移，若正在进行缩放或旋转操作，禁止平移
            if (self.getGestureView() != null
                    && (self.getGestureViewOperationState() == (GestureViewOperation.None.state() | GestureViewOperation.Moving.state()))) {
                float dx = e2.getRawX() - this.beginX;
                float dy = e2.getRawY() - this.beginY;

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.getGestureView().getLayoutParams();
                layoutParams.leftMargin = (int) Math.floor(this.originalLeftMargin + dx);
                layoutParams.topMargin = (int) Math.floor(this.originalTopMargin + dy);
                self.getGestureView().setLayoutParams(layoutParams);
                self.getLayerDelegate().onLayerViewTransforming(self, (DrawingLayerViewProtocol) self.getGestureView());
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
            // 对于text图层，双击操作有最高优先级
            if (self.getGestureView() != null
                    && self.getGestureView() instanceof DrawingLayerTextView) {
                self.setGestureViewOperationState(self.getGestureViewOperationState() | GestureViewOperation.DoubleTap.state());

                DrawingLayerTextView textView = (DrawingLayerTextView) self.getGestureView();
                textView.beginEdit(false);

                self.getLayerDelegate().onLayerTextViewEditBegin(self, textView);
            }
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    /**
     * 缩放手势检测
     */
    private ScaleGestureDetector scaleGestureDetector;
    private DrawingLayerContainer setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
        this.scaleGestureDetector = scaleGestureDetector;
        return this;
    }
    private ScaleGestureDetector getScaleGestureDetector() {
        if (this.scaleGestureDetector == null) {
            this.scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        }
        return this.scaleGestureDetector;
    }
    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (self.getGestureView() != null
                    && ((self.getGestureViewOperationState() & GestureViewOperation.Scaling.state()) == GestureViewOperation.Scaling.state()) ) {

                // 判断是否在操作处于编辑状态的text图层，若是，则禁止变换
                if (self.getGestureView() instanceof DrawingLayerTextView) {
                    DrawingLayerTextView textView = (DrawingLayerTextView) self.getGestureView();
                    if (textView.isEditing()) {
                        return true;
                    }
                }

                float scaleFactor = detector.getScaleFactor();
                self.getGestureView().setScaleX(self.getGestureView().getScaleX() * scaleFactor);
                self.getGestureView().setScaleY(self.getGestureView().getScaleY() * scaleFactor);
                self.getLayerDelegate().onLayerViewTransforming(self, (DrawingLayerViewProtocol) self.getGestureView());
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // 标记此时开始缩放操作
            if ((self.getGestureViewOperationState() & GestureViewOperation.Scaling.state()) != GestureViewOperation.Scaling.state()) {
                self.setGestureViewOperationState(self.getGestureViewOperationState() | GestureViewOperation.Scaling.state());
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    /**
     * 旋转手势检测
     */
    private RotationGestureDetector rotationGestureDetector;
    private DrawingLayerContainer setRotationGestureDetector(RotationGestureDetector rotationGestureDetector) {
        this.rotationGestureDetector = rotationGestureDetector;
        return this;
    }
    private RotationGestureDetector getRotationGestureDetector() {
        if (this.rotationGestureDetector == null) {
            this.rotationGestureDetector = new RotationGestureDetector(new RotationListener());
        }
        return this.rotationGestureDetector;
    }
    private class RotationListener implements RotationGestureDetector.OnRotationGestureListener {
        private static final float TriggerAngle = 10.0f;
        private float originalRotation;
        private float triggerOffset;
        @Override
        public void onRotate(RotationGestureDetector rotationDetector) {
            float angle = rotationDetector.getAngle();

            /**
             * 若旋转角度超过阈值，标记此时进行旋转操作
             * 记下在到达旋转阈值前未旋转的角度在之后补偿
             */
            if (Math.abs(angle) > TriggerAngle
                    && ((self.getGestureViewOperationState() & GestureViewOperation.Rotation.state()) != GestureViewOperation.Rotation.state()) ) {
                self.setGestureViewOperationState(self.getGestureViewOperationState() | GestureViewOperation.Rotation.state());
                this.originalRotation = self.getGestureView().getRotation();
                this.triggerOffset = -Math.signum(angle);
            }

            if (self.getGestureView() != null
                    && ((self.getGestureViewOperationState() & GestureViewOperation.Rotation.state()) == GestureViewOperation.Rotation.state()) ) {

                // 判断是否在操作处于编辑状态的text图层，若是，则禁止变换
                if (self.getGestureView() instanceof DrawingLayerTextView) {
                    DrawingLayerTextView textView = (DrawingLayerTextView) self.getGestureView();
                    if (textView.isEditing()) {
                        return;
                    }
                }

                self.getGestureView().setRotation(-(angle + this.triggerOffset - this.originalRotation));
                self.getLayerDelegate().onLayerViewTransforming(self, (DrawingLayerViewProtocol) self.getGestureView());
            }
        }
    }

    /**
     * 当前正在进行手势操作的图层view
     */
    private View gestureView;
    private DrawingLayerContainer setGestureView(View gestureView) {
        this.gestureView = gestureView;
        return this;
    }
    private View getGestureView() {
        return this.gestureView;
    }

    /**
     * {@link #gestureView}正在进行的操作
     */
    private int gestureViewOperationState;
    private DrawingLayerContainer setGestureViewOperationState(int gestureViewOperationState) {
        this.gestureViewOperationState = gestureViewOperationState;
        return this;
    }
    private int getGestureViewOperationState() {
        return this.gestureViewOperationState;
    }

    /**
     * 所有图层公用的touchListener
     * 此回调在复写的{@link #onTouchEvent(MotionEvent)}之前，可以进行判断拦截
     */
    private OnTouchListener layerOnTouchListener;
    public OnTouchListener getLayerOnTouchListener() {
        if (this.layerOnTouchListener == null) {
            this.layerOnTouchListener = new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    DrawingLayerViewProtocol layerViewProtocol = (DrawingLayerViewProtocol) v;
                    // 检测图层是否允许操作
                    if (!layerViewProtocol.canHandle()) {
                        return false;
                    }

                    // 检测是否是正在编辑的text图层
                    if (v instanceof DrawingLayerTextView) {
                        DrawingLayerTextView textView = (DrawingLayerTextView) v;
                        if (textView.isEditing()) {
                            return false;
                        }
                    }

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        self.setGestureView(v);
                        self.getLayerDelegate().onLayerViewTouchBegin(self, (DrawingLayerViewProtocol) v);
                    }
                    return false;
                }
            };
        }
        return this.layerOnTouchListener;
    }

    /* Overrides */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /** 若先前调用的{@link #layerOnTouchListener} 检测此时无法进行操作（gestureView为空），则不进行手势判断 */
        if (getGestureView() != null) {
            getScaleGestureDetector().onTouchEvent(event);
            getRotationGestureDetector().onTouchEvent(event);
            getGestureDetector().onTouchEvent(event);

            // 一轮触摸取消时，反馈到上级并重置手势判断相关参数
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {

                // 若此次操作未造成任何变化，则无需进行后续反馈步骤，相当于单击选中某个图层
                if (getGestureViewOperationState() == GestureViewOperation.None.state()) {
                    getLayerDelegate().requireHandlingLayerView(this, (DrawingLayerViewProtocol) getGestureView());
                    setGestureView(null);
                    return false;
                }

                // 若操作的图层此时是处于编辑状态的text图层，仅重置手势判断相关参数
                if (getGestureView() instanceof DrawingLayerTextView) {
                    if (getGestureViewOperationState() == (GestureViewOperation.None.state() | GestureViewOperation.DoubleTap.state())) {
                        DrawingLayerTextView textView = (DrawingLayerTextView) getGestureView();
                        if (textView.isEditing()) {
                            setGestureView(null);
                            setGestureViewOperationState(GestureViewOperation.None.state());
                            return false;
                        }
                    }
                }

                getLayerDelegate().onLayerViewTransformEnd(this, (DrawingLayerViewProtocol) getGestureView());

                setGestureView(null);
                setGestureViewOperationState(GestureViewOperation.None.state());

                return false;
            }

            return true;
        }
        return super.onTouchEvent(event);
    }

    /* Private Methods */
    private void init() {
        // layer view can draw border outside
        // 图层可以在其frame外显示内容
        setClipChildren(false);

        setGestureViewOperationState(GestureViewOperation.None.state());
    }

    /* Enums */
    /**
     * 对图层进行的手势操作类别
     * Sacling 和 Rotation 可以同时进行
     */
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