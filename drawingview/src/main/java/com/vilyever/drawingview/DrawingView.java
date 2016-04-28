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

import com.vilyever.drawingview.brush.Brush;
import com.vilyever.drawingview.brush.drawing.DrawingBrush;
import com.vilyever.drawingview.brush.drawing.PenBrush;
import com.vilyever.drawingview.brush.text.TextBrush;
import com.vilyever.drawingview.layer.DrawingLayerViewProtocol;
import com.vilyever.drawingview.layer.DrawingLayerBaseView;
import com.vilyever.drawingview.layer.DrawingLayerContainer;
import com.vilyever.drawingview.layer.DrawingLayerImageView;
import com.vilyever.drawingview.layer.DrawingLayerTextView;
import com.vilyever.drawingview.model.DrawingData;
import com.vilyever.drawingview.model.DrawingLayer;
import com.vilyever.drawingview.model.DrawingPoint;
import com.vilyever.drawingview.model.DrawingStep;
import com.vilyever.drawingview.util.DrawingAnimationView;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingView
 * AndroidDrawingBoard <com.vilyever.drawingboard>
 * Created by vilyever on 2015/9/18.
 * Feature:
 * 绘画板视图
 */
public class DrawingView extends RelativeLayout implements View.OnLayoutChangeListener, DrawingLayerTextView.TextChangeDelegate {
    private final DrawingView self = this;

    private static final int UnhandleAnyLayer = -1;

    /* Constructors */
    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /* Public Methods */

    /**
     * 清空当前所有绘制内容
     */
    public void clear() {
        // 如果处于正在绘制状态则无视此次请求
        if (isTouching()) {
            return;
        }

        // 当前已经是清空状态，无需在此clear
        if (getCurrentDrawingStep().isClearStep()) {
            return;
        }

        endUnfinishedStep(); // 结束当前未完成的step
        internalClear(); // 清空绘制
        getDrawingData()
            .newDrawingStepOnBaseLayer(getWidth(), getHeight())
            .setStepType(DrawingStep.StepType.Clear); // 记录清空step
        overCurrentStep(); // 结束当前step
    }
    
    /**
     * 获取当前正在绘制（或刚刚绘制完成）的step
     *
     * @return 当前step，任意修改此step可能导致错误
     */
    public DrawingStep getCurrentDrawingStep() {
        return getDrawingData().getDrawingStep();
    }

    /**
     * 在当前绘制基础上增加绘制传入的step，如果传入的step与当前未完成的step是同一step（编号相同，远程同步step可能有未完成和已完成两种状态），更新当前step
     *
     * @param step 将要绘制的step
     */
    public void drawNextStep(@NonNull DrawingStep step) {
        step.setRemote(true);
        if (step.getStep() != getCurrentDrawingStep().getStep()) {
            endUnfinishedStep();
            getDrawingData().addDrawingStep(step);
        }
        else {
            getDrawingData().replaceDrawingStep(step);
        }

        if (step.isCanceled()) {
            internalCancelCurrentStep();
            getDrawingData().cancelDrawingStep();
        }
        else {
            internalUpdateCurrentStep(false);
        }
    }

    /**
     * 在当前绘制基础上增加绘制传入的step，此step必须是stepOver状态
     * 在远程同步绘制时，采用低频同步，仅在每一步绘制完成后同步时调用此方法
     * 不可与{@link #drawNextStep(DrawingStep)}同时使用
     *
     * @param step 将要绘制的step
     */
    public void drawNextOverStep(@NonNull DrawingStep step) {
        if (!step.isStepOver()) {
            return;
        }

        getDrawingData().addDrawingStep(step);

        if (step.isCanceled()) {
            return;
        }

        internalUpdateCurrentStep(true);
    }

    /**
     * 使用传入的data更新绘制
     *
     * @param data 即将绘制的data
     */
    public void refreshWithDrawingData(DrawingData data) {
        // 传入data与当前data相同则忽视此次调用
        if (data == this.drawingData) {
            return;
        }
        setDrawingData(data);
        internalClear();
        internalDrawData();

        getUndoRedoStateDelegate().onUndoRedoStateChange(this, getDrawingData().canUndo(), getDrawingData().canRedo());
    }

    /**
     * 当外界判断此时当前drawingView失去绘制焦点时调用
     * i.e. 当前drawingView生成了一个空的文本layer，另一个drawingView此时获取touch事件也生成了一个空的文本layer，此时，重新回到当前drawingView后的首次touch事件必然将先前的空文本图层销毁
     */
    public void loseDrawingFocus() {
        if (isTouching()) {
            setWillLoseFocusAfterTouching(true);
        }
        else {
            endUnfinishedStep();
            setWillLoseFocusAfterTouching(false);
        }
    }

    /**
     * 为图层设置背景色，这个设置将会记录在绘制数据中
     *
     * @param layerHierarchy 图层层次
     * @param color          背景色
     */
    public void setBackgroundColor(int layerHierarchy, int color) {
        // 判断图层是否存在
        DrawingLayerViewProtocol layerViewProtocol = findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerViewProtocol == null) {
            return;
        }

        endUnfinishedStep();
        internalSetBackgroundColor(layerHierarchy, color);

        /**
         * 记录此step，获取图层的type
         */
        DrawingLayer.LayerType layerType = DrawingLayer.LayerType.BaseDrawing;
        if (layerViewProtocol instanceof DrawingLayerImageView) {
            layerType = DrawingLayer.LayerType.LayerDrawing;
        }
        else if (layerViewProtocol instanceof DrawingLayerTextView) {
            layerType = DrawingLayer.LayerType.LayerText;
        }

        getDrawingData()
            .newDrawingStepOnLayer(layerHierarchy, layerType, getWidth(), getHeight())
            .setStepType(DrawingStep.StepType.Background)
            .getDrawingLayer().setBackgroundColor(color);
        overCurrentStep();
    }

    /**
     * 为图层设置背景drawable，这个设置将会记录在绘制数据中
     *
     * @param layerHierarchy 图层层次
     * @param drawable       背景drawable，可以是颜色图片等
     * @param identifier     背景drawable的唯一标识ID
     *                       因为绘制数据中不宜保存背景drawable，故存储唯一标识id，在重绘时通过{@link com.vilyever.drawingview.DrawingView.BackgroundDatasource#gainBackground(DrawingView, String)}回调向外部通过id获取对应的背景drawable
     */
    public void setBackgroundDrawable(int layerHierarchy, Drawable drawable, String identifier) {
        // 判断图层是否存在
        DrawingLayerViewProtocol layerViewProtocol = findLayerViewByLayerHierarchy(layerHierarchy);
        if (layerViewProtocol == null) {
            return;
        }

        endUnfinishedStep();
        internalSetBackgroundDrawable(layerHierarchy, drawable);

        /**
         * 记录此step，获取图层的type
         */
        DrawingLayer.LayerType layerType = DrawingLayer.LayerType.BaseDrawing;
        if (layerViewProtocol instanceof DrawingLayerImageView) {
            layerType = DrawingLayer.LayerType.LayerDrawing;
        }
        else if (layerViewProtocol instanceof DrawingLayerTextView) {
            layerType = DrawingLayer.LayerType.LayerText;
        }

        getDrawingData()
            .newDrawingStepOnLayer(layerHierarchy, layerType, getWidth(), getHeight())
            .setStepType(DrawingStep.StepType.Background)
            .getDrawingLayer().setBackgroundImageIdentifier(identifier);
        overCurrentStep();
    }

    /**
     * 当前正在操作的图层，有可能为null
     */
    private DrawingLayerViewProtocol handlingLayerView;

    private DrawingView setHandlingLayerView(DrawingLayerViewProtocol handlingLayerView) {
        this.handlingLayerView = handlingLayerView;
        return this;
    }

    public DrawingLayerViewProtocol getHandlingLayerView() {
        if (!getCurrentDrawingStep().isStepOver()) {
            if (getCurrentDrawingStep().getHandlingLayer() == null) {
                getCurrentDrawingStep().setHandlingLayer(findLayerViewByLayerHierarchy(getCurrentDrawingStep().getDrawingLayer().getHierarchy()));
            }
            return getCurrentDrawingStep().getHandlingLayer();
        }

        return this.handlingLayerView;
    }

    /**
     * 删除当前操作的图层
     *
     * @return 是否成功删除
     */
    public boolean deleteHandlingLayer() {
        // 如果处于正在绘制状态则无视此次请求
        if (isTouching()) {
            return false;
        }

        // 如果当前操作的图层存在，删除数据，移除图层，记录step
        if (getHandlingLayerView() != null
            && getHandlingLayerView().getLayerHierarchy() > 0) {
            endUnfinishedStep();

            // 此时若因endUnfinishedStep消除了当前layer，则相当于成功删除了当前layer，但此步不会出现在undo，redo中
            if (getHandlingLayerView() == null) {
                return true;
            }

            getDrawingData()
                .newDrawingStepOnLayer(getHandlingLayerView().getLayerHierarchy(), DrawingLayer.LayerType.Unknown, getWidth(), getHeight())
                .setStepType(DrawingStep.StepType.DeleteLayer);

            getLayerContainer().removeLayerView(getHandlingLayerView());
            getLayerViews().remove(getHandlingLayerView());
            handleLayer(UnhandleAnyLayer);
            getCurrentDrawingStep().setHandlingLayer(null);

            overCurrentStep();

            return true;
        }

        return false;
    }

    /**
     * 撤销一步
     *
     * @return 是否撤销成功
     */
    public boolean undo() {
        // 若允许撤销，撤销当前step，通知委托方当前撤销/重做状态
        if (canUndo()) {
            getDrawingData().undo();
            internalClear();
            internalDrawData();

            getUndoRedoStateDelegate().onUndoRedoStateChange(this, getDrawingData().canUndo(), getDrawingData().canRedo());

            return true;
        }
        return false;
    }

    /**
     * 重做一步
     *
     * @return 是否重做成功
     */
    public boolean redo() {
        // 若允许重做，重做下一step，通知委托方当前撤销/重做状态
        if (canRedo()) {
            getDrawingData().redo();
            internalClear();
            internalDrawData();

            getUndoRedoStateDelegate().onUndoRedoStateChange(this, getDrawingData().canUndo(), getDrawingData().canRedo());

            return true;
        }
        return false;
    }

    /**
     * 判断当前是否可以撤销
     *
     * @return 是否允许撤销
     */
    public boolean canUndo() {
        return !isTouching() && getDrawingData().canUndo();
    }

    /**
     * 判断当前是否可以重做
     *
     * @return 是否允许重做
     */
    public boolean canRedo() {
        return !isTouching() && getDrawingData().canRedo();
    }

    /* Properties */

    /**
     * 绘制代理，通知状态变更和获取数据
     */
    public interface DrawingStepDelegate {
        /**
         * 当前绘制step创建时回调，通常用于远程同步
         * step处于变化状态
         *
         * @param drawingView 当前view
         * @param step        当前绘制step，任意修改此step可能导致错误
         */
        void onDrawingStepBegin(DrawingView drawingView, DrawingStep step);

        /**
         * 当前绘制step变更时回调，每次touch绘制都会执行，text图层修改内容也会执行，此回调执行频繁，通常用于远程同步
         * step处于变化状态
         *
         * @param drawingView 当前view
         * @param step        当前绘制step，任意修改此step可能导致错误
         */
        void onDrawingStepChange(DrawingView drawingView, DrawingStep step);

        /**
         * 当前绘制状态已改变，绘制一笔或进行撤销/重做/清空等变更记录数据的操作都会触发此回调
         * step已经完成
         *
         * @param drawingView 当前view
         * @param step        当前绘制step，任意修改此step可能导致错误
         */
        void onDrawingStepEnd(DrawingView drawingView, DrawingStep step);

        /**
         * 当前step撤销
         *
         * @param drawingView 当前view
         * @param step        当前绘制step，任意修改此step可能导致错误
         */
        void onDrawingStepCancel(DrawingView drawingView, DrawingStep step);
    }

    private DrawingStepDelegate drawingStepDelegate;

    public DrawingView setDrawingStepDelegate(DrawingStepDelegate drawingStepDelegate) {
        this.drawingStepDelegate = drawingStepDelegate;
        return this;
    }

    public DrawingStepDelegate getDrawingStepDelegate() {
        if (this.drawingStepDelegate == null) {
            this.drawingStepDelegate = new DrawingStepDelegate() {
                @Override
                public void onDrawingStepBegin(DrawingView drawingView, DrawingStep step) {

                }

                @Override
                public void onDrawingStepChange(DrawingView drawingView, DrawingStep step) {
                }

                @Override
                public void onDrawingStepEnd(DrawingView drawingView, DrawingStep step) {

                }

                @Override
                public void onDrawingStepCancel(DrawingView drawingView, DrawingStep step) {

                }
            };
        }
        return this.drawingStepDelegate;
    }
    
    public interface UndoRedoStateDelegate {
        /**
         * 撤销重做状态变更
         *
         * @param drawingView 当前view
         * @param canUndo     当前是否可以撤销，因为此回调可能在touching时调用，导致外部获取{@link #canUndo()}状态错误
         * @param canRedo     当前是否可以重做，因为此回调可能在touching时调用，导致外部获取{@link #canRedo()}状态错误
         */
        void onUndoRedoStateChange(DrawingView drawingView, boolean canUndo, boolean canRedo);
    }
    private UndoRedoStateDelegate undoRedoStateDelegate;
    public DrawingView setUndoRedoStateDelegate(UndoRedoStateDelegate undoRedoStateDelegate) {
        this.undoRedoStateDelegate = undoRedoStateDelegate;
        return this;
    }
    public UndoRedoStateDelegate getUndoRedoStateDelegate() {
        if (this.undoRedoStateDelegate == null) {
            this.undoRedoStateDelegate = new UndoRedoStateDelegate() {

                @Override
                public void onUndoRedoStateChange(DrawingView drawingView, boolean canUndo, boolean canRedo) {

                }
            };
        }
        return this.undoRedoStateDelegate;
    }
    
    
    public interface InterceptTouchDelegate {
        /**
         * 回报当前是否拦截了touch事件进行绘画（这个回调标志着绘画开始或结束），当此drawingView是recyclerView（或类似可滑动的view）的子view时可用于通知父view禁止滑动
         *
         * @param drawingView 当前view
         * @param isIntercept 是否拦截
         */
        void requireInterceptTouchEvent(DrawingView drawingView, boolean isIntercept);
    }
    private InterceptTouchDelegate interceptTouchDelegate;
    public DrawingView setInterceptTouchDelegate(InterceptTouchDelegate interceptTouchDelegate) {
        this.interceptTouchDelegate = interceptTouchDelegate;
        return this;
    }
    public InterceptTouchDelegate getInterceptTouchDelegate() {
        if (this.interceptTouchDelegate == null) {
            this.interceptTouchDelegate = new InterceptTouchDelegate() {
                @Override
                public void requireInterceptTouchEvent(DrawingView drawingView, boolean isIntercept) {
        
                }
            };
        }
        return this.interceptTouchDelegate;
    }

    public interface BackgroundDatasource {
        /**
         * 请求对应唯一标识ID的drawbale。{@link #setBackgroundDrawable(int, Drawable, String)}
         *
         * @param drawingView 当前view
         * @param identifier  drawable对应的标识id
         * @return 对应的drawable
         */
        Drawable gainBackground(DrawingView drawingView, String identifier);
    }
    private BackgroundDatasource backgroundDatasource;
    public DrawingView setBackgroundDatasource(BackgroundDatasource backgroundDatasource) {
        this.backgroundDatasource = backgroundDatasource;
        return this;
    }
    public BackgroundDatasource getBackgroundDatasource() {
        if (this.backgroundDatasource == null) {
            this.backgroundDatasource = new BackgroundDatasource() {
                @Override
                public Drawable gainBackground(DrawingView drawingView, String identifier) {
                    return null;
                }
            };
        }
        return this.backgroundDatasource;
    }

    /**
     * 当前view是否可以通过手指绘制
     * 若当前view是同步显示view，则可设为true禁止触摸绘制
     */
    private boolean disableTouchDraw;

    public DrawingView setDisableTouchDraw(boolean disableTouchDraw) {
        this.disableTouchDraw = disableTouchDraw;
        return this;
    }

    public boolean isDisableTouchDraw() {
        return this.disableTouchDraw;
    }

    /**
     * 当前记录的所有的绘制数据
     */
    private DrawingData drawingData;

    private DrawingView setDrawingData(DrawingData drawingData) {
        this.drawingData = drawingData;
        return this;
    }

    public DrawingData getDrawingData() {
        if (this.drawingData == null) {
            this.drawingData = new DrawingData();
            // init first drawTouchMoving step that clear the view
            this.drawingData
                    .newDrawingStepOnBaseLayer(getWidth(), getHeight())
                    .setStepType(DrawingStep.StepType.Clear)
                    .setStepOver(true);
        }
        return this.drawingData;
    }

    /**
     * 当前是否正在处理触摸事件
     */
    private boolean touching;

    private DrawingView setTouching(boolean touching) {
        this.touching = touching;
        if (!touching && willLoseFocusAfterTouching()) {
            loseDrawingFocus();
        }
        return this;
    }

    public boolean isTouching() {
        return this.touching;
    }

    private boolean willLoseFocusAfterTouching;

    private DrawingView setWillLoseFocusAfterTouching(boolean willLoseFocusAfterTouching) {
        this.willLoseFocusAfterTouching = willLoseFocusAfterTouching;
        return this;
    }

    private boolean willLoseFocusAfterTouching() {
        return this.willLoseFocusAfterTouching;
    }

    /**
     * 当前用于绘制的brush
     */
    private Brush brush;

    public <T extends Brush> T getBrush() {
        if (this.brush == null) {
            // 默认使用penBrush
            this.brush = PenBrush.defaultBrush();
        }
        return (T) this.brush;
    }

    public void setBrush(Brush brush) {
        this.brush = brush;

        if (isTouching()) {
            setBrushChanged(true);
        }
        else {
            endUnfinishedStep();
        }
    }

    /**
     * 标志当前brush已经变更，用于在下次开始处理触摸事件时更换brush
     * 推迟更换brush是由于如果正在绘制时调用了{@link #setBrush(Brush)}会导致各种状况
     */
    private boolean brushChanged;

    public DrawingView setBrushChanged(boolean brushChanged) {
        this.brushChanged = brushChanged;
        return this;
    }

    public boolean isBrushChanged() {
        return this.brushChanged;
    }

    /**
     * 绘制的底层图层，此图层不可进行平移/缩放/旋转
     */
    private DrawingLayerBaseView baseLayerImageView;

    public DrawingLayerBaseView getBaseLayerImageView() {
        if (this.baseLayerImageView == null) {
            this.baseLayerImageView = new DrawingLayerBaseView(getContext());
            this.baseLayerImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            this.baseLayerImageView.setBusyStateDelegate(new DrawingLayerBaseView.BusyStateDelegate() {
                @Override
                public void onDrawingBusyStateChange(DrawingLayerBaseView baseView, boolean busying) {
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
        return this.baseLayerImageView;
    }

    /**
     * 放置除底层以外所有图层的容器
     * 处理图层相关触摸操作，z轴上在底层上一层
     */
    private DrawingLayerContainer layerContainer;

    public DrawingLayerContainer getLayerContainer() {
        if (this.layerContainer == null) {
            this.layerContainer = new DrawingLayerContainer(getContext());
            this.layerContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            this.layerContainer.setLayerDelegate(new DrawingLayerContainer.LayerDelegate() {
                @Override
                public void onLayerTextViewEditBegin(DrawingLayerContainer container, DrawingLayerTextView textView) {

                    // 取消先前生成的变换step
                    self.cancelCurrentStep();
                    self.handleLayer(textView.getLayerHierarchy());

                    /**
                     * text图层在双击后进入编辑状态，此时可以输入
                     * 记录此step开始
                     */
                    self.getDrawingData()
                            .newDrawingStepOnLayer(textView.getLayerHierarchy(), DrawingLayer.LayerType.LayerText, getWidth(), getHeight())
                            .setStepType(DrawingStep.StepType.TextChange)
                            .setHandlingLayer(textView);
                    self.getDrawingStepDelegate().onDrawingStepBegin(self, self.getCurrentDrawingStep());
                }

                @Override
                public void onLayerViewTouchBegin(DrawingLayerContainer container, DrawingLayerViewProtocol layerView) {
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
                    DrawingLayer.LayerType layerType = DrawingLayer.LayerType.LayerDrawing;
                    if (layerView instanceof DrawingLayerTextView) {
                        layerType = DrawingLayer.LayerType.LayerText;
                    }

                    self.getDrawingData()
                            .newDrawingStepOnLayer(layerView.getLayerHierarchy(), layerType, getWidth(), getHeight())
                            .setStepType(DrawingStep.StepType.Transform)
                            .setHandlingLayer(layerView);
                    self.getDrawingStepDelegate().onDrawingStepBegin(self, getCurrentDrawingStep());
                }

                @Override
                public void onLayerViewTransforming(DrawingLayerContainer container, DrawingLayerViewProtocol layerView) {
                    View v = (View) layerView;

                    RelativeLayout.LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
                    self.getCurrentDrawingStep().getDrawingLayer().setLeft(layoutParams.leftMargin);
                    self.getCurrentDrawingStep().getDrawingLayer().setTop(layoutParams.topMargin);
                    self.getCurrentDrawingStep().getDrawingLayer().setRight(layoutParams.leftMargin + layoutParams.width);
                    self.getCurrentDrawingStep().getDrawingLayer().setBottom(layoutParams.topMargin + layoutParams.height);
                    self.getCurrentDrawingStep().getDrawingLayer().setScale(v.getScaleX());
                    self.getCurrentDrawingStep().getDrawingLayer().setRotation(v.getRotation());

                    self.getDrawingStepDelegate().onDrawingStepChange(self, self.getCurrentDrawingStep());
                }

                @Override
                public void onLayerViewTransformEnd(DrawingLayerContainer container, DrawingLayerViewProtocol layerView) {
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

                @Override
                public void requireHandlingLayerView(DrawingLayerContainer container, DrawingLayerViewProtocol layerView) {
                    self.cancelCurrentStep();

                    self.handleLayer(layerView.getLayerHierarchy());
                }
            });
        }
        return this.layerContainer;
    }

    /**
     * 存放所有图层
     */
    private List<DrawingLayerViewProtocol> layerViews;

    public List<DrawingLayerViewProtocol> getLayerViews() {
        if (this.layerViews == null) {
            this.layerViews = new ArrayList<>();
        }
        return this.layerViews;
    }

    /**
     * 耗时绘制动画层，此层位于最上方
     * 在底层进行大量数据耗时绘制时显示
     */
    private DrawingAnimationView animationView;

    public DrawingAnimationView getAnimationView() {
        if (this.animationView == null) {
            this.animationView = new DrawingAnimationView(getContext());
            this.animationView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return this.animationView;
    }

    /**
     * 是否应该处理触摸事件，用于禁用多指触摸绘制，多指触摸可能导致多种意外
     */
    private boolean shouldHandleOnTouch; // for limit only first finger can draw.

    private DrawingView setShouldHandleOnTouch(boolean shouldHandleOnTouch) {
        this.shouldHandleOnTouch = shouldHandleOnTouch;
        return this;
    }

    private boolean shouldHandleOnTouch() {
        return this.shouldHandleOnTouch;
    }

    /* Overrides */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isDisableTouchDraw()) {
            return false;
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 禁止父容器拦截touch事件，解决在recyclerView中无法绘画问题
                getParent().requestDisallowInterceptTouchEvent(true);
                getInterceptTouchDelegate().requireInterceptTouchEvent(this, true);
                setTouching(true);

                /**
                 * 若此时brush需要更换，则结束当前未完成step
                 */
                if (isBrushChanged()) {
                    setBrushChanged(false);
                    endUnfinishedStep();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getInterceptTouchDelegate().requireInterceptTouchEvent(this, false);
                getParent().requestDisallowInterceptTouchEvent(false);
                setTouching(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDisableTouchDraw()) {
            return false;
        }

        /**
         * 若底层正在进行耗时绘制，禁止此时进行触摸绘制
         */
        if (getBaseLayerImageView().isBusying()) {
            return true;
        }

        int action = event.getAction();
        int maskAction = event.getAction() & MotionEvent.ACTION_MASK;

        // only handle the first touch finger
        if (action == MotionEvent.ACTION_DOWN) {
            // 仅在第一只手指第一次接触屏幕时，允许此手指开始绘制
            setShouldHandleOnTouch(true);

            /**
             * 如果当前正在绘制的step是未完成的text图层（此时屏幕上应显示一个高亮的文本框，其中可能有内容，肯能没内容）
             * 结束当前text图层的绘制step
             * 即text图层无法主动判断step是否完成，用户可能无限时等待键盘输入，此时点击text图层意外的任意一点，即可表示完成此次text图层的step
             * 若进行此操作，显然不应继续使用此次触摸开始进行下一step的绘制
             *
             * 如果Action_UP事件被系统丢弃，自行补完step，防止崩溃
             */
            if (!getCurrentDrawingStep().isStepOver()) {
                endUnfinishedStep();
                setShouldHandleOnTouch(false);
            }
        }

        /**
         * 此时不允许此次触摸继续绘制
         */
        if (!shouldHandleOnTouch()) {
            return true;
        }

        /**
         * 第一只触摸的手指离开屏幕时，终止此轮触摸事件流继续绘制的许可
         */
        if (maskAction == MotionEvent.ACTION_POINTER_UP
            && event.getActionIndex() == 0) {
            action = MotionEvent.ACTION_UP;
            setShouldHandleOnTouch(false);
        }

        /**
         * 依照第一只手指的触摸状态绘制
         */
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                drawBeginTouch(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                drawTouchMoving(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drawTouchEnd(event.getX(), event.getY());
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
            internalClear();
            internalDrawData();
        }
    }
    /** {@link android.view.View.OnLayoutChangeListener} */

    /** {@link DrawingLayerTextView.TextChangeDelegate} */
    @Override
    public void onTextChange(DrawingLayerTextView textView, String text) {
        getCurrentDrawingStep().getDrawingLayer().setText(text);
        getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());
    }
    /** {@link DrawingLayerTextView.TextChangeDelegate} */

    /* Private Methods */

    /**
     * 初始化内部控件
     */
    private void init() {
        addOnLayoutChangeListener(this);

        // focusable, which can clear focus from edittext
        setFocusable(true);
        setFocusableInTouchMode(true);

        // setup base layer view
        getLayerViews().add(getBaseLayerImageView());
        addView(getBaseLayerImageView());
        addView(getLayerContainer());
        addView(getAnimationView());
        getAnimationView().setVisibility(View.GONE);
    }

    /**
     * 开始绘制
     * 若此时开始新的step绘制，生成一个新的未完成的step
     * 若此时继续绘制当前未完成的step，此次调用将类似于{@link #drawTouchMoving(float, float)}
     *
     * @param x 触摸x坐标
     * @param y 触摸y坐标
     */
    private void drawBeginTouch(float x, float y) {
        // 绘制point的标识符id递增
        DrawingPoint.IncrementPointerID();

        // 当前step已完成，开始绘制新的step
        if (getCurrentDrawingStep().isStepOver()) {
            handleLayer(UnhandleAnyLayer);

            // 根据brush类型进行绘制
            if (getBrush() instanceof DrawingBrush) {
                DrawingBrush drawingBrush = getBrush();

                // 根据绘图brush是否需要绘制新图层进行绘制，记录当前操作的图层
                if (drawingBrush.isOneStrokeToLayer()) {
                    /**
                     * 若绘制新图层，生成step，并生成对应的图层view
                     */
                    getDrawingData().newDrawingStepOnNextLayer(DrawingLayer.LayerType.LayerDrawing, getWidth(), getHeight())
                        .setStepType(DrawingStep.StepType.CreateLayer)
                        .setBrush(Brush.copy(drawingBrush))
                        .setHandlingLayer(new DrawingLayerImageView(getContext(), getCurrentDrawingStep().getDrawingLayer().getHierarchy()));
                    getLayerViews().add(getHandlingLayerView());
                    getLayerContainer().addLayerView(getHandlingLayerView());
                }
                else {
                    /**
                     * 若绘制在base底层，仅生成step
                     */
                    getDrawingData()
                        .newDrawingStepOnBaseLayer(getWidth(), getHeight())
                        .setStepType(DrawingStep.StepType.DrawOnBase)
                        .setBrush(Brush.copy(drawingBrush))
                        .setHandlingLayer(getBaseLayerImageView());
                }
            }
            else if (getBrush() instanceof TextBrush) {
                TextBrush textBrush = getBrush();

                if (textBrush.isOneStrokeToLayer()) {
                    /**
                     * text图层类似于 新绘画图层，生成step，生成图层view，记录操作图层
                     */
                    getDrawingData().newDrawingStepOnNextLayer(DrawingLayer.LayerType.LayerText, getWidth(), getHeight())
                        .setStepType(DrawingStep.StepType.CreateLayer)
                        .setBrush(Brush.copy(textBrush))
                        .setHandlingLayer(new DrawingLayerTextView(getContext(), getCurrentDrawingStep().getDrawingLayer().getHierarchy()));

                    getLayerViews().add(getHandlingLayerView());
                    getLayerContainer().addLayerView(getHandlingLayerView());
                }
                else {
                    /**
                     * 若绘制在base底层，生成step，同绘制图层相同生成临时输入text图层，step结束后消除图层
                     */
                    getDrawingData()
                        .newTextStepOnBaseLayer(getWidth(), getHeight())
                        .setStepType(DrawingStep.StepType.DrawTextOnBase)
                        .setBrush(Brush.copy(textBrush))
                        .setHandlingLayer(new DrawingLayerTextView(getContext(), getCurrentDrawingStep().getDrawingLayer().getHierarchy()));

//                    getHandlingLayerView().setCanHandle(false); // 不显示图层效果
                    getLayerViews().add(getHandlingLayerView());
                    getLayerContainer().addLayerView(getHandlingLayerView());
                }
            }

            getCurrentDrawingStep().getDrawingPath().addPoint(new DrawingPoint(x, y));
            getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.VeryBegin, Brush.DrawingPointerState.TouchDown));

            if (getHandlingLayerView() != null) {
                getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());
                getHandlingLayerView().setHandling(true);

                getDrawingStepDelegate().onDrawingStepBegin(this, getCurrentDrawingStep());
            }
        }
        else {
            /**
             * 若此时正在绘制的step未完成，则继续绘制此step
             */
            getCurrentDrawingStep().getDrawingPath().addPoint(new DrawingPoint(x, y));
            getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.TouchDown));

            if (getHandlingLayerView() != null) {
                getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());
                getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());
            }
        }

        if (getHandlingLayerView() == null) {
            cancelCurrentStep();
            setShouldHandleOnTouch(false);
        }
    }

    /**
     * 绘制进行中，完善绘制信息
     *
     * @param x 触摸x坐标
     * @param y 触摸y坐标
     */
    private void drawTouchMoving(float x, float y) {
        if (getHandlingLayerView() == null) {
            return;
        }
        if (getCurrentDrawingStep().getDrawingPath().addPoint(new DrawingPoint(x, y))) {
            getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.TouchMoving));
            getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());
            getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());
        }
    }

    /**
     * 当前触摸事件流结束
     * 调用此方法可能完成当前step，也可能未完成
     *
     * @param x 触摸x坐标
     * @param y 触摸y坐标
     */
    private void drawTouchEnd(float x, float y) {
        if (getHandlingLayerView() == null) {
            return;
        }
        getCurrentDrawingStep().getDrawingPath().addPoint(new DrawingPoint(x, y));

        switch (getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
            case BaseDrawing:
            case LayerDrawing:
                /**
                 * base底层和image图层判断当前step是否完成，若完成则进行 finish 步骤
                 */
                getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.TouchUp));
                boolean stepOver = !getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep()).requireMoreDetail;

                getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());

                if (stepOver) {
                    finishDraw();
                }
                break;
            case BaseText:
            case LayerText:
                /**
                 * 通过touch事件流不可能直接完成text图层的绘制
                 * 此时仅是显示一个高亮文本框提供用户进行输入
                 */
                getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.TouchUp));
                getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());
                DrawingLayerTextView textView = (DrawingLayerTextView) getHandlingLayerView();
                textView.setTextChangeDelegate(this);
                textView.beginEdit(true);

                getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());
                break;
        }
    }

    /**
     * 结束当前step
     * 无论当前step是否标记stepOver，都会强制结束
     * step是否应当调用此方法结束由调用处判断
     */
    private void finishDraw() {
        if (getHandlingLayerView() == null) {
            return;
        }
        getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.VeryEnd));
        getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());

        /**
         * 若当前step的brush通过对所有绘制触摸点的计算返回此时无需绘制任何事物时，取消当前step
         */
        if (getCurrentDrawingStep().getDrawingLayer().getFrame() == null) {
            // noting to draw, e.g. draw line with one point is pointless
            switch (getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
                case BaseDrawing:
                case BaseText:
                    break;
                case LayerDrawing:
                case LayerText:
                    /**
                     * 取消当前step前移除当前图层view
                     */
                    getLayerContainer().removeLayerView(getHandlingLayerView());
                    getLayerViews().remove(getHandlingLayerView());
                    break;
            }

            cancelCurrentStep();
            return;
        }

        overCurrentStep();
    }

    /**
     * 强制结束当前step
     * 完成当前未完成的step
     */
    private void endUnfinishedStep() {
        // 仅处理未完成的step
        if (getCurrentDrawingStep() != null
            && !getCurrentDrawingStep().isStepOver()
            && getHandlingLayerView() != null) {

            switch (getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
                case BaseDrawing: {
                    // 对base底层，向step发送当前需要强制结束的指示后结束step
                    getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.ForceFinish));
                    getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());

                    getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());

                    finishDraw();
                    break;
                }
                case LayerDrawing: {
                    // 对image图层，因暂时不提供二次编辑功能，故仅在创建图层时存在未完成状态，处理类似base底层
                    DrawingLayerImageView imageView = (DrawingLayerImageView) getHandlingLayerView();
                    switch (getCurrentDrawingStep().getStepType()) {
                        case CreateLayer:
                            getCurrentDrawingStep().setDrawingState(new Brush.DrawingState(Brush.DrawingPointerState.ForceFinish));
                            getHandlingLayerView().appendWithDrawingStep(getCurrentDrawingStep());

                            getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());

                            finishDraw();
                            break;
                        case Transform:
                            cancelCurrentStep();
                            break;
                    }
                    break;
                }
                case BaseText: {
                    if (getHandlingLayerView() instanceof DrawingLayerTextView) {
                        DrawingLayerTextView textView = (DrawingLayerTextView) getHandlingLayerView();
                        textView.endEdit();
                        if (textView.isChangedSinceLastStep()) {
                            // 记录文本，删除临时图层，在base图层绘制
                            getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());

                            // 拓印textView到baseLayer上，之后调用finishDraw虽然会传入baseLayer当前的step，但其state是VeryEnd，并不作画
                            textView.setHandling(false);
                            getBaseLayerImageView().drawView(textView);

                            getLayerContainer().removeLayerView(textView);
                            getLayerViews().remove(textView);

                            getCurrentDrawingStep().setHandlingLayer(getBaseLayerImageView());
                            handleLayer(getCurrentDrawingStep().getDrawingLayer().getHierarchy());

                            getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());

                            finishDraw();
                        }
                        else {
                            // 若未开始编辑，撤销此step
                            getLayerContainer().removeLayerView(textView);
                            getLayerViews().remove(textView);
                            cancelCurrentStep();
                        }
                    }
                    else {
                        getBaseLayerImageView().drawTextStep(getCurrentDrawingStep());

                        getDrawingStepDelegate().onDrawingStepChange(this, getCurrentDrawingStep());

                        finishDraw();
                    }
                    break;
                }
                case LayerText: {
                    DrawingLayerTextView textView = (DrawingLayerTextView) getHandlingLayerView();
                    switch (getCurrentDrawingStep().getStepType()) {
                        case CreateLayer:
                            textView.endEdit();
                            if (textView.isChangedSinceLastStep()) {
                                // 对text图层，若此时是第一次创建完图层，若已输入内容，结束此step
                                getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());
                                overCurrentStep();
                            }
                            else {
                                // 若未开始编辑，撤销此step
                                getLayerContainer().removeLayerView(textView);
                                getLayerViews().remove(textView);
                                cancelCurrentStep();
                            }
                            break;
                        case TextChange:
                            // 若是在二次编辑状态下，记录改变的内容，若内容为改变，撤销此step
                            textView.endEdit();
                            if (textView.isChangedSinceLastStep()) {
                                getCurrentDrawingStep().getDrawingLayer().setText(textView.getText().toString());
                                overCurrentStep();
                            }
                            else {
                                cancelCurrentStep();
                            }
                            break;
                        case Transform:
                            cancelCurrentStep();
                            break;
                    }
                    break;
                }
            }
        }
    }

    /**
     * 撤销当前step
     * 检测当前step是否完成，若完成则拒绝撤销操作
     */
    private void cancelCurrentStep() {
        if (!getCurrentDrawingStep().isStepOver()) {

            getCurrentDrawingStep().setCanceled(true);
            getDrawingStepDelegate().onDrawingStepCancel(this, getCurrentDrawingStep());

            getDrawingData().cancelDrawingStep();
            handleLayer(UnhandleAnyLayer);
        }
    }

    /**
     * 结束当前step
     * 记录数据
     * 通知代理状态变更
     */
    private void overCurrentStep() {
        setHandlingLayerView(getHandlingLayerView());
        switch (getCurrentDrawingStep().getDrawingLayer().getLayerType()) {
            case BaseDrawing:
            case BaseText:
                break;
            case LayerDrawing:
                getHandlingLayerView().setCanHandle(true);
                break;
            case LayerText:
                getHandlingLayerView().setCanHandle(true);
                break;
        }

        getCurrentDrawingStep().setStepOver(true);
        getDrawingStepDelegate().onDrawingStepEnd(this, getCurrentDrawingStep());
        getUndoRedoStateDelegate().onUndoRedoStateChange(this, getDrawingData().canUndo(), getDrawingData().canRedo());
    }

    /**
     * 切换当前操作的图层
     *
     * @param layerHierarchy 将变成当前操作图层的层级
     */
    private void handleLayer(int layerHierarchy) {
        setHandlingLayerView(null);

        for (DrawingLayerViewProtocol layerViewProtocol : getLayerViews()) {
            layerViewProtocol.setHandling(layerViewProtocol.getLayerHierarchy() == layerHierarchy);
            if (layerViewProtocol.getLayerHierarchy() == layerHierarchy) {
                setHandlingLayerView(layerViewProtocol);
            }
        }
    }

    /**
     * 根据层级查找图层
     *
     * @param layerHierarchy 层级
     * @return 图层
     */
    private DrawingLayerViewProtocol findLayerViewByLayerHierarchy(int layerHierarchy) {
        if (layerHierarchy == 0) {
            return getBaseLayerImageView();
        }

        for (DrawingLayerViewProtocol layerViewProtocol : getLayerViews()) {
            if (layerViewProtocol.getLayerHierarchy() == layerHierarchy) {
                return layerViewProtocol;
            }
        }

        return null;
    }

    /**
     * {@link #drawNextStep(DrawingStep)}
     */
    private void internalUpdateCurrentStep(boolean isSkipToStepOver) {
        DrawingStep step = getCurrentDrawingStep();
        step.updateDrawingRatio(getWidth(), getHeight());
        ArrayList<DrawingStep> stepList = new ArrayList<>();
        stepList.add(step);

        DrawingLayer.LayerType layerType = step.getDrawingLayer().getLayerType();
        DrawingStep.StepType stepType = step.getStepType();

        DrawingLayerViewProtocol layerViewProtocol = findLayerViewByLayerHierarchy(step.getDrawingLayer().getHierarchy());

        switch (stepType) {
            case Clear:
                internalClear();
                break;
            case DrawOnBase:
            case DrawTextOnBase:
//                if (isSkipToStepOver) {
//                    getBaseLayerImageView().appendWithSteps(stepList);
//                }
//                else {
//                    getBaseLayerImageView().appendWithDrawingStep(step);
//                }
                if (step.isStepOver()) {
                    setHandlingLayerView(getBaseLayerImageView());
                    step.setStepOver(false);
                    endUnfinishedStep();
                }
                else {
                    getBaseLayerImageView().appendWithDrawingStep(step);
                }
                break;
            case Background:
                if (step.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                    internalSetBackgroundDrawable(step.getDrawingLayer().getHierarchy(), getBackgroundDatasource().gainBackground(this, step.getDrawingLayer().getBackgroundImageIdentifier()));
                }
                else if (step.getDrawingLayer().getBackgroundColor() != DrawingLayer.UnsetValue) {
                    internalSetBackgroundColor(step.getDrawingLayer().getHierarchy(), step.getDrawingLayer().getBackgroundColor());
                }
                break;
            case CreateLayer:
                if (layerViewProtocol == null) {
                    switch (layerType) {
                        case LayerDrawing:
                            layerViewProtocol = new DrawingLayerImageView(getContext(), step.getDrawingLayer().getHierarchy());
                            getLayerViews().add(layerViewProtocol);
                            getLayerContainer().addLayerView(layerViewProtocol);
                            break;
                        case LayerText:
                            layerViewProtocol = new DrawingLayerTextView(getContext(), step.getDrawingLayer().getHierarchy());
                            getLayerViews().add(layerViewProtocol);
                            getLayerContainer().addLayerView(layerViewProtocol);
                            break;
                    }
                }

                if (layerViewProtocol == null) {
                    return;
                }

//                if (isSkipToStepOver) {
//                    layerViewProtocol.appendWithSteps(stepList);
//                }
//                else {
//                    layerViewProtocol.appendWithDrawingStep(step);
//                }

                if (step.isStepOver()) {
                    setHandlingLayerView(layerViewProtocol);
                    step.setStepOver(false);
                    endUnfinishedStep();
                }
                else {
                    getBaseLayerImageView().appendWithDrawingStep(step);
                }

                if (step.isStepOver()) {
                    layerViewProtocol.setCanHandle(true);
                }
                break;
            case Transform:
                if (layerViewProtocol != null) {
//                    if (isSkipToStepOver) {
//                        layerViewProtocol.appendWithSteps(stepList);
//                    }
//                    else {
//                        layerViewProtocol.appendWithDrawingStep(step);
//                    }
                    if (step.isStepOver()) {
                        setHandlingLayerView(layerViewProtocol);
                        step.setStepOver(false);
                        endUnfinishedStep();
                    }
                    else {
                        getBaseLayerImageView().appendWithDrawingStep(step);
                    }
                }
                break;
            case TextChange:
                if (layerViewProtocol != null) {
//                    if (isSkipToStepOver) {
//                        layerViewProtocol.appendWithSteps(stepList);
//                    }
//                    else {
//                        layerViewProtocol.appendWithDrawingStep(step);
//                    }
                    if (step.isStepOver()) {
                        setHandlingLayerView(layerViewProtocol);
                        step.setStepOver(false);
                        endUnfinishedStep();
                    }
                    else {
                        getBaseLayerImageView().appendWithDrawingStep(step);
                    }
                }
                break;
            case DeleteLayer:
                if (layerViewProtocol != null) {
                    getLayerContainer().removeLayerView(layerViewProtocol);
                    getLayerViews().remove(layerViewProtocol);
                    handleLayer(UnhandleAnyLayer);
                }
                break;
        }
    }

    private void internalCancelCurrentStep() {
        DrawingStep step = getCurrentDrawingStep();

        DrawingLayer.LayerType layerType = step.getDrawingLayer().getLayerType();
        DrawingStep.StepType stepType = step.getStepType();

        DrawingLayerViewProtocol layerViewProtocol = findLayerViewByLayerHierarchy(step.getDrawingLayer().getHierarchy());

        switch (stepType) {
            case DrawOnBase:
                getBaseLayerImageView().appendWithDrawingStep(step);
                break;
            case CreateLayer:
            case DrawTextOnBase:
                if (layerViewProtocol != null) {
                    getLayerContainer().removeLayerView(layerViewProtocol);
                    getLayerViews().remove(layerViewProtocol);
                }
                break;
            case TextChange:
                if (layerViewProtocol != null) {
                    layerViewProtocol.appendWithDrawingStep(step);
                    ((DrawingLayerTextView) layerViewProtocol).setText(((DrawingLayerTextView) layerViewProtocol).getUnchangedText());
                }
                break;
        }

        handleLayer(UnhandleAnyLayer);
    }

    /**
     * {@link #setBackgroundColor(int, int)}
     */
    private void internalSetBackgroundColor(int layerHierarchy, int color) {
        if (layerHierarchy < 0) {
            return;
        }
        else if (layerHierarchy == 0) {
            getBaseLayerImageView().setBackgroundColor(color);
        }
        else {
            DrawingLayerViewProtocol layerViewProtocol = findLayerViewByLayerHierarchy(layerHierarchy);
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
    private void internalSetBackgroundDrawable(int layerHierarchy, Drawable drawable) {
        if (layerHierarchy < 0) {
            return;
        }
        else if (layerHierarchy == 0) {
            getBaseLayerImageView().setBackground(drawable);
        }
        else {
            DrawingLayerViewProtocol layerViewProtocol = findLayerViewByLayerHierarchy(layerHierarchy);
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
    private void internalClear() {
        // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);

        cancelCurrentStep();

        getLayerContainer().clear();

        if (getLayerViews().size() > 1) {
            getLayerViews().subList(1, getLayerViews().size()).clear();
        }

        getBaseLayerImageView().clearDrawing();
        getBaseLayerImageView().setBackground(null);

        handleLayer(UnhandleAnyLayer);
    }

    /**
     * 依照记录的数据{@link #drawingData}进行绘制
     * 由于base层可能进行了不可预料次数的绘制，故base层的重绘将会在异步线程进行
     */
    private void internalDrawData() {
        List<DrawingStep> stepsToDraw = getDrawingData().getStepsToDraw();

        // 筛选出每层layer涉及的step
        List<List<DrawingStep>> eachLayerSteps = new ArrayList<>();
        for (int i = 0; i < stepsToDraw.size(); i++) {
            DrawingStep step = stepsToDraw.get(i);

            while (eachLayerSteps.size() <= step.getDrawingLayer().getHierarchy()) {
                eachLayerSteps.add(new ArrayList<DrawingStep>());
            }
            eachLayerSteps.get(step.getDrawingLayer().getHierarchy()).add(step);
        }

        for (int layerHierarchy = 0; layerHierarchy < eachLayerSteps.size(); layerHierarchy++) {
            List<DrawingStep> currentLayerSteps = eachLayerSteps.get(layerHierarchy);
            if (currentLayerSteps.size() == 0) {
                continue;
            }

            /**
             * 第一次遍历所有step
             * 设置当前绘制比例
             * 获取当前图层应当绘制的background在哪一步step
             */
            int lastBackgroundStepIndex = -1;
            for (int i = 0; i < currentLayerSteps.size(); i++) {
                DrawingStep step = currentLayerSteps.get(i);

                step.updateDrawingRatio(getWidth(), getHeight());

                if (step.getDrawingLayer().getBackgroundImageIdentifier() != null
                    || step.getDrawingLayer().getBackgroundColor() != DrawingLayer.UnsetValue) {
                    lastBackgroundStepIndex = i;
                }
            }


            if (layerHierarchy == 0) {
                /**
                 * 绘制背景，背景使用系统的background
                 */
                if (lastBackgroundStepIndex >= 0) {
                    DrawingStep lastBackgroundDrawingStep = currentLayerSteps.get(lastBackgroundStepIndex);
                    if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                        getBaseLayerImageView().setBackground(getBackgroundDatasource().gainBackground(this, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                    }
                    else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != DrawingLayer.UnsetValue) {
                        getBaseLayerImageView().setBackgroundColor(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                    }
                }

                // 绘制base底层，异步
                getBaseLayerImageView().refreshWithDrawnSteps(currentLayerSteps);
            }
            else if (layerHierarchy > 0) { // 图层生成
                // 生成各个图层，同步
                DrawingStep firstDrawingStep = currentLayerSteps.get(0);
                DrawingStep lastDrawingStep = currentLayerSteps.get(currentLayerSteps.size() - 1);
                if (lastDrawingStep.getStepType() == DrawingStep.StepType.DeleteLayer) {
                    // if the layer deleted, skip it.
                    continue;
                }
                switch (firstDrawingStep.getDrawingLayer().getLayerType()) {
                    case LayerDrawing: {
                        DrawingLayerImageView imageView = new DrawingLayerImageView(getContext(), firstDrawingStep.getDrawingLayer().getHierarchy());
                        /**
                         * 绘制背景，背景使用系统的background
                         */
                        if (lastBackgroundStepIndex >= 0) {
                            DrawingStep lastBackgroundDrawingStep = currentLayerSteps.get(lastBackgroundStepIndex);
                            if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                                imageView.setBackground(getBackgroundDatasource().gainBackground(this, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                            }
                            else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != DrawingLayer.UnsetValue) {
                                imageView.setBackgroundColor(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                            }
                        }
                        getLayerContainer().addLayerView(imageView);
                        getLayerViews().add(imageView);

                        imageView.refreshWithDrawnSteps(currentLayerSteps);
                        imageView.setCanHandle(true);
                        break;
                    }
                    case LayerText: {
                        DrawingLayerTextView textView = new DrawingLayerTextView(getContext(), firstDrawingStep.getDrawingLayer().getHierarchy());
                        /**
                         * 绘制背景，背景使用系统的background
                         */
                        if (lastBackgroundStepIndex >= 0) {
                            DrawingStep lastBackgroundDrawingStep = currentLayerSteps.get(lastBackgroundStepIndex);
                            if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier() != null) {
                                textView.setBackground(getBackgroundDatasource().gainBackground(this, lastBackgroundDrawingStep.getDrawingLayer().getBackgroundImageIdentifier()));
                            }
                            else if (lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor() != DrawingLayer.UnsetValue) {
                                textView.setBackgroundColor(lastBackgroundDrawingStep.getDrawingLayer().getBackgroundColor());
                            }
                        }

                        getLayerContainer().addLayerView(textView);
                        getLayerViews().add(textView);

                        textView.refreshWithDrawnSteps(currentLayerSteps);
                        textView.setCanHandle(true);
                        textView.setTextChangeDelegate(this);
                        break;
                    }
                }
            }
        }

        handleLayer(UnhandleAnyLayer);
    }

}
