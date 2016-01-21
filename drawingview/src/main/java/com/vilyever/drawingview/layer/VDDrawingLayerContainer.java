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
 * 图层容器，管理除base图层以外的所有图层的触摸响应
 */
public class VDDrawingLayerContainer extends RelativeLayout {
    final VDDrawingLayerContainer self = this;

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

    /**
     * 添加图层
     * @param layerView 添加的图层
     */
    public void addLayerView(VDDrawingLayerViewProtocol layerView) {
        self.addView((View) layerView);

        // 由容器控制layer的触摸响应
        ((View) layerView).setOnTouchListener(self.getLayerOnTouchListener());
    }

    /**
     * 移除图层
     * @param layerView 移除的图层
     */
    public void removeLayerView(VDDrawingLayerViewProtocol layerView) {
        self.removeView((View) layerView);
        self.setGestureView(null);
    }

    /**
     * 清空图层
     */
    public void clear() {
        self.removeAllViews();
        self.setGestureView(null);
    }

    public interface Delegate {
        /**
         * 图层被触摸ACTION_DOWN时的反馈，此时理应准备开始下一step，接收者应在此时结束先前未完成的step
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void layerViewDidBeginTouch(VDDrawingLayerContainer container, VDDrawingLayerViewProtocol layerView);

        /**
         * 图层每次变换（平移/旋转/缩放）时的反馈，此反馈是连续微小的主要用于远程同步
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void layerViewTransforming(VDDrawingLayerContainer container, VDDrawingLayerViewProtocol layerView);

        /**
         * 图层结束变换，此时当前变化step理应结束
         * @param container 当前容器
         * @param layerView 被触摸的图层
         */
        void layerViewDidEndTransform(VDDrawingLayerContainer container, VDDrawingLayerViewProtocol layerView);

        /**
         * 对于text图层有一个双击进行编辑的操作，此操作与其他操作独立
         * @param container 当前容器
         * @param textView 被触摸的text图层
         */
        void layerTextViewDidBeginEdit(VDDrawingLayerContainer container, VDDrawingLayerTextView textView);
    }
    private Delegate delegate;
    public VDDrawingLayerContainer setDelegate(Delegate delegate) {
        this.delegate = delegate;
        return this;
    }
    public Delegate getDelegate() {
        return delegate;
    }

    /**
     * 手势检测，主要用于平移和text图层的双击编辑
     */
    private GestureDetector gestureDetector;
    private VDDrawingLayerContainer setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
        return this;
    }
    private GestureDetector getGestureDetector() {
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(self.getContext(), new GestureListener());
        }
        return gestureDetector;
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
                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.getGestureView().getLayoutParams();
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
            // 存储平移操作
            if ((self.getGestureViewOperationState() & GestureViewOperation.Moving.state())  != GestureViewOperation.Moving.state()) {
                self.setGestureViewOperationState(self.getGestureViewOperationState() | GestureViewOperation.Moving.state());
            }

            // 判断是否在操作处于编辑状态的text图层，若是，则禁止变换
            if (self.getGestureView() instanceof VDDrawingLayerTextView) {
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getGestureView();
                if (textView.isEditing()) {
                    return true;
                }
            }

            // 缩放和旋转的操作优先于平移，若正在进行缩放或旋转操作，禁止平移
            if (self.getGestureView() != null
                    && (self.getGestureViewOperationState() == (GestureViewOperation.None.state() | GestureViewOperation.Moving.state()))) {
                float dx = e2.getRawX() - beginX;
                float dy = e2.getRawY() - beginY;

                RelativeLayout.LayoutParams layoutParams = (LayoutParams) self.getGestureView().getLayoutParams();
                layoutParams.leftMargin = (int) Math.floor(originalLeftMargin + dx);
                layoutParams.topMargin = (int) Math.floor(originalTopMargin + dy);
                self.getGestureView().setLayoutParams(layoutParams);
                self.getDelegate().layerViewTransforming(self, (VDDrawingLayerViewProtocol) self.getGestureView());
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
                    && self.getGestureView() instanceof VDDrawingLayerTextView) {
                self.setGestureViewOperationState(self.getGestureViewOperationState() | GestureViewOperation.DoubleTap.state());

                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getGestureView();
                textView.beginEdit(false);

                self.getDelegate().layerTextViewDidBeginEdit(self, textView);
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
    private VDDrawingLayerContainer setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
        this.scaleGestureDetector = scaleGestureDetector;
        return this;
    }
    private ScaleGestureDetector getScaleGestureDetector() {
        if (scaleGestureDetector == null) {
            scaleGestureDetector = new ScaleGestureDetector(self.getContext(), new ScaleListener());
        }
        return scaleGestureDetector;
    }
    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (self.getGestureView() != null
                    && ((self.getGestureViewOperationState() & GestureViewOperation.Scaling.state()) == GestureViewOperation.Scaling.state()) ) {

                // 判断是否在操作处于编辑状态的text图层，若是，则禁止变换
                if (self.getGestureView() instanceof VDDrawingLayerTextView) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getGestureView();
                    if (textView.isEditing()) {
                        return true;
                    }
                }

                float scaleFactor = detector.getScaleFactor();
                self.getGestureView().setScaleX(self.getGestureView().getScaleX() * scaleFactor);
                self.getGestureView().setScaleY(self.getGestureView().getScaleY() * scaleFactor);
                self.getDelegate().layerViewTransforming(self, (VDDrawingLayerViewProtocol) self.getGestureView());
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
    private VDRotationGestureDetector rotationGestureDetector;
    private VDDrawingLayerContainer setRotationGestureDetector(VDRotationGestureDetector rotationGestureDetector) {
        this.rotationGestureDetector = rotationGestureDetector;
        return this;
    }
    private VDRotationGestureDetector getRotationGestureDetector() {
        if (rotationGestureDetector == null) {
            rotationGestureDetector = new VDRotationGestureDetector(new RotationListener());
        }
        return rotationGestureDetector;
    }
    private class RotationListener implements VDRotationGestureDetector.OnRotationGestureListener {
        private static final float TriggerAngle = 10.0f;
        private float originalRotation;
        private float triggerOffset;
        @Override
        public void onRotation(VDRotationGestureDetector rotationDetector) {
            float angle = rotationDetector.getAngle();

            /**
             * 若旋转角度超过阈值，标记此时进行旋转操作
             * 记下在到达旋转阈值前未旋转的角度在之后补偿
             */
            if (Math.abs(angle) > TriggerAngle
                    && ((self.getGestureViewOperationState() & GestureViewOperation.Rotation.state()) != GestureViewOperation.Rotation.state()) ) {
                self.setGestureViewOperationState(self.getGestureViewOperationState() | GestureViewOperation.Rotation.state());
                originalRotation = self.getGestureView().getRotation();
                triggerOffset = -Math.signum(angle);
            }

            if (self.getGestureView() != null
                    && ((self.getGestureViewOperationState() & GestureViewOperation.Rotation.state()) == GestureViewOperation.Rotation.state()) ) {

                // 判断是否在操作处于编辑状态的text图层，若是，则禁止变换
                if (self.getGestureView() instanceof VDDrawingLayerTextView) {
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getGestureView();
                    if (textView.isEditing()) {
                        return;
                    }
                }

                self.getGestureView().setRotation(-(angle + triggerOffset - originalRotation));
                self.getDelegate().layerViewTransforming(self, (VDDrawingLayerViewProtocol) self.getGestureView());
            }
        }
    }

    /**
     * 当前正在进行手势操作的图层view
     */
    private View gestureView;
    private VDDrawingLayerContainer setGestureView(View gestureView) {
        this.gestureView = gestureView;
        return this;
    }
    private View getGestureView() {
        return gestureView;
    }

    /**
     * {@link #gestureView}正在进行的操作
     */
    private int gestureViewOperationState;
    private VDDrawingLayerContainer setGestureViewOperationState(int gestureViewOperationState) {
        this.gestureViewOperationState = gestureViewOperationState;
        return this;
    }
    private int getGestureViewOperationState() {
        return gestureViewOperationState;
    }

    /**
     * 所有图层公用的touchListener
     * 此回调在复写的{@link #onTouchEvent(MotionEvent)}之前，可以进行判断拦截
     */
    private OnTouchListener layerOnTouchListener;
    public OnTouchListener getLayerOnTouchListener() {
        if (layerOnTouchListener == null) {
            layerOnTouchListener = new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    VDDrawingLayerViewProtocol layerViewProtocol = (VDDrawingLayerViewProtocol) v;
                    // 检测图层是否允许操作
                    if (!layerViewProtocol.canHandle()) {
                        return false;
                    }

                    // 检测是否是正在编辑的text图层
                    if (v instanceof VDDrawingLayerTextView) {
                        VDDrawingLayerTextView textView = (VDDrawingLayerTextView) v;
                        if (textView.isEditing()) {
                            return false;
                        }
                    }

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        self.setGestureView(v);
                        self.getDelegate().layerViewDidBeginTouch(self, (VDDrawingLayerViewProtocol) v);
                    }
                    return false;
                }
            };
        }
        return layerOnTouchListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /** 若先前调用的{@link #layerOnTouchListener} 检测此时无法进行操作（gestureView为空），则不进行手势判断 */
        if (self.getGestureView() != null) {
            self.getScaleGestureDetector().onTouchEvent(event);
            self.getRotationGestureDetector().onTouchEvent(event);
            self.getGestureDetector().onTouchEvent(event);

            // 一轮触摸取消时，反馈到上级并重置手势判断相关参数
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {

                // 若此次操作未造成任何变化，则无需进行后续反馈步骤
                if (self.getGestureViewOperationState() == GestureViewOperation.None.state()) {
                    self.setGestureView(null);
                    return false;
                }

                // 若操作的图层此时是处于编辑状态的text图层，仅重置手势判断相关参数
                if (self.getGestureView() instanceof VDDrawingLayerTextView) {
                    if (self.getGestureViewOperationState() == (GestureViewOperation.None.state() | GestureViewOperation.DoubleTap.state())) {
                        VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getGestureView();
                        if (textView.isEditing()) {
                            self.setGestureView(null);
                            self.setGestureViewOperationState(GestureViewOperation.None.state());
                            return false;
                        }
                    }
                }

                self.getDelegate().layerViewDidEndTransform(self, (VDDrawingLayerViewProtocol) self.getGestureView());

                self.setGestureView(null);
                self.setGestureViewOperationState(GestureViewOperation.None.state());

                return false;
            }

            return true;
        }
        return super.onTouchEvent(event);
    }

    private void initial(Context context) {
        // layer view can draw border outside
        // 图层可以在其frame外显示内容
        self.setClipChildren(false);

        self.setGestureViewOperationState(GestureViewOperation.None.state());
    }

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