package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.R;
import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.text.VDTextBrush;
import com.vilyever.drawingview.model.VDDrawingLayer;
import com.vilyever.drawingview.model.VDDrawingStep;
import com.vilyever.unitconversion.VDDimenConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerTextView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/28.
 * Feature:
 * text图层view，展示可编辑输入框，可任意对图层进行变换
 */
public class VDDrawingLayerTextView extends EditText implements VDDrawingLayerViewProtocol {
    final VDDrawingLayerTextView self = this;

    /**
     * 边框虚线向外扩张1dp，以免遮挡图形
     * 父view理应取消{@link android.view.ViewGroup#setClipChildren(boolean)}
     */
    public final static int BorderOffset = VDDimenConversion.dpToPixel(1);

    /**
     * 边框虚线间距
     */
    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    /* #Constructors */
    public VDDrawingLayerTextView(Context context) {
        super(context);
        self.init();
    }

    /**
     * 进入编辑状态
     * @param firstEditing 是否第一次编辑，即图层刚刚被创建完成时
     */
    public void beginEdit(boolean firstEditing) {
        if (self.isEditing()) {
            return;
        }
        self.setEditing(true);
        self.setFirstEditing(firstEditing);

        self.setUnchangedText(self.getText().toString());

        // 弹出键盘
        self.requestFocus();
        InputMethodManager imm = (InputMethodManager) self.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(self, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 结束编辑状态
     */
    public void endEdit() {
        if (!self.isEditing()) {
            return;
        }
        self.setEditing(false);

        // 隐藏键盘
        self.clearFocus();
        InputMethodManager imm = (InputMethodManager) self.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(self.getWindowToken(), 0);
    }

    /**
     * 当前步骤是否改变了text内容
     * @return 是否改变
     */
    public boolean isChangedSinceLastStep() {
        return !self.getText().toString().equals(self.getUnchangedText());
    }

    public interface Delegate {
        /**
         * 实时监测text变化
         * @param textView 此view
         * @param text 变化后的text
         */
        void didChangeText(VDDrawingLayerTextView textView, String text);
    }
    private Delegate delegate;
    public VDDrawingLayerTextView setDelegate(Delegate delegate) {
        this.delegate = delegate;
        return this;
    }
    public Delegate getDelegate() {
        if (delegate == null) {
            delegate = new Delegate() {
                @Override
                public void didChangeText(VDDrawingLayerTextView textView, String text) {
                }
            };
        }
        return delegate;
    }

    /**
     * 当前绘制的所有step
     */
    private List<VDDrawingStep> drawnSteps;
    private VDDrawingLayerTextView setDrawnSteps(List<VDDrawingStep> drawnSteps) {
        this.drawnSteps = drawnSteps;
        return this;
    }
    public List<VDDrawingStep> getDrawnSteps() {
        if (drawnSteps == null) {
            drawnSteps = new ArrayList<>();
        }
        return drawnSteps;
    }

    /**
     * 是否编辑状态
     */
    private boolean editing;
    private VDDrawingLayerTextView setEditing(boolean editing) {
        this.editing = editing;
        return this;
    }
    public boolean isEditing() {
        return editing;
    }

    /**
     * 是否首次编辑
     */
    private boolean firstEditing;
    private VDDrawingLayerTextView setFirstEditing(boolean firstEditing) {
        this.firstEditing = firstEditing;
        return this;
    }
    public boolean isFirstEditing() {
        return firstEditing;
    }

    /**
     * 下一step开始时记录的先前的text内容
     */
    private String unchangedText;
    private VDDrawingLayerTextView setUnchangedText(String unchangedText) {
        this.unchangedText = unchangedText;
        return this;
    }
    public String getUnchangedText() {
        return unchangedText;
    }

    /**
     * 边线绘制矩形
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private RectF borderRect;
    public RectF getBorderRect() {
        if (borderRect == null) {
            borderRect = new RectF();
        }
        return borderRect;
    }

    /**
     * 当前editText画布的显示区域，由于EditText在现实长文本时，canvas的宽高会大于显示区域
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Rect canvasClipBounds;
    public Rect getCanvasClipBounds() {
        if (canvasClipBounds == null) {
            canvasClipBounds = new Rect();
        }
        return canvasClipBounds;
    }

    /**
     * 边线绘制paint
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Paint borderPaint;
    public Paint getBorderPaint() {
        if (borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
            borderPaint.setDither(true);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2);
            borderPaint.setShadowLayer(2, 0, 0, Color.DKGRAY);
        }
        return borderPaint;
    }

    /**
     * 边线绘制path
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Path borderPath;
    public Path getBorderPath() {
        if (borderPath == null) {
            borderPath = new Path();
        }
        return borderPath;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 如果正在编辑状态，则劫持touch事件并由editText处理
         * 如果没有处在编辑状态，则此时应优先处理图层变换
         */
        if (self.isEditing()) {
            super.onTouchEvent(event);
            return true;
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 图层在被操作时，绘制边框
        if (self.isHandling()) {
            self.getBorderRect().set(self.getLeft(), self.getTop(), self.getRight(), self.getBottom());
            canvas.getClipBounds(self.getCanvasClipBounds());
            int offset = self.getCanvasClipBounds().bottom - canvas.getHeight(); // 使边框绘制在现实区域边线上
            self.getBorderRect().offsetTo(0, offset);
            self.getBorderRect().left -= BorderOffset;
            self.getBorderRect().top -= BorderOffset;
            self.getBorderRect().right += BorderOffset;
            self.getBorderRect().bottom += BorderOffset;

            self.getBorderPath().reset();
            self.getBorderPath().addRect(self.getBorderRect(), Path.Direction.CW);

            int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerTextBorder);
            self.getBorderPaint().setColor(colors[0]);
            self.getBorderPaint().setPathEffect(FirstDashPathEffect);
            canvas.drawPath(self.getBorderPath(), self.getBorderPaint());

            self.getBorderPaint().setColor(colors[1]);
            self.getBorderPaint().setPathEffect(SecondDashPathEffect);
            canvas.drawPath(self.getBorderPath(), self.getBorderPaint());
        }
    }

    /** {@link VDDrawingLayerViewProtocol} */
    @Override
    public void clearDrawing() {
    }

    @Override
    public VDBrush.Frame appendWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        // 此类图层仅处理 创建图层，文本改动，图层变换 三种操作
        if (drawingStep.getStepType() != VDDrawingStep.StepType.CreateLayer
                && drawingStep.getStepType() != VDDrawingStep.StepType.TextChange
                && drawingStep.getStepType() != VDDrawingStep.StepType.Transform) {
            return null;
        }

        // 图层尺寸
        VDBrush.Frame frame = null;

        if (drawingStep.getStepType() == VDDrawingStep.StepType.CreateLayer) {
            if (drawingStep.getDrawingState().isVeryBegin()) {
                if (!self.getDrawnSteps().contains(drawingStep)) {
                    self.getDrawnSteps().add(drawingStep);
                }
            }

            // 确定图层尺寸
            frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), drawingStep.getDrawingState().newStateByJoin(VDBrush.DrawingPointerState.FetchFrame));
            drawingStep.getDrawingLayer().setFrame(frame);
        }

        self.updateFrame(drawingStep);

        return frame;
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps) {
        self.setDrawnSteps(drawnSteps);
        for (VDDrawingStep step : drawnSteps) {
            self.updateFrame(step);
        }
    }

    @Override
    public int getLayerHierarchy() {
        if (self.getDrawnSteps().size() > 0) {
            return self.getDrawnSteps().get(0).getDrawingLayer().getHierarchy();
        }
        return 0;
    }

    /** {@link VDDrawingLayerViewProtocol#setHandling(boolean)} {@link VDDrawingLayerViewProtocol#isHandling()} */
    private boolean handling;

    @Override
    public boolean isHandling() {
        return handling;
    }

    @Override
    public void setHandling(boolean handling) {
        self.handling = handling;
        self.invalidate();
    }

    /** {@link VDDrawingLayerViewProtocol#setCanHandle(boolean)} {@link VDDrawingLayerViewProtocol#canHandle()} */
    private boolean canHandle;
    @Override
    public boolean canHandle() {
        return self.canHandle;
    }

    @Override
    public void setCanHandle(boolean canHandle) {
        self.canHandle = canHandle;
    }
    /** {@link VDDrawingLayerViewProtocol} */

    /**
     * 初始化
     */
    private void init() {
        // 默认设置内边距，优化显示效果
        self.setPadding(VDTextBrush.BorderMargin, VDTextBrush.BorderMargin, VDTextBrush.BorderMargin, VDTextBrush.BorderMargin);

        // 默认清空背景
        self.setBackground(null);

        self.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                self.getDelegate().didChangeText(self, s.toString());
            }
        });
    }

    /**
     * 根据step更新图层状态
     * @param drawingStep 更新用step
     */
    private void updateFrame(VDDrawingStep drawingStep) {
        // 更新位置和大小
        if (drawingStep.getDrawingLayer().getFrame() != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) self.getLayoutParams();

            layoutParams.width = (int) Math.floor(drawingStep.getDrawingLayer().getWidth());
            layoutParams.height = (int) Math.floor(drawingStep.getDrawingLayer().getHeight());
            layoutParams.leftMargin = (int) Math.floor(drawingStep.getDrawingLayer().getLeft());
            layoutParams.topMargin = (int) Math.floor(drawingStep.getDrawingLayer().getTop());
            layoutParams.rightMargin = -Integer.MAX_VALUE;
            layoutParams.bottomMargin = -Integer.MAX_VALUE;

            self.setLayoutParams(layoutParams);
        }

        // 更新字体
        if (drawingStep.getBrush() != null) {
            VDTextBrush textBrush = drawingStep.getBrush();
            self.setTextSize(textBrush.getSize());
            self.setTextColor(textBrush.getColor());
            self.setTypeface(null, textBrush.getTypefaceStyle());
        }

        // 更新文本
        if (drawingStep.getDrawingLayer().getText() != null) {
            self.setText(drawingStep.getDrawingLayer().getText());
        }

        // 更新缩放
        if (drawingStep.getDrawingLayer().getScale() != VDDrawingLayer.UnsetValue) {
            self.setScaleX(drawingStep.getDrawingLayer().getScale());
            self.setScaleY(drawingStep.getDrawingLayer().getScale());
        }

        // 更新旋转
        if (drawingStep.getDrawingLayer().getRotation() != VDDrawingLayer.UnsetValue) {
            self.setRotation(drawingStep.getDrawingLayer().getRotation());
        }

        self.invalidate();
    }
}