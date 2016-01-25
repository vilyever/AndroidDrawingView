package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
 * 绘画板视图
 */
public class VDDrawingView extends RelativeLayout implements View.OnLayoutChangeListener, VDDrawingLayerTextView.Delegate {
    private final VDDrawingView self = this;

    private static final int UnhandleAnyLayer = -1;
    private static final int HandleAllLayer = -2;

    /* Constructors */
    public VDDrawingView(Context context) {
        this(context, null);
    }

    public VDDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VDDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        self.initial();
    }

    /* Public Methods */
    /**
     * 清空当前所有绘制内容
     */
    public void clear() {
        // 如果处于正在绘制状态则无视此次请求
        if (self.isTouching()) {
            return;
        }

        // 当前已经是清空状态，无需在此clear
        if (self.getCurrentDrawingStep().isClearStep()) {
            return;
        }

        self.endUnfinishedStep(); // 结束当前未完成的step
        self.nativeClear(); // 清空绘制
        self.getDrawingData()
                .newDrawingStepOnBaseLayer(self.getWidth(), self.getHeight())
                .setStepType(VDDrawingStep.StepType.Clear); // 记录清空step
        self.overCurrentStep(); // 结束当前step
    }

    /**
     * 获取当前正在绘制（或刚刚绘制完成）的step
     * @return 当前step
     */
    public VDDrawingStep getCurrentDrawingStep() {
        return self.getDrawingData().getDrawingStep();
    }

    /**
     * 在当前绘制基础上增加绘制传入的step，如果传入的step与当前未完成的step是同一step（编号相同，远程同步step可能有未完成和已完成两种状态），更新当前step
     * @param step 将要绘制的step
     */
    public void drawNextStep(@NonNull VDDrawingStep step) {
        if (step.getStep() != self.getCurrentDrawingStep().getStep()) {
            self.endUnfinishedStep();
            self.getDrawingData().addDrawingStep(step);
        }

        if (step.isCanceled()) {
            self.nativeCancelCurrentStep();
            self.getDrawingData().cancelDrawingStep();
        }
        else {
            self.nativeUpdateCurrentStep();
        }
    }

    /**
     * 使用传入的data更新绘制
     * @param data 即将绘制的data
     */
    public void refreshWithDrawingData(VDDrawingData data) {
        // 传入data与当前data相同则忽视此次调用
        if (data == self.drawingData) {
            return;
        }
        self.setDrawingData(data);
        self.nativeClear();
        self.nativeDrawData();

        self.getDrawingDelegate().didUpdateUndoRedoState(self, self.getDrawingData().canUndo(), self.getDrawingData().canRedo());
    }

    /**
     * 为图层设置背景色，这个设置将会记录在绘制数据中
     * @param layerHierarchy 图层层次
     * @param color 背景色
     */
    public void setBackgroundColor(int layerHierarchy, int color) {
        // 判断图层是否存在
        VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerViewProtocol == null) {
            return;
        }

        self.endUnfinishedStep();
        self.nativeSetBackgroundColor(layerHierarchy, color);

        /**
         * 记录此step，获取图层的type
         */
        VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Base;
        if (layerViewProtocol instanceof VDDrawingLayerImageView) {
            layerType = VDDrawingLayer.LayerType.Image;
        }
        else if (layerViewProtocol instanceof VDDrawingLayerTextView) {
            layerType = VDDrawingLayer.LayerType.Text;
        }

        self.getDrawingData()
                .newDrawingStepOnLayer(layerHierarchy, layerType, self.getWidth(), self.getHeight())
                .setStepType(VDDrawingStep.StepType.Background)
                .getDrawingLayer()
                .setBackgroundColor(color);
        self.overCurrentStep();
    }

    /**
     * 为图层设置背景drawable，这个设置将会记录在绘制数据中
     * @param layerHierarchy 图层层次
     * @param drawable 背景drawable，可以是颜色图片等
     * @param identifier 背景drawable的唯一标识ID
     * 因为绘制数据中不宜保存背景drawable，故存储唯一标识id，在重绘时通过{@link com.vilyever.drawingview.VDDrawingView.DrawingDelegate#gainBackground(VDDrawingView, String)}回调向外部通过id获取对应的背景drawable
     */
    public void setBackgroundDrawable(int layerHierarchy, Drawable drawable, String identifier) {
        // 判断图层是否存在
        VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerViewProtocol == null) {
            return;
        }

        self.endUnfinishedStep();
        self.nativeSetBackgroundDrawable(layerHierarchy, drawable);

        /**
         * 记录此step，获取图层的type
         */
        VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Base;
        if (layerViewProtocol instanceof VDDrawingLayerImageView) {
            layerType = VDDrawingLayer.LayerType.Image;
        }
        else if (layerViewProtocol instanceof VDDrawingLayerTextView) {
            layerType = VDDrawingLayer.LayerType.Text;
        }

        self.getDrawingData()
                .newDrawingStepOnLayer(layerHierarchy, layerType, self.getWidth(), self.getHeight())
                .setStepType(VDDrawingStep.StepType.Background)
                .getDrawingLayer()
                .setBackgroundImageIdentifier(identifier);
        self.overCurrentStep();
    }

    /**
     * 删除当前操作的图层
     * @return 是否成功删除
     */
    public boolean deleteHandlingLayer() {
        // 如果处于正在绘制状态则无视此次请求
        if (self.isTouching()) {
            return false;
        }

        // 如果当前操作的图层存在，删除数据，移除图层，记录step
        if (self.getHandlingLayerView() != null
                && self.getHandlingLayerView().getLayerHierarchy() > 0
                && self.getCurrentDrawingStep().isStepOver()) {
            self.endUnfinishedStep();

            self.getDrawingData()
                    .newDrawingStepOnLayer(self.getHandlingLayerView().getLayerHierarchy(), VDDrawingLayer.LayerType.Unkonwn, self.getWidth(), self.getHeight())
                    .setStepType(VDDrawingStep.StepType.DeleteLayer);

            self.getLayerContainer().removeLayerView(self.getHandlingLayerView());
            self.getLayerViews().remove(self.getHandlingLayerView());
            self.setHandlingLayerView(null);

            self.overCurrentStep();

            return true;
        }

        return false;
    }

    /**
     * 撤销一步
     * @return 是否撤销成功
     */
    public boolean undo() {
        // 若允许撤销，撤销当前step，通知委托方当前撤销/重做状态
        if (self.canUndo()) {
            self.getDrawingData().undo();
            self.nativeClear();
            self.nativeDrawData();

            self.getDrawingDelegate().didUpdateUndoRedoState(self, self.getDrawingData().canUndo(), self.getDrawingData().canRedo());

            return true;
        }
        return false;
    }

    /**
     * 重做一步
     * @return 是否重做成功
     */
    public boolean redo() {
        // 若允许重做，重做下一step，通知委托方当前撤销/重做状态
        if (self.canRedo()) {
            self.getDrawingData().redo();
            self.nativeClear();
            self.nativeDrawData();

            self.getDrawingDelegate().didUpdateUndoRedoState(self, self.getDrawingData().canUndo(), self.getDrawingData().canRedo());

            return true;
        }
        return false;
    }

    /**
     * 判断当前是否可以撤销
     * @return 是否允许撤销
     */
    public boolean canUndo() {
        return !self.isTouching() && self.getDrawingData().canUndo();
    }

    /**
     * 判断当前是否可以重做
     * @return 是否允许重做
     */
    public boolean canRedo() {
        return !self.isTouching() && self.getDrawingData().canRedo();
    }

    /* Properties */
    /**
     * 绘制代理，通知状态变更和获取数据
     */
    public interface DrawingDelegate {
        /**
         * 当前绘制step变更时回调，每次touch绘制都会执行，text图层修改内容也会执行，此回调执行频繁，通常用于远程同步
         * step处于变化状态
         * @param drawingView 当前view
         * @param step 当前绘制step
         */
        void didUpdateCurrentStep(VDDrawingView drawingView, VDDrawingStep step);

        /**
         * 当前绘制状态已改变，绘制一笔或进行撤销/重做/清空等变更记录数据的操作都会触发此回调
         * step已经完成
         * @param drawingView 当前view
         * @param data 绘制数据

         */
        void didUpdateDrawingData(VDDrawingView drawingView, VDDrawingData data);

        /**
         * 撤销重做状态变更
         * @param drawingView 当前view
         * @param canUndo 当前是否可以撤销，因为此回调可能在touching时调用，导致外部获取{@link #canUndo()}状态错误
         * @param canRedo 当前是否可以重做，因为此回调可能在touching时调用，导致外部获取{@link #canRedo()}状态错误
         */
        void didUpdateUndoRedoState(VDDrawingView drawingView, boolean canUndo, boolean canRedo);

        /**
         * 回报当前是否拦截了touch事件进行绘画（这个回调标志着绘画开始或结束），当此drawingView是recyclerView（或类似可滑动的view）的子view时可用于通知父view禁止滑动
         * @param drawingView 当前view
         * @param isIntercept 是否拦截
         */
        void didInterceptTouchEvent(VDDrawingView drawingView, boolean isIntercept);

        /**
         * 请求对应唯一标识ID的drawbale。{@link #setBackgroundDrawable(int, Drawable, String)}
         * @param drawingView 当前view
         * @param identifier drawable对应的标识id
         * @return 对应的drawable
         */
        Drawable gainBackground(VDDrawingView drawingView, String identifier);
    }
    private DrawingDelegate drawingDelegate;
    public VDDrawingView setDrawingDelegate(DrawingDelegate drawingDelegate) {
        this.drawingDelegate = drawingDelegate;
        return this;
    }
    public DrawingDelegate getDrawingDelegate() {
        if (drawingDelegate == null) {
            drawingDelegate = new DrawingDelegate() {
                @Override
                public void didUpdateCurrentStep(VDDrawingView drawingView, VDDrawingStep step) {
                }
                @Override
                public void didUpdateDrawingData(VDDrawingView drawingView, VDDrawingData data) {
                }
                @Override
                public void didUpdateUndoRedoState(VDDrawingView drawingView, boolean canUndo, boolean canRedo) {
                }
                @Override
                public void didInterceptTouchEvent(VDDrawingView drawingView, boolean isIntercept) {
                }
                @Override
                public Drawable gainBackground(VDDrawingView drawingView, String identifier) {
                    return null;
                }
            };
        }
        return drawingDelegate;
    }

    /**
     * 当前view是否可以通过手指绘制
     * 若当前view是同步显示view，则可设为true禁止触摸绘制
     */
    private boolean disableTouchDraw;
    public VDDrawingView setDisableTouchDraw(boolean disableTouchDraw) {
        this.disableTouchDraw = disableTouchDraw;
        return this;
    }
    public boolean isDisableTouchDraw() {
        return disableTouchDraw;
    }

    /**
     * 当前记录的所有的绘制数据
     */
    private VDDrawingData drawingData;
    private VDDrawingView setDrawingData(VDDrawingData drawingData) {
        this.drawingData = drawingData;
        return this;
    }
    public VDDrawingData getDrawingData() {
        if (self.drawingData == null) {
            self.drawingData = new VDDrawingData();
            // initial first drawTouchMoving step that clear the view
            self.drawingData
                    .newDrawingStepOnBaseLayer(self.getWidth(), self.getHeight())
                    .setStepType(VDDrawingStep.StepType.Clear)
                    .setStepOver(true);
        }
        return drawingData;
    }

    /**
     * 当前是否正在处理触摸事件
     */
    private boolean touching;
    private VDDrawingView setTouching(boolean touching) {
        this.touching = touching;
        return this;
    }
    public boolean isTouching() {
        return touching;
    }

    /**
     * 当前用于绘制的brush
     */
    private VDBrush brush;
    public <T extends VDBrush> T getBrush() {
        if (self.brush == null) {
            // 默认使用penBrush
            self.brush = VDPenBrush.defaultBrush();
        }
        return (T) brush;
    }
    public void setBrush(VDBrush brush) {
        this.brush = brush;
        self.setBrushChanged(true);
    }

    /**
     * 标志当前brush已经变更，用于在下次开始处理触摸事件时更换brush
     * 推迟更换brush是由于如果正在绘制时调用了{@link #setBrush(VDBrush)}会导致各种状况
     */
    private boolean brushChanged;
    public VDDrawingView setBrushChanged(boolean brushChanged) {
        this.brushChanged = brushChanged;
        return this;
    }
    public boolean isBrushChanged() {
        return brushChanged;
    }

    /**
     * 绘制的底层图层，此图层不可进行平移/缩放/旋转
     */
    private VDDrawingLayerBaseView baseLayerImageView;
    public VDDrawingLayerBaseView getBaseLayerImageView() {
        if (baseLayerImageView == null) {
            baseLayerImageView = new VDDrawingLayerBaseView(self.getContext());
            baseLayerImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            baseLayerImageView.setDelegate(new VDDrawingLayerBaseView.Delegate() {
                @Override
                public void didChangeBusyState(VDDrawingLayerBaseView baseView, boolean busying) {
                    /**
                     * base底层忙于耗时绘制时，显示动画层
                     */
                    self.getBaseLayerImageView().setVisibility(busying ? INVISIBLE : VISIBLE);
                    self.getLayerContainer().setVisibility(busying ? INVISIBLE : VISIBLE);

                    self.getAnimationView().setVisibility(busying ? VISIBLE : GONE);
                    self.getAnimationView().setAnimated(busying);
                }
            });
        }
        return baseLayerImageView;
    }

    /**
     * 放置除底层以外所有图层的容器
     * 处理图层相关触摸操作，z轴上在底层上一层
     */
    private VDDrawingLayerContainer layerContainer;
    public VDDrawingLayerContainer getLayerContainer() {
        if (layerContainer == null) {
            layerContainer = new VDDrawingLayerContainer(self.getContext());
            layerContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layerContainer.setDelegate(new VDDrawingLayerContainer.Delegate() {
                @Override
                public void layerTextViewDidBeginEdit(VDDrawingLayerContainer container, VDDrawingLayerTextView textView) {

                    // 取消先前生成的变换step
                    self.cancelCurrentStep();

                    /**
                     * text图层在双击后进入编辑状态，此时可以输入
                     * 记录此step开始
                     */
                    self.getDrawingData()
                            .newDrawingStepOnLayer(textView.getLayerHierarchy(), VDDrawingLayer.LayerType.Text, self.getWidth(), self.getHeight())
                            .setStepType(VDDrawingStep.StepType.TextChange);
                    self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
                }

                @Override
                public void layerViewDidBeginTouch(VDDrawingLayerContainer container, VDDrawingLayerViewProtocol layerView) {
                    /**
                     * 图层被触摸时，此后将会对图层进行平移/缩放/旋转
                     * 因此，结束当前未完成的step
                     * 标志当前正在操作的图层
                     */
                    self.endUnfinishedStep();
                    self.handleLayer(layerView.getLayerHierarchy());

                    /**
                     * 图层可能开始平移/缩放/旋转
                     * 预先记录此次操作step，若后续操作并非进行图层变换，则cancel此step
                     */
                    VDDrawingLayer.LayerType layerType = VDDrawingLayer.LayerType.Image;
                    if (layerView instanceof VDDrawingLayerTextView) {
                        layerType = VDDrawingLayer.LayerType.Text;
                    }

                    self.getDrawingData()
                            .newDrawingStepOnLayer(layerView.getLayerHierarchy(), layerType, self.getWidth(), self.getHeight())
                            .setStepType(VDDrawingStep.StepType.Transform);
                    self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
                }

                @Override
                public void layerViewTransforming(VDDrawingLayerContainer container, VDDrawingLayerViewProtocol layerView) {
                    View v = (View) layerView;

                    RelativeLayout.LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
                    self.getCurrentDrawingStep().getDrawingLayer().setLeft(layoutParams.leftMargin);
                    self.getCurrentDrawingStep().getDrawingLayer().setTop(layoutParams.topMargin);
                    self.getCurrentDrawingStep().getDrawingLayer().setRight(layoutParams.leftMargin + layoutParams.width);
                    self.getCurrentDrawingStep().getDrawingLayer().setBottom(layoutParams.topMargin + layoutParams.height);
                    self.getCurrentDrawingStep().getDrawingLayer().setScale(v.getScaleX());
                    self.getCurrentDrawingStep().getDrawingLayer().setRotation(v.getRotation());

                    self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
                }

                @Override
                public void layerViewDidEndTransform(VDDrawingLayerContainer container, VDDrawingLayerViewProtocol layerView) {
                    View v = (View) layerView;

                    RelativeLayout.LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
                    self.getCurrentDrawingStep().getDrawingLayer().setLeft(layoutParams.leftMargin);
                    self.getCurrentDrawingStep().getDrawingLayer().setTop(layoutParams.topMargin);
                    self.getCurrentDrawingStep().getDrawingLayer().setRight(layoutParams.leftMargin + layoutParams.width);
                    self.getCurrentDrawingStep().getDrawingLayer().setBottom(layoutParams.topMargin + layoutParams.height);
                    self.getCurrentDrawingStep().getDrawingLayer().setScale(v.getScaleX());
                    self.getCurrentDrawingStep().getDrawingLayer().setRotation(v.getRotation());

                    self.overCurrentStep();
                }
            });
        }
        return layerContainer;
    }

    /**
     * 存放所有图层
     */
    private List<VDDrawingLayerViewProtocol> layerViews;
    public List<VDDrawingLayerViewProtocol> getLayerViews() {
        if (layerViews == null) {
            layerViews = new ArrayList<>();
        }
        return layerViews;
    }

    /**
     * 当前正在操作的图层，有可能为null
     */
    private VDDrawingLayerViewProtocol handlingLayerView;
    private VDDrawingView setHandlingLayerView(VDDrawingLayerViewProtocol handlingLayerView) {
        this.handlingLayerView = handlingLayerView;
        return this;
    }
    public VDDrawingLayerViewProtocol getHandlingLayerView() {
        return handlingLayerView;
    }

    /**
     * 耗时绘制动画层，此层位于最上方
     * 在底层进行大量数据耗时绘制时显示
     */
    private VDDrawingAnimationView animationView;
    public VDDrawingAnimationView getAnimationView() {
        if (animationView == null) {
            animationView = new VDDrawingAnimationView(self.getContext());
            animationView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return animationView;
    }

    /**
     * 是否应该处理触摸事件，用于禁用多指触摸绘制，多指触摸可能导致多种意外
     */
    private boolean shouldHandleOnTouch; // for limit only first finger can draw.
    private VDDrawingView setShouldHandleOnTouch(boolean shouldHandleOnTouch) {
        this.shouldHandleOnTouch = shouldHandleOnTouch;
        return this; 
    }
    private boolean shouldHandleOnTouch() {
        return shouldHandleOnTouch;
    }

    /* Overrides */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (self.isDisableTouchDraw()) {
            return false;
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                self.getDrawingDelegate().didInterceptTouchEvent(self, true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                self.getDrawingDelegate().didInterceptTouchEvent(self, false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.isDisableTouchDraw()) {
            return false;
        }

        /**
         * 若底层正在进行耗时绘制，禁止此时进行触摸绘制
         */
        if (self.getBaseLayerImageView().isBusying()) {
            return true;
        }

        int action = event.getAction();
        int maskAction = event.getAction() & MotionEvent.ACTION_MASK;

        // only handle the first touch finger
        if (action == MotionEvent.ACTION_DOWN) {
            // 仅在第一只手指第一次接触屏幕时，允许此手指开始绘制
            self.setShouldHandleOnTouch(true);

            /**
             * 如果当前正在绘制的step是未完成的text图层（此时屏幕上应显示一个高亮的文本框，其中可能有内容，肯能没内容）
             * 结束当前text图层的绘制step
             * 即text图层无法主动判断step是否完成，用户可能无限时等待键盘输入，此时点击text图层意外的任意一点，即可表示完成此次text图层的step
             * 若进行此操作，显然不应继续使用此次触摸开始进行下一step的绘制
             */
            if (self.getCurrentDrawingStep().getDrawingLayer().getLayerType() == VDDrawingLayer.LayerType.Text
                    && !self.getCurrentDrawingStep().isStepOver()) {
                self.endUnfinishedStep();
                self.setShouldHandleOnTouch(false);
            }
        }

        /**
         * 此时不允许此次触摸继续绘制
         */
        if (!self.shouldHandleOnTouch()) {
            return true;
        }

        /**
         * 第一只触摸的手指离开屏幕时，终止此轮触摸事件流继续绘制的许可
         */
        if (maskAction == MotionEvent.ACTION_POINTER_UP
                && event.getActionIndex() == 0) {
            action = MotionEvent.ACTION_UP;
            self.setShouldHandleOnTouch(false);
        }

        /**
         * 依照第一只手指的触摸状态绘制
         */
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                self.setTouching(true);
                self.getParent().requestDisallowInterceptTouchEvent(true);
                self.drawBeginTouch(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                self.drawTouchMoving(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                self.drawTouchEnd(event.getX(), event.getY());
                self.getParent().requestDisallowInterceptTouchEvent(false);
                self.setTouching(false);
                break;
        }

        return true;
    }

    /* Delegates */
    /** {@link android.view.View.OnLayoutChangeListener} */
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;

        int width = right - left;
        int height = bottom - top;

        boolean changed = (oldWidth != width) || (oldHeight != height);

        /**
         * 宽高改变时，重新绘制记录的数据
         * 此时绘制的数据坐标将会进行缩放
         */
        if (changed) {
            self.nativeClear();
            self.nativeDrawData();
        }
    }
    /** {@link android.view.View.OnLayoutChangeListener} */

    /** {@link com.vilyever.drawingview.layer.VDDrawingLayerTextView.Delegate} */
    @Override
    public void didChangeText(VDDrawingLayerTextView textView, String text) {
        self.getCurrentDrawingStep().getDrawingLayer().setText(text);
        self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
    }
    /** {@link com.vilyever.drawingview.layer.VDDrawingLayerTextView.Delegate} */

    /* Private Methods */
    /**
     * 初始化内部控件
     */
    private void initial() {
        self.addOnLayoutChangeListener(self);

        // focusable, which can clear focus from edittext
        self.setFocusable(true);
        self.setFocusableInTouchMode(true);

        // setup base layer view
        self.getLayerViews().add(self.getBaseLayerImageView());
        self.addView(self.getBaseLayerImageView());
        self.addView(self.getLayerContainer());
        self.addView(self.getAnimationView());
        self.getAnimationView().setVisibility(View.GONE);
    }

    /**
     * 开始绘制
     * 若此时开始新的step绘制，生成一个新的未完成的step
     * 若此时继续绘制当前未完成的step，此次调用将类似于{@link #drawTouchMoving(float, float)}
     * @param x 触摸x坐标
     * @param y 触摸y坐标
     */
    private void drawBeginTouch(float x, float y) {
        /**
         * 若此时brush需要更换，则结束当前未完成step
         */
        if (self.isBrushChanged()) {
            self.setBrushChanged(false);
            self.endUnfinishedStep();
        }

        // 绘制point的标识符id递增
        VDDrawingPoint.IncrementPointerID();

        // 当前step已完成，开始绘制新的step
        if (self.getCurrentDrawingStep().isStepOver()) {
            self.handleLayer(UnhandleAnyLayer);

             // 根据brush类型进行绘制
            if (self.getBrush() instanceof VDDrawingBrush) {
                VDDrawingBrush drawingBrush = self.getBrush();

                // 根据绘图brush是否需要绘制新图层进行绘制，记录当前操作的图层
                if (drawingBrush.isOneStrokeToLayer()) {
                    /**
                     * 若绘制新图层，生成step，并生成对应的图层view
                     */
                    self.getDrawingData().newDrawingStepOnNextLayer(VDDrawingLayer.LayerType.Image, self.getWidth(), self.getHeight()).setStepType(VDDrawingStep.StepType.CreateLayer).setBrush(VDBrush.copy(drawingBrush));
                    self.setHandlingLayerView(new VDDrawingLayerImageView(self.getContext()));
                    self.getLayerViews().add(self.getHandlingLayerView());
                    self.getLayerContainer().addLayerView(self.getHandlingLayerView());
                }
                else {
                    /**
                     * 若绘制在base底层，仅生成step
                     */
                    self.getDrawingData()
                            .newDrawingStepOnBaseLayer(self.getWidth(), self.getHeight())
                            .setStepType(VDDrawingStep.StepType.DrawOnBase)
                            .setBrush(VDBrush.copy(drawingBrush));
                    self.setHandlingLayerView(self.getBaseLayerImageView());
                }
            }
            else if (self.getBrush() instanceof VDTextBrush) {
                /**
                 * text图层类似于 新绘画图层，生成step，生成图层view，记录操作图层
                 */
                VDTextBrush textBrush = self.getBrush();
                self.getDrawingData().newDrawingStepOnNextLayer(VDDrawingLayer.LayerType.Text, self.getWidth(), self.getHeight()).setStepType(VDDrawingStep.StepType.CreateLayer).setBrush(VDBrush.copy(textBrush));

                self.setHandlingLayerView(new VDDrawingLayerTextView(self.getContext()));
                self.getLayerViews().add(self.getHandlingLayerView());
                self.getLayerContainer().addLayerView(self.getHandlingLayerView());
            }

            self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.VeryBegin, VDBrush.DrawingPointerState.TouchDown));
            self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());

            self.getHandlingLayerView().setHandling(true);
        }
        else {
            /**
             * 若此时正在绘制的step未完成，则继续绘制此step
             */
            self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));
            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchDown));
            self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());
        }

        self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
    }

    /**
     * 绘制进行中，完善绘制信息
     * @param x 触摸x坐标
     * @param y 触摸y坐标
     */
    private void drawTouchMoving(float x, float y) {
        if (self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y))) {
            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchMoving));
            self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());
            self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
        }
    }

    /**
     * 当前触摸事件流结束
     * 调用此方法可能完成当前step，也可能未完成
     * @param x 触摸x坐标
     * @param y 触摸y坐标
     */
    private void drawTouchEnd(float x, float y) {
        self.getCurrentDrawingStep().getDrawingPath().addPoint(new VDDrawingPoint(x, y));

        switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
            case Base:
            case Image:
                /**
                 * base底层和image图层判断当前step是否完成，若完成则进行 finish 步骤
                 */
                self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchUp));
                boolean stepOver = !self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep()).requireMoreDetail;

                self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());

                if (stepOver) {
                    self.finishDraw();
                }
                break;
            case Text:
                /**
                 * 通过touch事件流不可能直接完成text图层的绘制
                 * 此时仅是显示一个高亮文本框提供用户进行输入
                 */
                self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.TouchUp));
                self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());
                VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getHandlingLayerView();
                textView.setDelegate(self);
                textView.beginEdit(true);

                self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());
                break;
        }
    }

    /**
     * 强制结束当前step
     * 无论当前step是否标记stepOver，都会强制结束
     * step是否应当调用此方法结束由调用处判断
     */
    private void finishDraw() {
        self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.VeryEnd));
        self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());

        /**
         * 若当前step的brush通过对所有绘制触摸点的计算返回此时无需绘制任何事物时，取消当前step
         */
        if (self.getCurrentDrawingStep().getDrawingLayer().getFrame() == null) {
            // noting to draw, e.g. draw line with one point is pointless
            switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
                case Base:
                    break;
                case Image:
                case Text:
                    /**
                     * 取消当前step前移除当前图层view
                     */
                    self.getLayerContainer().removeLayerView(self.getHandlingLayerView());
                    self.getLayerViews().remove(self.getHandlingLayerView());
                    break;
            }

            self.setHandlingLayerView(null);
            self.cancelCurrentStep();
            return;
        }

        self.overCurrentStep();
    }

    /**
     * 完成当前未完成的step
     */
    private void endUnfinishedStep() {
        // 仅处理未完成的step
        if (self.getCurrentDrawingStep() != null
                && !self.getCurrentDrawingStep().isStepOver()) {

            switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
                case Base:
                    // 对base底层，向step发送当前需要强制结束的指示后结束step
                    self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.ForceFinish));
                    self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());

                    self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());

                    self.finishDraw();
                    break;
                case Image:
                    // 对image图层，因暂时不提供二次编辑功能，故仅在创建图层时存在未完成状态，处理类似base底层
                    VDDrawingLayerImageView imageView = (VDDrawingLayerImageView) self.getHandlingLayerView();
                    switch (self.getCurrentDrawingStep().getStepType()) {
                        case CreateLayer:
                            self.getCurrentDrawingStep().setDrawingState(new VDBrush.DrawingState(VDBrush.DrawingPointerState.ForceFinish));
                            self.getHandlingLayerView().appendWithDrawingStep(self.getCurrentDrawingStep());

                            self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());

                            self.finishDraw();
                            break;
                    }
                    break;
                case Text:
                    VDDrawingLayerTextView textView = (VDDrawingLayerTextView) self.getHandlingLayerView();
                    switch (self.getCurrentDrawingStep().getStepType()) {
                        case CreateLayer:
                            textView.endEdit();
                            if (textView.isChangedSinceLastStep()) {
                                // 对text图层，若此时是第一次创建完图层，若已输入内容，结束此step
                                self.getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());
                                self.overCurrentStep();
                            }
                            else {
                                // 若未开始编辑，撤销此step
                                self.getLayerContainer().removeLayerView(textView);
                                self.getLayerViews().remove(textView);
                                self.setHandlingLayerView(null);
                                self.cancelCurrentStep();
                            }
                            break;
                        case TextChange:
                            // 若是在二次编辑状态下，记录改变的内容，若内容为改变，撤销此step
                            textView.endEdit();
                            if (textView.isChangedSinceLastStep()) {
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

    /**
     * 撤销当前step
     * 检测当前step是否完成，若完成则拒绝撤销操作
     */
    private void cancelCurrentStep() {
        if (!self.getCurrentDrawingStep().isStepOver()) {

            self.getCurrentDrawingStep().setCanceled(true);
            self.getDrawingDelegate().didUpdateCurrentStep(self, self.getCurrentDrawingStep());

            self.getDrawingData().cancelDrawingStep();
        }
    }

    /**
     * 结束当前step
     * 记录数据
     * 通知代理状态变更
     */
    private void overCurrentStep() {
        switch (self.getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
            case Base:
                break;
            case Image:
                self.getHandlingLayerView().setCanHandle(true);
                break;
            case Text:
                self.getHandlingLayerView().setCanHandle(true);
                break;
        }

        self.getCurrentDrawingStep().setStepOver(true);
        self.getDrawingDelegate().didUpdateDrawingData(self, self.getDrawingData());
        self.getDrawingDelegate().didUpdateUndoRedoState(self, self.getDrawingData().canUndo(), self.getDrawingData().canRedo());
    }

    /**
     * 切换当前操作的图层
     * @param layerHierarchy 将变成当前操作图层的层级
     */
    private void handleLayer(int layerHierarchy) {
        self.setHandlingLayerView(null);

        for (VDDrawingLayerViewProtocol layerViewProtocol : self.getLayerViews()) {
            layerViewProtocol.setHandling((layerViewProtocol.getLayerHierarchy() == layerHierarchy) || (layerHierarchy == HandleAllLayer));
            if (layerViewProtocol.getLayerHierarchy() == layerHierarchy
                    && layerHierarchy != HandleAllLayer) {
                self.setHandlingLayerView(layerViewProtocol);
            }
        }
    }

    /**
     * 根据层级查找图层
     * @param layerHierarchy 层级
     * @return 图层
     */
    private VDDrawingLayerViewProtocol findLayerViewByLayerHierarchy(int layerHierarchy) {
        if (layerHierarchy == 0) {
            return self.getBaseLayerImageView();
        }

        for (VDDrawingLayerViewProtocol layerViewProtocol : self.getLayerViews()) {
            if (layerViewProtocol.getLayerHierarchy() == layerHierarchy) {
                return layerViewProtocol;
            }
        }

        return null;
    }

    private void nativeUpdateCurrentStep() {
        VDDrawingStep step = self.getCurrentDrawingStep();

        VDDrawingLayer.LayerType layerType = step.getDrawingLayer().getLayerType();
        VDDrawingStep.StepType stepType = step.getStepType();

        VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(step.getDrawingLayer().getHierarchy());

        switch (stepType) {
            case Clear:
                self.nativeClear();
                break;
            case DrawOnBase:
                self.getBaseLayerImageView().appendWithDrawingStep(step);
                break;
            case Background:
                if (step.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                    self.nativeSetBackgroundDrawable(step.getDrawingLayer().getHierarchy(), self.getDrawingDelegate().gainBackground(self, step.getDrawingLayer().getBackgroundImageIdentifier()));
                } else if (step.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    self.nativeSetBackgroundColor(step.getDrawingLayer().getHierarchy(), step.getDrawingLayer().getBackgroundColor());
                }
                break;
            case CreateLayer:
                if (layerViewProtocol == null) {
                    switch (layerType) {
                        case Image:
                            self.setHandlingLayerView(new VDDrawingLayerImageView(self.getContext()));
                            self.getLayerViews().add(self.getHandlingLayerView());
                            self.getLayerContainer().addLayerView(self.getHandlingLayerView());
                            break;
                        case Text:
                            self.setHandlingLayerView(new VDDrawingLayerTextView(self.getContext()));
                            self.getLayerViews().add(self.getHandlingLayerView());
                            self.getLayerContainer().addLayerView(self.getHandlingLayerView());
                            break;
                    }
                    layerViewProtocol = self.getHandlingLayerView();
                }

                layerViewProtocol.appendWithDrawingStep(step);

                if (step.isStepOver()) {
                    layerViewProtocol.setCanHandle(true);
                }
                break;
            case Transform:
                if (layerViewProtocol != null) {
                    layerViewProtocol.appendWithDrawingStep(step);
                }
                break;
            case TextChange:
                if (layerViewProtocol != null) {
                    layerViewProtocol.appendWithDrawingStep(step);
                }
                break;
            case DeleteLayer:
                if (layerViewProtocol != null) {
                    self.getLayerContainer().removeLayerView(layerViewProtocol);
                    self.getLayerViews().remove(layerViewProtocol);
                    self.setHandlingLayerView(null);
                }
                break;
        }

        if (layerViewProtocol != null) {
            self.handleLayer(step.getDrawingLayer().getHierarchy());
        }
    }

    private void nativeCancelCurrentStep() {
        VDDrawingStep step = self.getCurrentDrawingStep();

        VDDrawingLayer.LayerType layerType = step.getDrawingLayer().getLayerType();
        VDDrawingStep.StepType stepType = step.getStepType();

        VDDrawingLayerViewProtocol layerViewProtocol = self.findLayerViewByLayerHierarchy(step.getDrawingLayer().getHierarchy());

        switch (stepType) {
            case DrawOnBase:
                self.getBaseLayerImageView().appendWithDrawingStep(step);
                break;
            case CreateLayer:
                if (layerViewProtocol != null) {
                    self.getLayerContainer().removeLayerView(layerViewProtocol);
                    self.getLayerViews().remove(layerViewProtocol);
                }
                break;
            case TextChange:
                if (layerViewProtocol != null) {
                    layerViewProtocol.appendWithDrawingStep(step);
                    ((VDDrawingLayerTextView) layerViewProtocol).setText(((VDDrawingLayerTextView) layerViewProtocol).getUnchangedText());
                }
                break;
        }

        self.handleLayer(UnhandleAnyLayer);
        self.setHandlingLayerView(null);
    }

    /**
     * {@link #setBackgroundColor(int, int)}
     */
    private void nativeSetBackgroundColor(int layerHierarchy, int color) {
        if (layerHierarchy < 0) {
            return;
        }
        else if (layerHierarchy == 0) {
            self.getBaseLayerImageView().setBackgroundColor(color);
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

    /**
     * {@link #setBackgroundDrawable(int, Drawable, String)}
     */
    private void nativeSetBackgroundDrawable(int layerHierarchy, Drawable drawable) {
        if (layerHierarchy < 0) {
            return;
        }
        else if (layerHierarchy == 0) {
            self.getBaseLayerImageView().setBackground(drawable);
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

    /**
     * {@link #clear()}
     */
    private void nativeClear() {
        // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) self.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(self.getWindowToken(), 0);

        self.cancelCurrentStep();

        self.getLayerContainer().clear();

        if (self.getLayerViews().size() > 1) {
            self.getLayerViews().subList(1, self.getLayerViews().size()).clear();
        }

        self.setHandlingLayerView(null);

        self.getBaseLayerImageView().clearDrawing();
        self.getBaseLayerImageView().setBackground(null);
    }

    /**
     * 依照记录的数据{@link #drawingData}进行绘制
     * 由于base层可能进行了不可预料次数的绘制，故base层的重绘将会在异步线程进行
     */
    private void nativeDrawData() {
        List<VDDrawingStep> stepsToDraw = self.getDrawingData().getStepsToDraw();

        // 筛选出每层layer涉及的step
        List<List<VDDrawingStep>> eachLayerSteps = new ArrayList<>();
        for (int i = 0; i < stepsToDraw.size(); i++) {
            VDDrawingStep step = stepsToDraw.get(i);

            while (eachLayerSteps.size() <= step.getDrawingLayer().getHierarchy()) {
                eachLayerSteps.add(new ArrayList<VDDrawingStep>());
            }
            eachLayerSteps.get(step.getDrawingLayer().getHierarchy()).add(step);
        }

        for (int layerHierarchy = 0; layerHierarchy < eachLayerSteps.size(); layerHierarchy++) {
            List<VDDrawingStep> currentLayerSteps = eachLayerSteps.get(layerHierarchy);
            if (currentLayerSteps.size() == 0) {
                continue;
            }

            /**
             * 第一次遍历所有step
             * 设置当前绘制比例
             * 获取当前图层应当绘制的background在哪一步step
             */
            int lastBackgroundStepIndex = -1;
            for (int i = 0; i <currentLayerSteps.size(); i++) {
                VDDrawingStep step = currentLayerSteps.get(i);

                step.updateDrawingRatio(self.getWidth(), self.getHeight());

                if (step.getDrawingLayer().getBackgroundImageIdentifier() != null
                        || step.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                    lastBackgroundStepIndex = i;
                }
            }


            if (layerHierarchy == 0) {
                /**
                 * 绘制背景，背景使用系统的background
                 */
                if (lastBackgroundStepIndex >= 0) {
                    VDDrawingStep lastBackgroundDrawingStep = currentLayerSteps.get(lastBackgroundStepIndex);
                    if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                        self.getBaseLayerImageView().setBackground(self.getDrawingDelegate().gainBackground(self, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                    }
                    else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                        self.getBaseLayerImageView().setBackgroundColor(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                    }
                }

                // 绘制base底层，异步
                self.getBaseLayerImageView().refreshWithDrawnSteps(currentLayerSteps);
            }
            else if (layerHierarchy > 0) { // 图层生成
                // 生成各个图层，同步
                VDDrawingStep firstDrawingStep = currentLayerSteps.get(0);
                VDDrawingStep lastDrawingStep = currentLayerSteps.get(currentLayerSteps.size() - 1);
                if (lastDrawingStep.getStepType() == VDDrawingStep.StepType.DeleteLayer) {
                    // if the layer deleted, skip it.
                    continue;
                }
                switch (firstDrawingStep.getDrawingLayer().getLayerType()) {
                    case Image: {
                        VDDrawingLayerImageView imageView = new VDDrawingLayerImageView(self.getContext());
                        /**
                         * 绘制背景，背景使用系统的background
                         */
                        if (lastBackgroundStepIndex >= 0) {
                            VDDrawingStep lastBackgroundDrawingStep = currentLayerSteps.get(lastBackgroundStepIndex);
                            if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                                imageView.setBackground(self.getDrawingDelegate().gainBackground(self, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                            }
                            else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                                imageView.setBackgroundColor(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                            }
                        }
                        self.getLayerContainer().addLayerView(imageView);
                        self.getLayerViews().add(imageView);

                        imageView.refreshWithDrawnSteps(currentLayerSteps);
                        imageView.setCanHandle(true);
                        break;
                    }
                    case Text: {
                        VDDrawingLayerTextView textView = new VDDrawingLayerTextView(self.getContext());
                        /**
                         * 绘制背景，背景使用系统的background
                         */
                        if (lastBackgroundStepIndex >= 0) {
                            VDDrawingStep lastBackgroundDrawingStep = currentLayerSteps.get(lastBackgroundStepIndex);
                            if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                                textView.setBackground(self.getDrawingDelegate().gainBackground(self, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                            }
                            else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != VDDrawingLayer.UnsetValue) {
                                textView.setBackgroundColor(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                            }
                        }

                        self.getLayerContainer().addLayerView(textView);
                        self.getLayerViews().add(textView);

                        textView.refreshWithDrawnSteps(currentLayerSteps);
                        textView.setCanHandle(true);
                        textView.setDelegate(self);
                        break;
                    }
                }
            }
        }

        self.handleLayer(UnhandleAnyLayer);
    }

}
