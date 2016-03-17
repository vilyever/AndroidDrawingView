package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.ContextHolder;
import com.vilyever.drawingview.R;
import com.vilyever.drawingview.brush.Brush;
import com.vilyever.drawingview.brush.text.TextBrush;
import com.vilyever.drawingview.model.DrawingLayer;
import com.vilyever.drawingview.model.DrawingStep;
import com.vilyever.unitconversion.DimenConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * DrawingLayerTextView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/28.
 * Feature:
 * text图层view，展示可编辑输入框，可任意对图层进行变换
 */
public class DrawingLayerTextView extends EditText implements DrawingLayerViewProtocol {
    final DrawingLayerTextView self = this;

    /**
     * 边框虚线向外扩张1dp，以免遮挡图形
     * 父view理应取消{@link android.view.ViewGroup#setClipChildren(boolean)}
     */
    public final static int BorderOffset = DimenConverter.dpToPixel(1);
    public final static int BorderWidth = DimenConverter.dpToPixel(2);
    public final static int BorderShadowRadius = DimenConverter.dpToPixel(2);


    public static final int DefaultPadding = DimenConverter.dpToPixel(8);
    public static final int FixItalicTextShadowRadius = DimenConverter.dpToPixel(5);


    /**
     * 边框虚线间距
     */
    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    /* #Constructors */
    public DrawingLayerTextView(Context context, int hierarchy) {
        super(context);
        init();
        setLayerHierarchy(hierarchy);
    }

    /* Public Methods */
    /**
     * 进入编辑状态
     * @param firstEditing 是否第一次编辑，即图层刚刚被创建完成时
     */
    public void beginEdit(boolean firstEditing) {
        if (isEditing()) {
            return;
        }
        setEditing(true);
        setFirstEditing(firstEditing);

        setUnchangedText(getText().toString());

        // 弹出键盘
        requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 结束编辑状态
     */
    public void endEdit() {
        if (!isEditing()) {
            return;
        }
        setEditing(false);

        // 隐藏键盘
        clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    /**
     * 当前步骤是否改变了text内容
     * @return 是否改变
     */
    public boolean isChangedSinceLastStep() {
        return !getText().toString().equals(getUnchangedText());
    }

    /* Properties */
    public interface TextChangeDelegate {
        /**
         * 实时监测text变化
         * @param textView 此view
         * @param text 变化后的text
         */
        void onTextChange(DrawingLayerTextView textView, String text);
    }
    private TextChangeDelegate textChangeDelegate;
    public DrawingLayerTextView setTextChangeDelegate(TextChangeDelegate textChangeDelegate) {
        this.textChangeDelegate = textChangeDelegate;
        return this;
    }
    public TextChangeDelegate getTextChangeDelegate() {
        if (this.textChangeDelegate == null) {
            this.textChangeDelegate = new TextChangeDelegate() {
                @Override
                public void onTextChange(DrawingLayerTextView textView, String text) {
                }
            };
        }
        return this.textChangeDelegate;
    }

    /**
     * 当前绘制的所有step
     */
    private List<DrawingStep> drawnSteps;
    private DrawingLayerTextView setDrawnSteps(List<DrawingStep> drawnSteps) {
        this.drawnSteps = drawnSteps;
        return this;
    }
    public List<DrawingStep> getDrawnSteps() {
        if (this.drawnSteps == null) {
            this.drawnSteps = new ArrayList<>();
        }
        return this.drawnSteps;
    }

    /**
     * 是否编辑状态
     */
    private boolean editing;
    private DrawingLayerTextView setEditing(boolean editing) {
        this.editing = editing;
        return this;
    }
    public boolean isEditing() {
        return this.editing;
    }

    /**
     * 是否首次编辑
     */
    private boolean firstEditing;
    private DrawingLayerTextView setFirstEditing(boolean firstEditing) {
        this.firstEditing = firstEditing;
        return this;
    }
    public boolean isFirstEditing() {
        return this.firstEditing;
    }

    /**
     * 下一step开始时记录的先前的text内容
     */
    private String unchangedText;
    private DrawingLayerTextView setUnchangedText(String unchangedText) {
        this.unchangedText = unchangedText;
        return this;
    }
    public String getUnchangedText() {
        return this.unchangedText;
    }

    /**
     * 边线绘制矩形
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private RectF borderRect;
    public RectF getBorderRect() {
        if (this.borderRect == null) {
            this.borderRect = new RectF();
        }
        return this.borderRect;
    }

    /**
     * 当前editText画布的显示区域，由于EditText在现实长文本时，canvas的宽高会大于显示区域
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Rect canvasClipBounds;
    public Rect getCanvasClipBounds() {
        if (this.canvasClipBounds == null) {
            this.canvasClipBounds = new Rect();
        }
        return this.canvasClipBounds;
    }

    /**
     * 边线绘制paint
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Paint borderPaint;
    public Paint getBorderPaint() {
        if (this.borderPaint == null) {
            this.borderPaint = new Paint();
            this.borderPaint.setAntiAlias(true);
            this.borderPaint.setDither(true);
            this.borderPaint.setStyle(Paint.Style.STROKE);
            this.borderPaint.setStrokeWidth(BorderWidth);
            this.borderPaint.setShadowLayer(BorderShadowRadius, 0, 0, Color.DKGRAY);
        }
        return this.borderPaint;
    }

    /**
     * 边线绘制path
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Path borderPath;
    public Path getBorderPath() {
        if (this.borderPath == null) {
            this.borderPath = new Path();
        }
        return this.borderPath;
    }

    /* Overrides */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 如果正在编辑状态，则劫持touch事件并由editText处理
         * 如果没有处在编辑状态，则此时应优先处理图层变换
         */
        if (isEditing()) {
            super.onTouchEvent(event);
            return true;
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 图层在被操作时，绘制边框
        if (isHandling()) {
            getBorderRect().set(getLeft(), getTop(), getRight(), getBottom());
            canvas.getClipBounds(getCanvasClipBounds());
            int offset = getCanvasClipBounds().bottom - canvas.getHeight(); // 使边框绘制在现实区域边线上
            getBorderRect().offsetTo(0, offset);
            getBorderRect().left -= BorderOffset;
            getBorderRect().top -= BorderOffset;
            getBorderRect().right += BorderOffset;
            getBorderRect().bottom += BorderOffset;

            getBorderPath().reset();
            getBorderPath().addRect(getBorderRect(), Path.Direction.CW);

            int[] colors = ContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerTextBorder);
            getBorderPaint().setColor(colors[0]);
            getBorderPaint().setPathEffect(FirstDashPathEffect);
            canvas.drawPath(getBorderPath(), getBorderPaint());

            getBorderPaint().setColor(colors[1]);
            getBorderPaint().setPathEffect(SecondDashPathEffect);
            canvas.drawPath(getBorderPath(), getBorderPaint());
        }
    }

    /* Delegates */
    /** {@link DrawingLayerViewProtocol} */
    @Override
    public void clearDrawing() {
    }

    @Override
    public Brush.Frame appendWithDrawingStep(@NonNull DrawingStep drawingStep) {
        // 此类图层仅处理 创建图层，文本改动，图层变换 三种操作
        if (drawingStep.getStepType() != DrawingStep.StepType.CreateLayer
                && drawingStep.getStepType() != DrawingStep.StepType.DrawTextOnBase
                && drawingStep.getStepType() != DrawingStep.StepType.TextChange
                && drawingStep.getStepType() != DrawingStep.StepType.Transform) {
            return null;
        }

        // 图层尺寸
        Brush.Frame frame = null;

        if (drawingStep.getStepType() == DrawingStep.StepType.CreateLayer
                || drawingStep.getStepType() == DrawingStep.StepType.DrawTextOnBase) {
            if (!getDrawnSteps().contains(drawingStep)) {
                if (getDrawnSteps().size() > 0) {
                    if (getDrawnSteps().get(getDrawnSteps().size() - 1).getStep() == drawingStep.getStep()) {
                        getDrawnSteps().remove(getDrawnSteps().size() - 1);
                    }
                }

                getDrawnSteps().add(drawingStep);
            }

            // 确定图层尺寸
            frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), drawingStep.getDrawingState().newStateByJoin(Brush.DrawingPointerState.FetchFrame));
            drawingStep.getDrawingLayer().setFrame(frame);
        }

        updateFrame(drawingStep);

        return frame;
    }

    @Override
    public void appendWithSteps(@NonNull List<DrawingStep> steps) {
        getDrawnSteps().addAll(steps);
        for (DrawingStep step : steps) {
            updateFrame(step);
        }
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<DrawingStep> drawnSteps) {
        getDrawnSteps().clear();
        appendWithSteps(drawnSteps);
    }

    /** {@link DrawingLayerViewProtocol#getLayerHierarchy()} */
    private int hierarchy;

    @Override
    public int getLayerHierarchy() {
        return this.hierarchy;
    }

    @Override
    public void setLayerHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    /** {@link DrawingLayerViewProtocol#setHandling(boolean)} {@link DrawingLayerViewProtocol#isHandling()} */
    private boolean handling;

    @Override
    public boolean isHandling() {
        if (!canHandle()) {
            return false;
        }
        return this.handling;
    }

    @Override
    public void setHandling(boolean handling) {
        if (!canHandle()) {
            return;
        }
        this.handling = handling;
        invalidate();
    }

    /** {@link DrawingLayerViewProtocol#setCanHandle(boolean)} {@link DrawingLayerViewProtocol#canHandle()} */
    private boolean canHandle = true;
    @Override
    public boolean canHandle() {
        return this.canHandle;
    }

    @Override
    public void setCanHandle(boolean canHandle) {
        this.canHandle = canHandle;
    }
    /** {@link DrawingLayerViewProtocol} */

    /* Private Methods */
    /**
     * 初始化
     */
    private void init() {
        setPadding(DefaultPadding, DefaultPadding, DefaultPadding, DefaultPadding);

        setTypeface(Typeface.MONOSPACE); // 支持中文斜体
        setShadowLayer(FixItalicTextShadowRadius, 0, 0, Color.TRANSPARENT); // 修复显示斜体字符右上角被clip的部分

        // 内容为空时用于显示cursor光标
        setMinWidth(getPaddingLeft() + getPaddingRight() + DimenConverter.dpToPixel(1));

        // 默认清空背景
        setBackground(null);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                getTextChangeDelegate().onTextChange(self, s.toString());
            }
        });
    }

    /**
     * 根据step更新图层状态
     * @param drawingStep 更新用step
     */
    private void updateFrame(DrawingStep drawingStep) {
        // 更新位置和大小
        if (drawingStep.getDrawingLayer().getFrame() != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();

            if (layoutParams != null) {
                // wrap_content动态变化
                layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.leftMargin = (int) Math.floor(drawingStep.getDrawingLayer().getLeft());
                layoutParams.topMargin = (int) Math.floor(drawingStep.getDrawingLayer().getTop());
                layoutParams.rightMargin = -Integer.MAX_VALUE;
                layoutParams.bottomMargin = -Integer.MAX_VALUE;

                setLayoutParams(layoutParams);
            }
        }

        // 更新字体
        if (drawingStep.getBrush() != null) {
            TextBrush textBrush = drawingStep.getBrush();
            setTextSize(textBrush.getSize());
            setTextColor(textBrush.getColor());
            setTypeface(getTypeface(), textBrush.getTypefaceStyle());
        }

        // 更新文本
        if (drawingStep.getDrawingLayer().getText() != null) {
            setText(drawingStep.getDrawingLayer().getText());
        }

        // 更新缩放
        if (drawingStep.getDrawingLayer().getScale() != DrawingLayer.UnsetValue) {
            setScaleX(drawingStep.getDrawingLayer().getScale());
            setScaleY(drawingStep.getDrawingLayer().getScale());
        }

        // 更新旋转
        if (drawingStep.getDrawingLayer().getRotation() != DrawingLayer.UnsetValue) {
            setRotation(drawingStep.getDrawingLayer().getRotation());
        }

        invalidate();
    }
}